package com.theiyer.whatstheplan;

import java.util.concurrent.ExecutionException;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.theiyer.whatstheplan.entity.Plan;
import com.thoughtworks.xstream.XStream;

public class EditPlanActivity  extends FragmentActivity {

	private String oldName;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.edit_plan);
		ActionBar aBar = getActionBar();
		Resources res = getResources();
		Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
		aBar.setBackgroundDrawable(actionBckGrnd);
		aBar.setTitle(" Edit Plan");

		SharedPreferences prefs = getSharedPreferences("Prefs",
				Activity.MODE_PRIVATE);
		String userName = prefs.getString("userName", "New User");
		TextView userNameValue = (TextView) findViewById(R.id.welcomeEditPlanLabel);
		userNameValue.setText(userName + ", edit selected plan here!");
		
		String selectedPlan = prefs.getString("selectedPlan", "New User");
		TextView selectedPlanValue = (TextView) findViewById(R.id.editPlanTitleValue);
		selectedPlanValue.setText(selectedPlan);
		oldName = selectedPlan;

		TextView errorFieldValue = (TextView) findViewById(R.id.viewPlanErrorField);
		String searchQuery = "/fetchPlan?planName="
				+ selectedPlan.replace(" ", "%20");

		RestWebServiceClient restClient = new RestWebServiceClient(this);
		try {
			String response = restClient.execute(new String[] { searchQuery })
					.get();

			if (response != null) {
				XStream xstream = new XStream();
				xstream.alias("Plan", Plan.class);
				xstream.alias("memberNames", String.class);
				xstream.addImplicitCollection(Plan.class, "memberNames");
				Plan plan = (Plan) xstream.fromXML(response);
				if (plan != null && selectedPlan.equals(plan.getName())) {
					
					TextView planDateValue = (TextView) findViewById(R.id.newPlanDateValue);
					planDateValue
							.setText(plan.getStartTime().substring(0,10));
					
					TextView planTimeValue = (TextView) findViewById(R.id.newPlanTimeValue);
					String time = plan.getStartTime().substring(11,16);
					String hour = time.substring(0, 2);
	            	String min = time.substring(3);
	            	int hourInt = Integer.valueOf(hour);
	            	String ampm = "AM";
	            	if(hourInt > 12){
	            		hour = String.valueOf(hourInt - 12);
	            		if(Integer.valueOf(hour) < 10){
	            			hour = "0"+hour;
	            		}
	            		ampm = "PM";
	            	}
	            	
					planTimeValue
							.setText(hour+":"+min+" "+ampm);

					EditText planLocationValue = (EditText) findViewById(R.id.editPlanLocationValue);
					planLocationValue
							.setText(plan.getLocation());

				}
			}
		} catch (InterruptedException e) {
			
			errorFieldValue
					.setText("Apologies for any inconvenience caused. There is a problem with the service!");
		} catch (ExecutionException e) {
			
			errorFieldValue
					.setText("Apologies for any inconvenience caused. There is a problem with the service!");
		}
	}
	
	/** Called when the user clicks the Edit Plan button */
	public void editPlan(View view) {

		Button button = (Button) findViewById(R.id.editPlanButton);
		button.setTextColor(getResources().getColor(R.color.click_button_1));
		
		EditText planNameEditText = (EditText) findViewById(R.id.editPlanTitleValue);
		String planName = planNameEditText.getText().toString();

		SharedPreferences prefs = getSharedPreferences("Prefs",
				Activity.MODE_PRIVATE);

		TextView planDateEditText = (TextView) findViewById(R.id.newPlanDateValue);
		String planDate = planDateEditText.getText().toString();

		TextView planTimeEditText = (TextView) findViewById(R.id.newPlanTimeValue);
		String planTime = planTimeEditText.getText().toString();

		String hour = planTime.substring(0, 2);
    	String min = planTime.substring(3,5);
    	if(planTime.contains("PM")){
    		hour = String.valueOf((Integer.valueOf(hour) +12));
    	}
    	planTime = hour+":"+min;
    	
		EditText planLocationEditText = (EditText) findViewById(R.id.editPlanLocationValue);
		String planLocation = planLocationEditText.getText().toString();
		
		String selectedGroup = prefs.getString("selectedGroup", "");;


		String insertQuery = "/editPlan?newName=" + planName.replace(" ", "%20")
				+ "&oldName=" + oldName.replace(" ", "%20") 
				+ "&date=" + planDate + "&time="
				+ planTime+":00" + "&location=" + planLocation.replace(" ", "%20")
				+ "&groupName=" + selectedGroup.replace(" ", "%20");

		TextView errorFieldValue = (TextView) findViewById(R.id.editPlanErrorField);
		errorFieldValue.setText("");
		RestWebServiceClient restClient = new RestWebServiceClient(this);
		try {
			String response = restClient.execute(new String[] { insertQuery })
					.get();

			if (response != null) {
				XStream xstream = new XStream();
				xstream.alias("Plan", Plan.class);
				xstream.alias("memberNames", String.class);
				xstream.addImplicitCollection(Plan.class, "memberNames");
				Plan plan = (Plan) xstream.fromXML(response);
				if (plan != null && planName.equals(plan.getName())) {

					SharedPreferences.Editor editor = prefs.edit();
					editor.putString("selectedPlan", planName);
					editor.apply();
					Intent intent = new Intent(this, ViewMyPlansActivity.class);
					startActivity(intent);
				} else {
					
					errorFieldValue.setText("Plan Edit Failed");
				}
			} else {
				errorFieldValue.setText("Plan Edit Failed");
			}

		} catch (InterruptedException e) {
			
			errorFieldValue
					.setText("Apologies for any inconvenience caused. There is a problem with the service!");
		} catch (ExecutionException e) {
			
			errorFieldValue
					.setText("Apologies for any inconvenience caused. There is a problem with the service!");
		}

	}
	
	public void setTime(View v) {
		Button button = (Button) findViewById(R.id.editTimeButton);
		button.setTextColor(getResources().getColor(R.color.click_button_2));
		DialogFragment newFragment = new TimePickerFragment();
		newFragment.show(getSupportFragmentManager(), "timePicker");
		button.setTextColor(getResources().getColor(R.color.button_text));
	}

	public void setDate(View v) {
		Button button = (Button) findViewById(R.id.editDateButton);
		button.setTextColor(getResources().getColor(R.color.click_button_2));
		DialogFragment newFragment = new DatePickerFragment();
		newFragment.show(getSupportFragmentManager(), "datePicker");
		button.setTextColor(getResources().getColor(R.color.button_text));
	}
	
	@Override
	public void onBackPressed() {
	    Intent intent = new Intent(this, ViewMyPlansActivity.class);
	    startActivity(intent);
	}

}
