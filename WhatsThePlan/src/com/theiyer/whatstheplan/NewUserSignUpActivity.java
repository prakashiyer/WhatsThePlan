package com.theiyer.whatstheplan;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.theiyer.whatstheplan.entity.UserInformation;
import com.theiyer.whatstheplan.util.WTPConstants;
import com.thoughtworks.xstream.XStream;

public class NewUserSignUpActivity extends Activity {
	
	public static final String EXTRA_MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "registration_id";

	String SENDER_ID = "358164918628";
	static final String TAG = "Just Meet GCM";

	TextView mDisplay;
	GoogleCloudMessaging gcm;
	AtomicInteger msgId = new AtomicInteger();
	SharedPreferences prefs;
	Context context;

	String regid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_user_registration);
		ActionBar aBar = getActionBar();
		Resources res = getResources();
		Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
		aBar.setBackgroundDrawable(actionBckGrnd);
		aBar.setTitle(" Sign Up Form");
	}

	/** Called when the user clicks the New User Register button */
	public void logNewUser(View view) {
		Button button = (Button) findViewById(R.id.registerButton);
		button.setTextColor(getResources().getColor(R.color.click_button_1));
		EditText userNameEditText = (EditText) findViewById(R.id.newUserNameValue);
		String userName = userNameEditText.getText().toString();

		EditText phoneText = (EditText) findViewById(R.id.newUserPhoneValue);
		String phone = phoneText.getText().toString();

		EditText passwordEditText = (EditText) findViewById(R.id.newPasswordValue);
		String password = passwordEditText.getText().toString();

		if (TextUtils.isEmpty(userName)) {
			Toast.makeText(getApplicationContext(), "Don't you have a name?",
					Toast.LENGTH_LONG).show();
		} else if (TextUtils.isEmpty(phone)) {
			Toast.makeText(getApplicationContext(),
					"Can I have your phone number?", Toast.LENGTH_LONG).show();
		} else if (TextUtils.isEmpty(password)) {
			Toast.makeText(getApplicationContext(),
					"Nothing works without a password!", Toast.LENGTH_LONG)
					.show();
		} else {
			
			String insertQuery = "/addUserInformation?name="
					+ userName.replace(" ", "%20") + "&phone=" + phone;

			RestWebServiceClient restClient = new RestWebServiceClient(this);
			try {
				String response = restClient.execute(new String[] { insertQuery })
						.get();

				if (response != null) {
					XStream xstream = new XStream();
					xstream.alias("UserInformation", UserInformation.class);
					xstream.alias("groupNames", String.class);
					xstream.addImplicitCollection(UserInformation.class,
							"groupNames", "groupNames", String.class);
					xstream.alias("pendingGroupNames", String.class);
					xstream.addImplicitCollection(UserInformation.class,
							"pendingGroupNames", "pendingGroupNames", String.class);
					UserInformation user = (UserInformation) xstream
							.fromXML(response);
					if (user != null && phone.equals(user.getPhone())) {
						
						SharedPreferences prefs = getSharedPreferences("Prefs",
								Activity.MODE_PRIVATE);
						SharedPreferences.Editor editor = prefs.edit();
						editor.putString("phone", user.getPhone());
						editor.putString("userName", user.getName());
						editor.apply();

						AccountManager am = AccountManager.get(this);
						final Account account = new Account(user.getPhone(),
								"com.theiyer.whatstheplan");
						final Bundle bundle = new Bundle();
						bundle.putString("userName", user.getName());
						bundle.putString("phone", user.getPhone());
						bundle.putString(AccountManager.KEY_ACCOUNT_NAME,
								account.name);
						am.addAccountExplicitly(account, user.getPhone(), bundle);
						am.setAuthToken(account, "Full Access", user.getPhone());
						
						gcm = GoogleCloudMessaging.getInstance(this);
						Asyncer syncer = new Asyncer();
						syncer.execute(new String[] {phone});
						

						button.setTextColor(getResources().getColor(
								R.color.button_text));
						Intent intent = new Intent(this,
								ProfileImageUploadActivity.class);
						startActivity(intent);
					} else {
						Toast.makeText(getApplicationContext(),
								"Something's wrong. Try later.",
								Toast.LENGTH_LONG).show();
					}
				} else {
					Toast.makeText(getApplicationContext(),
							"Something's wrong. Try later.",
							Toast.LENGTH_LONG).show();
				}
			} catch (InterruptedException e) {
				Toast.makeText(getApplicationContext(),
						"Something's wrong. Try later.", Toast.LENGTH_LONG).show();
			} catch (ExecutionException e) {
				Toast.makeText(getApplicationContext(),
						"Something's wrong. Try later.", Toast.LENGTH_LONG).show();
			}
		}
	}

	
	/**
	 * For GCM registration and storage
	 * @author Dell
	 *
	 */
	private class Asyncer extends AsyncTask<String, Integer, String> {

		private ProgressDialog pDlg;
		
		private void showProgressDialog() {

			pDlg = new ProgressDialog(context);
			pDlg.setMessage("Processing ....");
			pDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			pDlg.setCancelable(false);
			pDlg.show();

		}

		@Override
		protected void onPreExecute() {
			showProgressDialog();

		}

		@Override
		protected String doInBackground(String... params) {
			String msg = "";
			if (gcm == null) {
				gcm = GoogleCloudMessaging.getInstance(context);
			}
			try {
				regid = gcm.register(SENDER_ID);
			} catch (IOException ex) {
				msg = "Error :" + ex.getMessage();
				// If there is an error, don't just keep trying to register.
				// Require the user to click a button again, or perform
				// exponential back-off.
			}
			msg = "Device registered, registration ID=" + regid;

			if (regid != null && regid != "") {
				// Persist the regID - no need to register again.
				storeRegistrationId(context, regid);

				// Store the reg id in server

				String path = "/WhatsThePlan/operation/addRegId?regId="
						+ regid + "&phone="+params[0];

				// HttpHost target = new HttpHost(TARGET_HOST);
				HttpHost target = new HttpHost(WTPConstants.TARGET_HOST, 8080);
				HttpClient client = new DefaultHttpClient();
				HttpGet get = new HttpGet(path);
				try {
					client.execute(target, get);
				} catch (Exception e) {

				}
			}

			return msg;
		}

		@Override
		protected void onPostExecute(String msg) {
			mDisplay.append(msg + "\n");
		}

	}
	
	/**
	 * Stores the registration ID and app versionCode in the application's
	 * {@code SharedPreferences}.
	 * 
	 * @param context
	 *            application's context.
	 * @param regId
	 *            registration ID
	 */
	private void storeRegistrationId(Context context, String regId) {
		SharedPreferences prefs = getSharedPreferences("Prefs",
				Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("regId", regId);
		editor.apply();		
	}
	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case (R.id.aboutUs):
			Intent intent = new Intent(this, AboutUsActivity.class);
			startActivity(intent);
			return true;
		default:
			return false;
		}
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
	}
	
	
	

}
