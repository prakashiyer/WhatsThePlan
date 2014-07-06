package com.theiyer.whatstheplan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

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
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

import com.theiyer.whatstheplan.entity.Group;
import com.theiyer.whatstheplan.entity.GroupList;
import com.theiyer.whatstheplan.util.WTPConstants;
import com.thoughtworks.xstream.XStream;

public class ViewExistingGroupsActivity extends Activity implements
OnItemClickListener {

	GridView groupsGridView;
	GroupsGridAdapter adapter;
	List<Map<String, Group>> groupsList;
	Context context;
	List<Map<String, Group>> filteredList;
	String selectedGroups;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (haveInternet(this)) {
			setContentView(R.layout.existing_groups_grid);
			ActionBar aBar = getActionBar();
			Resources res = getResources();
			Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
			aBar.setBackgroundDrawable(actionBckGrnd);
			aBar.setTitle(" Existing Groups");

			groupsList = new ArrayList<Map<String, Group>>();
			filteredList = new ArrayList<Map<String, Group>>();
			groupsGridView = (GridView) findViewById(R.id.viewExistingGroupsGrid);
			adapter = new GroupsGridAdapter(this);
			groupsGridView.setOnItemClickListener(this);
			selectedGroups = "";
			context = this;

			SharedPreferences prefs = getSharedPreferences("Prefs",
					Activity.MODE_PRIVATE);
			String phone = prefs.getString("phone", "");
			
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString("selectedGroups", selectedGroups);
			editor.apply();
			
			String searchQuery = "/fetchExistingGroups?phone="
					+ phone;

			WebServiceClient restClient = new WebServiceClient(this);
			restClient.execute(new String[] { searchQuery });
		} else {
			Intent intent = new Intent(this, RetryActivity.class);
			startActivity(intent);
		}

		SearchView searchView = (SearchView) findViewById(R.id.groupsSearchView);
		
		searchView.setOnQueryTextListener(new OnQueryTextListener() {
			
			@Override
			public boolean onQueryTextSubmit(String query) {
				if (!groupsList.isEmpty()) {
					
				    filteredList = new ArrayList<Map<String,Group>>();
					for(Map<String, Group> groupEntry: groupsList){
						for(Entry<String, Group> entry : groupEntry.entrySet()){
							Group group = entry.getValue();
							if(group.getName().toLowerCase(Locale.ENGLISH).contains(query.toLowerCase(Locale.ENGLISH))){
								filteredList.add(groupEntry);
							}
						}
					}
					
					adapter.setData(filteredList);
					groupsGridView.setAdapter(adapter);
					//memberListLabel.setVisibility(TextView.VISIBLE);
					groupsGridView.setVisibility(GridView.VISIBLE);
					
					
				}
				return true;
			}
			
			@Override
			public boolean onQueryTextChange(String newText) {
				if (!groupsList.isEmpty()) {
					
					filteredList = new ArrayList<Map<String,Group>>();
					for(Map<String, Group> groupEntry: groupsList){
						for(Entry<String, Group> entry : groupEntry.entrySet()){
							Group group = entry.getValue();
							if(group.getName().toLowerCase(Locale.ENGLISH).contains(newText.toLowerCase(Locale.ENGLISH))){
								filteredList.add(groupEntry);
							}
						}
					}
					
					adapter.setData(filteredList);
					groupsGridView.setAdapter(adapter);
					//memberListLabel.setVisibility(TextView.VISIBLE);
					groupsGridView.setVisibility(GridView.VISIBLE);
					
					
				}
				return true;
			}
		});
		
		
	}
	
	/** Called when the user clicks the see members button */
	public void goToPlanSelection(View view) {
		Button button = (Button) findViewById(R.id.goToPlanSelectionButton);
		button.setTextColor(getResources().getColor(R.color.click_button_1));
		Intent intent = new Intent(this, NewPlanActivity.class);
		startActivity(intent);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (filteredList != null && !filteredList.isEmpty()) {
			Map<String, Group> selectedMap = filteredList.get(position);

			for (Entry<String, Group> entry : selectedMap.entrySet()) {
				SharedPreferences prefs = getSharedPreferences("Prefs",
						Activity.MODE_PRIVATE);
				SharedPreferences.Editor editor = prefs.edit();
				Group group = entry.getValue();
				if(group.isSelected()){
					group.setSelected(false);
					selectedGroups = selectedGroups.replace(group.getName()+",", "");
					editor.putString("selectedGroups", selectedGroups);
					editor.apply();
					adapter.setData(filteredList);
					groupsGridView.setAdapter(adapter);
					//memberListLabel.setVisibility(TextView.VISIBLE);
					groupsGridView.setVisibility(GridView.VISIBLE);
				} else {
					group.setSelected(true);
					selectedGroups = selectedGroups + group.getName()+",";
					editor.putString("selectedGroups", selectedGroups);
					System.out.println("selected Groups: " +selectedGroups);
					editor.apply();
					adapter.setData(filteredList);
					groupsGridView.setAdapter(adapter);
					groupsGridView.setVisibility(GridView.VISIBLE);
				}
				
				break;
			}
		}
	}
	
	
	@Override
	protected void onNewIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			if (!groupsList.isEmpty()) {
				
				List<Map<String, Group>> filteredList = new ArrayList<Map<String,Group>>();
				for(Map<String, Group> groupEntry: groupsList){
					for(Entry<String, Group> entry : groupEntry.entrySet()){
						Group group = entry.getValue();
						if(group.getName().toLowerCase(Locale.ENGLISH).contains(query.toLowerCase(Locale.ENGLISH))){
							filteredList.add(groupEntry);
						}
					}
				}
				
				adapter.setData(filteredList);
				groupsGridView.setAdapter(adapter);
				groupsGridView.setVisibility(GridView.VISIBLE);
				
				
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
				System.out.println("RESPONSE: "+response);
				XStream groupsXstream = new XStream();
				groupsXstream.alias("GroupList", GroupList.class);
				groupsXstream.addImplicitCollection(GroupList.class, "groups");
				groupsXstream.alias("groups", Group.class);
				
				groupsXstream.alias("members", String.class);
				groupsXstream.addImplicitCollection(Group.class, "members","members",String.class);
				groupsXstream.alias("planNames", String.class);
				groupsXstream.addImplicitCollection(Group.class, "planNames","planNames",String.class);
				groupsXstream.alias("pendingMembers", String.class);
				groupsXstream.addImplicitCollection(Group.class, "pendingMembers","pendingMembers",String.class);
				GroupList groupList = (GroupList) groupsXstream.fromXML(response);
				if (groupList != null) {
					
					List<Group> groups = groupList.getGroups();
					if(groups != null && !groups.isEmpty()){
						for(Group group: groups){
							Map<String, Group> groupMap = new HashMap<String, Group>();
							groupMap.put(String.valueOf(group.getId()), group);
							groupsList.add(groupMap);
							
						}
						
						if (!groupsList.isEmpty()) {
							filteredList = new ArrayList<Map<String,Group>>();
							filteredList.addAll(groupsList);
							adapter.setData(filteredList);
							groupsGridView.setAdapter(adapter);
							groupsGridView.setVisibility(GridView.VISIBLE);
							
							
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
