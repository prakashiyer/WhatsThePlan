package com.theiyer.whatstheplan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ListView;

import com.theiyer.whatstheplan.entity.Group;
import com.theiyer.whatstheplan.entity.User;
import com.thoughtworks.xstream.XStream;

public class ViewGroupMembersActivity extends Activity {

	
	ListView memberListView;
	MemberListAdapter adapter;
	private ProgressDialog pDlg;
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
			restClient.execute(new String[] { searchQuery });
		    
			String response = restClient.get();
			
			pDlg = new ProgressDialog(this);
			pDlg.setMessage("Processing ....");
			pDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			pDlg.setCancelable(false);
			pDlg.show();

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
					

					List<String> members = group.getMembers();
					
					if(members != null && !members.isEmpty()){
						List<Map<String, byte[]>> membersList = new ArrayList<Map<String, byte[]>>();
						for(String phone: members){
							String userQuery = "/fetchUser?phone=" + phone;
							RestWebServiceClient userRestClient = new RestWebServiceClient(this);
							String userResp = userRestClient.execute(new String[] { userQuery })
									.get();
							
							if(userResp != null){
								XStream userXstream = new XStream();
								userXstream.alias("UserInformation", User.class);
								userXstream.alias("groupNames", String.class);
								userXstream.addImplicitCollection(User.class, "groupNames","groupNames",String.class);
								userXstream.alias("pendingGroupNames", String.class);
								userXstream.addImplicitCollection(User.class, "pendingGroupNames","pendingGroupNames",String.class);
								User user = (User) userXstream
										.fromXML(userResp);
								if(user != null){
									ImageRetrieveRestWebServiceClient userImageClient = new ImageRetrieveRestWebServiceClient(
											this);
									userImageClient.execute(new String[] { "fetchUserImage", phone });
									byte[] userImage = userImageClient.get();
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

						pDlg.dismiss();
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
