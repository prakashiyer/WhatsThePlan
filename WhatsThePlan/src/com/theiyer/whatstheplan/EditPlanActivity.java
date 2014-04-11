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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.theiyer.whatstheplan.entity.Plan;
import com.theiyer.whatstheplan.util.WTPConstants;
import com.thoughtworks.xstream.XStream;

public class EditPlanActivity  extends FragmentActivity {

	private String oldName;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(haveInternet(this)){
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

			String searchQuery = "/fetchPlan?planName="
					+ selectedPlan.replace(" ", "%20");

			WebServiceClient restClient = new WebServiceClient(this);
			restClient.execute(new String[] { searchQuery });
		} else {
			Intent intent = new Intent(this, RetryActivity.class);
			startActivity(intent);
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
		WebServiceClient restClient = new WebServiceClient(this);
		 restClient.execute(new String[] { insertQuery });
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("selectedPlan", planName);
		editor.apply();
		CalendarHelper calendarHelper = new CalendarHelper(this);
		calendarHelper.execute(new String[] { planTime,
				planName, planLocation,
				"", "", "update",planDate, oldName});
		Intent intent = new Intent(this, ViewMyPlansActivity.class);
		startActivity(intent);
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
	
	public class WebServiceClient extends AsyncTask<String, Integer, String> {

		private Context mContext;
		private ProgressDialog pDlg;
		private boolean isFetchPlan;

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

			if(params[0].contains("fetchPlan")){
				isFetchPlan = true;
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
			if (response != null && isFetchPlan) {
				XStream xstream = new XStream();
				xstream.alias("Plan", Plan.class);
				xstream.alias("memberNames", String.class);
				xstream.addImplicitCollection(Plan.class, "memberNames");
				Plan plan = (Plan) xstream.fromXML(response);
				if (plan != null) {
					
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
