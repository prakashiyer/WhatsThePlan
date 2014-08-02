package com.theiyer.whatstheplan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.theiyer.whatstheplan.entity.User;
import com.theiyer.whatstheplan.entity.UserList;
import com.theiyer.whatstheplan.util.WTPConstants;
import com.thoughtworks.xstream.XStream;

public class ViewGroupMembersFragment extends Fragment {

	private static final String TAG = "ViewGroupMembersActivity";
	
	private GridView memberListView;
	private MemberListNewAdapter adapter;
	private List<Map<String, byte[]>> membersList;
	private Activity activity;
	View rootView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity = this.getActivity();
		
		if(haveInternet(activity)){
			rootView = inflater.inflate(R.layout.group_member_list, container, false);
			ActionBar aBar = activity.getActionBar();
			Resources res = getResources();
			Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
			aBar.setBackgroundDrawable(actionBckGrnd);
			aBar.setTitle(" Group Members");

			SharedPreferences prefs = activity.getSharedPreferences("Prefs",
					Activity.MODE_PRIVATE);
			

			String selectedGroup = prefs.getString("selectedGroup", "");
			String searchQuery = "/fetchGroupUsers?groupName=" + selectedGroup.replace(" ", "%20");

			
			membersList = new ArrayList<Map<String, byte[]>>();
			memberListView = (GridView) rootView.findViewById(R.id.viewgroupMemberList);
			adapter = new MemberListNewAdapter(activity);
			
			WebServiceClient restClient = new WebServiceClient(activity);
			restClient.execute(new String[] { searchQuery });
		} else {
			Intent intent = new Intent(activity, RetryActivity.class);
			startActivity(intent);
		}
		return rootView;
	}
	
	private class WebServiceClient extends AsyncTask<String, Integer, String> {

		private Context mContext;
		private ProgressDialog pDlg;

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
			String path = WTPConstants.SERVICE_PATH+params[0];

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
				Log.i(TAG, response);
				XStream userXstream = new XStream();
				userXstream.alias("UserList", UserList.class);
				userXstream.addImplicitCollection(UserList.class, "users");
				userXstream.alias("users", User.class);
				userXstream.alias("groupNames", String.class);
				userXstream.addImplicitCollection(User.class, "groupNames",
						"groupNames", String.class);
				userXstream.alias("pendingGroupNames", String.class);
				userXstream.addImplicitCollection(User.class,
						"pendingGroupNames", "pendingGroupNames", String.class);
				UserList userList = (UserList) userXstream.fromXML(response);
				if (userList != null) {
					
					List<User> users = userList.getUsers();
					Log.i(TAG, "Got User list " +users.size());
					if(users != null){
						
						for(User user: users){
							Map<String, byte[]> memberMap = new HashMap<String, byte[]>();
							memberMap.put(user.getName(), user.getImage());
							membersList.add(memberMap);
						}
						
					}
					if(!membersList.isEmpty()){
						adapter.setData(membersList);
						memberListView.setAdapter(adapter);
					}
				}           
			}
			pDlg.dismiss();
		}
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

}
