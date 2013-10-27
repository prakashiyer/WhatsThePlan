package com.theiyer.whatstheplan;

import java.util.concurrent.ExecutionException;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.theiyer.whatstheplan.entity.Group;
import com.thoughtworks.xstream.XStream;

public class LeaveGroupActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.leave_group);
		ActionBar aBar = getActionBar();
		Resources res = getResources();
		Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
		aBar.setBackgroundDrawable(actionBckGrnd);
		aBar.setTitle(" Group Exit Form");

		SharedPreferences prefs = getSharedPreferences("Prefs",
				Activity.MODE_PRIVATE);
		String userName = prefs.getString("userName", "New User");
		String selectedGroup = prefs.getString("selectedGroup", "New User");
		TextView welcomeStmnt = (TextView) findViewById(R.id.welcomeLeaveGroupLabel);
		welcomeStmnt.setText(userName + ", You can exit this group.");

		TextView groupNameValue = (TextView) findViewById(R.id.leaveGroupName);
		groupNameValue.setText("Group Name: " + selectedGroup);


	}


	/** Called when the user clicks the leave group button */
	public void leaveGroup(View view) {
		Button button = (Button) findViewById(R.id.leaveGroupButton);
		button.setTextColor(getResources().getColor(R.color.click_button_2));
		SharedPreferences prefs = getSharedPreferences("Prefs",
				Activity.MODE_PRIVATE);
		String phone = prefs.getString("phone", "");
		String selectedGroup = prefs.getString("selectedGroup", "New User");
		String searchQuery = "/leaveGroup?phone=" + phone
				+ "&groupName=" + selectedGroup.replace(" ", "%20");

		TextView errorFieldValue = (TextView) findViewById(R.id.leaveGroupErrorField);
		RestWebServiceClient restClient = new RestWebServiceClient(this);
		try {
			String response = restClient.execute(
					new String[] { searchQuery }).get();

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
					Intent intent = new Intent(this, HomePlanActivity.class);
				     startActivity(intent);
				} else {
					errorFieldValue
							.setText("Apologies for any inconvenience caused. There is a problem with the service!");
				}
			} else {
				errorFieldValue
						.setText("Apologies for any inconvenience caused. There is a problem with the service!");
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
