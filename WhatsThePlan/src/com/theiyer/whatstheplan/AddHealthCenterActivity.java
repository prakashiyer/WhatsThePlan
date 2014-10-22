package com.theiyer.whatstheplan;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.theiyer.whatstheplan.AddDoctorActivity.UserWebServiceClient;
import com.theiyer.whatstheplan.entity.CenterList;
import com.theiyer.whatstheplan.entity.Center;
import com.theiyer.whatstheplan.entity.User;
import com.theiyer.whatstheplan.util.WTPConstants;
import com.thoughtworks.xstream.XStream;

public class AddHealthCenterActivity extends Activity implements OnItemClickListener{
	private GridView healthGridView;
	private CenterGridAdapter adapter;
	private List<Map<String, Center>> healthCenterList;
	private List<Map<String, Center>> filteredList;
	private String selectedHealthCenter;
	private Context context;

	private GoogleCloudMessaging gcm;
	private String regid;

	private static final String TAG = "Health Meet Health Center search";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (haveInternet(this)) {
			setContentView(R.layout.add_health_centre);
			ActionBar aBar = getActionBar();
			context = getApplicationContext();
			Resources res = getResources();
			Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
			aBar.setBackgroundDrawable(actionBckGrnd);
			aBar.setTitle(" Select a Health Center");

			SharedPreferences prefs = getSharedPreferences("Prefs",
					Activity.MODE_PRIVATE);
			String userName = prefs.getString("name", "New User");
			String newUserFlag = prefs.getString("newUser", "N");
			TextView userNameValue = (TextView) findViewById(R.id.welcomeAddHealthCentreLabel);
			userNameValue.setText(userName + ", Search and join your Health Centre!");
			if ("N".equals(newUserFlag)) {
				Button button = (Button) findViewById(R.id.registerButton);
				button.setText("Update Health Center");
			}
			healthCenterList = new ArrayList<Map<String, Center>>();
			filteredList = new ArrayList<Map<String, Center>>();
			healthGridView = (GridView) findViewById(R.id.viewhealthCenterGrid);
			System.out.println("HEATH GRID VIEW : " + healthGridView);
			adapter = new CenterGridAdapter(this);
			healthGridView.setOnItemClickListener(this);
			selectedHealthCenter = "";

			SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
			final SearchView searchView = (SearchView) findViewById(R.id.healthCentreSearchView);
			SearchableInfo searchableInfo = searchManager.getSearchableInfo(getComponentName());
			searchView.setSearchableInfo(searchableInfo);
			System.out.println("****** Search Manager **** " + searchableInfo);
			Intent intent = getIntent();
			System.out.println("**** INTENT *** " + intent);
			if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
				onNewIntent(intent);
			}
		} else {
			Intent intent = new Intent(this, RetryActivity.class);
			startActivity(intent);
		}
		
	}
	
	/** Called when the user clicks the Join Group button */
	public void goFromJoinGroupToViewGroups(View view) {
		Button button = (Button) findViewById(R.id.registerButton);
		button.setTextColor(getResources().getColor(R.color.click_button_2));
		SharedPreferences prefs = getSharedPreferences("Prefs",
				Activity.MODE_PRIVATE);
		String name = prefs.getString("name", "");
		String phone = prefs.getString("phone", "");
		String dob = prefs.getString("dob", "");
		String gender = prefs.getString("gender", "");
		String bloodGrp = prefs.getString("bloodGrp", "");
		String address = prefs.getString("Address", "");
		String doctor = prefs.getString("doctor", "");
		String selectedDoctor = prefs.getString("selectedDoctor", "0");
		gcm = GoogleCloudMessaging.getInstance(context);
		Asyncer syncer = new Asyncer();
		syncer.execute(new String[] {phone});
		if (selectedHealthCenter == "") {
			selectedHealthCenter = "0";
		}
		String newUserFlag = prefs.getString("newUser", "N");
		if ("Y".equals(newUserFlag)) {
			System.out.println("selectedDoctor : " + selectedDoctor);
			System.out.println("selectedHealthCenter : " + selectedHealthCenter);
			String userQuery = "/addUser?phone="+phone+"&name="+name
					+"&bloodGroup=" + bloodGrp
					+"&dob=" + dob
					+"&sex=" + gender
					+"&address=" + address
					+"&doctorFlag=" + doctor
					+"&primaryCenterId="+ selectedHealthCenter
					+"&primaryDoctorId="+ selectedDoctor
					+"&centers=" + "";
			UserWebServiceClient userRestClient = new UserWebServiceClient(this);
			userRestClient.execute(new String[] { userQuery});
			AccountManager am = AccountManager.get(this);
			final Account account = new Account(phone,
					WTPConstants.ACCOUNT_ADDRESS);
			final Bundle bundle = new Bundle();
			bundle.putString("userName", name);
			bundle.putString("phone", phone);
			bundle.putString("doctor", doctor);
			bundle.putString(AccountManager.KEY_ACCOUNT_NAME,
					account.name);
			am.addAccountExplicitly(account, phone, bundle);
			am.setAuthToken(account, "Full Access", phone);
			Intent intent = new Intent(this, ProfileImageUploadActivity.class);
			startActivity(intent);
			Toast.makeText(getApplicationContext(), "Congratulations! Your Profile has been activated.",
					Toast.LENGTH_LONG).show();
		} else if ("N".equals(newUserFlag)) {
			String userQuery = "/addCenter?phone="+phone
					+"&primaryCenterId="+ selectedHealthCenter;
			UserWebServiceClient userRestClient = new UserWebServiceClient(this);
			userRestClient.execute(new String[] { userQuery});
			Toast.makeText(getApplicationContext(), "Selected Center has been added as your primary center.",
					Toast.LENGTH_LONG).show();
		}
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
							}
			}
			pDlg.dismiss();
		}

	}
	
	//@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (filteredList != null && !filteredList.isEmpty()) {
			SharedPreferences prefs = getSharedPreferences("Prefs",
					Activity.MODE_PRIVATE);
			SharedPreferences.Editor editor = prefs.edit();
			for(Map<String, Center> selectedMap: healthCenterList){
				for (Entry<String, Center> entry : selectedMap.entrySet()) {
					Center center = entry.getValue();
					if(center.isSelected()){
						center.setSelected(false);
					}
				}
			}
			for(Map<String, Center> selectedMap: filteredList){
				for (Entry<String, Center> entry : selectedMap.entrySet()) {
					Center center = entry.getValue();
					if(center.isSelected()){
						center.setSelected(false);
						selectedHealthCenter = "";
						editor.putString("selectedHealthCenter", selectedHealthCenter);
						editor.apply();
						adapter.setData(filteredList);
						healthGridView.setAdapter(adapter);
						healthGridView.setVisibility(GridView.VISIBLE);
					}
				}
			}
			Map<String, Center> selectedMap = filteredList.get(position);

			for (Entry<String, Center> entry : selectedMap.entrySet()) {
				Center center = entry.getValue();
				center.setSelected(true);
				selectedHealthCenter = center.getAdminPhone();
				editor.putString("selectedHealthCenter", selectedHealthCenter);
				editor.apply();
				adapter.setData(filteredList);
				healthGridView.setAdapter(adapter);
				healthGridView.setVisibility(GridView.VISIBLE);
				
				break;
			}
		}
	}
	
	
	@Override
	protected void onNewIntent(Intent intent) {
		System.out.println("***************I M HERE *****" + intent.getAction());
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String centerName = intent.getStringExtra(SearchManager.QUERY);
			System.out.println("I M HERE *****" + centerName);
			String searchQuery = "/searchCenter?name=" + centerName.replace(" ", "%20");
		    WebServiceClient restClient = new WebServiceClient(this);
			TextView centerNameLabel= (TextView) findViewById(R.id.healthCentreSearchResultsLabel);
			centerNameLabel.setVisibility(TextView.INVISIBLE);
			adapter = new CenterGridAdapter(this);
			restClient.execute(
					new String[] { searchQuery });
		}
	}
	
	@Override
	public void onBackPressed() {
		Intent intent = new Intent(this,HomePlanGroupFragmentActivity.class);
		startActivity(intent);
	}

	public class WebServiceClient extends AsyncTask<String, Integer, String> {

		private Context mContext;
		private ProgressDialog pDlg;
		private boolean isSearchCall;
		public WebServiceClient(Context mContext) {
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
			if(params[0].contains("searchCenter")){
				isSearchCall = true;
			}
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
			

			if (response != null && isSearchCall) {
				System.out.println("RESPONSE: "+response);				
				XStream userXs = new XStream();
			    userXs.alias("CenterList", CenterList.class);
				userXs.addImplicitCollection(CenterList.class, "centers");
			    userXs.alias("centers", Center.class);
				userXs.alias("members", String.class);
				userXs.addImplicitCollection(Center.class, "members",
						"members", String.class);
				CenterList centerList = (CenterList) userXs.fromXML(response);
				System.out.println("**** CENTRES : " + centerList.getCenters());
				if (centerList.getCenters() != null) {
					TextView healthCentreResult = (TextView) findViewById(R.id.healthCentreSearchResultsLabel);
					healthCentreResult.setVisibility(TextView.INVISIBLE);
					Button button = (Button) findViewById(R.id.registerButton);
					button.setVisibility(Button.VISIBLE);
					for(Center center: centerList.getCenters()){
						Log.i(TAG, center.getName());
						Map<String, Center> centerMap = new HashMap<String, Center>();
						centerMap.put(String.valueOf(center.getId()), center);
						healthCenterList.add(centerMap);
					}
					if (!healthCenterList.isEmpty()) {
						filteredList = new ArrayList<Map<String,Center>>();
						filteredList.addAll(healthCenterList);
						adapter.setData(filteredList);
						healthGridView.setAdapter(adapter);
						healthGridView.setVisibility(GridView.VISIBLE);				
				}
			} else {
				System.out.println("I m IN else *** no helath centres found");
				TextView healthCentreResult = (TextView) findViewById(R.id.healthCentreSearchResultsLabel);
				healthCentreResult.setVisibility(TextView.VISIBLE);
				healthCentreResult.setText("No health centres found for your search. Please try again.");
			}
			pDlg.dismiss();
		} 
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
}
