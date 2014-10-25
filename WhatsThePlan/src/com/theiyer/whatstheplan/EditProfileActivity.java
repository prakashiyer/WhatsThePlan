package com.theiyer.whatstheplan;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

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
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.theiyer.whatstheplan.entity.User;
import com.theiyer.whatstheplan.util.WTPConstants;
import com.thoughtworks.xstream.XStream;

public class EditProfileActivity extends FragmentActivity implements
		OnItemSelectedListener {
	private String[] genderString = { "Male", "Female" };
	private String[] bloodGrpString = { "A-positive", "B-positive",
			"B-negative", "A-negative", "O-positive", "O-negative",
			"AB-positive", "AB-negative" };
	Spinner genderSpinner;
	CheckBox checkBox;
	private String genderVar;
	private String doctorFlag;
	private String bloodVar;

	private static final String TAG = "Health Meet/Edit";

	Spinner bloodGroup;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (haveInternet(this)) {
			setContentView(R.layout.new_user_registration);
			ActionBar aBar = getActionBar();
			Resources res = getResources();
			Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
			aBar.setBackgroundDrawable(actionBckGrnd);
			aBar.setTitle(" Edit Profile");
			SharedPreferences prefs = getSharedPreferences("Prefs",
					Activity.MODE_PRIVATE);
			genderSpinner = (Spinner) findViewById(R.id.genderDrp);
			ArrayAdapter<String> adapter_state = new ArrayAdapter<String>(this,
					android.R.layout.simple_spinner_item, genderString);
			adapter_state
					.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			genderSpinner.setAdapter(adapter_state);
			genderSpinner.setOnItemSelectedListener(this);
			bloodGroup = (Spinner) findViewById(R.id.bloodGrp);
			ArrayAdapter<String> adapter_state1 = new ArrayAdapter<String>(
					this, android.R.layout.simple_spinner_item, bloodGrpString);
			adapter_state1
					.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			bloodGroup.setAdapter(adapter_state1);
			bloodGroup.setOnItemSelectedListener(this);
			String userName = prefs.getString("userName", "New User");

			TextView userNameValue = (TextView) findViewById(R.id.newUserNameValue);
			userNameValue.setText(userName);

			Button button = (Button) findViewById(R.id.registerButton);
			button.setText("Save Profile Changes");
			String phone = prefs.getString("phone", "");
			String userQuery = "/fetchUser?phone=" + phone;
			UserWebServiceClient userRestClient = new UserWebServiceClient(this);
			userRestClient.execute(new String[] { userQuery });

		} else {
			Intent intent = new Intent(this, RetryActivity.class);
			startActivity(intent);
		}

	}

	public class UserWebServiceClient extends
			AsyncTask<String, Integer, String> {

		private Context mContext;
		private ProgressDialog pDlg;
		private String query;

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
			String path = WTPConstants.SERVICE_PATH + params[0];
			query = params[0];

			// HttpHost target = new HttpHost(TARGET_HOST);
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
			if (response != null && query.contains("fetchUser")) {
				XStream userXs = new XStream();
				userXs.alias("UserInformation", User.class);
				userXs.alias("centers", String.class);
				userXs.addImplicitCollection(User.class, "centers", "centers",
						String.class);
				User user = (User) userXs.fromXML(response);
				if (user != null && user.getName() != null) {
					LinearLayout phoneValue = (LinearLayout) findViewById(R.id.newUserPhoneLayout);
					phoneValue.setVisibility(TextView.INVISIBLE);
					TextView dobValue = (TextView) findViewById(R.id.dateOfBirth);
					dobValue.setText(user.getDob());

					if ("Male".equals(user.getSex())) {
						genderSpinner.setSelection(0);
					} else {
						genderSpinner.setSelection(1);
					}
					if ("A-positive".equals(user.getBloodGroup())) {
						bloodGroup.setSelection(0);
					} else if ("B-positive".equals(user.getBloodGroup())) {
						bloodGroup.setSelection(1);
					} else if ("B-negative".equals(user.getBloodGroup())) {
						bloodGroup.setSelection(2);
					} else if ("A-negative".equals(user.getBloodGroup())) {
						bloodGroup.setSelection(3);
					} else if ("O-positive".equals(user.getBloodGroup())) {
						bloodGroup.setSelection(4);
					} else if ("O-negative".equals(user.getBloodGroup())) {
						bloodGroup.setSelection(5);
					} else if ("AB-positive".equals(user.getBloodGroup())) {
						bloodGroup.setSelection(6);
					} else if ("AB-negative".equals(user.getBloodGroup())) {
						bloodGroup.setSelection(7);
					}
					checkBox = (CheckBox) findViewById(R.id.codeCheckBox);
					TextView addressValue = (TextView) findViewById(R.id.newUserAddressValue);
					addressValue.setText(user.getAddress());
					if ("Y".equals(user.getDoctorFlag())) {
						checkBox.setChecked(true);
						doctorFlag = "Y";
					} else {
						checkBox.setChecked(false);
						doctorFlag = "N";
					}
				}				
			}
			pDlg.dismiss();
		}

	}

	/** Called when the user checks the change password */
	public void enterDocCheck(View view) {
		if (checkBox.isChecked()) {
			doctorFlag = "Y";
		} else {
			doctorFlag = "N";
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
		button.setText(" Edit User ");
		EditText userNameEditText = (EditText) findViewById(R.id.newUserNameValue);
		String userName = userNameEditText.getText().toString();

		EditText phoneText = (EditText) findViewById(R.id.newUserPhoneValue);
		String phone = phoneText.getText().toString();

		TextView dob = (TextView) findViewById(R.id.dateOfBirth);
		String dobText = dob.getText().toString();

		EditText addressText = (EditText) findViewById(R.id.newUserAddressValue);
		String address = addressText.getText().toString();

		SharedPreferences prefs = getSharedPreferences("Prefs",
				Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("userName", userName);
		editor.putString("phone", phone);
		editor.putString("dob", dobText);
		editor.putString("gender", genderVar);
		editor.putString("bloodGrp", bloodVar);
		editor.putString("Address", address);
		editor.putString("docFlag", doctorFlag);
		editor.apply();
		String userQuery = "/editUser?phone=" + phone + "&name=" + userName
				+ "&bloodGroup=" + bloodVar + "&dob=" + dobText + "&sex="
				+ genderVar + "&address=" + address + "&doctorFlag="
				+ doctorFlag + "&primaryCenterId=" + "&primaryDoctorId="
				+ "&centers=";
		UserWebServiceClientNew userRestClientNew = new UserWebServiceClientNew(
				this);
		userRestClientNew.execute(new String[] { userQuery });
		
	}

	public class UserWebServiceClientNew extends
			AsyncTask<String, Integer, String> {

		private Context mContext;
		private ProgressDialog pDlg;

		public UserWebServiceClientNew(Context mContext) {
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
			String path = WTPConstants.SERVICE_PATH + params[0];

			// HttpHost target = new HttpHost(TARGET_HOST);
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
				userXs.addImplicitCollection(User.class, "centers", "centers",
						String.class);
				User user = (User) userXs.fromXML(response);
				if (user != null && user.getName() != null) {
					Toast.makeText(getApplicationContext(), "User profile Updated.",
							Toast.LENGTH_LONG).show();
					Intent intent = new Intent(mContext, HomePlanGroupFragmentActivity.class);
					startActivity(intent);
				}
			}
			pDlg.dismiss();
		}

	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		switch (parent.getId()) {
		case R.id.genderDrp:
			genderSpinner.setSelection(position);
			genderVar = (String) genderSpinner.getSelectedItem();
			break;
		case R.id.bloodGrp:
			bloodGroup.setSelection(position);
			bloodVar = (String) bloodGroup.getSelectedItem();
			break;
		}
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
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}

}
