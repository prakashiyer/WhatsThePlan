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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import com.theiyer.whatstheplan.entity.Group;
import com.theiyer.whatstheplan.entity.Plan;
import com.theiyer.whatstheplan.entity.User;
import com.theiyer.whatstheplan.util.WTPConstants;
import com.thoughtworks.xstream.XStream;

public class ViewPlanMembersActivity extends Activity {

	ListView memberListView;
	MemberListAdapter adapter;
	List<Map<String, byte[]>> membersList;
	TextView memberListLabel;
	String viewSelected ="";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(haveInternet(this)){
			setContentView(R.layout.plan_member_list);
			ActionBar aBar = getActionBar();
			Resources res = getResources();
			Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
			aBar.setBackgroundDrawable(actionBckGrnd);
			aBar.setTitle(" Plan Members");

			SharedPreferences prefs = getSharedPreferences("Prefs",
					Activity.MODE_PRIVATE);
			membersList = new ArrayList<Map<String, byte[]>>();
			memberListView = (ListView) findViewById(R.id.viewplanmemberList);
			adapter = new MemberListAdapter(this);
			memberListLabel = (TextView) findViewById(R.id.viewPlanMemberListLabel);

			String selectedPlan = prefs.getString("selectedPlan", "New User");
			String selectedPlanIndex = prefs.getString("selectedPlanIndex", "");
			System.out.println("View Selected: "  +selectedPlan);
			String searchQuery = "/fetchPlan?planName="
					+ selectedPlan.replace(" ", "%20")+"&planIndex="+selectedPlanIndex;
			viewSelected = prefs.getString("viewSelected", "members");
			System.out.println("View Selected: "  +viewSelected);

			WebServiceClient restClient = new WebServiceClient(this);
			restClient.execute(new String[] { searchQuery });
		} else {
			Intent intent = new Intent(this, RetryActivity.class);
			startActivity(intent);
		}
		
	}
	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		MenuItem viewProfileItem = menu.findItem(R.id.viewProfile);
		viewProfileItem.setVisible(true);
		
		MenuItem changeProfilePicItem = menu.findItem(R.id.changeProfilePic);
		changeProfilePicItem.setVisible(true);
		
		MenuItem deactivateAccountItem = menu.findItem(R.id.deactivateAccount);
		deactivateAccountItem.setVisible(true);		
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case (R.id.viewProfile):
			Intent viewProfileIntent = new Intent(this, ViewProfileActivity.class);
            startActivity(viewProfileIntent);
			return true;
		case (R.id.changeProfilePic):
			Intent changeProfilePicIntent = new Intent(this, ProfileImageUploadActivity.class);
            startActivity(changeProfilePicIntent);
			return true;
		case (R.id.deactivateAccount):
			Intent deactivateAccountIntent = new Intent(this, DeactivateAccountActivity.class);
            startActivity(deactivateAccountIntent);
			return true;
		case (R.id.aboutUs):
			Intent aboutUsIntent = new Intent(this, AboutUsActivity.class);
            startActivity(aboutUsIntent);
			return true;
		default:
			return false;
		}
	}
	
	@Override
	public void onBackPressed() {
	    Intent intent = new Intent(this, ViewMyNewPlansActivity.class);
	    startActivity(intent);
	}
	
	public class WebServiceClient extends AsyncTask<String, Integer, String> {

		private Context mContext;
		private ProgressDialog pDlg;
		private String query;
		private String isLastMember = "false";
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

			if(query.contains("fetchUser") || query.contains("fetchGroup")){
				isLastMember = params[1];
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
			if (response != null && query.contains("fetchPlan")) {
				System.out.println("PLAN RESPONSE: " +response);
				XStream xstream = new XStream();
				xstream.alias("Plan", Plan.class);
				xstream.alias("memberNames", String.class);
				xstream.addImplicitCollection(Plan.class, "memberNames", "memberNames", String.class);
				xstream.alias("membersInvited", String.class);
				xstream.addImplicitCollection(Plan.class, "membersInvited", "membersInvited", String.class);
				xstream.alias("groupsInvited", String.class);
				xstream.addImplicitCollection(Plan.class, "groupsInvited", "groupsInvited", String.class);
				Plan plan = (Plan) xstream.fromXML(response);
				if (plan != null) {

//					System.out.println("Plan selected: " +plan.getName());
//					List<String> members = new ArrayList<String>();
//					if(viewSelected.equals("members")){
//						members = plan.getMemberNames();
//					} else if(viewSelected.equals("groupsInvited")){
//						members = plan.getGroupsInvited();
//					} else if(viewSelected.equals("membersInvited")){
//						members = plan.getMembersInvited();
//					}

					/*if (members != null && !members.isEmpty()) {
						String isLastMember = "false";
						for(int i=0; i<members.size(); i++){
							String userQuery = "";
							if(viewSelected.equals("groupsInvited")){
								userQuery = "/fetchGroup?groupIndex=" + members.get(i);
							} else {
								userQuery = "/fetchUser?phone=" + members.get(i);
							}
							
							if(i == members.size()-1){
								isLastMember = "true";
							}
							System.out.println("USER QUERY: " +userQuery);
							WebServiceClient userRestClient = new WebServiceClient(mContext);
							userRestClient.execute(new String[] { userQuery, isLastMember });
						}
					}*/

				}
			}
			
			if(response != null && query.contains("fetchUser")){
				System.out.println("USER RESPONSE: " +response);
				XStream userXstream = new XStream();
				userXstream.alias("UserInformation", User.class);
				userXstream.alias("groupNames", String.class);
				userXstream.addImplicitCollection(User.class, "groupNames","groupNames",String.class);
				userXstream.alias("pendingGroupNames", String.class);
				userXstream.addImplicitCollection(User.class, "pendingGroupNames","pendingGroupNames",String.class);
				userXstream.alias("groupIds", String.class);
				userXstream.addImplicitCollection(User.class, "groupIds","groupIds",String.class);
				userXstream.alias("pendingGroupIds", String.class);
				userXstream.addImplicitCollection(User.class, "pendingGroupIds","pendingGroupIds",String.class);
				User user = (User) userXstream
						.fromXML(response);
				if(user != null){
					Map<String, byte[]> memberMap = new HashMap<String, byte[]>();
					memberMap.put(user.getName(), user.getImage());
					membersList.add(memberMap);	
					
					if(!membersList.isEmpty() && isLastMember.equals("true")){
						
					    adapter.setData(membersList);
						memberListView.setAdapter(adapter);
						memberListLabel.setVisibility(TextView.VISIBLE);
						memberListView.setVisibility(ListView.VISIBLE);
					}
				}	
			} else if (response != null && query.contains("fetchGroup")) {
				System.out.println("GRP RESPONSE: " +response);
				XStream xstream = new XStream();
				xstream.alias("Group", Group.class);
				
				xstream.alias("members", String.class);
				xstream.addImplicitCollection(Group.class, "members","members",String.class);
				xstream.alias("planNames", String.class);
				xstream.addImplicitCollection(Group.class, "planNames","planNames",String.class);
				xstream.alias("pendingMembers", String.class);
				xstream.addImplicitCollection(Group.class, "pendingMembers","pendingMembers",String.class);
				xstream.alias("planIds", String.class);
				xstream.addImplicitCollection(Group.class, "planIds","planIds",String.class);
				Group group = (Group) xstream.fromXML(response);
				
				if (group != null) {
					String groupName = group.getName();
						Map<String, byte[]> memberMap = new HashMap<String, byte[]>();
						memberMap.put(groupName, group.getImage());
						membersList.add(memberMap);	
						
						if(!membersList.isEmpty() && isLastMember.equals("true")){
							
						    adapter.setData(membersList);
							memberListView.setAdapter(adapter);
							memberListLabel.setVisibility(TextView.VISIBLE);
							memberListView.setVisibility(ListView.VISIBLE);
						}					
				}
			}
			pDlg.dismiss();
		}

	}
	
	public class WebImageRetrieveRestWebServiceClient extends AsyncTask<String, Integer, byte[]> {

		private Context mContext;
		private ProgressDialog pDlg;
		private String userName;
		private String isLastMember;

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
			String path = WTPConstants.SERVICE_PATH+"/"+method;
			userName = params[2];
        	isLastMember = params[3];

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
			
			
			if(response != null){
				Map<String, byte[]> memberMap = new HashMap<String, byte[]>();
				memberMap.put(userName, response);
				membersList.add(memberMap);	
				
				if(!membersList.isEmpty() && isLastMember.equals("true")){
					
				    adapter.setData(membersList);
					memberListView.setAdapter(adapter);
					memberListLabel.setVisibility(TextView.VISIBLE);
					memberListView.setVisibility(ListView.VISIBLE);
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
