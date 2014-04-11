package com.theiyer.whatstheplan;

import java.util.ArrayList;
import java.util.List;

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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.theiyer.whatstheplan.entity.Group;
import com.theiyer.whatstheplan.entity.Plan;
import com.theiyer.whatstheplan.entity.PlanList;
import com.theiyer.whatstheplan.util.WTPConstants;
import com.thoughtworks.xstream.XStream;

public class ViewMyGroupActivity extends Activity implements
		OnItemSelectedListener {
	private static final String TAG = "ViewMyGroupActivity";
	private boolean isAdmin;
	private Context context;
	private String selectedGroup;
	private String phone;
	private Spinner plansListSpinner;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(haveInternet(this)){
			setContentView(R.layout.view_group);
			ActionBar aBar = getActionBar();
			Resources res = getResources();
			Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
			aBar.setBackgroundDrawable(actionBckGrnd);
			aBar.setTitle(" Group Information");
			context = this;

			SharedPreferences prefs = getSharedPreferences("Prefs",
					Activity.MODE_PRIVATE);

			selectedGroup = prefs.getString("selectedGroup", "");
			phone = prefs.getString("phone", "");

			TextView selectedGroupValue = (TextView) findViewById(R.id.selectedGroupValue);
			selectedGroupValue.setText(" " + selectedGroup);

			String searchGrpQuery = "/searchGroup?groupName="
					+ selectedGroup.replace(" ", "%20");

			WebServiceClient groupRestClient = new WebServiceClient(this);
			groupRestClient.execute(new String[] { searchGrpQuery });

			WebImageRetrieveRestWebServiceClient imageClient = new WebImageRetrieveRestWebServiceClient(
					this);

			imageClient.execute(new String[] { "fetchGroupImage",
					selectedGroup.replace(" ", "%20") });

			WebServiceClient restClient = new WebServiceClient(this);
			String searchQuery = "/fetchGroupPlans?groupName="
					+ selectedGroup.replace(" ", "%20");

			restClient.execute(new String[] { searchQuery });

			plansListSpinner = (Spinner) findViewById(R.id.viewGroupPlansListSpinner);
			plansListSpinner.setOnItemSelectedListener(this);
		} else {
			Intent intent = new Intent(this, RetryActivity.class);
			startActivity(intent);
		}
		
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long id) {

		if (R.id.viewGroupPlansListSpinner == parent.getId()) {
			Button viewPlanButton = (Button) findViewById(R.id.viewSelectedPlanButton);
			viewPlanButton.setVisibility(Button.VISIBLE);
		}
	}

	/** Called when the user clicks the View plan button */
	public void goFromViewGroupsToViewPlans(View view) {
		Button button = (Button) findViewById(R.id.viewSelectedPlanButton);
		button.setTextColor(getResources().getColor(R.color.click_button_1));
		SharedPreferences prefs = getSharedPreferences("Prefs",
				Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		Spinner plansListSpinner = (Spinner) findViewById(R.id.viewGroupPlansListSpinner);
		editor.putString("selectedPlan",
				(String) plansListSpinner.getSelectedItem());
		editor.apply();

		Intent intent = new Intent(this, ViewMyPlansActivity.class);
		startActivity(intent);
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub

	}

	/** Called when the user clicks the Create Plan button */
	public void createPlan(View view) {
		Button button = (Button) findViewById(R.id.createPlanButton);
		button.setTextColor(getResources().getColor(R.color.click_button_2));
		Intent intent = new Intent(this, CreatePlanActivity.class);
		startActivity(intent);
	}

	/** Called when the user clicks the view members button */
	public void viewMembers(View view) {
		Button button = (Button) findViewById(R.id.viewMembersButton);
		button.setTextColor(getResources().getColor(R.color.click_button_1));
		Intent intent = new Intent(this, ViewGroupMembersActivity.class);
		startActivity(intent);
	}

	/** Called when the user clicks the settle bills button */
	public void settle(View view) {
		Button button = (Button) findViewById(R.id.settleButton);
		button.setTextColor(getResources().getColor(R.color.click_button_2));
		Intent intent = new Intent(this, PlanHistoryActivity.class);
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

		MenuItem changeGroupPicItem = menu.findItem(R.id.changeGroupPic);
		changeGroupPicItem.setVisible(true);

		MenuItem leaveGroupItem = menu.findItem(R.id.leaveGroup);
		leaveGroupItem.setVisible(true);

		if (isAdmin) {
			MenuItem inviteMembersItem = menu.findItem(R.id.inviteMembers);
			inviteMembersItem.setVisible(true);
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
		case (R.id.changeGroupPic):
			Intent changeGroupPicIntent = new Intent(this,
					GroupImageChangeActivity.class);
			startActivity(changeGroupPicIntent);
			return true;
		case (R.id.inviteMembers):
			Intent inviteIntent = new Intent(this, InviteListActivity.class);
			startActivity(inviteIntent);
			return true;
		case (R.id.leaveGroup):
			Intent leaveGroupIntent = new Intent(this, LeaveGroupActivity.class);
			startActivity(leaveGroupIntent);
			return true;
		case (R.id.deactivateAccount):
			Intent deactivateAccountIntent = new Intent(this,
					DeactivateAccountActivity.class);
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
			String path = WTPConstants.SERVICE_PATH + params[0];

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
				PlanList planList = (PlanList) xstream.fromXML(response);
				if (planList != null && planList.getPlans() != null) {

					List<Plan> plans = planList.getPlans();

					if (plans != null && !plans.isEmpty()) {
						List<String> planTitles = new ArrayList<String>();
						for (Plan plan : plans) {
							planTitles.add(plan.getName());
						}
						ArrayAdapter<String> plansAdapter = new ArrayAdapter<String>(
								context,
								android.R.layout.simple_spinner_dropdown_item,
								planTitles);
						plansListSpinner.setAdapter(plansAdapter);
						TextView planListLabel = (TextView) findViewById(R.id.groupPlanListLabel);
						planListLabel.setVisibility(TextView.VISIBLE);
						plansListSpinner.setVisibility(Spinner.VISIBLE);
					}

				}
			}

			pDlg.dismiss();
		}

	}

	public class WebImageRetrieveRestWebServiceClient extends
			AsyncTask<String, Integer, byte[]> {

		private Context mContext;
		private ProgressDialog pDlg;

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
			String path = WTPConstants.SERVICE_PATH + "/" + method;

			if ("fetchUserImage".equals(method)) {
				path = path + "?phone=" + params[1];
			} else {
				path = path + "?groupName=" + params[1];
			}
			// HttpHost target = new HttpHost(TARGET_HOST);
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

			if (response != null) {
				Bitmap img = BitmapFactory.decodeByteArray(response, 0,
						response.length);

				Activity activity = (Activity) mContext;

				// For ViewMyGroupActivity
				ImageView imgView = (ImageView) activity
						.findViewById(R.id.selectedGroupPicThumbnail);
				if (imgView != null) {
					imgView.setImageBitmap(img);
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
