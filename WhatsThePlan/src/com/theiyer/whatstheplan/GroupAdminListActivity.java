package com.theiyer.whatstheplan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.theiyer.whatstheplan.entity.Group;
import com.theiyer.whatstheplan.entity.UserInformation;
import com.thoughtworks.xstream.XStream;

public class GroupAdminListActivity extends Activity implements OnItemClickListener {

	ListView list;
	MemberListAdapter adapter;
	List<Map<String, byte[]>> membersList;
	Map<String, String> userMap;
	private Context context = this;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.group_admin_list);
        ActionBar aBar = getActionBar();
		Resources res = getResources();
		Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
		aBar.setBackgroundDrawable(actionBckGrnd);
		aBar.setTitle(" Group Admin To-Do List");
        
        SharedPreferences prefs = getSharedPreferences("Prefs", Activity.MODE_PRIVATE);
        String userName = prefs.getString("userName", "New User");
        TextView userNameValue = (TextView)findViewById(R.id.welcomeGroupAdminLabel);
        userNameValue.setText(userName+", new member requests are listed below:");
        
        String selectedGroup = prefs.getString("selectedGroup", "");
        
        TextView selectedGroupValue = (TextView)findViewById(R.id.selectedGroupAdminValue);
        selectedGroupValue.setText(" "+selectedGroup);
        
        String searchQuery = "/searchGroup?groupName=" + selectedGroup.replace(" ", "%20");

        ImageRetrieveRestWebServiceClient imageClient = new ImageRetrieveRestWebServiceClient(
				this);
		RestWebServiceClient restClient = new RestWebServiceClient(this);
		TextView errorFieldValue = (TextView) findViewById(R.id.viewGroupAdminErrorField);
		errorFieldValue.setText("");
		try {
			byte[] image = imageClient.execute(
					new String[] { "fetchGroupImage", selectedGroup.replace(" ", "%20") })
					.get();
			Bitmap img = BitmapFactory.decodeByteArray(image, 0,
					image.length);

			ImageView imgView = (ImageView) findViewById(R.id.selectedGroupAdminPicThumbnail);
			imgView.setImageBitmap(img);
			
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

					List<String> pendingMembers = group.getPendingMembers();
					
					if(pendingMembers != null && !pendingMembers.isEmpty()){
						
						membersList = new ArrayList<Map<String, byte[]>>();
						userMap = new HashMap<String, String>();
						for(String emailId: pendingMembers){
							String userQuery = "/fetchUserInformation?emailId=" + emailId;
							RestWebServiceClient userRestClient = new RestWebServiceClient(this);
							String userResp = userRestClient.execute(new String[] { userQuery })
									.get();
							
							if(userResp != null){
								XStream userXs = new XStream();
								userXs.alias("UserInformation", UserInformation.class);
								userXs.alias("groupNames", String.class);
								userXs.addImplicitCollection(UserInformation.class, "groupNames","groupNames",String.class);
								userXs.alias("pendingGroupNames", String.class);
								userXs.addImplicitCollection(UserInformation.class, "pendingGroupNames","pendingGroupNames",String.class);
								UserInformation user = (UserInformation) userXs
										.fromXML(userResp);
								if(user != null){
									ImageRetrieveRestWebServiceClient userImageClient = new ImageRetrieveRestWebServiceClient(
											this);
									byte[] userImage = userImageClient.execute(new String[] { "fetchUserImage", emailId }).get();
									Map<String, byte[]> memberMap = new HashMap<String, byte[]>();
									memberMap.put(user.getName(), userImage);
									userMap.put(user.getName(), user.getPhone());
									membersList.add(memberMap);
									
								}
								
							}
						}
						
						if(!membersList.isEmpty()){
							list = (ListView) findViewById(R.id.memberListAdmin);

							// Getting adapter by passing xml data ArrayList
							adapter = new MemberListAdapter(this, membersList);
							list.setAdapter(adapter);
							list.setVisibility(ListView.VISIBLE);
							// Click event for single list row
							list.setOnItemClickListener(this);
						}
						
					} else {
						
						Intent intent = new Intent(this, ViewMyGroupActivity.class);
						startActivity(intent);
					}
				
				} else {					
					errorFieldValue
							.setText("You have no groups!");
				}
			} else {
				errorFieldValue.setText("Group information fetch failed!");
			}
		} catch (InterruptedException e) {
			
			errorFieldValue
					.setText("Apologies for any inconvenience caused. There is a problem with the service!");
		} catch (ExecutionException e) {
			
			errorFieldValue
					.setText("Apologies for any inconvenience caused. There is a problem with the service!");
		}
        
    }
    
    @Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    	
		String selectedMember = "";
		String selectedMemberEmailId = "";
		if(membersList != null && !membersList.isEmpty()){
			Map<String,byte[]> selectedMap = membersList.get(position);
			
			for(Entry<String,byte[]> entry: selectedMap.entrySet()){
				SharedPreferences prefs = getSharedPreferences(
						"Prefs", Activity.MODE_PRIVATE);
				SharedPreferences.Editor editor = prefs.edit();
				selectedMember = entry.getKey();
				editor.putString("selectedMember",selectedMember);
				selectedMemberEmailId = userMap.get(selectedMember);
				editor.putString("selectedMemberEmailId", selectedMemberEmailId);
				editor.apply();
				break;
			}
			
			AlertDialog.Builder ad = new AlertDialog.Builder(context);
		    ad.setTitle("Membership Request");
		    ad.setMessage("Do you Accept or reject this request?");
		    ad.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
					SharedPreferences prefs = getSharedPreferences("Prefs",
							Activity.MODE_PRIVATE);
					String selectedMemberEmailId = prefs.getString("selectedMemberEmailId",
							"New User");

					String selectedGroup = prefs.getString("selectedGroup", "");					
					
					RestWebServiceClient restClient = new RestWebServiceClient(context);
					String searchQuery = "/setAdminDecisionForUser?emailId="
							+ selectedMemberEmailId + "&groupName="
							+ selectedGroup.replace(" ", "%20") + "&decision=yes";
					TextView errorFieldValue = (TextView) findViewById(R.id.viewGroupAdminErrorField);
					try {
						restClient.execute(new String[] { searchQuery }).get();

						Intent intent = new Intent(context, GroupAdminListActivity.class);
						startActivity(intent);
					} catch (InterruptedException e) {
						
						
						errorFieldValue
								.setText("Apologies for any inconvenience caused. There is a problem with the service!");
					} catch (ExecutionException e) {
						
						errorFieldValue
								.setText("Apologies for any inconvenience caused. There is a problem with the service!");
					}
				}
			}); 
		    ad.setNegativeButton("Reject", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					SharedPreferences prefs = getSharedPreferences("Prefs",
							Activity.MODE_PRIVATE);
					String selectedMemberEmailId = prefs.getString("selectedMemberEmailId",
							"New User");

					String selectedGroup = prefs.getString("selectedGroup", "");					
					
					RestWebServiceClient restClient = new RestWebServiceClient(context);
					String searchQuery = "/setAdminDecisionForUser?emailId="
							+ selectedMemberEmailId + "&groupName="
							+ selectedGroup.replace(" ", "%20") + "&decision=no";
					TextView errorFieldValue = (TextView) findViewById(R.id.viewGroupAdminErrorField);
					try {
						restClient.execute(new String[] { searchQuery }).get();

						Intent intent = new Intent(context, GroupAdminListActivity.class);
						startActivity(intent);
					} catch (InterruptedException e) {
						
						
						errorFieldValue
								.setText("Apologies for any inconvenience caused. There is a problem with the service!");
					} catch (ExecutionException e) {
						
						errorFieldValue
								.setText("Apologies for any inconvenience caused. There is a problem with the service!");
					}
				}
			}); 
		    ad.show();
		}
    }
	
	
	/** Called when the user clicks the Proceed to View Groups button */
	public void proceedAdminToViewGroups(View view) {
		Intent intent = new Intent(this, ViewMyGroupActivity.class);
		startActivity(intent);
	}

	
	

}
