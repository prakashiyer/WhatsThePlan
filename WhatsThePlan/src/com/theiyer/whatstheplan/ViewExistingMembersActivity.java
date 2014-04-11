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
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.ListView;

import com.theiyer.whatstheplan.entity.User;
import com.theiyer.whatstheplan.entity.UserList;
import com.theiyer.whatstheplan.util.WTPConstants;
import com.thoughtworks.xstream.XStream;

public class ViewExistingMembersActivity extends Activity {

	GridView memberListView;
	MemberGridAdapter adapter;
	List<Map<String, byte[]>> membersList;
	//TextView memberListLabel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (haveInternet(this)) {
			setContentView(R.layout.existing_members_grid);
			ActionBar aBar = getActionBar();
			Resources res = getResources();
			Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
			aBar.setBackgroundDrawable(actionBckGrnd);
			aBar.setTitle(" Existing Members");

			membersList = new ArrayList<Map<String, byte[]>>();
			memberListView = (GridView) findViewById(R.id.viewexistingmemberGrid);
			adapter = new MemberGridAdapter(this);
			//memberListLabel = (TextView) findViewById(R.id.viewExistingMemberListLabel);

			Cursor phones = getContentResolver().query(
					ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
					null, null, null);
			List<String> phoneList = new ArrayList<String>();
			while (phones.moveToNext()) {
				int phoneType = phones
						.getInt(phones
								.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
				String name = phones
						.getString(phones
								.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
				String phoneNumber = phones
						.getString(
								phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
						.trim().replaceAll("[(|)|+|-|-| ]", "");
				int len = phoneNumber.length();
				if (len >= 10) {
					phoneNumber = phoneNumber.substring(len - 10);
					switch (phoneType) {
					case Phone.TYPE_MOBILE:
						phoneList.add(phoneNumber);
						break;
					case Phone.TYPE_HOME:
						phoneList.add(phoneNumber);
						break;
					case Phone.TYPE_WORK:
						phoneList.add(phoneNumber);
						break;
					case Phone.TYPE_OTHER:
						phoneList.add(phoneNumber);
						break;
					default:
						break;
					}
				}

			}
			phones.close();
			
			List<String> phoneList1 = new ArrayList<String>();
			phoneList1.add("9833599535");
			phoneList1.add("7506039891");
			
			XStream xstream = new XStream();
			xstream.alias("phoneList", List.class);
			xstream.alias("phone", String.class);
			String listXml = xstream.toXML(phoneList1);
			listXml = listXml.replaceAll("\n", "");
			listXml = listXml.replaceAll("\t", "");
			listXml = listXml.replaceAll(" ", "");
			String searchQuery = "/fetchExistingUsers?phoneList="
					+ "9833599535,7506039891";

			WebServiceClient restClient = new WebServiceClient(this);
			restClient.execute(new String[] { searchQuery });
		} else {
			Intent intent = new Intent(this, RetryActivity.class);
			startActivity(intent);
		}

	}


	@Override
	public void onBackPressed() {
		Intent intent = new Intent(this,HomePlanActivity.class);
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
				userXstream.alias("groupNames", String.class);
				userXstream.addImplicitCollection(User.class, "groupNames",
						"groupNames", String.class);
				userXstream.alias("pendingGroupNames", String.class);
				userXstream.addImplicitCollection(User.class,
						"pendingGroupNames", "pendingGroupNames", String.class);
				UserList userList = (UserList) userXstream.fromXML(response);
				if (userList != null) {
					
					List<User> users = userList.getUsers();
					if(users != null && !users.isEmpty()){
						for(User user: users){
							Map<String, byte[]> memberMap = new HashMap<String, byte[]>();
							memberMap.put(user.getName(), user.getImage());
							membersList.add(memberMap);
							
						}
						
						if (!membersList.isEmpty()) {

							adapter.setData(membersList);
							memberListView.setAdapter(adapter);
							//memberListLabel.setVisibility(TextView.VISIBLE);
							memberListView.setVisibility(GridView.VISIBLE);
						}
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
