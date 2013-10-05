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
import android.widget.ListView;

import com.theiyer.whatstheplan.entity.Group;
import com.theiyer.whatstheplan.entity.UserInformation;
import com.thoughtworks.xstream.XStream;

public class ViewGroupMembersActivity extends Activity {

	ListView memberListView;
	MemberListAdapter adapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.group_member_list);
		ActionBar aBar = getActionBar();
		Resources res = getResources();
		Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
		aBar.setBackgroundDrawable(actionBckGrnd);
		aBar.setTitle(" Group Members");

		SharedPreferences prefs = getSharedPreferences("Prefs",
				Activity.MODE_PRIVATE);
		

		String selectedGroup = prefs.getString("selectedGroup", "");
		String searchQuery = "/searchGroup?groupName=" + selectedGroup.replace(" ", "%20");

		RestWebServiceClient restClient = new RestWebServiceClient(this);
		try {
			String response = restClient.execute(new String[] { searchQuery })
					.get();

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
                if (group != null && selectedGroup.equals(group.getName())) {
					

					List<String> memberEmailIds = group.getMemberEmailIds();
					
					if(memberEmailIds != null && !memberEmailIds.isEmpty()){
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
							memberListView = (ListView) findViewById(R.id.viewgroupMemberList);

							adapter = new MemberListAdapter(this, membersList);
							memberListView.setAdapter(adapter);
						}

						
					}

				}
			}
		} catch (InterruptedException e) {
			
			
		} catch (ExecutionException e) {
			
			
		}
	}
	
	@Override
	public void onBackPressed() {
	    Intent intent = new Intent(this, ViewMyGroupActivity.class);
	    startActivity(intent);
	}

}
