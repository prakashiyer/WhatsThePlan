package com.theiyer.whatstheplan;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.theiyer.whatstheplan.entity.Plan;
import com.theiyer.whatstheplan.util.WTPConstants;
import com.theiyer.whatstheplan.util.WhatstheplanUtil;
import com.thoughtworks.xstream.XStream;

public class ViewMyNewPlansActivity extends Activity {

	private String selectedPlan;
	private String selectedPlanIndex;
	private Context context = this;
	private Menu menu;
	public static Plan plan;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(haveInternet(this)){
			setContentView(R.layout.view_new_plan);
			ActionBar aBar = getActionBar();
			Resources res = getResources();
			Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
			aBar.setBackgroundDrawable(actionBckGrnd);
			aBar.setTitle(" Plan Information");

			SharedPreferences prefs = getSharedPreferences("Prefs",
					Activity.MODE_PRIVATE);
			String userName = prefs.getString("userName", "New User");
			TextView userNameValue = (TextView) findViewById(R.id.welcomeViewNewPlanLabel);
			userNameValue.setText(userName + ", here's selected plan details!");

			
			selectedPlan = prefs.getString("selectedPlan", "New User");
			selectedPlanIndex = prefs.getString("selectedPlanIndex", "");
			String docFlag = prefs.getString("docFlag", "");
			TextView selectedPlanValue = (TextView) findViewById(R.id.viewNewPlanTitle);
			selectedPlanValue.setText(" " + selectedPlan);

			String searchQuery = "/fetchPlan?id="+selectedPlanIndex;
			String phone = prefs.getString("phone", "");

			WebServiceClient restClient = new WebServiceClient(this);
			restClient.execute(new String[] { searchQuery, phone, docFlag });
		} else {
			Intent intent = new Intent(this, RetryActivity.class);
			startActivity(intent);
		}
		
	}

	/** Called when the user clicks the see members button */
	public void seeMembers(View view) {
		Button button = (Button) findViewById(R.id.seeMembersAttendingButton);
		button.setTextColor(getResources().getColor(R.color.click_button_1));
		Intent intent = new Intent(this, ViewAppointmentMembersActivity.class);
		startActivity(intent);
	}

	/** Called when the user clicks the rsvp plan button */
	public void rsvpPlan(View view) {
		TextView rsvpLabel = (TextView) findViewById(R.id.rsvpNewLabel);
		Button rsvpPlanButton = (Button) findViewById(R.id.rsvpNewPlanButton);
		rsvpPlanButton.setTextColor(getResources().getColor(
				R.color.click_button_2));
		SharedPreferences prefs = getSharedPreferences("Prefs",
				Activity.MODE_PRIVATE);
		String phone = prefs.getString("phone", "");
		String selectedPlan = prefs.getString("selectedPlan", "");
		String selectedPlanIndex = prefs.getString("selectedPlanIndex", "");
		String docFlag = prefs.getString("docFlag", "");
		String centerPlanFlag = prefs.getString("centerPlanFlag", "");
		String rsvp = "no";

		if (rsvpPlanButton.getText().equals("Say Yes")) {
			rsvp = "yes";
		}

		String updateQuery = "/rsvpPlan?id="+selectedPlanIndex + "&phone"+phone+"&docFlag=" + docFlag
				+"&centerPlanFlag="+centerPlanFlag+ "&rsvp=" + rsvp;
		if (rsvp == "no") { 
		CalendarHelper calendarHelper = new CalendarHelper(context);
		calendarHelper.execute(new String[] { "",
				selectedPlan, "", "", "", "delete"});
		System.out.println("Plan deleted.....");
		rsvpPlanButton.setVisibility(1);
		rsvpLabel.setVisibility(1);
		Intent intent = new Intent(this, HomePlanGroupFragmentActivity.class);
		startActivity(intent);
		} else {
			System.out.println("Selected plan : " + selectedPlan);
			System.out.println("******** Plan : " +plan.getStartTime().toString());
			CalendarHelper calendarHelper = new CalendarHelper(context);
			String[] startPlanTime = null;
			String planTime = plan.getStartTime();
			if(planTime != null) {
				startPlanTime = WhatstheplanUtil.createGmtToLocalTime(planTime);
			}
			calendarHelper.execute(new String[] { startPlanTime[0] +" " + startPlanTime[1],
					//selectedPlan, plan.getLocation(),
					String.valueOf(plan.getId()), phone, "create", "01:20", "2014-04-02" });
		}
		WebServiceClient restClient = new WebServiceClient(this);
		restClient.execute(new String[] { updateQuery, phone, docFlag });

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.menu = menu;
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		MenuItem viewProfileItem = menu.findItem(R.id.viewProfile);
		viewProfileItem.setVisible(true);

		MenuItem changeProfilePicItem = menu.findItem(R.id.changeProfilePic);
		changeProfilePicItem.setVisible(true);

		MenuItem editPlanItem = menu.findItem(R.id.editPlan);
		editPlanItem.setVisible(true);
		/*
		MenuItem viewGroupsInvitedListItem = menu.findItem(R.id.viewGroupsInvitedList);
		viewGroupsInvitedListItem.setVisible(true);*/
		
		MenuItem viewMembersInvitedListItem = menu.findItem(R.id.viewMembersInvitedList);
		viewMembersInvitedListItem.setVisible(true);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		
		SharedPreferences prefs = getSharedPreferences("Prefs",
				Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		
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
					String updateQuery = "/editPlan?id="
							+ selectedPlanIndex
							+ "&phone=&title=&date=&time=&endDate=&endTime=&userPhone=&userRsvp=&docPhone=&docRsvp=&centerPlanFlag=&cancelFlag=Y";
					WebServiceClient restClient = new WebServiceClient(context);
					restClient.execute(new String[] { updateQuery });
					CalendarHelper calendarHelper = new CalendarHelper(context);
					calendarHelper.execute(new String[] { "",
							selectedPlan, "", "", "", "delete"});
					Intent homeIntent = new Intent(context,
							HomePlanGroupFragmentActivity.class);
					startActivity(homeIntent);
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
			
		/*case (R.id.viewGroupsInvitedList):
			
			editor.putString("viewSelected", "groupsInvited");
			editor.apply();
			Intent viewGroupsInvitedIntent = new Intent(this, ViewPlanMembersActivity.class);
			startActivity(viewGroupsInvitedIntent);
			return true;*/
			
		case (R.id.viewMembersInvitedList):
			editor.putString("viewSelected", "membersInvited");
			editor.apply();
			Intent viewMembersInvitedIntent = new Intent(this, ViewPlanMembersActivity.class);
			startActivity(viewMembersInvitedIntent);
			return true;
			
		default:
			return false;
		}
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent(this, HomePlanGroupFragmentActivity.class);
		startActivity(intent);
	}

	public class WebServiceClient extends AsyncTask<String, Integer, String> {

		private Context mContext;
		private ProgressDialog pDlg;
		private String phone;
		private String docFlag;
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
			String path = WTPConstants.SERVICE_PATH + query;
			if (query.contains("fetchPlan") || query.contains("rsvpPlan")) {
				phone = params[1];
				docFlag = params[2];
			}

			// HttpHost target = new HttpHost(TARGET_HOST);
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
			Button rsvpPlanButton = (Button) findViewById(R.id.rsvpNewPlanButton);
			if (response != null && query.contains("fetchPlan")) {
				XStream xstream = new XStream();
				xstream.alias("Plan", Plan.class);
				Plan plan = (Plan) xstream.fromXML(response);
				ViewMyNewPlansActivity.plan = plan;
				if (plan != null) {

					if (phone.equals(plan.getUserPhone())) {
						MenuItem deletePlanItem = menu.findItem(R.id.deletePlan);
						deletePlanItem.setVisible(true);
					}

					TextView planTimeValue = (TextView) findViewById(R.id.viewNewPlanTime);
					TextView planEndTimeValue = (TextView) findViewById(R.id.viewNewPlanEndTime);

					String date = plan.getStartTime().substring(0, 10);
					String time = plan.getStartTime().substring(11, 16);
					String[] postedLocalDate = WhatstheplanUtil.createGmtToLocalTime(date + " " + time+":00");
					date = postedLocalDate[0];
					time = postedLocalDate[1];
					String hour = time.substring(0, 2);
					String min = time.substring(3,5);
					String endDate = plan.getEndTime().substring(0, 10);
					String endTime = plan.getEndTime().substring(11, 16);
					String[] endLocalDate = WhatstheplanUtil.createGmtToLocalTime(endDate + " " + endTime+":00");
					endDate = endLocalDate[0];
					endTime = endLocalDate[1];
					String endHour = endTime.substring(0, 2);
					String endMin = endTime.substring(3,5);
					int hourInt = Integer.valueOf(hour);
					String ampm = "AM";
					if (hourInt > 12) {
						hour = String.valueOf(hourInt - 12);
						if (Integer.valueOf(hour) < 10) {
							hour = "0" + hour;
						}
						ampm = "PM";
					}
					int endHourInt = Integer.valueOf(endHour);
					String endAmPm = "AM";
					if (endHourInt > 12) {
						endHour = String.valueOf(endHourInt - 12);
						if (Integer.valueOf(endHour) < 10) {
							endHour = "0" + endHour;
						}
						endAmPm = "PM";
					}
					planTimeValue.setText(" " + date + " " + hour + ":" + min
							+ " " + ampm);
					
					planEndTimeValue.setText(" "+ endDate + " " + endHour + ":" + endMin
					+ " " + endAmPm);

					TextView planLocationValue = (TextView) findViewById(R.id.viewNewPlanLocation);
					
					Button membersAttending = (Button) findViewById(R.id.seeMembersAttendingButton);
					TextView rsvpLabel = (TextView) findViewById(R.id.rsvpNewLabel);
					
					SharedPreferences prefs = getSharedPreferences("Prefs",
							Activity.MODE_PRIVATE);
					SharedPreferences.Editor editor = prefs.edit();
					editor.putString("centerPlanFlag", plan.getCenterPlanFlag());
					editor.apply();
					if("Y".equals(plan.getCenterPlanFlag())){
						
						
						
						planLocationValue.setText(" " + plan.getCenterName());
						String planFile = plan.getPlanFile();
						String[] membersArray = StringUtils.splitByWholeSeparator(planFile, ",");
						int count=0;
						for(String memberRsvp: membersArray){
							if (!plan.getUserPhone().equals(phone)) {
								if (memberRsvp.contains(phone)
										&& memberRsvp.contains("Y")) {
									rsvpLabel
											.setText("You are going, Click here to");
									rsvpPlanButton.setText("Say No");
								} else {
									rsvpLabel
											.setText("Are you attending? Click here to");
									rsvpPlanButton.setText("Say Yes");
								}
								rsvpPlanButton.setVisibility(Button.VISIBLE);
							} else {
								rsvpLabel.setVisibility(TextView.INVISIBLE);
								rsvpPlanButton.setVisibility(Button.INVISIBLE);
							}
							
							if(memberRsvp.contains("Y")){
								count = count +1;
							}
						}
						membersAttending.setText("Members Attending ("
								+ String.valueOf(count) + ") >>");
						
					} else {
						planLocationValue.setText(" " + plan.getDocName());
						int count=0;
						
						if("Y".equals(docFlag) && "Y".equals(plan.getDocRsvp())){
							rsvpLabel.setText("You are going, Click here to");
							rsvpPlanButton.setText("Say No");
						} else if ("Y".equals(docFlag) && "N".equals(plan.getDocRsvp())){
							rsvpLabel
									.setText("Are you attending? Click here to");
							rsvpPlanButton.setText("Say Yes");
						}
						
						if("N".equals(docFlag) && "Y".equals(plan.getUserRsvp())){
							rsvpLabel.setText("You are going, Click here to");
							rsvpPlanButton.setText("Say No");
						} else if ("N".equals(docFlag) && "N".equals(plan.getUserRsvp())){
							rsvpLabel
									.setText("Are you attending? Click here to");
							rsvpPlanButton.setText("Say Yes");
						}
						
						
						if("Y".equals(plan.getUserRsvp())){
							count = 1;
						}
						
						if("Y".equals(plan.getDocRsvp())){
							count = 2;
						}
						membersAttending.setText("Members Attending ("
								+ String.valueOf(count) + ") >>");
					}

				}
			}

			if (response != null && query.contains("rsvpPlan")) {
				XStream xstream = new XStream();
				xstream.alias("Plan", Plan.class);
				Plan plan = (Plan) xstream.fromXML(response);
				if (plan != null) {
					TextView planLocationValue = (TextView) findViewById(R.id.viewNewPlanLocation);
					Button membersAttending = (Button) findViewById(R.id.seeMembersAttendingButton);
					TextView rsvpLabel = (TextView) findViewById(R.id.rsvpNewLabel);
					if ("Y".equals(plan.getCenterPlanFlag())) {
						planLocationValue.setText(" " + plan.getCenterName());
						String planFile = plan.getPlanFile();
						String[] membersArray = StringUtils
								.splitByWholeSeparator(planFile, ",");
						int count = 0;
						for (String memberRsvp : membersArray) {
							if (!plan.getUserPhone().equals(phone)) {
								if (memberRsvp.contains(phone)
										&& memberRsvp.contains("Y")) {
									rsvpLabel
											.setText("You are going, Click here to");
									rsvpPlanButton.setText("Say No");
								} else {
									rsvpLabel
											.setText("Are you attending? Click here to");
									rsvpPlanButton.setText("Say Yes");
								}
								rsvpPlanButton.setVisibility(Button.VISIBLE);
							} else {
								rsvpLabel.setVisibility(TextView.INVISIBLE);
								rsvpPlanButton.setVisibility(Button.INVISIBLE);
							}

							if (memberRsvp.contains("Y")) {
								count = count + 1;
							}
						}
						membersAttending.setText("Members Attending ("
								+ String.valueOf(count) + ") >>");
						rsvpPlanButton.setTextColor(getResources().getColor(
								R.color.button_text));

					} else {
						planLocationValue.setText(" " + plan.getDocName());
						int count = 0;

						if ("Y".equals(docFlag)
								&& "Y".equals(plan.getDocRsvp())) {
							rsvpLabel.setText("You are going, Click here to");
							rsvpPlanButton.setText("Say No");
						} else if ("Y".equals(docFlag)
								&& "N".equals(plan.getDocRsvp())) {
							rsvpLabel
									.setText("Are you attending? Click here to");
							rsvpPlanButton.setText("Say Yes");
						}

						if ("N".equals(docFlag)
								&& "Y".equals(plan.getUserRsvp())) {
							rsvpLabel.setText("You are going, Click here to");
							rsvpPlanButton.setText("Say No");
						} else if ("N".equals(docFlag)
								&& "N".equals(plan.getUserRsvp())) {
							rsvpLabel
									.setText("Are you attending? Click here to");
							rsvpPlanButton.setText("Say Yes");
						}

						if ("Y".equals(plan.getUserRsvp())) {
							count = 1;
						}

						if ("Y".equals(plan.getDocRsvp())) {
							count = 2;
						}
						membersAttending.setText("Members Attending ("
								+ String.valueOf(count) + ") >>");
						
						rsvpPlanButton.setTextColor(getResources().getColor(
										R.color.button_text));
						
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
