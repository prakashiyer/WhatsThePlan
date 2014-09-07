package com.theiyer.whatstheplan;

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
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.theiyer.whatstheplan.entity.Plan;
import com.theiyer.whatstheplan.util.WTPConstants;
import com.theiyer.whatstheplan.util.WhatstheplanUtil;
import com.thoughtworks.xstream.XStream;

public class NewPlanActivity extends FragmentActivity {
	String planEndTime = null;
	String planEndDate = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(haveInternet(this)){
			setContentView(R.layout.new_plan);
			ActionBar aBar = getActionBar();
			Resources res = getResources();
			Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
			aBar.setBackgroundDrawable(actionBckGrnd);
			aBar.setTitle(" New Plan");

			SharedPreferences prefs = getSharedPreferences("Prefs",
					Activity.MODE_PRIVATE);
			String userName = prefs.getString("userName", "New User");
			TextView userNameValue = (TextView) findViewById(R.id.welcomeNewPlanLabel);
			userNameValue.setText(userName + ", create a plan here!");
			
		} else {
			Intent intent = new Intent(this, RetryActivity.class);
			startActivity(intent);
		}
		
	}

	/** Called when the user clicks the Register Plan button */
	public void goFromCreatePlanToViewPlans(View view) {

		Button button = (Button) findViewById(R.id.registerNewPlanButton);
		button.setTextColor(getResources().getColor(R.color.click_button_1));
		
		EditText planNameEditText = (EditText) findViewById(R.id.newPlanTitle);
		String planName = planNameEditText.getText().toString();

		SharedPreferences prefs = getSharedPreferences("Prefs",
				Activity.MODE_PRIVATE);
		String phone = prefs.getString("phone", "");

		TextView planDateEditText = (TextView) findViewById(R.id.newPlanStartDate);
		String planDate = planDateEditText.getText().toString();
		
		TextView planEndDateEditText = (TextView) findViewById(R.id.newPlanEndDate);
		planEndDate = planEndDateEditText.getText().toString();

		TextView planTimeEditText = (TextView) findViewById(R.id.newPlanStartTime);
		String planTime = planTimeEditText.getText().toString();

		String hour = planTime.substring(0, 2);
    	String min = planTime.substring(3,5);
    	if(planTime.contains("PM")){
    		hour = String.valueOf((Integer.valueOf(hour) +12));
    	}
    	planTime = hour+":"+min;
    	
    	TextView planEndTimeEditText = (TextView) findViewById(R.id.newPlanEndTime);
		planEndTime = planEndTimeEditText.getText().toString();
		System.out.println("****** end Time " + planEndTime);

		String endHour = planEndTime.substring(0, 2);
    	String endMin = planEndTime.substring(3,5);
    	if(planEndTime.contains("PM")){
    		endHour = String.valueOf((Integer.valueOf(endHour) +12));
    	}
    	planEndTime = endHour+":"+endMin;
    	
    	System.out.println("planEndTime ***** " + planEndTime);
    	
		EditText planLocationEditText = (EditText) findViewById(R.id.newPlanLocationValue);
		String planLocation = planLocationEditText.getText().toString();

		String groupList = prefs.getString("selectedGroups", "");
		System.out.println("Group List: "+groupList);
		String phoneList = prefs.getString("selectedIndividuals", "");
		System.out.println("Phone List: "+phoneList);

		String[] planDates = WhatstheplanUtil.createLocalToGmtTime(planDate+" "+planTime+":00");
		String[] planEndDates = WhatstheplanUtil.createLocalToGmtTime(planEndDate+" "+planEndTime+":00");
		
		String insertQuery = "/newPlan?name=" + planName.replace(" ", "%20")
				+ "&phone=" + phone + "&date=" + planDates[0] + "&time="
				+ planDates[1] + "&location=" + planLocation.replace(" ", "%20")
				+ "&phoneList=" + phoneList
				+ "&groupList=" + groupList.replace(" ", "%20")
				+ "&creator=" + phone+ "&endDate=" + planEndDates[0] + "&endTime="
						+ planEndDates[1] ;
		System.out.println("QUERY: "+insertQuery);
		
		WebServiceClient restClient = new WebServiceClient(this);
		restClient.execute(new String[] { insertQuery, phone });

		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("selectedPlan", planName);
		editor.apply();
			
			

		
	}
	
	

	public void setTime(View v) {
		Button button = (Button) findViewById(R.id.setStartTimeButton);
		button.setTextColor(getResources().getColor(R.color.click_button_2));
		DialogFragment newFragment = new TimeNewPickerFragment("start");
		newFragment.show(getSupportFragmentManager(), "timePicker");
		button.setTextColor(getResources().getColor(R.color.button_text));
	}
	public void setEndTime(View v) {
		Button button = (Button) findViewById(R.id.setEndTimebutton);
		button.setTextColor(getResources().getColor(R.color.click_button_2));
		DialogFragment newFragment = new TimeNewPickerFragment("end");
		newFragment.show(getSupportFragmentManager(), "timePicker");
		button.setTextColor(getResources().getColor(R.color.button_text));
	}

	public void setDate(View v) {
		Button button = (Button) findViewById(R.id.setStartDateButton);
		button.setTextColor(getResources().getColor(R.color.click_button_2));
		DialogFragment newFragment = new DateNewPickerFragment("start");
		newFragment.show(getSupportFragmentManager(), "datePicker");
		button.setTextColor(getResources().getColor(R.color.button_text));
	}
	public void setEndDate(View v) {
		Button button = (Button) findViewById(R.id.setEndDatebutton);
		button.setTextColor(getResources().getColor(R.color.click_button_2));
		DialogFragment newFragment = new DateNewPickerFragment("end");
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
	    Intent intent = new Intent(this, HomePlanGroupFragmentActivity.class);
	    startActivity(intent);
	}
	
	
	
	public class WebServiceClient extends AsyncTask<String, Integer, String> {

		private Context mContext;
		private ProgressDialog pDlg;
		private String phone;

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
			phone = params[1];
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
				XStream xstream = new XStream();
				xstream.alias("Plan", Plan.class);
				xstream.alias("memberNames", String.class);
				xstream.addImplicitCollection(Plan.class, "memberNames");
				xstream.alias("membersInvited", String.class);
				xstream.addImplicitCollection(Plan.class, "membersInvited");
				xstream.alias("groupsInvited", String.class);
				xstream.addImplicitCollection(Plan.class, "groupsInvited");
				Plan plan = (Plan) xstream.fromXML(response);
				if (plan != null) {
					CalendarHelper calendarHelper = new CalendarHelper(mContext);
					String startTime = plan.getStartTime();
					String[] startPlanTime = null;
					if(startTime != null) {
						startPlanTime = WhatstheplanUtil.createGmtToLocalTime(startTime);
					}
					calendarHelper.execute(new String[] { startPlanTime[0] +" " + startPlanTime[1],
							plan.getName(), plan.getLocation(),
							String.valueOf(plan.getId()), phone, "create", planEndTime, planEndDate});
					SharedPreferences prefs = getSharedPreferences("Prefs",
							Activity.MODE_PRIVATE);
					SharedPreferences.Editor editor = prefs.edit();
					editor.putString("selectedPlan", plan.getName());
					editor.putString("selectedPlanIndex", String.valueOf(plan.getId()));
					editor.apply();
					
					Intent intent = new Intent(mContext, ViewMyNewPlansActivity.class);
					startActivity(intent);
				} else {
					Toast.makeText(mContext, "Plan creation failed", Toast.LENGTH_SHORT).show();
					Intent intent = new Intent(mContext, ViewExistingMembersActivity.class);
					startActivity(intent);
				}
			}
			pDlg.dismiss();
		}

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
}
