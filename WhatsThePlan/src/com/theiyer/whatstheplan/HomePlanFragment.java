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
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.theiyer.whatstheplan.entity.Plan;
import com.theiyer.whatstheplan.entity.PlanList;
import com.theiyer.whatstheplan.util.WTPConstants;
import com.thoughtworks.xstream.XStream;

public class HomePlanFragment extends Fragment implements OnItemClickListener {
	Activity activity;
	ListView planListView;
	View rootView;
	PlanListAdapter adapter;
	List<Map<String, Plan>> plansResult;
	String centerFlag;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity = this.getActivity();
		if (activity != null && haveInternet(activity)) {
			ActionBar aBar = activity.getActionBar();
			Resources res = getResources();
			Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
			aBar.setBackgroundDrawable(actionBckGrnd);
			aBar.setTitle(" Upcoming Appointments");
			rootView = inflater.inflate(R.layout.home_plans, container, false);

			SharedPreferences prefs = activity.getSharedPreferences("Prefs",
					Activity.MODE_PRIVATE);

			String phone = prefs.getString("phone", "");
			String docFlag = prefs.getString("docFlag", "");
			centerFlag = prefs.getString("centerFlag", "");
			String searchQuery = "/fetchUpcomingPlans?phone=" + phone;

			adapter = new PlanListAdapter(activity);
			planListView = (ListView) rootView
					.findViewById(R.id.viewupcomingplansList);
			planListView.setOnItemClickListener(this);

			Button button = (Button) rootView.findViewById(R.id.createPlanBtn);
			if ("Y".equals(docFlag)) {
				button.setVisibility(Button.INVISIBLE);
			}

			WebServiceClient restClient = new WebServiceClient(activity);
			restClient.execute(new String[] { searchQuery });
		} else {
			Intent intent = new Intent(activity, RetryActivity.class);
			startActivity(intent);
		}
		return rootView;
	}

	/** Called when the user clicks the create plan button */
	public void newPlan(View view) {

		Button button = (Button) activity.findViewById(R.id.createPlanBtn);
		button.setTextColor(getResources().getColor(R.color.click_button_2));
		if ("Y".equals(centerFlag)) {
			Intent intent = new Intent(activity, AppointmentActivity.class);
			startActivity(intent);
		} else {
			Intent intent = new Intent(activity,
					ViewExistingMembersActivity.class);
			startActivity(intent);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		SharedPreferences prefs = activity.getSharedPreferences("Prefs",
				Activity.MODE_PRIVATE);
		String selectedPlan = "";
		String selectedPlanIndex = "";
		if (plansResult != null && !plansResult.isEmpty()) {
			Map<String, Plan> selectedMap = plansResult.get(position);
			for (Entry<String, Plan> entry : selectedMap.entrySet()) {

				SharedPreferences.Editor editor = prefs.edit();
				selectedPlan = entry.getValue().getTitle();
				selectedPlanIndex = entry.getKey();
				editor.putString("selectedPlan", selectedPlan);
				editor.putString("selectedPlanIndex", selectedPlanIndex);
				editor.apply();
				break;
			}
			Intent intent = new Intent(activity, ViewMyNewPlansActivity.class);
			startActivity(intent);
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
			if (response != null && response.contains("PlanList")) {
				XStream xstream = new XStream();
				xstream.alias("PlanList", PlanList.class);
				xstream.alias("plans", Plan.class);
				xstream.addImplicitCollection(PlanList.class, "plans");
				PlanList planList = (PlanList) xstream.fromXML(response);

				if (planList != null && planList.getPlans() != null) {

					List<Plan> plans = planList.getPlans();

					if (plans != null && !plans.isEmpty()) {
						plansResult = new ArrayList<Map<String, Plan>>();
						for (Plan plan : plans) {
							Map<String, Plan> planMap = new HashMap<String, Plan>();
							planMap.put(String.valueOf(plan.getId()), plan);
							plansResult.add(planMap);

						}

						if (!plansResult.isEmpty()) {
							planListView.setVisibility(ListView.VISIBLE);
							adapter.setData(plansResult);
							planListView.setAdapter(adapter);
							// Click event for single list row
						} else {
							planListView.setVisibility(ListView.INVISIBLE);
							TextView planLabel = (TextView) rootView
									.findViewById(R.id.upcomingPlanListLabel);
							planLabel.setText("No upcoming appointments.");
						}

					} else {
						planListView.setVisibility(ListView.INVISIBLE);
						TextView planLabel = (TextView) rootView
								.findViewById(R.id.upcomingPlanListLabel);
						planLabel.setText("No upcoming appointments.");
					}
				} else {
					planListView.setVisibility(ListView.INVISIBLE);
					TextView planLabel = (TextView) rootView
							.findViewById(R.id.upcomingPlanListLabel);
					planLabel.setText("No upcoming appointments.");
				}
			} else {
				planListView.setVisibility(ListView.INVISIBLE);
				TextView planLabel = (TextView) rootView
						.findViewById(R.id.upcomingPlanListLabel);
				planLabel.setText("No upcoming appointments.");
			}
			pDlg.dismiss();
		}
	}

	public void onBackPressed() {
		// Do Nothing
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

	public void setActivity(Activity activity) {
		this.activity = activity;
	}
}
