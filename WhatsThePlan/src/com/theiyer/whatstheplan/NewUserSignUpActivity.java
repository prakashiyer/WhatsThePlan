package com.theiyer.whatstheplan;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.R.string;
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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.theiyer.whatstheplan.AddHealthCenterActivity.UserWebServiceClient;
import com.theiyer.whatstheplan.entity.Plan;
import com.theiyer.whatstheplan.entity.PlanList;
import com.theiyer.whatstheplan.entity.User;
import com.theiyer.whatstheplan.util.WTPConstants;
import com.thoughtworks.xstream.XStream;

public class NewUserSignUpActivity extends FragmentActivity implements OnItemSelectedListener {
	
	private Context context;
	private String genderVar;
	private String doctorFlag;
	private String bloodVar;

	private static final String TAG = "Health Meet GCM";

	private GoogleCloudMessaging gcm;
	private String regid;
	Spinner gender; 
	 //TextView selGender;
	 Spinner bloodGrp;
	 //TextView selblood;
	 private String[] genderString = { "(Select)", "Male", "Female" };
	 private String[] bloodGrpString = {"(Select)", "A-positive", "B-positive", "B-negative", "A-negative", "O-positive" , "O-negative", "AB-positive", "AB-negative" };
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		if(haveInternet(this)){
			setContentView(R.layout.new_user_registration);
			ActionBar aBar = getActionBar();
			Resources res = getResources();
			Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
			aBar.setBackgroundDrawable(actionBckGrnd);
			aBar.setTitle(" Individual Registration form");
			context = getApplicationContext();
			doctorFlag = "N";
			  //selGender = (TextView) findViewById(R.id.selGender);
			  gender = (Spinner) findViewById(R.id.genderDrp);
			  ArrayAdapter<String> adapter_state = new ArrayAdapter<String>(this,
			    android.R.layout.simple_spinner_item, genderString);
			  adapter_state
			    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			  gender.setAdapter(adapter_state);
			  gender.setOnItemSelectedListener(this);
			  
			  System.out.println("blood group : " + bloodGrpString.length);
			  //selblood = (TextView) findViewById(R.id.selblood);
			  bloodGrp = (Spinner) findViewById(R.id.bloodGrp);
			  ArrayAdapter<String> adapter_state1 = new ArrayAdapter<String>(this,
			    android.R.layout.simple_spinner_item, bloodGrpString);
			  adapter_state1
			    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			  bloodGrp.setAdapter(adapter_state1);
			  bloodGrp.setOnItemSelectedListener(this);
		} else {
			Intent intent = new Intent(this, RetryActivity.class);
			startActivity(intent);
		}
		

	}
	
	/** Called when the user checks the change password */
	public void enterDocCheck(View view) {
		CheckBox checkBox = (CheckBox) findViewById(R.id.codeCheckBox);
		if(checkBox.isChecked()){
			doctorFlag="Y";
		} else {
			doctorFlag="N";
		}
			
	}
	public void setDate(View v) {
		Button button = (Button) findViewById(R.id.setDateOfBirth);
		button.setTextColor(getResources().getColor(R.color.click_button_2));
		DialogFragment newFragment = new DateNewPickerFragment("birth");
		newFragment.show(getSupportFragmentManager(), "datePicker");
		button.setTextColor(getResources().getColor(R.color.button_text));
	}

	/** Called when the user clicks the New User Register button */
	public void logNewUser(View view) {
		Button button = (Button) findViewById(R.id.registerButton);
		button.setTextColor(getResources().getColor(R.color.click_button_1));
		EditText userNameEditText = (EditText) findViewById(R.id.newUserNameValue);
		String userName = userNameEditText.getText().toString();

		EditText phoneText = (EditText) findViewById(R.id.newUserPhoneValue);
		String phone = phoneText.getText().toString();
		
		TextView dob = (TextView) findViewById(R.id.dateOfBirth);
		String dobText = dob.getText().toString();

		EditText addressText = (EditText) findViewById(R.id.newUserAddressValue);
		String address = addressText.getText().toString();

		if (TextUtils.isEmpty(userName)) {
			Toast.makeText(getApplicationContext(), "Don't you have a name?",
					Toast.LENGTH_LONG).show();
		} else if (TextUtils.isEmpty(phone)) {
			Toast.makeText(getApplicationContext(),
					"Can I have your phone number?", Toast.LENGTH_LONG).show();
		} else if (TextUtils.isEmpty(dobText)) {			
			Toast.makeText(getApplicationContext(),
					"Can I have your date of birth?", Toast.LENGTH_LONG).show();
		} else if (TextUtils.isEmpty(genderVar)) {			
			Toast.makeText(getApplicationContext(),
					"Can I have your gender?", Toast.LENGTH_LONG).show();
		} else {
			SharedPreferences prefs = getSharedPreferences("Prefs",
					Activity.MODE_PRIVATE);
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString("name", userName);
			editor.putString("phone", phone);
			editor.putString("dob", dobText);
			editor.putString("gender", genderVar);
			editor.putString("bloodGrp", bloodVar);
			editor.putString("Address", address);
			editor.putString("doctor", doctorFlag);
			editor.putString("docFlag", doctorFlag);
			editor.putString("centerFlag", "N");
			editor.apply();
			System.out.println("****** bloodVar  ::  " + bloodVar);
			if("Y".equals(doctorFlag)){
				String userQuery = "/addUser?phone="+phone+"&name="+userName.replace(" ", "%20")
						+"&bloodGroup=" + bloodVar
						+"&dob=" + dobText
						+"&sex=" + genderVar
						+"&address=" + address
						+"&doctorFlag=" + doctorFlag
						+"&primaryCenterId="+ "0"
						+"&primaryDoctorId="+ "0"
						+"&centers=" + "";
				UserWebServiceClient userRestClient = new UserWebServiceClient(this);
				userRestClient.execute(new String[] { userQuery});
						
				
			} else {
				Intent intent = new Intent(this,
						AddDoctorActivity.class);
				startActivity(intent);
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
	
	public class UserWebServiceClient extends AsyncTask<String, Integer, String> {

		private Context mContext;
		private ProgressDialog pDlg;

		public UserWebServiceClient(Context mContext) {
			this.mContext = mContext;
		}

		private void showProgressDialog() {

			pDlg = new ProgressDialog(mContext);
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
			String path = WTPConstants.SERVICE_PATH+params[0];
			
			//HttpHost target = new HttpHost(TARGET_HOST);
			HttpHost target = new HttpHost(WTPConstants.TARGET_HOST, 8080);
			HttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet(path);
			HttpEntity results = null;

			try {
				HttpResponse response = client.execute(target, get);
				results = response.getEntity(); 
				String result = EntityUtils.toString(results);
				return result;
			} catch (Exception e) {
				
			}
			return null;
		}

		@Override
		protected void onPostExecute(String response) {
			if (response != null) {
				    Log.i(TAG, response);
				    XStream userXs = new XStream();
					userXs.alias("UserInformation", User.class);
					userXs.alias("centers", String.class);
					userXs.addImplicitCollection(User.class, "centers",
							"centers", String.class);
					User user = (User) userXs.fromXML(response);
					if (user != null && user.getName() != null) {
						 Log.i(TAG, user.getName());
						 AccountManager am = AccountManager.get(mContext);
							final Account account = new Account(user.getPhone(),
									WTPConstants.ACCOUNT_ADDRESS);
							final Bundle bundle = new Bundle();
							bundle.putString("userName", user.getName());
							bundle.putString("phone", user.getPhone());
							bundle.putString("doctor", doctorFlag);
							bundle.putString(AccountManager.KEY_ACCOUNT_NAME,
									account.name);
							am.addAccountExplicitly(account, user.getPhone(), bundle);
							am.setAuthToken(account, "Full Access", user.getPhone());
						 Toast.makeText(getApplicationContext(), "Congratulations! Your Profile has been activated.",
									Toast.LENGTH_LONG).show();
						 Intent intent = new Intent(mContext, ProfileImageUploadActivity.class);
							startActivity(intent);
					}
			}
			pDlg.dismiss();
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
	
	/**
	 * Checks if we have a valid Internet Connection on the device.
	 * 
	 * @param ctx
	 * @return True if device has internet
	 * 
	 *         Code from: http://www.androidsnippets.org/snippets/131/
	 */
	public static boolean haveInternet(Context ctx) {

		NetworkInfo info = (NetworkInfo) ((ConnectivityManager) ctx
				.getSystemService(Context.CONNECTIVITY_SERVICE))
				.getActiveNetworkInfo();

		if (info == null || !info.isConnected()) {
			return false;
		}

		return true;
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			   long id) {
		switch(parent.getId()) {
		case R.id.genderDrp:
			gender.setSelection(position);
			genderVar = (String) gender.getSelectedItem();
			System.out.println("Selected gender is : " + genderVar);
			break;
		case R.id.bloodGrp:
			bloodGrp.setSelection(position);
			bloodVar = (String) bloodGrp.getSelectedItem();
			System.out.println("Selected blood group is : " + bloodVar);
			break;
		}
			  
			  //selGender.setText("Gender :" + selState);
			 }

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}
}
