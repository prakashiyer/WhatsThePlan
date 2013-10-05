package com.theiyer.whatstheplan;

import java.util.List;
import java.util.concurrent.ExecutionException;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.theiyer.whatstheplan.entity.Plan;
import com.thoughtworks.xstream.XStream;

public class ViewMyPlansActivity extends Activity {

	private boolean isCreator;
	private String selectedPlan;
	private String selectedGroup;
	private Context context = this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.view_plan);
		ActionBar aBar = getActionBar();
		Resources res = getResources();
		Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
		aBar.setBackgroundDrawable(actionBckGrnd);
		aBar.setTitle(" Plan Information");

		SharedPreferences prefs = getSharedPreferences("Prefs",
				Activity.MODE_PRIVATE);
		String userName = prefs.getString("userName", "New User");
		TextView userNameValue = (TextView) findViewById(R.id.welcomeViewPlanLabel);
		userNameValue.setText(userName + ", here's selected plan details!");

		selectedGroup = prefs.getString("selectedGroup", "New User");
		selectedPlan = prefs.getString("selectedPlan", "New User");
		TextView selectedPlanValue = (TextView) findViewById(R.id.viewPlanTitle);
		selectedPlanValue.setText(" " + selectedPlan);

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

					String emailId = prefs.getString("emailId", "");
					if (emailId.equals(plan.getCreator())) {
						isCreator = true;
					}
					TextView planGroupValue = (TextView) findViewById(R.id.viewPlanGroup);
					planGroupValue.setText(" " + plan.getGroupName());

					TextView planTimeValue = (TextView) findViewById(R.id.viewPlanTime);

					String date = plan.getStartTime().substring(0, 10);
	            	String time = plan.getStartTime().substring(11, 16);
	            	String hour = time.substring(0, 2);
	            	String min = time.substring(3);
					int hourInt = Integer.valueOf(hour);
					String ampm = "AM";
					if (hourInt > 12) {
						hour = String.valueOf(hourInt-12);
						if (Integer.valueOf(hour) < 10) {
							hour = "0" + hour;
						}
						ampm = "PM";
					}
					planTimeValue.setText(" " +date+" "+ hour + ":" + min + " " + ampm);

					TextView planLocationValue = (TextView) findViewById(R.id.viewPlanLocation);
					planLocationValue.setText(" " + plan.getLocation());

					List<String> memberEmailIds = plan.getMemberNames();

					if (memberEmailIds != null && !memberEmailIds.isEmpty()) {

						Button membersAttending = (Button) findViewById(R.id.seeMembersButton);
						membersAttending.setText("Members Attending ("+String.valueOf(memberEmailIds.size())+") >>");
						TextView rsvpLabel = (TextView) findViewById(R.id.rsvpLabel);
						Button rsvpPlanButton = (Button) findViewById(R.id.rsvpPlanButton);
						if (memberEmailIds.contains(emailId)) {
							rsvpLabel.setText("You are going, Click here to");
							rsvpPlanButton.setText("Say No");
						} else {
							rsvpLabel.setText("Are you attending? Click here to");
							rsvpPlanButton.setText("Say Yes");
						}
						rsvpPlanButton.setVisibility(Button.VISIBLE);
					}

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

	/** Called when the user clicks the see members button */
	public void seeMembers(View view) {
		Button button = (Button) findViewById(R.id.seeMembersButton);
		button.setTextColor(getResources().getColor(R.color.click_button_1));
		Intent intent = new Intent(this, ViewPlanMembersActivity.class);
		startActivity(intent);
	}


	/** Called when the user clicks the rsvp plan button */
	public void rsvpPlan(View view) {
		Button rsvpPlanButton = (Button) findViewById(R.id.rsvpPlanButton);
		rsvpPlanButton.setTextColor(getResources().getColor(
				R.color.click_button_2));
		SharedPreferences prefs = getSharedPreferences("Prefs",
				Activity.MODE_PRIVATE);
		String emailId = prefs.getString("emailId", "");
		String selectedPlan = prefs.getString("selectedPlan", "");
		String rsvp = "no";

		if (rsvpPlanButton.getText().equals("Say Yes")) {
			rsvp = "yes";
		}

		String updateQuery = "/rsvpPlan?planName="
				+ selectedPlan.replace(" ", "%20") + "&emailId=" + emailId
				+ "&rsvp=" + rsvp;

		TextView errorFieldValue = (TextView) findViewById(R.id.viewPlanErrorField);
		RestWebServiceClient restClient = new RestWebServiceClient(this);
		try {
			String response = restClient.execute(new String[] { updateQuery })
					.get();

			if (response != null) {
				XStream xstream = new XStream();
				xstream.alias("Plan", Plan.class);
				xstream.alias("memberNames", String.class);
				xstream.addImplicitCollection(Plan.class, "memberNames");
				Plan plan = (Plan) xstream.fromXML(response);
				if (plan != null && selectedPlan.equals(plan.getName())) {
					List<String> memberEmailIds = plan.getMemberNames();

					if (memberEmailIds != null && !memberEmailIds.isEmpty()) {

						Button membersAttending = (Button) findViewById(R.id.seeMembersButton);
						membersAttending.setText("Members Attending ("+String.valueOf(memberEmailIds.size())+") >>");
						TextView rsvpLabel = (TextView) findViewById(R.id.rsvpLabel);
						if (memberEmailIds.contains(emailId)) {
							rsvpLabel.setText("You are going, Click here to");
							rsvpPlanButton.setText("Say No");
						} else {
							rsvpLabel.setText("Are you attending? Click here to");
							rsvpPlanButton.setText("Say Yes");
						}
						rsvpPlanButton.setTextColor(getResources().getColor(
								R.color.button_text));
					}
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		MenuItem viewProfileItem = menu.findItem(R.id.viewProfile);
		viewProfileItem.setVisible(true);

		MenuItem changeProfilePicItem = menu.findItem(R.id.changeProfilePic);
		changeProfilePicItem.setVisible(true);

		MenuItem editPlanItem = menu.findItem(R.id.editPlan);
		editPlanItem.setVisible(true);

		if (isCreator) {
			MenuItem deletePlanItem = menu.findItem(R.id.deletePlan);
			deletePlanItem.setVisible(true);
		}

		MenuItem deactivateAccountItem = menu.findItem(R.id.deactivateAccount);
		deactivateAccountItem.setVisible(true);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case (R.id.viewProfile):
			Intent viewProfileIntent = new Intent(this,
					ViewProfileActivity.class);
			startActivity(viewProfileIntent);
			return true;
		case (R.id.changeProfilePic):
			Intent changeProfilePicIntent = new Intent(this,
					ProfileImageUploadActivity.class);
			startActivity(changeProfilePicIntent);
			return true;
		case (R.id.editPlan):
			Intent editPlanIntent = new Intent(this, EditPlanActivity.class);
			startActivity(editPlanIntent);
			return true;
		case (R.id.deletePlan):
			AlertDialog.Builder ad = new AlertDialog.Builder(this);
			ad.setTitle("Delete Plan confirmation");
			ad.setMessage("Are you sure about deleting this plan?");

			ad.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String updateQuery = "/deletePlan?planName="
							+ selectedPlan.replace(" ", "%20") + "&groupName="
							+ selectedGroup.replace(" ", "%20");
					RestWebServiceClient restClient = new RestWebServiceClient(
							context);
					try {
						String response = restClient.execute(
								new String[] { updateQuery }).get();

						if (response != null) {
							XStream xstream = new XStream();
							xstream.alias("Plan", Plan.class);
							xstream.alias("memberNames", String.class);
							xstream.addImplicitCollection(Plan.class,
									"memberNames");
							Plan plan = (Plan) xstream.fromXML(response);
							if (plan != null) {
								Intent homeIntent = new Intent(context,
										HomePlanActivity.class);

								startActivity(homeIntent);
							}
						}
					} catch (InterruptedException e) {
						

					} catch (ExecutionException e) {
						

					}
				}
			});
			ad.setNegativeButton("No", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// do nothing
				}
			});
			ad.show();
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
	    Intent intent = new Intent(this, HomePlanActivity.class);
	    startActivity(intent);
	}

}
