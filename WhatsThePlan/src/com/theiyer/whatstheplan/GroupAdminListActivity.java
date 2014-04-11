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
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.theiyer.whatstheplan.entity.Group;
import com.theiyer.whatstheplan.entity.User;
import com.theiyer.whatstheplan.util.WTPConstants;
import com.thoughtworks.xstream.XStream;

public class GroupAdminListActivity extends Activity implements
		OnItemClickListener {

	ListView list;
	MemberListAdapter adapter;
	List<Map<String, byte[]>> membersList;
	Map<String, String> userMap;
	private Context context = this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        if(haveInternet(this)){
        	setContentView(R.layout.group_admin_list);
    		ActionBar aBar = getActionBar();
    		Resources res = getResources();
    		Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
    		aBar.setBackgroundDrawable(actionBckGrnd);
    		aBar.setTitle(" Group Admin To-Do List");

    		SharedPreferences prefs = getSharedPreferences("Prefs",
    				Activity.MODE_PRIVATE);
    		String userName = prefs.getString("userName", "New User");
    		TextView userNameValue = (TextView) findViewById(R.id.welcomeGroupAdminLabel);
    		userNameValue.setText(userName
    				+ ", new member requests are listed below:");

    		String selectedGroup = prefs.getString("selectedGroup", "");

    		TextView selectedGroupValue = (TextView) findViewById(R.id.selectedGroupAdminValue);
    		selectedGroupValue.setText(" " + selectedGroup);

    		String searchQuery = "/searchGroup?groupName="
    				+ selectedGroup.replace(" ", "%20");

    		WebImageRetrieveRestWebServiceClient imageClient = new WebImageRetrieveRestWebServiceClient(
    				this);
    		WebServiceClient restClient = new WebServiceClient(this);
    		membersList = new ArrayList<Map<String, byte[]>>();
    		userMap = new HashMap<String, String>();
    		list = (ListView) findViewById(R.id.memberListAdmin);
    		// Click event for single list row
    		list.setOnItemClickListener(this);
    		adapter = new MemberListAdapter(this);
    		imageClient.execute(new String[] { "fetchGroupImage",
    				selectedGroup.replace(" ", "%20") });

    		restClient.execute(new String[] { searchQuery });
		} else {
			Intent intent = new Intent(this, RetryActivity.class);
			startActivity(intent);
		}
		

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {

		String selectedMember = "";
		String selectedMemberPhone = "";
		if (membersList != null && !membersList.isEmpty()) {
			Map<String, byte[]> selectedMap = membersList.get(position);

			for (Entry<String, byte[]> entry : selectedMap.entrySet()) {
				SharedPreferences prefs = getSharedPreferences("Prefs",
						Activity.MODE_PRIVATE);
				SharedPreferences.Editor editor = prefs.edit();
				selectedMember = entry.getKey();
				editor.putString("selectedMember", selectedMember);
				selectedMemberPhone = userMap.get(selectedMember);
				editor.putString("selectedMemberPhone", selectedMemberPhone);
				editor.apply();
				break;
			}

			AlertDialog.Builder ad = new AlertDialog.Builder(context);
			ad.setTitle("Membership Request");
			ad.setMessage("Do you Accept or reject this request?");
			ad.setPositiveButton("Accept",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {

							SharedPreferences prefs = getSharedPreferences(
									"Prefs", Activity.MODE_PRIVATE);
							String phone = prefs.getString(
									"selectedMemberPhone", "New User");

							String selectedGroup = prefs.getString(
									"selectedGroup", "");

							WebServiceClient restClient = new WebServiceClient(
									context);
							String searchQuery = "/setAdminDecisionForUser?phone="
									+ phone
									+ "&groupName="
									+ selectedGroup.replace(" ", "%20")
									+ "&decision=yes";
							restClient.execute(new String[] { searchQuery });

							Intent intent = new Intent(context,
									GroupAdminListActivity.class);
							startActivity(intent);

						}
					});
			ad.setNegativeButton("Reject",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							SharedPreferences prefs = getSharedPreferences(
									"Prefs", Activity.MODE_PRIVATE);
							String phone = prefs.getString(
									"selectedMemberPhone", "New User");

							String selectedGroup = prefs.getString(
									"selectedGroup", "");

							WebServiceClient restClient = new WebServiceClient(
									context);
							String searchQuery = "/setAdminDecisionForUser?phone="
									+ phone
									+ "&groupName="
									+ selectedGroup.replace(" ", "%20")
									+ "&decision=no";
							restClient.execute(new String[] { searchQuery });

							Intent intent = new Intent(context,
									GroupAdminListActivity.class);
							startActivity(intent);
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

	public class WebImageRetrieveRestWebServiceClient extends
			AsyncTask<String, Integer, byte[]> {

		private Context mContext;
		private ProgressDialog pDlg;
		private boolean isFetchUserImage = false;
		private String userName;
		private String isLastMember;

		public WebImageRetrieveRestWebServiceClient(Context mContext) {
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
		protected byte[] doInBackground(String... params) {
			String method = params[0];
			String path = WTPConstants.SERVICE_PATH + "/" + method;

			if ("fetchUserImage".equals(method)) {
				path = path + "?phone=" + params[1];
				isFetchUserImage = true;
				userName = params[2];
				isLastMember = params[3];
			} else {
				path = path + "?groupName=" + params[1];
			}
			// HttpHost target = new HttpHost(TARGET_HOST);
			HttpHost target = new HttpHost(WTPConstants.TARGET_HOST, 8080);
			HttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet(path);
			HttpEntity results = null;

			try {

				HttpResponse response = client.execute(target, get);
				results = response.getEntity();
				byte[] byteresult = EntityUtils.toByteArray(results);
				return byteresult;
			} catch (Exception e) {
			}
			return null;
		}

		@Override
		protected void onPostExecute(byte[] response) {
			if (response != null && !isFetchUserImage) {
				Bitmap img = BitmapFactory.decodeByteArray(response, 0,
						response.length);

				ImageView imgView = (ImageView) findViewById(R.id.selectedGroupAdminPicThumbnail);
				imgView.setImageBitmap(img);
			}

			if (response != null && isFetchUserImage) {
				Map<String, byte[]> memberMap = new HashMap<String, byte[]>();
				memberMap.put(userName, response);
				membersList.add(memberMap);
				if (isLastMember.equals("true")) {
					if (!membersList.isEmpty()) {
						// Getting adapter by passing xml data ArrayList
						adapter.setData(membersList);
						list.setAdapter(adapter);
						list.setVisibility(ListView.VISIBLE);

					}
				}
			}

			pDlg.dismiss();
		}

	}

	public class WebServiceClient extends AsyncTask<String, Integer, String> {

		private Context mContext;
		private ProgressDialog pDlg;
		private String isLastMember = "false";
		private String query = "";

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
			String path = WTPConstants.SERVICE_PATH + query;

			if (params.length == 2) {
				isLastMember = params[1];
			}
			// HttpHost target = new HttpHost(TARGET_HOST);
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
			if (response != null && query.contains("searchGroup")) {
				XStream xstream = new XStream();
				xstream.alias("Group", Group.class);
				xstream.alias("members", String.class);
				xstream.addImplicitCollection(Group.class, "members",
						"members", String.class);
				xstream.alias("planNames", String.class);
				xstream.addImplicitCollection(Group.class, "planNames",
						"planNames", String.class);
				xstream.alias("pendingMembers", String.class);
				xstream.addImplicitCollection(Group.class, "pendingMembers",
						"pendingMembers", String.class);
				Group group = (Group) xstream.fromXML(response);
				if (group != null) {

					List<String> pendingMembers = group.getPendingMembers();

					if (pendingMembers != null && !pendingMembers.isEmpty()) {
						String isLastMember = "false";
						int size = pendingMembers.size();
						for (int i = 0; i < size; i++) {
							String userQuery = "/fetchUser?phone="
									+ pendingMembers.get(i);
							if (i == size - 1) {
								isLastMember = "true";
							}
							WebServiceClient userRestClient = new WebServiceClient(
									mContext);
							userRestClient.execute(new String[] { userQuery,
									isLastMember });
						}
					} else {

						Intent intent = new Intent(mContext,
								ViewMyGroupActivity.class);
						startActivity(intent);
					}

				}
			}

			if (response != null && query.contains("fetchUser")) {
				XStream userXs = new XStream();
				userXs.alias("UserInformation", User.class);
				userXs.alias("groupNames", String.class);
				userXs.addImplicitCollection(User.class, "groupNames",
						"groupNames", String.class);
				userXs.alias("pendingGroupNames", String.class);
				userXs.addImplicitCollection(User.class, "pendingGroupNames",
						"pendingGroupNames", String.class);
				User user = (User) userXs.fromXML(response);
				if (user != null) {
					WebImageRetrieveRestWebServiceClient userImageClient = new WebImageRetrieveRestWebServiceClient(
							mContext);
					userImageClient.execute(new String[] { "fetchUserImage",
							user.getPhone(), user.getName(), isLastMember });
					userMap.put(user.getName(), user.getPhone());
				}

			}
			pDlg.dismiss();
		}

	}
	
	/**
	 * Checks if we have a valid Internet Connection on the device.
	 * @param ctx
	 * @return True if device has internet
	 *
	 * Code from: http://www.androidsnippets.org/snippets/131/
	 */
	public static boolean haveInternet(Context ctx) {

	    NetworkInfo info = (NetworkInfo) ((ConnectivityManager) ctx
	            .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

	    if (info == null || !info.isConnected()) {
	        return false;
	    }
	    
	    return true;
	}
}
