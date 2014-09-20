package com.theiyer.whatstheplan;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.theiyer.whatstheplan.ViewGroupMembersActivity.WebImageRetrieveRestWebServiceClient;
import com.theiyer.whatstheplan.entity.Center;
import com.theiyer.whatstheplan.entity.CenterList;
import com.theiyer.whatstheplan.entity.Plan;
import com.theiyer.whatstheplan.entity.PlanList;
import com.theiyer.whatstheplan.entity.User;
import com.theiyer.whatstheplan.entity.UserList;
import com.theiyer.whatstheplan.util.WTPConstants;
import com.thoughtworks.xstream.XStream;

public class MainActivity extends Activity {

	private static final String TAG = "MainActivity";

	private static int SPLASH_TIME_OUT = 3000;
	private Context context;
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setTheme(R.style.AppTheme);

		setContentView(R.layout.activity_main);
		ActionBar aBar = getActionBar();
		Resources res = getResources();
		Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
		aBar.setBackgroundDrawable(actionBckGrnd);
		aBar.setTitle(" Network for doctors and patient");

		context = this;
		
		String userQuery = "/fetchUpcomingPlans?phone=123";
		//String userQuery = "/joinCenter?id=2&phone=123";
		//String userQuery = "/fetchUserCenters?phone=123";
		//String userQuery = "/searchCenter?name=as";
		//String userQuery = "/fetchExistingCenters?centerList=345";
		//String userQuery = "/editCenter?id=1&name=asd&adminName=asd&adminPhone=345&address=asff";
		//String userQuery = "/fetchCenter?id=1";
		/*WebImageRestWebServiceClient webImageRestWebServiceClient = new WebImageRestWebServiceClient(this);
		webImageRestWebServiceClient.execute(new String[] {userQuery});*/
		UserWebServiceClient userRestClient = new UserWebServiceClient(this);
		userRestClient.execute(new String[] { userQuery});	
		
		AccountManager am = AccountManager.get(context); // "this" references the current Context
   		Account[] accounts = am.getAccountsByType(WTPConstants.ACCOUNT_ADDRESS);
   		if(accounts != null && accounts.length > 0){
   			Account account = accounts[0];
   			SharedPreferences prefs = getSharedPreferences("Prefs", Activity.MODE_PRIVATE);
               SharedPreferences.Editor editor = prefs.edit();
               editor.putString("userName", am.getUserData(account, "userName"));
               editor.putString("phone", account.name);
               editor.apply();
               setTheme(R.style.AppTheme);
               Log.i(TAG, "Logging as an existing user: "+account.name);
               
               
               
               Intent intent = new Intent(context, HomePlanGroupFragmentActivity.class);
               startActivity(intent);
       	} else {
       		Log.i(TAG, "New User logs in");
       		
       		Intent intent = new Intent(context, NewRegistrationPage.class);
    		startActivity(intent);
   		}

	}
	
	/**
	 * For GCM registration and storage
	 * @author Dell
	 *
	 */
	private class Asyncer extends AsyncTask<String, Integer, String> {

		@Override
		protected void onPreExecute() {

		}

		@Override
		protected String doInBackground(String... params) {
			
			

			
				// Persist the regID - no need to register again.
				//storeRegistrationId(context, regid);

				// Store the reg id in server

				String path = WTPConstants.SERVICE_PATH+"/addRegId?regId="
						+ "123" + "&phone="+params[0];
                
				// HttpHost target = new HttpHost(TARGET_HOST);
				HttpHost target = new HttpHost(WTPConstants.TARGET_HOST, 8080);
				HttpClient client = new DefaultHttpClient();
				HttpGet get = new HttpGet(path);
				try {
					client.execute(target, get);
				} catch (Exception e) {
					e.printStackTrace();

				}
			

			return "";
		}

		@Override
		protected void onPostExecute(String msg) {
			
		}

	}
	
	private class UserWebServiceClient extends AsyncTask<String, Integer, String> {

		private Context mContext;
		private ProgressDialog pDlg;

		public UserWebServiceClient(Context mContext) {
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
				    XStream xstream = new XStream();
					xstream.alias("PlanList", PlanList.class);
					xstream.alias("plans", Plan.class);
					xstream.addImplicitCollection(PlanList.class, "plans");
					PlanList planList = (PlanList) xstream.fromXML(response);
					if (planList != null && planList.getPlans() != null) {

						List<Plan> plans = planList.getPlans();

						if (plans != null && !plans.isEmpty()) {
							for (Plan plan : plans) {
						 Log.i(TAG, plan.getTitle());
							}
						}
					}
				
			}
			pDlg.dismiss();
		}

	}
	
	public class WebImageRestWebServiceClient extends AsyncTask<String, Integer, String> {

		private Context mContext;
		private ProgressDialog pDlg;

		public WebImageRestWebServiceClient(Context mContext) {
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
			
			String method = params[0];
			String path = WTPConstants.SERVICE_PATH+"/"+method;
			Log.i(TAG, path);

			//HttpHost target = new HttpHost(TARGET_HOST);
			HttpHost target = new HttpHost(WTPConstants.TARGET_HOST, 8080);
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(path);
			HttpEntity results = null;
			try {
		        MultipartEntity entity = new MultipartEntity();
		        entity.addPart("name", new StringBody("ghf"));
		        entity.addPart("adminName", new StringBody("asd"));
		        entity.addPart("adminPhone", new StringBody("345"));
		        entity.addPart("address", new StringBody("asff"));
		        entity.addPart("members",  new StringBody(""));
		        entity.addPart("image", new FileBody(new File("@drawable/ic_launcher")));
		        
		        
		        post.setEntity(entity);

		        HttpResponse response = client.execute(target, post);
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
				    XStream userXs = new XStream();
				    userXs.alias("Center", Center.class);
					userXs.alias("members", String.class);
					userXs.addImplicitCollection(Center.class, "members",
							"members", String.class);
					Center center = (Center) userXs.fromXML(response);
					if (center != null) {
						 Log.i(TAG, center.getName());
					}
				
			}
			pDlg.dismiss();
		}
	}

}
