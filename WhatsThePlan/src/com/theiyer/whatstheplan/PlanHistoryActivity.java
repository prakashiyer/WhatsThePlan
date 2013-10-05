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
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.theiyer.whatstheplan.entity.Plan;
import com.theiyer.whatstheplan.entity.PlanList;
import com.thoughtworks.xstream.XStream;

public class PlanHistoryActivity extends Activity implements OnItemClickListener {

	ListView planListView;
	PlanListAdapter adapter;
	List<Map<String, String>> plansResult;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.plan_history);
		ActionBar aBar = getActionBar();
		Resources res = getResources();
		Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
		aBar.setBackgroundDrawable(actionBckGrnd);
		aBar.setTitle(" Plan History");

		SharedPreferences prefs = getSharedPreferences("Prefs",
				Activity.MODE_PRIVATE);

		String selectedGroup = prefs.getString("selectedGroup", "");
		String searchQuery = "/fetchPlanHistory?groupName="+selectedGroup.replace(" ", "%20");

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
							planListView = (ListView) findViewById(R.id.viewPlanHistoryList);
							adapter = new PlanListAdapter(this, plansResult);
							planListView.setAdapter(adapter);
							planListView.setOnItemClickListener(this);
						}

					}

				}
			}
		} catch (InterruptedException e) {
			

		} catch (ExecutionException e) {
			

		}

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
			
			Intent intent = new Intent(this, ExpenseReportActivity.class);
			startActivity(intent);		
		}
	}
	
	@Override
	public void onBackPressed() {
	    Intent intent = new Intent(this, ViewMyGroupActivity.class);
	    startActivity(intent);
	}
}
