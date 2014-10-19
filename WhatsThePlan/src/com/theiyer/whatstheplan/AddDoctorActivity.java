package com.theiyer.whatstheplan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.theiyer.whatstheplan.entity.UserList;
import com.theiyer.whatstheplan.entity.User;
import com.theiyer.whatstheplan.util.WTPConstants;
import com.thoughtworks.xstream.XStream;

public class AddDoctorActivity extends Activity implements OnItemClickListener{
	
		private GridView doctorGridView;
		private int lastPos;
		private DoctorGridAdapter adapter;
		private List<Map<String, User>> doctorList;
		private Context context;
		private List<Map<String, User>> filteredList;
		private String selectedDoctor;

		private static final String TAG = "Health Meet Doctor search";
		
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			if (haveInternet(this)) {
				setContentView(R.layout.add_doctor);
				ActionBar aBar = getActionBar();
				Resources res = getResources();
				Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
				aBar.setBackgroundDrawable(actionBckGrnd);
				aBar.setTitle(" Select a Doctor");

				SharedPreferences prefs = getSharedPreferences("Prefs",
						Activity.MODE_PRIVATE);
				String userName = prefs.getString("name", "New User");
				TextView userNameValue = (TextView) findViewById(R.id.welcomeAddDoctorLabel);
				userNameValue.setText(userName + ", Search and select your doctor!");

				doctorList = new ArrayList<Map<String, User>>();
				filteredList = new ArrayList<Map<String, User>>();
				doctorGridView = (GridView) findViewById(R.id.viewhealthCenterGrid);
				System.out.println("HEATH GRID VIEW " + doctorGridView);
				adapter = new DoctorGridAdapter(this);
				doctorGridView.setOnItemClickListener(this);
				selectedDoctor = "";
				context = this;
				
				
				
				
				SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
				final SearchView searchView = (SearchView) findViewById(R.id.DoctorSearchView);
				SearchableInfo searchableInfo = searchManager.getSearchableInfo(getComponentName());
				searchView.setSearchableInfo(searchableInfo);
				System.out.println("****** Search Manager **** " + searchableInfo);
				Intent intent = getIntent();
				System.out.println("**** INTENT *** " + intent);
				if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
					onNewIntent(intent);
				}
				/*String name = prefs.getString("name", "");
				
				SharedPreferences.Editor editor = prefs.edit();
				editor.putString("selectedHealthCenter", selectedHealthCenter);
				editor.apply();*/
				
