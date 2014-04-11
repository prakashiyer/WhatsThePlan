package com.theiyer.whatstheplan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.theiyer.whatstheplan.entity.Group;
import com.theiyer.whatstheplan.util.WTPConstants;
import com.thoughtworks.xstream.XStream;

public class JoinGroupActivity extends Activity {

	private ListView list;
	private GroupListAdapter adapter;
	private List<Map<String, byte[]>> groupsList;
	private String phone ;
	private List<String> members;
	private List<String> pendingMembers;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if(haveInternet(this)){
			setContentView(R.layout.join_group);
			ActionBar aBar = getActionBar();
			Resources res = getResources();
			Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
			aBar.setBackgroundDrawable(actionBckGrnd);
			aBar.setTitle(" Group Membership");

			SharedPreferences prefs = getSharedPreferences("Prefs",
					Activity.MODE_PRIVATE);
			String userName = prefs.getString("userName", "New User");
			TextView userNameValue = (TextView) findViewById(R.id.welcomeJoinGroupLabel);
			userNameValue.setText(userName + ", Search and join your group!");
			
			SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
			final SearchView searchView = (SearchView) findViewById(R.id.groupSearchView);
			SearchableInfo searchableInfo = searchManager.getSearchableInfo(getComponentName());
			searchView.setSearchableInfo(searchableInfo);
		} else {
			Intent intent = new Intent(this, RetryActivity.class);
			startActivity(intent);
		}	
		
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			
			String groupName = intent.getStringExtra(SearchManager.QUERY);
			// Search Group Names and add to a list
			String searchQuery = "/searchGroup?groupName=" + groupName.replace(" ", "%20");
		    WebServiceClient restClient = new WebServiceClient(this);
			TextView groupNameLabel= (TextView) findViewById(R.id.groupSearchResultsLabel);
			groupNameLabel.setVisibility(TextView.INVISIBLE);
			adapter = new GroupListAdapter(this);
			restClient.execute(
					new String[] { searchQuery });			
		}
	}

	/** Called when the user clicks the Join Group button */
	public void goFromJoinGroupToViewGroups(View view) {
		Button button = (Button) findViewById(R.id.joinGroupButton);
		button.setTextColor(getResources().getColor(R.color.click_button_2));
		TextView groupNameValue = (TextView) findViewById(R.id.groupSearchResultValue);
		String groupName = groupNameValue.getText().toString();
		SharedPreferences prefs = getSharedPreferences("Prefs",
				Activity.MODE_PRIVATE);
		String phone = prefs.getString("phone", "");

		String joinQuery = "/joinGroup?groupName=" + groupName.replace(" ", "%20")
				+ "&phone=" + phone;
		WebServiceClient restClient = new WebServiceClient(this);
		restClient.execute(
				new String[] { joinQuery });
		Intent intent = new Intent(this, GroupsListActivity.class);
		startActivity(intent);
		
	}
	
	private class WebServiceClient extends AsyncTask<String, Integer, String> {

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
			String path = WTPConstants.SERVICE_PATH+params[0];

			if(params[0].contains("searchGroup")){
				isSearchCall = true;
			}
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
			
			SharedPreferences prefs = getSharedPreferences("Prefs",
					Activity.MODE_PRIVATE);
			if (response != null && isSearchCall) {
				XStream xstream = new XStream();
				xstream.alias("Group", Group.class);
				
				xstream.alias("members", String.class);
				xstream.addImplicitCollection(Group.class, "members","members",String.class);
				xstream.alias("planNames", String.class);
				xstream.addImplicitCollection(Group.class, "planNames","planNames",String.class);
				xstream.alias("pendingMembers", String.class);
				xstream.addImplicitCollection(Group.class, "pendingMembers","pendingMembers",String.class);
				Group group = (Group) xstream.fromXML(response);
				if (group != null) {

					String groupName = group.getName();
					groupsList = new ArrayList<Map<String, byte[]>>();

					WebImageRetrieveRestWebServiceClient imageClient = new WebImageRetrieveRestWebServiceClient(
							mContext);
					list = (ListView) findViewById(R.id.joingroupList);
					phone = prefs.getString("phone","");
					members = group.getMembers();
				    pendingMembers = group.getPendingMembers();
					imageClient.execute(
							new String[] { "fetchGroupImage", groupName.replace(" ", "%20") });
					
				} else {
					Toast.makeText(mContext,
							"Seems like an invalid group name", Toast.LENGTH_LONG)
							.show();
				}
					 
			} else {
				setContentView(R.layout.join_group);
				Toast.makeText(mContext,
						"Seems like an invalid group name", Toast.LENGTH_LONG)
						.show();
			}
			pDlg.dismiss();
		}

	}
	
	public class WebImageRetrieveRestWebServiceClient extends AsyncTask<String, Integer, byte[]> {

		private Context mContext;
		private ProgressDialog pDlg;
		private String groupName;

		public WebImageRetrieveRestWebServiceClient(Context mContext) {
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
		protected byte[] doInBackground(String... params) {
			String method = params[0];
			groupName = params[1].replace("%20", " ");
			String path = WTPConstants.SERVICE_PATH+"/"+method;

			if("fetchUserImage".equals(method)){
	        	path = path+"?phone="+params[1];
	        } else {
	        	path = path+"?groupName="+params[1];
	        }
			//HttpHost target = new HttpHost(TARGET_HOST);
			HttpHost target = new HttpHost(WTPConstants.TARGET_HOST, 8080);
			HttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet(path);
			HttpEntity results = null;

			try {
				
				HttpResponse response = client.execute(target, get);
				results = response.getEntity(); 
				byte[] byteresult = EntityUtils.toByteArray(results);
				return byteresult;
			} catch (Exception e) {
			}
			return null;
		}

		@Override
		protected void onPostExecute(byte[] response) {
			TextView groupNameLabel= (TextView) findViewById(R.id.groupSearchResultsLabel);
			TextView groupNameValue = (TextView) findViewById(R.id.groupSearchResultValue);
			
			if(response != null){
				Map<String, byte[]> groupDetails = new HashMap<String, byte[]>();
				groupDetails.put(groupName, response);
				groupsList.add(groupDetails);
				adapter.setData(groupsList);
				list.setAdapter(adapter);
				groupNameLabel.setVisibility(TextView.VISIBLE);
				groupNameValue.setText(groupName);
				
				if(pendingMembers == null || (!pendingMembers.contains(phone) && !members.contains(phone))){
					Button joinButton = (Button) findViewById(R.id.joinGroupButton);
					joinButton.setVisibility(Button.VISIBLE);
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
