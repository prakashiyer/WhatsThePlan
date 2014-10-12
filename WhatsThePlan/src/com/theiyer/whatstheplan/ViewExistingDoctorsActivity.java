package com.theiyer.whatstheplan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
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
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

import com.theiyer.whatstheplan.entity.User;
import com.theiyer.whatstheplan.entity.UserList;
import com.theiyer.whatstheplan.util.WTPConstants;
import com.thoughtworks.xstream.XStream;

public class ViewExistingDoctorsActivity extends Activity implements
OnItemClickListener {

	private GridView doctorsGridView;
	private DoctorsGridAdapter adapter;
	private List<Map<String, User>> existingDoctorsList;
	private Context context;
	private List<Map<String, User>> filteredList;
	private String selectedIndividuals;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (haveInternet(this)) {
			setContentView(R.layout.existing_doctors_grid);
			ActionBar aBar = getActionBar();
			Resources res = getResources();
			Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
			aBar.setBackgroundDrawable(actionBckGrnd);
			aBar.setTitle("Select a Doctor");

			existingDoctorsList = new ArrayList<Map<String, User>>();
			filteredList = new ArrayList<Map<String, User>>();
			doctorsGridView = (GridView) findViewById(R.id.viewExistingDoctorsGrid);
			adapter = new DoctorsGridAdapter(this);
			doctorsGridView.setOnItemClickListener(this);
			selectedIndividuals = "";
			context = this;

			SharedPreferences prefs = getSharedPreferences("Prefs",
					Activity.MODE_PRIVATE);
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString("selectedIndividuals", selectedIndividuals);
			editor.apply();
			
			Cursor phones = getContentResolver().query(
					ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
					null, null, null);
			StringBuffer phoneBuffer = new StringBuffer();
			
			while (phones.moveToNext()) {
				int phoneType = phones
						.getInt(phones
								.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
				
				String phoneNumber = phones
						.getString(
								phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
						.trim();
				String[] source = new String[]{"(",")","+","-","."," "};
				String[] replace = new String[]{"","","","","",""};
				phoneNumber = StringUtils.replaceEach(phoneNumber, source, replace);
				int len = phoneNumber.length();
				if (len >= 10 && StringUtils.isNumeric(phoneNumber)) {
					phoneNumber = phoneNumber.substring(len - 10);
					switch (phoneType) {
					case Phone.TYPE_MOBILE:
						phoneBuffer.append(phoneNumber);
						System.out.println("Phone: "+phoneNumber);
						phoneBuffer.append(",");
						break;
					case Phone.TYPE_HOME:
						phoneBuffer.append(phoneNumber);
						System.out.println("Phone: "+phoneNumber);
						phoneBuffer.append(",");
						break;
					case Phone.TYPE_WORK:
						phoneBuffer.append(phoneNumber);
						System.out.println("Phone: "+phoneNumber);
						phoneBuffer.append(",");
						break;
					case Phone.TYPE_OTHER:
						phoneBuffer.append(phoneNumber);
						System.out.println("Phone: "+phoneNumber);
						phoneBuffer.append(",");
						break;
					default:
						break;
					}
				}

			}
			phones.close();
			
			phoneBuffer.deleteCharAt(phoneBuffer.lastIndexOf(","));
			
			String searchQuery = "/fetchExistingDoctors?phoneList="
					+ phoneBuffer.toString();

			WebServiceClient restClient = new WebServiceClient(this);
			restClient.execute(new String[] { searchQuery });
		} else {
			Intent intent = new Intent(this, RetryActivity.class);
			startActivity(intent);
		}

		
		
		SearchView searchView = (SearchView) findViewById(R.id.docSearchView);	
		searchView.setOnQueryTextListener(new OnQueryTextListener() {
			
			@Override
			public boolean onQueryTextSubmit(String query) {
				
				String searchQuery = "/searchDoctors?name="
						+ query.replace(" ", "%20");

				WebServiceClient restClient = new WebServiceClient(context);
				restClient.execute(new String[] { searchQuery });
				return true;
			}
			
			@Override
			public boolean onQueryTextChange(String newText) {
				if (!existingDoctorsList.isEmpty()) {
					
					filteredList = new ArrayList<Map<String,User>>();
					for(Map<String, User> member: existingDoctorsList){
						for(Entry<String, User> entry : member.entrySet()){
							User user = entry.getValue();
							if(user.getName().toLowerCase(Locale.ENGLISH).contains(newText.toLowerCase(Locale.ENGLISH))){
								filteredList.add(member);
							}
						}
					}
					
					adapter.setData(filteredList);
					doctorsGridView.setAdapter(adapter);
					//memberListLabel.setVisibility(TextView.VISIBLE);
					doctorsGridView.setVisibility(GridView.VISIBLE);
					
					
				}
				return true;
			}
		});
		
		
		
	}
	
	/** Called when the user clicks the see members button */
	public void goToCentersSelection(View view) {
		Button button = (Button) findViewById(R.id.goToCentersSelectionButton);
		button.setTextColor(getResources().getColor(R.color.click_button_1));
		Intent intent = new Intent(this, ViewExistingCentersActivity.class);
		startActivity(intent);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		
		for(Map<String, User> selectedMap: existingDoctorsList){
			for (Entry<String, User> entry : selectedMap.entrySet()) {
				User user = entry.getValue();
				if(user.isSelected()){
					user.setSelected(false);
				}
			}
		}
		
		if (filteredList != null && !filteredList.isEmpty()) {
			SharedPreferences prefs = getSharedPreferences("Prefs",
					Activity.MODE_PRIVATE);
			SharedPreferences.Editor editor = prefs.edit();
			for(Map<String, User> selectedMap: filteredList){
				for (Entry<String, User> entry : selectedMap.entrySet()) {
					User user = entry.getValue();
					if(user.isSelected()){
						user.setSelected(false);
						selectedIndividuals = "";
						editor.putString("selectedIndividuals", selectedIndividuals);
						editor.apply();
						adapter.setData(filteredList);
						doctorsGridView.setAdapter(adapter);
						doctorsGridView.setVisibility(GridView.VISIBLE);
					}
				}
			}
			Map<String, User> selectedMap = filteredList.get(position);

			for (Entry<String, User> entry : selectedMap.entrySet()) {
				
				String selectedMember = entry.getKey();
				User user = entry.getValue();
				user.setSelected(true);
				selectedIndividuals = selectedMember;
				editor.putString("selectedIndividuals", selectedIndividuals);
				editor.apply();
				adapter.setData(filteredList);
				doctorsGridView.setAdapter(adapter);
				doctorsGridView.setVisibility(GridView.VISIBLE);
				
				break;
			}
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
				
				XStream userXstream = new XStream();
				userXstream.alias("UserList", UserList.class);
				userXstream.addImplicitCollection(UserList.class, "users");
				userXstream.alias("users", User.class);
				userXstream.alias("centers", String.class);
				userXstream.addImplicitCollection(User.class, "centers",
						"centers", String.class);
				UserList userList = (UserList) userXstream.fromXML(response);
				if (userList != null) {
					
					List<User> users = userList.getUsers();
					if(users != null && !users.isEmpty()){
						for(User user: users){
							Map<String, User> memberMap = new HashMap<String, User>();
							memberMap.put(user.getPhone(), user);
							existingDoctorsList.add(memberMap);
							
						}
						
						if (!existingDoctorsList.isEmpty()) {
							filteredList = new ArrayList<Map<String,User>>();
							filteredList.addAll(existingDoctorsList);
							adapter.setData(filteredList);
							doctorsGridView.setAdapter(adapter);
							//memberListLabel.setVisibility(TextView.VISIBLE);
							doctorsGridView.setVisibility(GridView.VISIBLE);
							
							
						}
					}
					

				}
			}
			pDlg.dismiss();
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
