package com.theiyer.whatstheplan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import android.app.ActionBar;
import android.app.Activity;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.theiyer.whatstheplan.entity.Group;
import com.thoughtworks.xstream.XStream;

public class JoinGroupActivity extends Activity {

	ListView list;
	GroupListAdapter adapter;
	List<Map<String, byte[]>> groupsList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.join_group);
		ActionBar aBar = getActionBar();
		Resources res = getResources();
		Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
		aBar.setBackgroundDrawable(actionBckGrnd);
		aBar.setTitle(" Group Membership");

		SharedPreferences prefs = getSharedPreferences("Prefs",
				Activity.MODE_PRIVATE);
		String userName = prefs.getString("userName", "New User");
		TextView userNameValue = (TextView) findViewById(R.id.welcomeJoinGroupLabel);
		userNameValue.setText(userName + ", Search and join your group!");
		
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		final SearchView searchView = (SearchView) findViewById(R.id.groupSearchView);
		SearchableInfo searchableInfo = searchManager.getSearchableInfo(getComponentName());
		searchView.setSearchableInfo(searchableInfo);
		
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			
			String groupName = intent.getStringExtra(SearchManager.QUERY);
			// Search Group Names and add to a list
			String searchQuery = "/searchGroup?groupName=" + groupName.replace(" ", "%20");
			RestWebServiceClient restClient = new RestWebServiceClient(this);
			TextView groupNameLabel= (TextView) findViewById(R.id.groupSearchResultsLabel);
			TextView errorFieldValue = (TextView) findViewById(R.id.joinGroupErrorField);
			errorFieldValue.setText("");
			groupNameLabel.setVisibility(TextView.INVISIBLE);
			try {
				String response = restClient.execute(
						new String[] { searchQuery }).get();
				SharedPreferences prefs = getSharedPreferences("Prefs",
						Activity.MODE_PRIVATE);
				if (response != null) {
					XStream xstream = new XStream();
					xstream.alias("Group", Group.class);
					
					xstream.alias("memberEmailIds", String.class);
					xstream.addImplicitCollection(Group.class, "memberEmailIds","memberEmailIds",String.class);
					xstream.alias("planNames", String.class);
					xstream.addImplicitCollection(Group.class, "planNames","planNames",String.class);
					xstream.alias("pendingMembers", String.class);
					xstream.addImplicitCollection(Group.class, "pendingMembers","pendingMembers",String.class);
					Group group = (Group) xstream.fromXML(response);
					if (group != null && groupName.equals(group.getName())) {

						groupsList = new ArrayList<Map<String, byte[]>>();

						ImageRetrieveRestWebServiceClient imageClient = new ImageRetrieveRestWebServiceClient(
								this);
						Map<String, byte[]> groupDetails = new HashMap<String, byte[]>();
						byte[] image = imageClient.execute(
								new String[] { "fetchGroupImage", groupName.replace(" ", "%20") })
								.get();
						groupDetails.put(groupName, image);
						groupsList.add(groupDetails);
						
						if(!groupsList.isEmpty()){
							list = (ListView) findViewById(R.id.joingroupList);

							// Getting adapter by passing xml data ArrayList
							adapter = new GroupListAdapter(this, groupsList);
							list.setAdapter(adapter);
						}
						
						
						groupNameLabel.setVisibility(TextView.VISIBLE);
						TextView groupNameValue = (TextView) findViewById(R.id.groupSearchResultValue);
						groupNameValue.setText(groupName);

						String emailId = prefs.getString("emailId","");
						List<String> memberEmailIds = group.getMemberEmailIds();
						List<String> pendingMembers = group.getPendingMembers();
						if(pendingMembers == null || (!pendingMembers.contains(emailId) && !memberEmailIds.contains(emailId))){
							Button joinButton = (Button) findViewById(R.id.joinGroupButton);
							joinButton.setVisibility(Button.VISIBLE);
						} else {
							errorFieldValue
									.setText("You are either already a part of this group or have applied for membership!");
						}
						
					} else {
						
						errorFieldValue
								.setText("This name is not available. Enter a unique valid group name!");
					}
				} else {
					setContentView(R.layout.join_group);
					errorFieldValue
							.setText("This name is not available. Enter a unique valid group name!");
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

	/** Called when the user clicks the Join Group button */
	public void goFromJoinGroupToViewGroups(View view) {
		Button button = (Button) findViewById(R.id.joinGroupButton);
		button.setTextColor(getResources().getColor(R.color.click_button_2));
		TextView groupNameValue = (TextView) findViewById(R.id.groupSearchResultValue);
		String groupName = groupNameValue.getText().toString();
		SharedPreferences prefs = getSharedPreferences("Prefs",
				Activity.MODE_PRIVATE);
		String emailId = prefs.getString("emailId", "");

		String joinQuery = "/joinGroup?groupName=" + groupName.replace(" ", "%20")
				+ "&emailId=" + emailId;
		TextView errorFieldValue = (TextView) findViewById(R.id.joinGroupErrorField);
		RestWebServiceClient restClient = new RestWebServiceClient(this);
		try {
			String response = restClient.execute(
					new String[] { joinQuery }).get();
			XStream xstream = new XStream();
			xstream.alias("Group", Group.class);
			
			xstream.alias("memberEmailIds", String.class);
			xstream.addImplicitCollection(Group.class, "memberEmailIds","memberEmailIds",String.class);
			xstream.alias("planNames", String.class);
			xstream.addImplicitCollection(Group.class, "planNames","planNames",String.class);
			Group group = (Group) xstream.fromXML(response);
			if (group != null && groupName.equals(group.getName())) {
				Intent intent = new Intent(this, GroupsListActivity.class);
				startActivity(intent);
			} else {
				setContentView(R.layout.join_group);
				errorFieldValue
						.setText("The join request failed. Try again later!");
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
