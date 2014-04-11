package com.theiyer.whatstheplan;

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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.theiyer.whatstheplan.util.WTPConstants;

public class ViewProfileActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if(haveInternet(this)){
			setContentView(R.layout.view_profile);
			ActionBar aBar = getActionBar();
			Resources res = getResources();
			Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
			aBar.setBackgroundDrawable(actionBckGrnd);
			aBar.setTitle(" Profile Details");

			SharedPreferences prefs = getSharedPreferences("Prefs",
					Activity.MODE_PRIVATE);
			String userName = prefs.getString("userName", "New User");
			TextView welcomeStmnt = (TextView) findViewById(R.id.welcomeViewProfileLabel);
			welcomeStmnt.setText(userName + ", Your profile details!");

			TextView userNameValue = (TextView) findViewById(R.id.viewProfileName);
			userNameValue.setText(userName);
			
			String phone = prefs.getString("phone", "");
			TextView phoneValue = (TextView) findViewById(R.id.viewProfilePhone);
			phoneValue.setText("Phone: " + phone);

			WebImageRetrieveRestWebServiceClient userImageClient = new WebImageRetrieveRestWebServiceClient(
					this);
			userImageClient.execute(new String[] { "fetchUserImage", phone });
		} else {
			Intent intent = new Intent(this, RetryActivity.class);
			startActivity(intent);
		}
		

	}

	
	private class WebImageRetrieveRestWebServiceClient extends AsyncTask<String, Integer, byte[]> {

		private Context mContext;
		private ProgressDialog pDlg;

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
			String path = WTPConstants.SERVICE_PATH+"/"+method;

			if("fetchUserImage".equals(method)){
	        	path = path+"?phone="+params[1];
	        } else {
	        	path = path+"?groupName="+params[1];
	        }
			//HttpHost target = new HttpHost(TARGET_HOST);
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
			
			
			if(response != null){
				if (response != null) {
					ImageView imgView = (ImageView) findViewById(R.id.viewProfilePicThumbnail);
					Bitmap img = BitmapFactory.decodeByteArray(response, 0,
							response.length);
					imgView.setImageBitmap(img);
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
