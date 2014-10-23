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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.theiyer.whatstheplan.entity.User;
import com.theiyer.whatstheplan.entity.UserList;
import com.theiyer.whatstheplan.util.WTPConstants;
import com.thoughtworks.xstream.XStream;

public class ViewGroupMembersFragment extends Fragment implements
OnItemClickListener {

	private static final String TAG = "ViewGroupMembersActivity";
	
	private GridView memberListView;
	MemberGridAdapter adapter;
	List<Map<String, User>> membersList;
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
			aBar.setTitle(" Center Members");

			SharedPreferences prefs = activity.getSharedPreferences("Prefs",
					Activity.MODE_PRIVATE);
			

			String phone = prefs.getString("phone", "");
			String searchQuery = "/fetchCenterUsers?phone=" + phone;

			
			membersList = new ArrayList<Map<String, User>>();
			memberListView = (GridView) rootView.findViewById(R.id.viewgroupMemberList);
			adapter = new MemberGridAdapter(activity);
			memberListView.setOnItemClickListener(this);
			WebServiceClient restClient = new WebServiceClient(activity);
			restClient.execute(new String[] { searchQuery });
		} else {
			Intent intent = new Intent(activity, RetryActivity.class);
			startActivity(intent);
		}
		return rootView;
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (membersList != null && !membersList.isEmpty()) {
			Map<String, User> selectedMap = membersList.get(position);

			for (Entry<String, User> entry : selectedMap.entrySet()) {
				SharedPreferences prefs = activity.getSharedPreferences("Prefs",
						Activity.MODE_PRIVATE);
				SharedPreferences.Editor editor = prefs.edit();
				String selectedMember = entry.getKey();
				editor.putString("memberPhone", selectedMember);
				editor.apply();
				break;
			}
			
			Intent intent = new Intent(activity, ViewMemberProfileActivity.class);
			startActivity(intent);
		}
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
				userXstream.alias("centers", String.class);
				userXstream.addImplicitCollection(User.class, "centers",
						"centers", String.class);
				UserList userList = (UserList) userXstream.fromXML(response);
				if (userList != null) {
					
					List<User> users = userList.getUsers();
					
					if(users != null){
						Log.i(TAG, "Got User list " +users.size());
						for(User user: users){
							Map<String, User> memberMap = new HashMap<String, User>();
							memberMap.put(user.getPhone(), user);
							membersList.add(memberMap);
						}
						
					}
					if(!membersList.isEmpty()){
						adapter.setData(membersList);
						memberListView.setAdapter(adapter);
					} else {
						memberListView.setVisibility(ListView.INVISIBLE);
						TextView planLabel = (TextView) rootView.findViewById(R.id.viewGroupMemberListLabel);
						planLabel.setText("No members found for this center.");
					}
				} else {
					memberListView.setVisibility(ListView.INVISIBLE);
					TextView planLabel = (TextView) rootView.findViewById(R.id.viewGroupMemberListLabel);
					planLabel.setText("No members found for this center.");
				}          
			} else {
				memberListView.setVisibility(ListView.INVISIBLE);
				TextView planLabel = (TextView) rootView.findViewById(R.id.viewGroupMemberListLabel);
				planLabel.setText("No members found for this center.");
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
	
	public void setActivity(Activity activity) {
		this.activity = activity;
	}

}
