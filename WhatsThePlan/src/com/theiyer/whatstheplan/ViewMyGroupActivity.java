package com.theiyer.whatstheplan;

import java.util.ArrayList;
import java.util.List;
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
import com.thoughtworks.xstream.XStream;

public class ViewMyGroupActivity extends Activity implements
		OnItemSelectedListener {

	boolean isAdmin;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.view_group);
		ActionBar aBar = getActionBar();
		Resources res = getResources();
		Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
		aBar.setBackgroundDrawable(actionBckGrnd);
		aBar.setTitle(" Group Information");

		SharedPreferences prefs = getSharedPreferences("Prefs",
				Activity.MODE_PRIVATE);

		String selectedGroup = prefs.getString("selectedGroup", "");
		String phone = prefs.getString("phone", "");

		TextView selectedGroupValue = (TextView) findViewById(R.id.selectedGroupValue);
		selectedGroupValue.setText(" " + selectedGroup);
		
		String searchGrpQuery = "/searchGroup?groupName=" + selectedGroup.replace(" ", "%20");

		RestWebServiceClient groupRestClient = new RestWebServiceClient(this);
		try {
			String response = groupRestClient.execute(new String[] { searchGrpQuery })
					.get();

			if (response != null) {
				XStream xstream = new XStream();
				xstream.alias("Group", Group.class);
				
				xstream.alias("members", String.class);
				xstream.addImplicitCollection(Group.class, "members","members",String.class);
				xstream.alias("planNames", String.class);
				xstream.addImplicitCollection(Group.class, "planNames","planNames",String.class);
				xstream.alias("pendingMembers", String.class);
				xstream.addImplicitCollection(Group.class, "pendingMembers","pendingMembers",String.class);
				Group group = (Group) xstream.fromXML(response);
                if (group != null && selectedGroup.equals(group.getName())) {
                	if(phone.equals(group.getAdmin())){
                		isAdmin = true;
                	} else {
                		isAdmin = false;
                	}
                		
                }
			}
		} catch (InterruptedException e) {
			
		} catch (ExecutionException e) {
			
		}

		ImageRetrieveRestWebServiceClient imageClient = new ImageRetrieveRestWebServiceClient(
				this);
		RestWebServiceClient restClient = new RestWebServiceClient(this);
		String searchQuery = "/fetchGroupPlans?groupName="
				+ selectedGroup.replace(" ", "%20");
		try {
			byte[] image = imageClient.execute(
					new String[] { "fetchGroupImage",
							selectedGroup.replace(" ", "%20") }).get();
			if(image != null){
				Bitmap img = BitmapFactory.decodeByteArray(image, 0, image.length);

				ImageView imgView = (ImageView) findViewById(R.id.selectedGroupPicThumbnail);
				imgView.setImageBitmap(img);
			}
			

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

					Spinner plansListSpinner = (Spinner) findViewById(R.id.viewGroupPlansListSpinner);
					List<Plan> plans = planList.getPlans();

					if (plans != null && !plans.isEmpty()) {
						List<String> planTitles = new ArrayList<String>();
						for (Plan plan : plans) {
							planTitles.add(plan.getName());
						}
						ArrayAdapter<String> plansAdapter = new ArrayAdapter<String>(
								this,
								android.R.layout.simple_spinner_dropdown_item,
								planTitles);
						plansListSpinner.setAdapter(plansAdapter);
						plansListSpinner.setOnItemSelectedListener(this);
						TextView planListLabel = (TextView) findViewById(R.id.groupPlanListLabel);
						planListLabel.setVisibility(TextView.VISIBLE);
						plansListSpinner.setVisibility(Spinner.VISIBLE);
					}

				}
			}
		} catch (InterruptedException e) {
			
		} catch (ExecutionException e) {
			
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
		
		if(isAdmin){
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
			Intent inviteIntent = new Intent(this,
					InviteListActivity.class);
			startActivity(inviteIntent);
			return true;
		case (R.id.leaveGroup):
			Intent leaveGroupIntent = new Intent(this,
					LeaveGroupActivity.class);
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

}
