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
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.theiyer.whatstheplan.entity.User;
import com.theiyer.whatstheplan.util.WTPConstants;
import com.thoughtworks.xstream.XStream;

public class EmergencyCallTabFragment extends Fragment {
	Activity activity;
	View rootView = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		activity = this.getActivity();
		if(activity != null && haveInternet(activity)){
			ActionBar aBar = activity.getActionBar();
			Resources res = getResources();
			Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
			aBar.setBackgroundDrawable(actionBckGrnd);
			aBar.setTitle(" Emergency Contacts");
			rootView = inflater.inflate(R.layout.emergency_tab, container, false);

			SharedPreferences prefs = activity.getSharedPreferences("Prefs",
					Activity.MODE_PRIVATE);
			String selectedDoctor = prefs.getString("selectedDoctor", "0");
			String selectedHealthCenter = prefs.getString("selectedHealthCenter", "0");
			String phone = prefs.getString("phone", "");
			
			if("0".equals(selectedDoctor) || "0".equals(selectedHealthCenter)){
				String userQuery = "/fetchUser?phone=" + phone;
				UserWebServiceClientem userRestClient = new UserWebServiceClientem(
						activity);
				userRestClient.execute(new String[] { userQuery });
			}
			
			
		} else {
			Intent intent = new Intent(activity, RetryActivity.class);
			startActivity(intent);
		}
		return rootView;
		
	}

	public class UserWebServiceClientem extends
			AsyncTask<String, Integer, String> {

		private Context mContext;
		private ProgressDialog pDlg;
		private String query;

		public UserWebServiceClientem(Context mContext) {
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
			query = params[0];

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
			SharedPreferences prefs = activity.getSharedPreferences("Prefs",
					Activity.MODE_PRIVATE);
			if (response != null && query.contains("fetchUser")) {
				XStream userXs = new XStream();
				userXs.alias("UserInformation", User.class);
				userXs.alias("centers", String.class);
				userXs.addImplicitCollection(User.class, "centers", "centers",
						String.class);
				User user = (User) userXs.fromXML(response);
				if (user != null && user.getName() != null) {
					SharedPreferences.Editor editor = prefs.edit();
					String docPhone = user.getPrimaryDoctorId();
					String centerPhone = user.getPrimaryCenterId();
					editor.putString("selectedDoctor", docPhone);
					editor.putString("selectedHealthCenter", centerPhone);
					editor.apply();
					if (docPhone != null && docPhone == "") {
						Button button = (Button) activity
								.findViewById(R.id.call_doc_button);
						button.setVisibility(TextView.INVISIBLE);
						Toast.makeText(
								activity,
								"Please select a primary doctor using the menu option",
								Toast.LENGTH_SHORT).show();
					}
					if (centerPhone != null && centerPhone == "") {
						Button button = (Button) activity
								.findViewById(R.id.call_health);
						button.setVisibility(TextView.INVISIBLE);
						Toast.makeText(
								activity,
								"Please select a primary health center using the menu option",
								Toast.LENGTH_SHORT).show();
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

	public void setActivity(Activity activity) {
		this.activity = activity;
	}
}