package com.theiyer.whatstheplan;

import java.util.List;
import java.util.Map;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class ViewGroupNewPlanHistoryFragmentActivity extends FragmentActivity implements ActionBar.TabListener {

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
	        actionBar.addTab(actionBar.newTab().setText(R.string.group_member)
	        		.setTabListener(this));
	        actionBar.addTab(actionBar.newTab().setText(R.string.settle_bill)
	        		.setTabListener(this));
	        actionBar.addTab(actionBar.newTab().setText(R.string.group_upcomming_plan)
	        		.setTabListener(this));
	        
	        if(savedInstanceState != null) {
		        int index = savedInstanceState.getInt("index");
		        getActionBar().setSelectedNavigationItem(index);
		    }
	        
	    } else {
			Intent intent = new Intent(this, RetryActivity.class);
			startActivity(intent);
		}
		
	}
	
	/** Called when the user clicks the create plan button */
	public void newPlan(View view) {
		
		Button button = (Button) activity.findViewById(R.id.createPlanBtn);
		button.setTextColor(getResources().getColor(R.color.click_button_2));
		
		Intent intent = new Intent(activity, ViewExistingMembersActivity.class);
		startActivity(intent);
	}
	
	/** Called when the user clicks the join group button */
	public void joinGroups(View view) {
		Button button = (Button) activity.findViewById(R.id.joinGroupBtn);
		button.setTextColor(getResources().getColor(R.color.click_button_1));
		Intent intent = new Intent(activity, JoinGroupActivity.class);
		
		startActivity(intent);
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
		    Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
		}
		
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
				ViewGroupMembersFragment viewGroupMembersFragment = new ViewGroupMembersFragment();
			    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, viewGroupMembersFragment).commit();
			 } 
			 else if (tab.getPosition() == 1) {
			   	PlanHistoryFragment planHistoryFragment = new PlanHistoryFragment();
			   	getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, planHistoryFragment).commit();
			 }
			 else if (tab.getPosition() == 2) {
			   	ViewGroupUpcomingPlanFragment viewGroupUpcomingPlanFragment = new ViewGroupUpcomingPlanFragment();
			   	getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, viewGroupUpcomingPlanFragment).commit();
			}
		}

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			if (tab.getPosition() == 0) {
				ViewGroupMembersFragment viewGroupMembersFragment = new ViewGroupMembersFragment();
			    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, viewGroupMembersFragment).commit();
			 } 
			 else if (tab.getPosition() == 1) {
			   	PlanHistoryFragment planHistoryFragment = new PlanHistoryFragment();
			   	getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, planHistoryFragment).commit();
			 }
			 else if (tab.getPosition() == 2) {
			   	ViewGroupUpcomingPlanFragment viewGroupUpcomingPlanFragment = new ViewGroupUpcomingPlanFragment();
			   	getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, viewGroupUpcomingPlanFragment).commit();
			}
		}



		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		}
		
		@Override
		protected void onSaveInstanceState(Bundle outState) {
		    super.onSaveInstanceState(outState);
		    int i = getActionBar().getSelectedNavigationIndex();
		    outState.putInt("index", i);
		}
		
		
}
