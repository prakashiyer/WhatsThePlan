package com.theiyer.whatstheplan;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.theiyer.whatstheplan.entity.User;
import com.theiyer.whatstheplan.util.WTPConstants;
import com.thoughtworks.xstream.XStream;

public class NewUserSignUpActivity extends Activity {
	
	private SmsManager smsManager;
	private Context context;
	private String code;

	private static final String TAG = "Just Meet GCM";

	private GoogleCloudMessaging gcm;
	private String regid;

	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_user_registration);
		ActionBar aBar = getActionBar();
		Resources res = getResources();
		Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
		aBar.setBackgroundDrawable(actionBckGrnd);
		aBar.setTitle(" Sign Up Form");
		context = getApplicationContext();
		smsManager = SmsManager.getDefault(); 

	}
	
	/** Called when the user checks the change password */
	public void enterCodeCheck(View view) {
		CheckBox checkBox = (CheckBox) findViewById(R.id.codeCheckBox);

		EditText phoneText = (EditText) findViewById(R.id.newUserPhoneValue);
		String phone = phoneText.getText().toString();
		TextView codeLabel = (TextView) findViewById(R.id.newUserCodeLabel);
		EditText newPassword = (EditText) findViewById(R.id.newPasswordValue);
		Button regButton = (Button) findViewById(R.id.registerButton);
		if (checkBox.isChecked()) {
			if(!TextUtils.isEmpty(phone)){
				String code = phone.substring(3, 7);
				String message = "Welcome to Just Meet. Please enter the code "+code+" to prove you are sane.";
				smsManager.sendTextMessage(phone, null, message, null, null);
				codeLabel.setVisibility(EditText.VISIBLE);
				newPassword.setVisibility(EditText.VISIBLE);
				regButton.setVisibility(Button.VISIBLE);
				gcm = GoogleCloudMessaging.getInstance(context);
				Asyncer syncer = new Asyncer();
				syncer.execute(new String[] {phone});
			} else {
				Toast.makeText(getApplicationContext(),
						"Can I have your phone number?", Toast.LENGTH_LONG).show();
			}
			
		} else {
			codeLabel.setVisibility(EditText.INVISIBLE);
			newPassword.setVisibility(EditText.INVISIBLE);
			regButton.setVisibility(Button.INVISIBLE);
		}

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
		} else if (TextUtils.isEmpty(password) && !TextUtils.equals(password, code)) {
			Toast.makeText(getApplicationContext(),
					"Nothing works without a right password!", Toast.LENGTH_LONG)
					.show();
		} else {
			
			String insertQuery = "/addUser?name="
					+ userName.replace(" ", "%20") + "&phone=" + phone;

			RestWebServiceClient restClient = new RestWebServiceClient(this);
			try {
				String response = restClient.execute(new String[] { insertQuery })
						.get();

				if (response != null) {
					XStream xstream = new XStream();
					xstream.alias(WTPConstants.XS_USER, User.class);
					xstream.alias(WTPConstants.XS_GROUP_NAMES, String.class);
					xstream.addImplicitCollection(User.class,
							WTPConstants.XS_GROUP_NAMES, WTPConstants.XS_GROUP_NAMES, String.class);
					xstream.alias(WTPConstants.XS_PENDING_GROUP_NAMES, String.class);
					xstream.addImplicitCollection(User.class,
							WTPConstants.XS_PENDING_GROUP_NAMES, WTPConstants.XS_PENDING_GROUP_NAMES, String.class);
					User user = (User) xstream
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
								WTPConstants.ACCOUNT_ADDRESS);
						final Bundle bundle = new Bundle();
						bundle.putString("userName", user.getName());
						bundle.putString("phone", user.getPhone());
						bundle.putString(AccountManager.KEY_ACCOUNT_NAME,
								account.name);
						am.addAccountExplicitly(account, user.getPhone(), bundle);
						am.setAuthToken(account, "Full Access", user.getPhone());

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
	
	/**
	 * For GCM registration and storage
	 * @author Dell
	 *
	 */
	private class Asyncer extends AsyncTask<String, Integer, String> {

		@Override
		protected void onPreExecute() {

		}

		@Override
		protected String doInBackground(String... params) {
			String msg = "";
			if (gcm == null) {
				gcm = GoogleCloudMessaging.getInstance(context);
			}
			try {
				Log.i(TAG, "Registering GCM");
				regid = gcm.register(WTPConstants.SENDER_ID);
			} catch (IOException ex) {
				msg = "Error :" + ex.getMessage();
				Log.e(TAG, msg);
				
				ex.printStackTrace();
				// If there is an error, don't just keep trying to register.
				// Require the user to click a button again, or perform
				// exponential back-off.
			}
			msg = "Device registered, registration ID=" + regid;
			Log.i(TAG, msg);

			if (regid != null && regid != "") {
				// Persist the regID - no need to register again.
				storeRegistrationId(context, regid);

				// Store the reg id in server

				String path = WTPConstants.SERVICE_PATH+"/addRegId?regId="
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
	

}
