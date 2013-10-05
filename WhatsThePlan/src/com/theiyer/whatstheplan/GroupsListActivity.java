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
import android.widget.ListView;
import android.widget.TextView;

import com.theiyer.whatstheplan.entity.Group;
import com.theiyer.whatstheplan.entity.UserInformation;
import com.thoughtworks.xstream.XStream;

public class GroupsListActivity extends Activity implements OnItemClickListener {

	ListView list;
	GroupListAdapter adapter;
	List<Map<String, byte[]>> groupsList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.splashscreen);
		
		
		ActionBar aBar = getActionBar();
		Resources res = getResources();
		Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
		aBar.setBackgroundDrawable(actionBckGrnd);
		aBar.setTitle(" My Groups");

		SharedPreferences prefs = getSharedPreferences("Prefs",
				Activity.MODE_PRIVATE);
		String userName = prefs.getString("userName", "New User");
		
		

		String emailId = prefs.getString("emailId", "");

		String searchQuery = "/fetchUserInformation?emailId=" + emailId;

		
		RestWebServiceClient restClient = new RestWebServiceClient(this);
		try {
			String response = restClient.execute(new String[] { searchQuery })
					.get();

			if (response != null) {
				XStream xstream = new XStream();
				xstream.alias("UserInformation", UserInformation.class);
				xstream.alias("groupNames", String.class);
				xstream.addImplicitCollection(UserInformation.class, "groupNames","groupNames",String.class);
				xstream.alias("pendingGroupNames", String.class);
				xstream.addImplicitCollection(UserInformation.class, "pendingGroupNames","pendingGroupNames",String.class);
				UserInformation user = (UserInformation) xstream
						.fromXML(response);
				if (user != null && user.getGroupNames()!= null && !(user.getGroupNames().isEmpty())) {

					List<String> groupNames = user.getGroupNames();
					groupsList = new ArrayList<Map<String, byte[]>>();

					
					for (String groupName : groupNames) {
						ImageRetrieveRestWebServiceClient imageClient = new ImageRetrieveRestWebServiceClient(
								this);
						Map<String, byte[]> groupDetails = new HashMap<String, byte[]>();
						byte[] image = imageClient.execute(
								new String[] { "fetchGroupImage", groupName.replace(" ", "%20") })
								.get();
						groupDetails.put(groupName, image);
						groupsList.add(groupDetails);
					}
					setContentView(R.layout.groups_list);
					TextView errorFieldValue = (TextView) findViewById(R.id.listGroupsErrorField);
					TextView userNameValue = (TextView) findViewById(R.id.welcomeListGroupsLabel);
					userNameValue.setText(userName + ", View all the groups here!");
					list = (ListView) findViewById(R.id.groupList);

					// Getting adapter by passing xml data ArrayList
					adapter = new GroupListAdapter(this, groupsList);
					list.setAdapter(adapter);
					// Click event for single list row
					list.setOnItemClickListener(this);

					
				} else {
					setContentView(R.layout.groups_list);
					TextView errorFieldValue = (TextView) findViewById(R.id.listGroupsErrorField);
					errorFieldValue.setText("You have no groups!");
				}
			} else {
				setContentView(R.layout.groups_list);
				TextView errorFieldValue = (TextView) findViewById(R.id.listGroupsErrorField);
				errorFieldValue.setText("Group information fetch failed!");
			}
		} catch (InterruptedException e) {
			setContentView(R.layout.groups_list);
			TextView errorFieldValue = (TextView) findViewById(R.id.listGroupsErrorField);
			errorFieldValue
					.setText("Apologies for any inconvenience caused. There is a problem with the service!");
		} catch (ExecutionException e) {
			setContentView(R.layout.groups_list);
			TextView errorFieldValue = (TextView) findViewById(R.id.listGroupsErrorField);
			errorFieldValue
					.setText("Apologies for any inconvenience caused. There is a problem with the service!");
		}

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		SharedPreferences prefs = getSharedPreferences(
				"Prefs", Activity.MODE_PRIVATE);
		String emailId = prefs.getString("emailId", "");
		String selectedGroup = "";
		TextView errorFieldValue = (TextView) findViewById(R.id.listGroupsErrorField);
		if(groupsList != null && !groupsList.isEmpty()){
			Map<String,byte[]> selectedMap = groupsList.get(position);
			for(Entry<String,byte[]> entry: selectedMap.entrySet()){
				
				SharedPreferences.Editor editor = prefs.edit();
				selectedGroup = entry.getKey();
				editor.putString("selectedGroup",selectedGroup);
				editor.apply();
				break;
			}
			
			String searchQuery = "/searchGroup?groupName=" + selectedGroup.replace(" ", "%20");
			RestWebServiceClient restClient = new RestWebServiceClient(this);
			try {
				String response = restClient.execute(new String[] { searchQuery })
						.get();
				if(response!=null){
					XStream xstream = new XStream();
					xstream.alias("Group", Group.class);
					
					xstream.alias("memberEmailIds", String.class);
					xstream.addImplicitCollection(Group.class, "memberEmailIds","memberEmailIds",String.class);
					xstream.alias("planNames", String.class);
					xstream.addImplicitCollection(Group.class, "planNames","planNames",String.class);
					xstream.alias("pendingMembers", String.class);
					xstream.addImplicitCollection(Group.class, "pendingMembers","pendingMembers",String.class);
					Group group = (Group) xstream.fromXML(response);
					if (group != null && selectedGroup.equals(group.getName())) {
						if(emailId.equals(group.getAdmin())){
							
							Intent intent = new Intent(this, GroupAdminListActivity.class);
							startActivity(intent);
						} else {
							
							Intent intent = new Intent(this, ViewMyGroupActivity.class);
							startActivity(intent);
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
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		MenuItem viewProfileItem = menu.findItem(R.id.viewProfile);
		viewProfileItem.setVisible(true);
		
		MenuItem changeProfilePicItem = menu.findItem(R.id.changeProfilePic);
		changeProfilePicItem.setVisible(true);
		
		MenuItem joinGroupItem = menu.findItem(R.id.joinGroup);
		joinGroupItem.setVisible(true);
		
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
		case (R.id.joinGroup):
			Intent joinGroupIntent = new Intent(this, JoinGroupActivity.class);
            startActivity(joinGroupIntent);
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
	    Intent intent = new Intent(this, HomePlanActivity.class);
	    startActivity(intent);
	}
}
