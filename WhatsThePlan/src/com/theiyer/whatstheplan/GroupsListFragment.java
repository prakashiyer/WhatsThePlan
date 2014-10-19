package com.theiyer.whatstheplan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import com.theiyer.whatstheplan.entity.Center;
import com.theiyer.whatstheplan.entity.CenterList;
import com.theiyer.whatstheplan.util.WTPConstants;
import com.thoughtworks.xstream.XStream;

public class GroupsListFragment extends Fragment implements OnItemClickListener {
	
	Activity activity;
	
	//ListView list;
	GridView gridView;
	GroupListAdapter adapter;
	List<Map<String, Center>> centersList;
	List<Center> allCenters;
	String phone;
	View rootView;
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity = this.getActivity();
		if(activity != null && haveInternet(activity)){
			ActionBar aBar = activity.getActionBar();
			Resources res = getResources();
			Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
			aBar.setBackgroundDrawable(actionBckGrnd);
			aBar.setTitle(" My Groups");

			SharedPreferences prefs = activity.getSharedPreferences("Prefs",
					Activity.MODE_PRIVATE);
			String userName = prefs.getString("userName", "New User");
			rootView = inflater.inflate(R.layout.groups_list, container, false);
			TextView userNameValue = (TextView)rootView.findViewById(R.id.welcomeListGroupsLabel);
			userNameValue.setText(userName + ", View all the groups here!");
			
			adapter = new GroupListAdapter(activity);
			gridView = (GridView) rootView.findViewById(R.id.groupList);
			gridView.setOnItemClickListener(this);

			phone = prefs.getString("phone", "");

			String searchQuery = "/fetchUserCenters?phone="
					+ phone;
			
			WebServiceClient restClient = new WebServiceClient(activity);
			restClient.execute(new String[] { searchQuery });
		} else {
			Intent intent = new Intent(activity, RetryActivity.class);
			startActivity(intent);
		}
        return rootView;
	}
	
	/** Called when the user clicks the Create group button */
	public void joinGroups(View view) {
		Button button = (Button) activity.findViewById(R.id.joinGroupBtn);
		button.setTextColor(getResources().getColor(R.color.click_button_1));
		Intent intent = new Intent(activity, JoinGroupActivity.class);
		
		startActivity(intent);
	}
	

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		SharedPreferences prefs = activity.getSharedPreferences(
				"Prefs", Activity.MODE_PRIVATE);
		String selectedCenterPhone = "";
		if(centersList != null && !centersList.isEmpty()){
			Map<String, Center> selectedMap = centersList.get(position);
			for(Entry<String, Center> entry: selectedMap.entrySet()){
				SharedPreferences.Editor editor = prefs.edit();
				selectedCenterPhone = entry.getValue().getAdminPhone();
				editor.putString("selectedCenterPhone",selectedCenterPhone);
				editor.putString("selectedCenterName",entry.getValue().getName());
				editor.putString("selectedCenterId",String.valueOf(entry.getValue().getId()));
				editor.apply();
				break;
			}
			
			Intent intent = new Intent(activity, ViewCenterUpcomingPlansActivity.class);
			startActivity(intent);
		}
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Inflate the menu; this adds items to the action bar if it is present.
		activity.getMenuInflater().inflate(R.menu.main, menu);
		MenuItem viewProfileItem = menu.findItem(R.id.viewProfile);
		viewProfileItem.setVisible(true);
		
		MenuItem changeProfilePicItem = menu.findItem(R.id.changeProfilePic);
		changeProfilePicItem.setVisible(true);
		
		MenuItem deactivateAccountItem = menu.findItem(R.id.deactivateAccount);
		deactivateAccountItem.setVisible(true);	
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case (R.id.viewProfile):
			Intent viewProfileIntent = new Intent(activity, ViewProfileActivity.class);
            startActivity(viewProfileIntent);
			return true;
		case (R.id.changeProfilePic):
			Intent changeProfilePicIntent = new Intent(activity, ProfileImageUploadActivity.class);
            startActivity(changeProfilePicIntent);
			return true;
		case (R.id.deactivateAccount):
			Intent deactivateAccountIntent = new Intent(activity, DeactivateAccountActivity.class);
            startActivity(deactivateAccountIntent);
			return true;
		case (R.id.aboutUs):
			Intent aboutUsIntent = new Intent(activity, AboutUsActivity.class);
            startActivity(aboutUsIntent);
			return true;
		default:
			return false;
		}
	}
	
	private class WebServiceClient extends AsyncTask<String, Integer, String> {

		private Context mContext;
		private ProgressDialog pDlg;
		private String query;

		public WebServiceClient(Context mContext) {
			this.mContext = mContext;
		}

		private void showProgressDialog() {

			pDlg = new ProgressDialog(mContext);
			pDlg.setMessage("Processing ....");
			pDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			pDlg.setCancelable(false);
			pDlg.show();

		}

		@Override
		protected void onPreExecute() {
			
		   showProgressDialog();

		}

		@Override
		protected String doInBackground(String... params) {
			query = params[0];
			String path = WTPConstants.SERVICE_PATH+query;
			
			
			//HttpHost target = new HttpHost(TARGET_HOST);
			HttpHost target = new HttpHost(WTPConstants.TARGET_HOST, 8080);
			HttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet(path);
			HttpEntity results = null;

			try {
				HttpResponse response = client.execute(target, get);
				results = response.getEntity(); 
				String result = EntityUtils.toString(results);
				return result;
			} catch (Exception e) {
				
			}
			return null;
		}

		@Override
		protected void onPostExecute(String response) {
			
			if (response != null) {
				System.out.println("RESPONSE: "+response);
				XStream userXs = new XStream();
			    userXs.alias("CenterList", CenterList.class);
				userXs.addImplicitCollection(CenterList.class, "centers");
			    userXs.alias("centers", Center.class);
				userXs.alias("members", String.class);
				userXs.addImplicitCollection(Center.class, "members",
						"members", String.class);
				CenterList centerList = (CenterList) userXs.fromXML(response);
				if (centerList != null) {
					centersList = new ArrayList<Map<String, Center>>();
					allCenters = centerList.getCenters();
					if(allCenters != null && !allCenters.isEmpty()){
						for(Center center: allCenters){
							Map<String, Center> groupDetails = new HashMap<String, Center>();
							groupDetails.put(String.valueOf(center.getId()), center);
							centersList.add(groupDetails);
						}
						if(!centersList.isEmpty()){
							adapter.setData(centersList);
							gridView.setAdapter(adapter);
						}
					}					
				} else {
					gridView.setVisibility(ListView.INVISIBLE);
					TextView userNameValue = (TextView)rootView.findViewById(R.id.welcomeListGroupsLabel);
					userNameValue.setText("You haven't joined any groups");
					activity.setContentView(R.layout.groups_list);
					
				}
			}
			pDlg.dismiss();
		}	
			
	}
		
	
	
	public void onBackPressed() {
	    Intent intent = new Intent(activity, MainActivity.class);
	    startActivity(intent);
	}
	
	/**
	 * Checks if we have a valid Internet Connection on the device.
	 * 
	 * @param ctx
	 * @return True if device has internet
	 * 
	 *         Code from: http://www.androidsnippets.org/snippets/131/
	 */
	public static boolean haveInternet(Context ctx) {

		NetworkInfo info = (NetworkInfo) ((ConnectivityManager) ctx
				.getSystemService(Context.CONNECTIVITY_SERVICE))
				.getActiveNetworkInfo();

		if (info == null || !info.isConnected()) {
			return false;
		}

		return true;
	}
	
	public void setActivity(Activity activity) {
		this.activity = activity;
	}
}
