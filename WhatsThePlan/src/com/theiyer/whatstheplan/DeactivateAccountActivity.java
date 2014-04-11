package com.theiyer.whatstheplan;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
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
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.theiyer.whatstheplan.util.WTPConstants;

public class DeactivateAccountActivity extends Activity {

	private Context context;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if(haveInternet(this)){
			context = this;
			setContentView(R.layout.delete_profile);
			ActionBar aBar = getActionBar();
			Resources res = getResources();
			Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
			aBar.setBackgroundDrawable(actionBckGrnd);
			aBar.setTitle(" De-activate Account Form");

			SharedPreferences prefs = getSharedPreferences("Prefs",
					Activity.MODE_PRIVATE);
			String userName = prefs.getString("userName", "New User");
			TextView welcomeStmnt = (TextView) findViewById(R.id.welcomeDeleteProfileLabel);
			welcomeStmnt.setText(userName + ", manage your profile here!");

			TextView userNameValue = (TextView) findViewById(R.id.deleteProfileName);
			userNameValue.setText("Name: " + userName);

			String phone = prefs.getString("phone", "");
			TextView phoneValue = (TextView) findViewById(R.id.deleteProfilePhone);
			phoneValue.setText("Phone: " + phone);
		} else {
			Intent intent = new Intent(this, RetryActivity.class);
			startActivity(intent);
		}
		

	}

	/** Called when the user clicks the delete profile button */
	public void deleteProfile(View view) {
		Button changePassButton = (Button) findViewById(R.id.deleteProfileButton);
		changePassButton.setTextColor(getResources().getColor(R.color.click_button_2));
		SharedPreferences prefs = getSharedPreferences("Prefs",
				Activity.MODE_PRIVATE);
		String phone = prefs.getString("phone", "");
		TextView errorFieldValue = (TextView) findViewById(R.id.deleteProfileErrorField);
		errorFieldValue.setText("");
		

		String searchQuery = "/deleteAccount?phone=" + phone;

		WebServiceClient restClient = new WebServiceClient(this);
		restClient.execute(
				new String[] { searchQuery });
		
		 AccountManager am = AccountManager.get(this);
		 Account[] accounts = am.getAccountsByType("com.theiyer.whatstheplan");
			if(accounts != null && accounts.length > 0){
				Account account = accounts[0];
				am.removeAccount(account, new OnTokenAcquired(),          // Callback called when a token is successfully acquired
					    new Handler());
			}
			
		changePassButton.setTextColor(getResources().getColor(R.color.button_text));
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
	}
	
	private class OnTokenAcquired implements AccountManagerCallback<Boolean> {
	    @Override
	    public void run(AccountManagerFuture<Boolean> result) {
	        // Get the result of the operation from the AccountManagerFuture.
	    	Intent intent = new Intent(context, MainActivity.class);
			startActivity(intent);
	    }
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
