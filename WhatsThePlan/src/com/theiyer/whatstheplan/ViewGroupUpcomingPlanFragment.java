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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.theiyer.whatstheplan.entity.Group;
import com.theiyer.whatstheplan.entity.Plan;
import com.theiyer.whatstheplan.entity.PlanList;
import com.theiyer.whatstheplan.entity.User;
import com.theiyer.whatstheplan.util.WTPConstants;
import com.thoughtworks.xstream.XStream;

public class ViewGroupUpcomingPlanFragment extends Fragment implements OnItemClickListener {

	private static final String TAG = "ViewGroupMembersActivity";
	
	private ListView memberListView;
	private boolean isLastMember = false;
	private Activity activity;
	PlanListAdapter adapter;
	ListView planListView;
	List<Map<String, Plan>> plansResult;
	View rootView;
	String selectedGroup;
	private String phone;
	private boolean isAdmin;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity = this.getActivity();
		
		if(haveInternet(activity)){
			rootView = inflater.inflate(R.layout.group_upcoming_plans, container, false);
			ActionBar aBar = activity.getActionBar();
			Resources res = getResources();
			Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
			aBar.setBackgroundDrawable(actionBckGrnd);
			aBar.setTitle(" Group Members");

			SharedPreferences prefs = activity.getSharedPreferences("Prefs",
					Activity.MODE_PRIVATE);
			
			
			selectedGroup = prefs.getString("selectedGroup", "");
			phone = prefs.getString("phone", "");
			String searchQuery = "/fetchGroupPlans?groupName=" + selectedGroup.replace(" ", "%20");

			
			adapter = new PlanListAdapter(activity);
			planListView = (ListView) rootView.findViewById(R.id.viewGroupUpcomingplansList);
			planListView.setOnItemClickListener(this);
			
			WebServiceClient restClient = new WebServiceClient(activity);
			restClient.execute(new String[] { searchQuery });
		} else {
			Intent intent = new Intent(activity, RetryActivity.class);
			startActivity(intent);
		}
		return rootView;
	}
	
	private class WebServiceClient extends AsyncTask<String, Integer, String> {

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

			if (response != null && response.contains("<Group>")) {
				Log.i(TAG, response);
				XStream xstream = new XStream();
				xstream.alias("Group", Group.class);

				xstream.alias("members", String.class);
				xstream.addImplicitCollection(Group.class, "members",
						"members", String.class);
				xstream.alias("planNames", String.class);
				xstream.addImplicitCollection(Group.class, "planNames",
						"planNames", String.class);
				xstream.alias("pendingMembers", String.class);
				xstream.addImplicitCollection(Group.class, "pendingMembers",
						"pendingMembers", String.class);
				Group group = (Group) xstream.fromXML(response);
				if (group != null && selectedGroup.equals(group.getName())) {
					if (phone.equals(group.getAdmin())) {
						isAdmin = true;
					} else {
						isAdmin = false;
					}

				}
			}

			if (response != null && response.contains("<PlanList>")) {
				Log.i(TAG, response);
				XStream xstream = new XStream();
				xstream.alias("PlanList", PlanList.class);
				xstream.alias("plans", Plan.class);
				xstream.addImplicitCollection(PlanList.class, "plans");
				xstream.alias("memberNames", String.class);
				xstream.addImplicitCollection(Plan.class, "memberNames");
				xstream.alias("membersInvited", String.class);
				xstream.addImplicitCollection(Plan.class, "membersInvited");
				xstream.alias("groupsInvited", String.class);
				xstream.addImplicitCollection(Plan.class, "groupsInvited");
				PlanList planList = (PlanList) xstream.fromXML(response);
				if (planList != null && planList.getPlans() != null) {

					List<Plan> plans = planList.getPlans();

					if (plans != null && !plans.isEmpty()) {
						plansResult = new ArrayList<Map<String, Plan>>();
						for (Plan plan : plans) {
							Map<String, Plan> planMap = new HashMap<String, Plan>();
							planMap.put(plan.getName(), plan);
							plansResult.add(planMap);

						}

						if (!plansResult.isEmpty()) {
							planListView.setVisibility(ListView.VISIBLE);
							adapter.setData(plansResult);
							planListView.setAdapter(adapter);
							// Click event for single list row
						}
					}  else {
						planListView.setVisibility(ListView.INVISIBLE);
						TextView planLabel = (TextView) rootView.findViewById(R.id.upcomingGroupPlanListLabel);
						planLabel.setText("No upcoming plans for you.");
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
				Log.i(TAG, response.toString());
				Map<String, byte[]> memberMap = new HashMap<String, byte[]>();
				memberMap.put(userName, response);
/*//				membersList.add(memberMap);	
				if(!membersList.isEmpty() && isLastMember){
					adapter.setData(membersList);
					memberListView.setAdapter(adapter);
				}*/
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

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		SharedPreferences prefs = activity.getSharedPreferences(
				"Prefs", Activity.MODE_PRIVATE);
		String selectedPlan = "";
		if(plansResult != null && !plansResult.isEmpty()){
			Map<String,Plan> selectedMap = plansResult.get(position);
			for(Entry<String,Plan> entry: selectedMap.entrySet()){
				
				SharedPreferences.Editor editor = prefs.edit();
				selectedPlan = entry.getKey();
				System.out.println("Selected Plan: " +selectedPlan);
				editor.putString("selectedPlan",selectedPlan);
				editor.apply();
				break;
			}
			Intent intent = new Intent(activity, ViewMyNewPlansActivity.class);
			startActivity(intent);		
		}
		
	}

}
