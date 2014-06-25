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
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;

import com.theiyer.whatstheplan.entity.Plan;
import com.theiyer.whatstheplan.entity.PlanList;
import com.theiyer.whatstheplan.util.WTPConstants;
import com.thoughtworks.xstream.XStream;

public class HomePlanGroupFragmentActivity extends FragmentActivity implements ActionBar.TabListener {

	PlanListAdapter adapter;
	List<Map<String, String>> plansResult;
	Activity activity;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		activity = this;
		if(haveInternet(this)){
			
			//super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_main);
			 
	        // Set up the action bar.
	        final ActionBar actionBar = getActionBar();
	        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	   
	        // For each of the sections in the app, add a tab to the action bar.
	        actionBar.addTab(actionBar.newTab().setText(R.string.home_plan_label)
	        		.setIcon(R.drawable.ic_plan).setTabListener(this));
	        actionBar.addTab(actionBar.newTab().setText(R.string.groups_list_label)
	        		.setIcon(R.drawable.ic_groupicon).setTabListener(this));
	        
	        if(savedInstanceState != null) {
		        int index = savedInstanceState.getInt("index");
		        getActionBar().setSelectedNavigationItem(index);
		    }
	        
	    } else {
			Intent intent = new Intent(this, RetryActivity.class);
			startActivity(intent);
		}
		
	}

	/** Called when the user clicks the Create group button *//*
	public void createGroups(View view) {
		Button button = (Button) findViewById(R.id.createGroupBtn);
		button.setTextColor(getResources().getColor(R.color.click_button_2));
		Intent intent = new Intent(this, CreateGroupActivity.class);
		
		startActivity(intent);
	}*/
	
	/** Called when the user clicks the view group button *//*
	public void viewGroups(View view) {
		
		Button button = (Button) findViewById(R.id.viewGroupBtn);
		button.setTextColor(getResources().getColor(R.color.click_button_1));
		
		Intent intent = new Intent(this, GroupsListActivity.class);
		startActivity(intent);
	}*/
	
	/*@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		SharedPreferences prefs = getSharedPreferences(
				"Prefs", Activity.MODE_PRIVATE);
		String selectedPlan = "";
		if(plansResult != null && !plansResult.isEmpty()){
			Map<String,String> selectedMap = plansResult.get(position);
			for(Entry<String,String> entry: selectedMap.entrySet()){
				
				SharedPreferences.Editor editor = prefs.edit();
				selectedPlan = entry.getKey();
				editor.putString("selectedPlan",selectedPlan);
				editor.apply();
				break;
			}
			Intent intent = new Intent(this, ViewMyPlansActivity.class);
			startActivity(intent);		
		}
	}*/
	
	/* @Override
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
			case (R.id.viewUserList):
				Intent viewUsers = new Intent(this, ViewExistingMembersActivity.class);
	            startActivity(viewUsers);
				return true;
			default:
				return false;
			}
		}*/
		
		/*private class WebServiceClient extends AsyncTask<String, Integer, String> {

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
				super.onPostExecute(response);
				if(pDlg.isShowing()) {
					pDlg.dismiss();
				}
				if (response != null && response.contains("PlanList")) {
					XStream xstream = new XStream();
					xstream.alias("PlanList", PlanList.class);
					xstream.alias("plans", Plan.class);
					xstream.addImplicitCollection(PlanList.class, "plans");
					xstream.alias("memberNames", String.class);
					xstream.addImplicitCollection(Plan.class, "memberNames");
					PlanList planList = (PlanList) xstream.fromXML(response);
					if (planList != null && planList.getPlans() != null) {

						List<Plan> plans = planList.getPlans();

						if (plans != null && !plans.isEmpty()) {
						    plansResult = new ArrayList<Map<String, String>>();
							for (Plan plan : plans) {
								Map<String, String> planMap = new HashMap<String, String>();
								planMap.put(plan.getName(), plan.getStartTime());
								plansResult.add(planMap);

							}
						}
					}
				}
			}

		}*/

/*		@Override
		public void onBackPressed() {
		    Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
		}*/
		
		/**
		 * Checks if we have a valid Internet Connection on the device.
		 * @param ctx
		 * @return True if device has internet
		 *
		 * Code from: http://www.androidsnippets.org/snippets/131/
		 */
		public static boolean haveInternet(Context ctx) {

		    NetworkInfo info = (NetworkInfo) ((ConnectivityManager) ctx
		            .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

		    if (info == null || !info.isConnected()) {
		        return false;
		    }
		    
		    return true;
		}

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
			if (tab.getPosition() == 0) {
		    	 HomePlanFragment homePlanFragment = new HomePlanFragment();
		    	 homePlanFragment.setActivity(activity);
			     getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, homePlanFragment).commit();
			 } 
			 else if (tab.getPosition() == 1) {
			   	GroupsListFragment goupsList = new GroupsListFragment();
			   	goupsList.setActivity(activity);
			   	getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, goupsList).commit();
			 }
		}

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
		     if (tab.getPosition() == 0) {
		    	 HomePlanFragment homePlanFragment = new HomePlanFragment();
		    	 homePlanFragment.setActivity(activity);
			     getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, homePlanFragment).commit();
			 } 
			 else if (tab.getPosition() == 1) {
			   	GroupsListFragment goupsList = new GroupsListFragment();
			   	goupsList.setActivity(activity);
			   	getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, goupsList).commit();
			 }
		}



		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			if (tab.getPosition() == 0) {
		    	 HomePlanFragment homePlanFragment = new HomePlanFragment();
		    	 homePlanFragment.setActivity(activity);
			     getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, homePlanFragment).commit();
			 } 
			 else if (tab.getPosition() == 1) {
			   	GroupsListFragment goupsList = new GroupsListFragment();
			   	goupsList.setActivity(activity);
			   	getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, goupsList).commit();
			 }
		}
		
		@Override
		protected void onSaveInstanceState(Bundle outState) {
		    super.onSaveInstanceState(outState);
		    int i = getActionBar().getSelectedNavigationIndex();
		    outState.putInt("index", i);
		}
		
		
}
