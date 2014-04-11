package com.theiyer.whatstheplan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;

import com.theiyer.whatstheplan.entity.Group;
import com.thoughtworks.xstream.XStream;

public class InviteListActivity extends Activity implements MultiChoiceModeListener {

	ListView inviteListView;
	InviteListAdapter adapter;
	Map<String, String> contactsMap;
	List<Map<String, String>> contactList = new ArrayList<Map<String, String>>();
	List<String> selectedList = new ArrayList<String>();
	DatabaseTable db;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.invite_list);
		ActionBar aBar = getActionBar();
		Resources res = getResources();
		Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
		aBar.setBackgroundDrawable(actionBckGrnd);
		aBar.setTitle(" Add/Invite Members");
		
		
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		final SearchView searchView = (SearchView) findViewById(R.id.memberSearchView);
		SearchableInfo searchableInfo = searchManager.getSearchableInfo(getComponentName());
		searchView.setSearchableInfo(searchableInfo);
		
		
		ContactsHelper contacts = new ContactsHelper(this);
		try {
			contactsMap = contacts.execute(new String[]{""}).get();
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			
		}
		
		db =  new DatabaseTable(this, contactsMap);
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			
			
			
			String query = intent.getStringExtra(SearchManager.QUERY);
			
			String name = db.getName(query);
			Map<String, String> emailMap = new HashMap<String, String>();
			if(name != null){
				emailMap.put(query, name);
				contactList.add(emailMap);
			}
			if (contactList != null && !contactList.isEmpty()) {
				
				inviteListView = (ListView) findViewById(R.id.inviteList);
				adapter = new InviteListAdapter(this, contactList);
				inviteListView.setAdapter(adapter);
				
				// Click event for single list row
				inviteListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
				inviteListView.setMultiChoiceModeListener(this);
			}
			
			/*Cursor cursor = db.getEmailMatches(query, null);
			if(cursor!=null){
				int nameIdx = cursor
						.getColumnIndexOrThrow(DatabaseTable.COL_NAME);
				int emailIdx = cursor
						.getColumnIndexOrThrow(DatabaseTable.COL_EMAIL);
				
				// Iterate over the result Cursor.
				System.out.println("IN HERE 2");
				while (cursor.moveToNext()) {
					Map<String, String> emailMap = new HashMap<String, String>();
					String name = cursor.getString(nameIdx);
					System.out.println("IN HERE 3");
					String emailId = cursor.getString(emailIdx);
					if(emailId != null){
						emailMap.put(emailId, name);
						contactList.add(emailMap);
					}
					
				}
				System.out.println("IN HERE 4");
				// Close the Cursor.
				cursor.close();
				
				if (contactList != null && !contactList.isEmpty()) {
					System.out.println("IN HERE 5");
					inviteListView = (ListView) findViewById(R.id.inviteList);
					adapter = new InviteListAdapter(this, contactList);
					inviteListView.setAdapter(adapter);
					System.out.println("IN HERE 6");
					// Click event for single list row
					inviteListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
					inviteListView.setMultiChoiceModeListener(this);
				}
			}*/
			
		}
	}
	
	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void onDestroyActionMode(ActionMode mode) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void onItemCheckedStateChanged(ActionMode mode, int position,
			long id, boolean checked) {
		if(checked){
			Map<String,String> emailMap = contactList.get(position);
            for(Entry<String,String> entry: emailMap.entrySet()){
            	selectedList.add(entry.getKey());
            }
		} else {
			Map<String,String> emailMap = contactList.get(position);
            for(Entry<String,String> entry: emailMap.entrySet()){
            	String email = entry.getKey();
            	if(!selectedList.isEmpty() && selectedList.contains(email)){
            		selectedList.remove(entry.getKey());
            	}            	
            }
		}
	}
	
	/** Called when the user clicks the invite button */
	public void invite(View view){
		Button button = (Button) findViewById(R.id.inviteButton);
		button.setTextColor(getResources().getColor(R.color.click_button_2));
		SharedPreferences prefs = getSharedPreferences("Prefs",
				Activity.MODE_PRIVATE);
		String groupName = prefs.getString("selectedGroup", "");
		if(!selectedList.isEmpty()){
			for(String phone: selectedList){
				String joinQuery = "/invite?groupName=" + groupName.replace(" ", "%20")
						+ "&phone=" + phone;
				/*RestWebServiceClient restClient = new RestWebServiceClient(this);
				try {
					String response = restClient.execute(
							new String[] { joinQuery }).get();
					XStream xstream = new XStream();
					xstream.alias("Group", Group.class);
					
					xstream.alias("members", String.class);
					xstream.addImplicitCollection(Group.class, "members","members",String.class);
					xstream.alias("planNames", String.class);
					xstream.addImplicitCollection(Group.class, "planNames","planNames",String.class);
					Group group = (Group) xstream.fromXML(response);
					if (group != null && groupName.equals(group.getName())) {
						
						button.setTextColor(getResources().getColor(R.color.button_text));
						Intent intent = new Intent(this, GroupsListActivity.class);
						startActivity(intent);
					} else {
					}
				} catch (InterruptedException e) {
					
				} catch (ExecutionException e) {
					
				}*/
			}
		}
	}
}
