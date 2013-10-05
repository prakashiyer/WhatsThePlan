package com.theiyer.whatstheplan;

import java.util.concurrent.ExecutionException;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.theiyer.whatstheplan.entity.Plan;
import com.thoughtworks.xstream.XStream;

public class CreatePlanActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.create_plan);
		ActionBar aBar = getActionBar();
		Resources res = getResources();
		Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
		aBar.setBackgroundDrawable(actionBckGrnd);
		aBar.setTitle(" New Plan");

		SharedPreferences prefs = getSharedPreferences("Prefs",
				Activity.MODE_PRIVATE);
		String userName = prefs.getString("userName", "New User");
		TextView userNameValue = (TextView) findViewById(R.id.welcomeCreatePlanLabel);
		userNameValue.setText(userName + ", create a plan here!");

		String selectedGroup = prefs.getString("selectedGroup", "");

		TextView selectedGroupValue = (TextView) findViewById(R.id.selectedgroupNameField);
		selectedGroupValue.setText(" " + selectedGroup);

		ImageRetrieveRestWebServiceClient imageClient = new ImageRetrieveRestWebServiceClient(
				this);
		try {

			byte[] image = imageClient.execute(
					new String[] { "fetchGroupImage",
							selectedGroup.replace(" ", "%20") }).get();
			Bitmap img = BitmapFactory.decodeByteArray(image, 0, image.length);

			ImageView imgView = (ImageView) findViewById(R.id.selectedgroupPicThumbnail);
			imgView.setImageBitmap(img);

		} catch (InterruptedException e) {
			
		} catch (ExecutionException e) {
			
		}
	}

	/** Called when the user clicks the Register Plan button */
	public void goFromCreatePlanToViewPlans(View view) {

		Button button = (Button) findViewById(R.id.registerPlanButton);
		button.setTextColor(getResources().getColor(R.color.click_button_1));
		
		EditText planNameEditText = (EditText) findViewById(R.id.newPlanTitleValue);
		String planName = planNameEditText.getText().toString();

		SharedPreferences prefs = getSharedPreferences("Prefs",
				Activity.MODE_PRIVATE);
		String emailId = prefs.getString("emailId", "");

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
    	
		EditText planLocationEditText = (EditText) findViewById(R.id.planLocationValue);
		String planLocation = planLocationEditText.getText().toString();

		String selectedGroup = prefs.getString("selectedGroup", "");

		String insertQuery = "/addPlan?name=" + planName.replace(" ", "%20")
				+ "&emailId=" + emailId + "&date=" + planDate + "&time="
				+ planTime+":00" + "&location=" + planLocation.replace(" ", "%20")
				+ "&groupName=" + selectedGroup.replace(" ", "%20")
				+ "&creator=" + emailId;

		TextView errorFieldValue = (TextView) findViewById(R.id.createPlanErrorField);
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

					CalendarHelper calendarHelper = new CalendarHelper(this);
					calendarHelper.execute(new String[] { plan.getStartTime(),
							plan.getName(), plan.getLocation(),
							String.valueOf(plan.getId()), emailId });
					Intent intent = new Intent(this, ViewMyPlansActivity.class);
					startActivity(intent);
				} else {

					errorFieldValue.setText("Plan Addition Failed");
				}
			} else {
				errorFieldValue.setText("Plan Addition Failed");
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
		Button button = (Button) findViewById(R.id.setTimeButton);
		button.setTextColor(getResources().getColor(R.color.click_button_2));
		DialogFragment newFragment = new TimePickerFragment();
		newFragment.show(getSupportFragmentManager(), "timePicker");
		button.setTextColor(getResources().getColor(R.color.button_text));
	}

	public void setDate(View v) {
		Button button = (Button) findViewById(R.id.setDateButton);
		button.setTextColor(getResources().getColor(R.color.click_button_2));
		DialogFragment newFragment = new DatePickerFragment();
		newFragment.show(getSupportFragmentManager(), "datePicker");
		button.setTextColor(getResources().getColor(R.color.button_text));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public void onBackPressed() {
	    Intent intent = new Intent(this, ViewMyGroupActivity.class);
	    startActivity(intent);
	}

}
