package com.theiyer.whatstheplan;

import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.theiyer.whatstheplan.entity.Center;
import com.theiyer.whatstheplan.entity.User;
import com.theiyer.whatstheplan.util.WTPConstants;
import com.thoughtworks.xstream.XStream;

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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class HomePlanGroupFragmentActivity extends FragmentActivity implements ActionBar.TabListener {

	PlanListAdapter adapter;
	List<Map<String, String>> plansResult;
	Activity activity;
	String centerFlag;
	String docFlag;
	private static final String TAG = "Health Meet/HomePlanFragment";
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
	        
	        SharedPreferences prefs = activity.getSharedPreferences("Prefs",
					Activity.MODE_PRIVATE);
			centerFlag = prefs.getString("centerFlag", "");
			docFlag = prefs.getString("docFlag", "");
			String phone = prefs.getString("phone", "");
	   
	        // For each of the sections in the app, add a tab to the action bar.
	        actionBar.addTab(actionBar.newTab().setText(R.string.home_plan_label)
	        		.setIcon(R.drawable.ic_plan).setTabListener(this));
	        
	        if(!"Y".equals(centerFlag) && !"Y".equals(docFlag)){
	        	 actionBar.addTab(actionBar.newTab().setText(R.string.groups_list_label)
	 	        		.setIcon(R.drawable.ic_groupicon).setTabListener(this));
	 	        actionBar.addTab(actionBar.newTab().setText(R.string.emergency_label)
	 	        		.setIcon(R.drawable.ic_emergency).setTabListener(this));
	 	       actionBar.addTab(actionBar.newTab().setText(R.string.prescription_label)
	 	        		.setIcon(R.drawable.ic_prescription).setTabListener(this));
	        }
	        if("Y".equals(centerFlag)){
	        	 actionBar.addTab(actionBar.newTab().setText(R.string.member_list_group_text)
	 	        		.setIcon(R.drawable.ic_groupicon).setTabListener(this));
	        }
	       
	       
	        
	        if(savedInstanceState != null) {
		        int index = savedInstanceState.getInt("index");
		        getActionBar().setSelectedNavigationItem(index);
		    }
	        
	        
	        
	        
	    } else {
			Intent intent = new Intent(this, RetryActivity.class);
			startActivity(intent);
		}
		
	}
	
	public void callDoctor(View view) {

		Button button = (Button) activity.findViewById(R.id.call_doc_button);
		button.setTextColor(getResources().getColor(R.color.click_button_2));
		Intent intent = new Intent(Intent.ACTION_DIAL);
		SharedPreferences prefs = activity.getSharedPreferences("Prefs",
				Activity.MODE_PRIVATE);
		String doctorPhone = prefs.getString("doctorPhone", "456");
		System.out.println("emergency.doctorPhone :*** " + doctorPhone);
		intent.setData(Uri.parse("tel:"+ doctorPhone));
		button.setTextColor(getResources().getColor(R.color.button_text));
		startActivity(intent);
	}
	public void callAmbu(View view) {

		Button button = (Button) activity.findViewById(R.id.call_doc_button);
		button.setTextColor(getResources().getColor(R.color.click_button_2));
		Intent intent = new Intent(Intent.ACTION_DIAL);
		intent.setData(Uri.parse("tel:102"));
		button.setTextColor(getResources().getColor(R.color.button_text));
		startActivity(intent);
	}
	public void callPolice(View view) {

		Button button = (Button) activity.findViewById(R.id.call_police);
		button.setTextColor(getResources().getColor(R.color.click_button_2));
		Intent intent = new Intent(Intent.ACTION_DIAL);
		intent.setData(Uri.parse("tel:100"));
		button.setTextColor(getResources().getColor(R.color.button_text));
		startActivity(intent);
	}
	
	public void callHealth(View view) {

		Button button = (Button) activity.findViewById(R.id.call_doc_button);
		button.setTextColor(getResources().getColor(R.color.click_button_2));
		Intent intent = new Intent(Intent.ACTION_DIAL);
		SharedPreferences prefs = activity.getSharedPreferences("Prefs",
				Activity.MODE_PRIVATE);
		String centerPhone = prefs.getString("centerPhone", "123");
		System.out.println("emergency.centerPhone :*** " + centerPhone);
		intent.setData(Uri.parse("tel:"+ centerPhone));
		button.setTextColor(getResources().getColor(R.color.button_text));
		startActivity(intent);
	}
	
	/** Called when the user clicks the create plan button */
	public void newPlan(View view) {
		
		Button button = (Button) activity.findViewById(R.id.createPlanBtn);
		button.setTextColor(getResources().getColor(R.color.click_button_2));
		if("Y".equals(centerFlag)){
			Intent intent = new Intent(activity, AppointmentActivity.class);
			startActivity(intent);
		} else {
			Intent intent = new Intent(activity, ViewExistingDoctorsActivity.class);
			startActivity(intent);
		}
	}
	
	/** Called when the user clicks the Add Prescription button */
	public void addPrescription(View view) {

		Button button = (Button) activity.findViewById(R.id.addPrescription);
		button.setTextColor(getResources().getColor(R.color.click_button_1));
		
		EditText titleText = (EditText) activity.findViewById(R.id.medTitleValue);
		String title = titleText.getText().toString();
		
		EditText medicineText = (EditText) activity.findViewById(R.id.medValue);
		String medicine = medicineText.getText().toString();
		
		EditText daysText = (EditText) activity.findViewById(R.id.numberOfDaysPrescription);
		String days = daysText.getText().toString();

		TextView planDateEditText = (TextView) activity.findViewById(R.id.medDateValue);
		String planDate = planDateEditText.getText().toString();

		CheckBox morningCheck = (CheckBox) activity.findViewById(R.id.morningId);
		if(morningCheck.isChecked()){
			CalendarHelper calendarHelper = new CalendarHelper(activity);
			calendarHelper.execute(new String[] { planDate+" 09:00",
					title, medicine, "prescription", days,""});	
		}
		
		CheckBox afternoonCheck = (CheckBox) activity.findViewById(R.id.afternoonId);
		if(afternoonCheck.isChecked()){
			CalendarHelper calendarHelper = new CalendarHelper(activity);
			calendarHelper.execute(new String[] { planDate+" 13:00",
					title, medicine, "prescription", days,""});	
		}
		
		CheckBox eveningCheck = (CheckBox) activity.findViewById(R.id.eveningId);
		if(eveningCheck.isChecked()){
			CalendarHelper calendarHelper = new CalendarHelper(activity);
			calendarHelper.execute(new String[] { planDate+" 17:00",
					title, medicine, "prescription", days,""});	
		}
		
		CheckBox nightCheck = (CheckBox) activity.findViewById(R.id.nightId);
		if(nightCheck.isChecked()){
			CalendarHelper calendarHelper = new CalendarHelper(activity);
			calendarHelper.execute(new String[] { planDate+" 21:00",
					title, medicine, "prescription", days,""});	
		}
		
		Toast.makeText(activity.getApplicationContext(), "The medicine prescription has been added to your Google calendar.",
				Toast.LENGTH_LONG).show();
		
		button.setTextColor(getResources().getColor(R.color.button_text));
		
		AddPrescriptionActivity callTab = new AddPrescriptionActivity();
	   	callTab.setActivity(activity);
	   	getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, callTab).commit();
		
			
	}
	
	public void setMedDate(View v) {
		Button button = (Button) activity.findViewById(R.id.medDateButton);
		button.setTextColor(getResources().getColor(R.color.click_button_2));
		DialogFragment newFragment = new DateNewPickerFragment("prescription");
		newFragment.show(getSupportFragmentManager(), "datePicker");
		button.setTextColor(getResources().getColor(R.color.button_text));
		
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
			MenuItem viewProfileItem = menu.findItem(R.id.editProfile);
			viewProfileItem.setVisible(true);
			
			MenuItem addDoctorItem = menu.findItem(R.id.addDoctor);
			addDoctorItem.setVisible(true);
			
			MenuItem addCenterItem = menu.findItem(R.id.addCenter);
			addCenterItem.setVisible(true);
			
			MenuItem editProfileItem = menu.findItem(R.id.viewProfile);
			editProfileItem.setVisible(true);
			
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
			case (R.id.addDoctor):
				SharedPreferences prefs = getSharedPreferences("Prefs",
						Activity.MODE_PRIVATE);
				SharedPreferences.Editor editor = prefs.edit();
				editor.putString("newUser", "N");
				editor.apply();
				Intent addDoctorIntent = new Intent(this, AddDoctorActivity.class);
	            startActivity(addDoctorIntent);
				return true;
			case (R.id.editProfile):
				if("Y".equals(centerFlag)){
					Intent editProfileIntent = new Intent(this, EditCenterProfileActivity.class);
		            startActivity(editProfileIntent);
				} else {
					Intent editProfileIntent = new Intent(this, EditProfileActivity.class);
		            startActivity(editProfileIntent);
				}
				
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
			/*if (tab.getPosition() == 0) {
		    	 HomePlanFragment homePlanFragment = new HomePlanFragment();
		    	 homePlanFragment.setActivity(activity);
			     getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, homePlanFragment).commit();
			 } 
			 else if (tab.getPosition() == 1) {
				 if(!"Y".equals(centerFlag) && !"Y".equals(docFlag)){
					 GroupsListFragment goupsList = new GroupsListFragment();
					 goupsList.setActivity(activity);
					 getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, goupsList).commit();
				 }
				 if("Y".equals(centerFlag)){
					 ViewGroupMembersFragment members = new ViewGroupMembersFragment();
					 members.setActivity(activity);
					 getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, members).commit();
				 }
			   	
			   	
			 }
			 else if (tab.getPosition() == 2) {
				 if(!"Y".equals(centerFlag)){
					 EmergencyCallTabFragment callTab = new EmergencyCallTabFragment();
					   	callTab.setActivity(activity);
					   	getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, callTab).commit();
				 } 
			 }
			 else if (tab.getPosition() == 3) {
				 if(!"Y".equals(centerFlag) && !"Y".equals(docFlag)){
					 AddPrescriptionActivity callTab = new AddPrescriptionActivity();
					   	callTab.setActivity(activity);
					   	getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, callTab).commit();
				 }
			 }*/
		}

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
		     if (tab.getPosition() == 0) {
		    	 HomePlanFragment homePlanFragment = new HomePlanFragment();
		    	 homePlanFragment.setActivity(activity);
			     getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, homePlanFragment).commit();
			 } 
			 else if (tab.getPosition() == 1) {
				 if(!"Y".equals(centerFlag) && !"Y".equals(docFlag)){
			   	GroupsListFragment goupsList = new GroupsListFragment();
			   	goupsList.setActivity(activity);
			   	getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, goupsList).commit();
				 }
				 if("Y".equals(centerFlag)){
					 ViewGroupMembersFragment members = new ViewGroupMembersFragment();
					 members.setActivity(activity);
					 getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, members).commit();
				 }
			 }
			 else if (tab.getPosition() == 2) {
				 if(!"Y".equals(centerFlag)){
				   	EmergencyCallTabFragment callTab = new EmergencyCallTabFragment();
				   	callTab.setActivity(activity);
				   	getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, callTab).commit();
				 } 
		     }
			 else if (tab.getPosition() == 3) {
				 if(!"Y".equals(centerFlag) && !"Y".equals(docFlag)){
					 AddPrescriptionActivity callTab = new AddPrescriptionActivity();
					   	callTab.setActivity(activity);
					   	getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, callTab).commit();
				 }
			 }
		}



		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			/*if (tab.getPosition() == 0) {
		    	 HomePlanFragment homePlanFragment = new HomePlanFragment();
		    	 homePlanFragment.setActivity(activity);
			     getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, homePlanFragment).commit();
			 } 
			 else if (tab.getPosition() == 1) {
				 if(!"Y".equals(centerFlag) && !"Y".equals(docFlag)){
			   	GroupsListFragment goupsList = new GroupsListFragment();
			   	goupsList.setActivity(activity);
			   	getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, goupsList).commit();
				 }
				 if("Y".equals(centerFlag)){
					 ViewGroupMembersFragment members = new ViewGroupMembersFragment();
					 members.setActivity(activity);
					 getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, members).commit();
				 }
			 }
			 else if (tab.getPosition() == 2) {
				 if(!"Y".equals(centerFlag)){
				   	EmergencyCallTabFragment callTab = new EmergencyCallTabFragment();
				   	callTab.setActivity(activity);
				   	getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, callTab).commit();
				 }
		     }
			 else if (tab.getPosition() == 3) {
				 if(!"Y".equals(centerFlag) && !"Y".equals(docFlag)){
					 AddPrescriptionActivity callTab = new AddPrescriptionActivity();
					   	callTab.setActivity(activity);
					   	getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, callTab).commit();
				 }
			 }*/
		}
		
		@Override
		protected void onSaveInstanceState(Bundle outState) {
		    super.onSaveInstanceState(outState);
		    int i = getActionBar().getSelectedNavigationIndex();
		    outState.putInt("index", i);
		}
		
		
}
