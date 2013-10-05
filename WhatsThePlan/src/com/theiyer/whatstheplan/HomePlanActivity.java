package com.theiyer.whatstheplan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.theiyer.whatstheplan.entity.Plan;
import com.theiyer.whatstheplan.entity.PlanList;
import com.thoughtworks.xstream.XStream;

public class HomePlanActivity extends Activity implements OnItemClickListener {

	ListView planListView;
	PlanListAdapter adapter;
	List<Map<String, String>> plansResult;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home_plans);
		ActionBar aBar = getActionBar();
		Resources res = getResources();
		Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
		aBar.setBackgroundDrawable(actionBckGrnd);
		aBar.setTitle(" Home");

		SharedPreferences prefs = getSharedPreferences("Prefs",
				Activity.MODE_PRIVATE);

		String phone = prefs.getString("phone", "");
		String searchQuery = "/fetchUpcomingPlans?phone=" + phone;

		RestWebServiceClient restClient = new RestWebServiceClient(this);
		try {
			String response = restClient.execute(new String[] { searchQuery })
					.get();

			if (response != null) {
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
					    plansResult = new ArrayList<Map<String, String>>();
						for (Plan plan : plans) {
							Map<String, String> planMap = new HashMap<String, String>();
							planMap.put(plan.getName(), plan.getStartTime());
							plansResult.add(planMap);

						}

						if (!plansResult.isEmpty()) {
							planListView = (ListView) findViewById(R.id.viewupcomingplansList);
							adapter = new PlanListAdapter(this, plansResult);
							planListView.setAdapter(adapter);
							// Click event for single list row
							planListView.setOnItemClickListener(this);
						}

					}

				}
			}
		} catch (InterruptedException e) {
			

		} catch (ExecutionException e) {
			

		}

	}

	/** Called when the user clicks the Create group button */
	public void createGroups(View view) {
		Button button = (Button) findViewById(R.id.createGroupBtn);
		button.setTextColor(getResources().getColor(R.color.click_button_2));
		Intent intent = new Intent(this, CreateGroupActivity.class);
		
		startActivity(intent);
	}
	
	/** Called when the user clicks the view group button */
	public void viewGroups(View view) {
		
		Button button = (Button) findViewById(R.id.viewGroupBtn);
		button.setTextColor(getResources().getColor(R.color.click_button_1));
		
		Intent intent = new Intent(this, GroupsListActivity.class);
		startActivity(intent);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		SharedPreferences prefs = getSharedPreferences(
				"Prefs", Activity.MODE_PRIVATE);
		String selectedPlan = "";
		if(plansResult != null && !plansResult.isEmpty()){
			Map<String,String> selectedMap = plansResult.get(position);
			for(Entry<String,String> entry: selectedMap.entrySet()){
				
				SharedPreferences.Editor editor = prefs.edit();
				selectedPlan = entry.getKey();
				editor.putString("selectedPlan",selectedPlan);
				editor.apply();
				break;
			}
			Intent intent = new Intent(this, ViewMyPlansActivity.class);
			startActivity(intent);		
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
		    // do nothing.
		}
}
