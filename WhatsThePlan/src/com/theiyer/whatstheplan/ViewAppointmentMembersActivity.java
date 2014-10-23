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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.theiyer.whatstheplan.entity.User;
import com.theiyer.whatstheplan.entity.UserList;
import com.theiyer.whatstheplan.util.WTPConstants;
import com.thoughtworks.xstream.XStream;

public class ViewAppointmentMembersActivity extends Activity implements
OnItemClickListener {

	GridView membersGridView;
	MemberGridAdapter adapter;
	List<Map<String, User>> membersList;
	Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (haveInternet(this)) {
			setContentView(R.layout.members_attending_grid);
			ActionBar aBar = getActionBar();
			Resources res = getResources();
			Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
			aBar.setBackgroundDrawable(actionBckGrnd);
			aBar.setTitle(" Members Attending");

			membersList = new ArrayList<Map<String, User>>();
			membersGridView = (GridView) findViewById(R.id.viewattendingmemberGrid);
			adapter = new MemberGridAdapter(this);
			context = this;
			membersGridView.setOnItemClickListener(this);
			SharedPreferences prefs = getSharedPreferences("Prefs",
					Activity.MODE_PRIVATE);
			String selectedPlanIndex = prefs.getString("selectedPlanIndex", "");
			String searchQuery = "/fetchPlanUsers?id="
					+selectedPlanIndex;

			WebServiceClient restClient = new WebServiceClient(this);
			restClient.execute(new String[] { searchQuery });
		} else {
			Intent intent = new Intent(this, RetryActivity.class);
			startActivity(intent);
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (membersList != null && !membersList.isEmpty()) {
			Map<String, User> selectedMap = membersList.get(position);

			for (Entry<String, User> entry : selectedMap.entrySet()) {
				SharedPreferences prefs = getSharedPreferences("Prefs",
						Activity.MODE_PRIVATE);
				SharedPreferences.Editor editor = prefs.edit();
				String selectedMember = entry.getKey();
				editor.putString("memberPhone", selectedMember);
				editor.apply();
				break;
			}
			
			Intent intent = new Intent(this, ViewMemberProfileActivity.class);
			startActivity(intent);
		}
	}
	
	
	@Override
	public void onBackPressed() {
		Intent intent = new Intent(this,HomePlanGroupFragmentActivity.class);
		startActivity(intent);
	}

	public class WebServiceClient extends AsyncTask<String, Integer, String> {

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
			
			String path = WTPConstants.SERVICE_PATH + params[0];

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
			

			if (response != null) {
				System.out.println("RESPONSE: "+response);
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
					if(users != null && !users.isEmpty()){
						for(User user: users){
							Map<String, User> memberMap = new HashMap<String, User>();
							memberMap.put(user.getPhone(), user);
							membersList.add(memberMap);
							
						}
						
						if (!membersList.isEmpty()) {
							adapter.setData(membersList);
							membersGridView.setAdapter(adapter);
							//memberListLabel.setVisibility(TextView.VISIBLE);
							membersGridView.setVisibility(GridView.VISIBLE);
							
							
						} else {
							membersGridView.setVisibility(ListView.INVISIBLE);
							TextView label =(TextView) findViewById(R.id.viewMembersAttendingListLabel);
							label.setText("No members found for this center.");
						}
					} else {
						membersGridView.setVisibility(ListView.INVISIBLE);
						TextView label =(TextView) findViewById(R.id.viewMembersAttendingListLabel);
						label.setText("No members are attending this appointment.");
					}
					

				} else {
					membersGridView.setVisibility(ListView.INVISIBLE);
					TextView label =(TextView) findViewById(R.id.viewMembersAttendingListLabel);
					label.setText("No members are attending this appointment.");
				}
			} else {
				membersGridView.setVisibility(ListView.INVISIBLE);
				TextView label =(TextView) findViewById(R.id.viewMembersAttendingListLabel);
				label.setText("No members are attending this appointment.");
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