				/*String searchQuery = "/fetchExistingGroups?name="
						+ name;

				WebServiceClient restClient = new WebServiceClient(this);
				restClient.execute(new String[] { searchQuery });*/
			} else {
				Intent intent = new Intent(this, RetryActivity.class);
				startActivity(intent);
			}
			
		}
		
		/** Called when the user clicks the Join Group button */
		public void goFromJoinGroupToViewGroups(View view) {
			Button button = (Button) findViewById(R.id.registerButton);
			button.setTextColor(getResources().getColor(R.color.click_button_2));

			Toast.makeText(getApplicationContext(), "Selected Doctor Added to your account",
					Toast.LENGTH_LONG).show();
			Intent intent = new Intent(this, AddHealthCenterActivity.class);
			startActivity(intent);
			
		}
		
		//@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			if (filteredList != null && !filteredList.isEmpty()) {

				/*for (Entry<String, User> entry : selectedMap.entrySet()) {
					SharedPreferences prefs = getSharedPreferences("Prefs",
							Activity.MODE_PRIVATE);
					SharedPreferences.Editor editor = prefs.edit();
					User doctor = entry.getValue();
					if(doctor.isSelected()){
						doctor.setSelected(false);
						selectedDoctor = entry.getKey();
						System.out.println("***** doctor " + entry.getKey());
						editor.putString("selectedDoctor", selectedDoctor);
						editor.apply();
						System.out.println("selected doctor****: " +selectedDoctor);
						adapter.setData(filteredList);
						doctorGridView.setAdapter(adapter);
						lastPos = position;
						doctorGridView.setSelection((int)(doctorGridView.getAdapter()).getItemId(lastPos));
						//memberListLabel.setVisibility(TextView.VISIBLE);
						doctorGridView.setVisibility(GridView.VISIBLE);
					} else {
						doctor.setSelected(true);
						editor.putString("selectedDoctor", selectedDoctor);
						System.out.println("selected doctor: " +selectedDoctor);
						editor.apply();
						adapter.setData(filteredList);
						doctorGridView.setAdapter(adapter);
						doctorGridView.setVisibility(GridView.VISIBLE);
					}
					
					break;
				}*/
				SharedPreferences prefs = getSharedPreferences("Prefs",
						Activity.MODE_PRIVATE);
				SharedPreferences.Editor editor = prefs.edit();
				for(Map<String, User> selectedMap: doctorList){
					for (Entry<String, User> entry : selectedMap.entrySet()) {
						User user = entry.getValue();
						if(user.isSelected()){
							user.setSelected(false);
						}
					}
				}
				for(Map<String, User> selectedMap: filteredList){
					for (Entry<String, User> entry : selectedMap.entrySet()) {
						User user = entry.getValue();
						if(user.isSelected()){
							user.setSelected(false);
							selectedDoctor = "";
							editor.putString("selectedDoctor", selectedDoctor);
							editor.apply();
							adapter.setData(filteredList);
							doctorGridView.setAdapter(adapter);
							doctorGridView.setVisibility(GridView.VISIBLE);
						}
					}
				}
				Map<String, User> selectedMap = filteredList.get(position);

				for (Entry<String, User> entry : selectedMap.entrySet()) {
					
					String selectedMember = entry.getKey();
					User user = entry.getValue();
					user.setSelected(true);
					selectedDoctor = selectedMember;
					editor.putString("selectedDoctor", selectedDoctor);
					editor.apply();
					adapter.setData(filteredList);
					doctorGridView.setAdapter(adapter);
					doctorGridView.setVisibility(GridView.VISIBLE);
					
					break;
				}
			}
		}
		
		
		@Override
		protected void onNewIntent(Intent intent) {
			System.out.println("***************I M HERE *****" + intent.getAction());
			if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
				String doctorName = intent.getStringExtra(SearchManager.QUERY);
				System.out.println("I M HERE *****" + doctorName);
				String searchQuery = "/searchDoctors?name=" + doctorName.replace(" ", "%20");
			    WebServiceClient restClient = new WebServiceClient(this);
				TextView doctorNameLabel = (TextView) findViewById(R.id.DoctorSearchResultsLabel);
				doctorNameLabel.setVisibility(TextView.INVISIBLE);
				adapter = new DoctorGridAdapter(this);
				restClient.execute(
						new String[] { searchQuery });
				
				/*if (!healthCenterList.isEmpty()) {
					String query = intent.getStringExtra(SearchManager.QUERY);
					List<Map<String, Center>> filteredList = new ArrayList<Map<String,Center>>();
					for(Map<String, Center> centerEntry: healthCenterList){
						for(Entry<String, Center> entry : centerEntry.entrySet()){
							Center healthCenter = entry.getValue();
							if(healthCenter.getName().toLowerCase(Locale.ENGLISH).contains(query.toLowerCase(Locale.ENGLISH))){
								filteredList.add(centerEntry);
							}
						}
					}
					
					adapter.setData(filteredList);
					doctorGridView.setAdapter(adapter);
					doctorGridView.setVisibility(GridView.VISIBLE);
					
					
				}*/
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
				if(params[0].contains("searchDoctors")){
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
					XStream userXstream = new XStream();
					userXstream.alias("UserList", UserList.class);
					userXstream.addImplicitCollection(UserList.class, "users");
					userXstream.alias("users", User.class);
					userXstream.alias("centers", String.class);
					userXstream.addImplicitCollection(User.class, "centers",
							"centers", String.class);
					UserList userList = (UserList) userXstream.fromXML(response);
					System.out.println("**** Doctors : " + userList.getUsers());
					if (userList.getUsers() != null) {
						TextView healthCentreResult = (TextView) findViewById(R.id.DoctorSearchResultsLabel);
						healthCentreResult.setVisibility(TextView.INVISIBLE);
						for(User doctor: userList.getUsers()){
							Log.i(TAG, doctor.getName());
							Map<String, User> doctorMap = new HashMap<String, User>();
							doctorMap.put(String.valueOf(doctor.getPhone()), doctor);
							doctorList.add(doctorMap);
						}
						if (!doctorList.isEmpty()) {
							filteredList = new ArrayList<Map<String,User>>();
							filteredList.addAll(doctorList);
							adapter.setData(filteredList);
							doctorGridView.setAdapter(adapter);
							doctorGridView.setVisibility(GridView.VISIBLE);				
					}
				} else {
					System.out.println("I m IN else *** no doctors found");
					TextView doctorResult = (TextView) findViewById(R.id.DoctorSearchResultsLabel);
					doctorResult.setVisibility(TextView.VISIBLE);
					doctorResult.setText("No Doctors found for your search. Please try again.");
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