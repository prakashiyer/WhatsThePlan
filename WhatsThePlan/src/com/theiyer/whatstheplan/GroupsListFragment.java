package com.theiyer.whatstheplan;

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
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import com.theiyer.whatstheplan.entity.Group;
import com.theiyer.whatstheplan.entity.GroupList;
import com.theiyer.whatstheplan.util.WTPConstants;
import com.thoughtworks.xstream.XStream;

public class GroupsListFragment extends Fragment implements OnItemClickListener {
	
	Activity activity;
	
	//ListView list;
	GridView gridView;
	GroupListAdapter adapter;
	List<Map<String, Group>> groupsList;
	List<Group> allGroups;
	String phone;
	View rootView;
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity = this.getActivity();
		if(activity != null && haveInternet(activity)){
			ActionBar aBar = activity.getActionBar();
			Resources res = getResources();
			Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
			aBar.setBackgroundDrawable(actionBckGrnd);
			aBar.setTitle(" My Groups");

			SharedPreferences prefs = activity.getSharedPreferences("Prefs",
					Activity.MODE_PRIVATE);
			String userName = prefs.getString("userName", "New User");
			rootView = inflater.inflate(R.layout.groups_list, container, false);
			TextView userNameValue = (TextView)rootView.findViewById(R.id.welcomeListGroupsLabel);
			userNameValue.setText(userName + ", View all the groups here!");
			
			adapter = new GroupListAdapter(activity);
			gridView = (GridView) rootView.findViewById(R.id.groupList);
			gridView.setOnItemClickListener(this);

			phone = prefs.getString("phone", "");

			String searchQuery = "/fetchExistingGroups?phone="
					+ phone;
			
			WebServiceClient restClient = new WebServiceClient(activity);
			restClient.execute(new String[] { searchQuery });
		} else {
			Intent intent = new Intent(activity, RetryActivity.class);
			startActivity(intent);
		}
        return rootView;
	}
	
	/** Called when the user clicks the Create group button */
	public void joinGroups(View view) {
		Button button = (Button) activity.findViewById(R.id.joinGroupBtn);
		button.setTextColor(getResources().getColor(R.color.click_button_1));
		Intent intent = new Intent(activity, JoinGroupActivity.class);
		
		startActivity(intent);
	}
	
	/** Called when the user clicks the Create group button */
	public void createGroups(View view) {
		Button button = (Button) activity.findViewById(R.id.createGroupBtn);
		button.setTextColor(getResources().getColor(R.color.click_button_2));
		Intent intent = new Intent(activity, CreateGroupActivity.class);
		
		startActivity(intent);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		SharedPreferences prefs = activity.getSharedPreferences(
				"Prefs", Activity.MODE_PRIVATE);
		String selectedGroup = "";
		String selectedGroupIndex = "";
		if(groupsList != null && !groupsList.isEmpty()){
			Map<String,Group> selectedMap = groupsList.get(position);
			for(Entry<String,Group> entry: selectedMap.entrySet()){
				
				SharedPreferences.Editor editor = prefs.edit();
				selectedGroup = entry.getValue().getName();
				selectedGroupIndex = entry.getKey();
				editor.putString("selectedGroup",selectedGroup);
				editor.putString("selectedGroupIndex",selectedGroupIndex);
				editor.apply();
				break;
			}
			
			for(Group group: allGroups){
				
				if(selectedGroup.equals(group.getName())){
					if(phone.equals(group.getAdmin()) && group.getPendingMembers() != null && group.getPendingMembers().size() > 0){
						Intent intent = new Intent(activity, GroupAdminListActivity.class);
						startActivity(intent);
					} else {
						Intent intent = new Intent(activity, ViewGroupNewPlanHistoryFragmentActivity.class);
						startActivity(intent);
					}
				}
			}
		}
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Inflate the menu; this adds items to the action bar if it is present.
		activity.getMenuInflater().inflate(R.menu.main, menu);
		MenuItem viewProfileItem = menu.findItem(R.id.viewProfile);
		viewProfileItem.setVisible(true);
		
		MenuItem changeProfilePicItem = menu.findItem(R.id.changeProfilePic);
		changeProfilePicItem.setVisible(true);
		
		MenuItem joinGroupItem = menu.findItem(R.id.joinGroup);
		joinGroupItem.setVisible(true);
		
		MenuItem deactivateAccountItem = menu.findItem(R.id.deactivateAccount);
		deactivateAccountItem.setVisible(true);	
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case (R.id.viewProfile):
			Intent viewProfileIntent = new Intent(activity, ViewProfileActivity.class);
            startActivity(viewProfileIntent);
			return true;
		case (R.id.changeProfilePic):
			Intent changeProfilePicIntent = new Intent(activity, ProfileImageUploadActivity.class);
            startActivity(changeProfilePicIntent);
			return true;
		case (R.id.joinGroup):
			Intent joinGroupIntent = new Intent(activity, JoinGroupActivity.class);
            startActivity(joinGroupIntent);
			return true;
		case (R.id.deactivateAccount):
			Intent deactivateAccountIntent = new Intent(activity, DeactivateAccountActivity.class);
            startActivity(deactivateAccountIntent);
			return true;
		case (R.id.aboutUs):
			Intent aboutUsIntent = new Intent(activity, AboutUsActivity.class);
            startActivity(aboutUsIntent);
			return true;
		default:
			return false;
		}
	}
	
	private class WebServiceClient extends AsyncTask<String, Integer, String> {

		private Context mContext;
		private ProgressDialog pDlg;
		private String query;

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
			query = params[0];
			String path = WTPConstants.SERVICE_PATH+query;
			
			
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
					groupsList = new ArrayList<Map<String, Group>>();
					allGroups = groupList.getGroups();
					if(allGroups != null && !allGroups.isEmpty()){
						for(Group group: allGroups){
							Map<String, Group> groupDetails = new HashMap<String, Group>();
							groupDetails.put(String.valueOf(group.getId()), group);
							groupsList.add(groupDetails);
						}
						if(!groupsList.isEmpty()){
							adapter.setData(groupsList);
							gridView.setAdapter(adapter);
						}
					}					
				} else {
					gridView.setVisibility(ListView.INVISIBLE);
					TextView userNameValue = (TextView)rootView.findViewById(R.id.welcomeListGroupsLabel);
					userNameValue.setText("You haven't joined any groups");
					activity.setContentView(R.layout.groups_list);
					
				}
			}
			pDlg.dismiss();
		}	
			
	}
		
	
	
	public void onBackPressed() {
	    Intent intent = new Intent(activity, MainActivity.class);
	    startActivity(intent);
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
	
	public void setActivity(Activity activity) {
		this.activity = activity;
	}
}
