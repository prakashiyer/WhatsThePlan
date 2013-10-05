package com.theiyer.whatstheplan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import android.widget.ListView;
import android.widget.TextView;

import com.theiyer.whatstheplan.entity.Plan;
import com.theiyer.whatstheplan.entity.UserInformation;
import com.thoughtworks.xstream.XStream;

public class ViewPlanMembersActivity extends Activity {

	ListView memberListView;
	MemberListAdapter adapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.plan_member_list);
		ActionBar aBar = getActionBar();
		Resources res = getResources();
		Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
		aBar.setBackgroundDrawable(actionBckGrnd);
		aBar.setTitle(" Plan Members");

		SharedPreferences prefs = getSharedPreferences("Prefs",
				Activity.MODE_PRIVATE);
		

		String selectedPlan = prefs.getString("selectedPlan", "New User");
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

					List<String> memberEmailIds = plan.getMemberNames();

					if (memberEmailIds != null && !memberEmailIds.isEmpty()) {
						List<Map<String, byte[]>> membersList = new ArrayList<Map<String, byte[]>>();
						for(String memberEmailId: memberEmailIds){
							String userQuery = "/fetchUserInformation?emailId=" + memberEmailId;
							RestWebServiceClient userRestClient = new RestWebServiceClient(this);
							String userResp = userRestClient.execute(new String[] { userQuery })
									.get();
							
							if(userResp != null){
								XStream userXstream = new XStream();
								userXstream.alias("UserInformation", UserInformation.class);
								userXstream.alias("groupNames", String.class);
								userXstream.addImplicitCollection(UserInformation.class, "groupNames","groupNames",String.class);
								userXstream.alias("pendingGroupNames", String.class);
								userXstream.addImplicitCollection(UserInformation.class, "pendingGroupNames","pendingGroupNames",String.class);
								UserInformation user = (UserInformation) userXstream
										.fromXML(userResp);
								if(user != null){
									
									ImageRetrieveRestWebServiceClient userImageClient = new ImageRetrieveRestWebServiceClient(
											this);
									byte[] userImage = userImageClient.execute(new String[] { "fetchUserImage", memberEmailId }).get();
									Map<String, byte[]> memberMap = new HashMap<String, byte[]>();
									memberMap.put(user.getName(), userImage);
									membersList.add(memberMap);	
								}	
							}
						}
						
						if(!membersList.isEmpty()){
							
							memberListView = (ListView) findViewById(R.id.viewplanmemberList);

							adapter = new MemberListAdapter(this, membersList);
							memberListView.setAdapter(adapter);
							TextView memberListLabel = (TextView) findViewById(R.id.viewPlanMemberListLabel);
							memberListLabel.setVisibility(TextView.VISIBLE);
							memberListView.setVisibility(ListView.VISIBLE);
						}

						
					}

				}
			}
		} catch (InterruptedException e) {
			
			
		} catch (ExecutionException e) {
			
			
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
	    Intent intent = new Intent(this, ViewMyPlansActivity.class);
	    startActivity(intent);
	}

}
