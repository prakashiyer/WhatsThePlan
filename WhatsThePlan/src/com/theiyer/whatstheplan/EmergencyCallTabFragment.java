package com.theiyer.whatstheplan;


import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.theiyer.whatstheplan.entity.User;
import com.theiyer.whatstheplan.util.WTPConstants;
import com.thoughtworks.xstream.XStream;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class EmergencyCallTabFragment extends Fragment {
	Activity activity;
	View rootView = null;
	Context context;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		context = getActivity();
		super.onCreate(savedInstanceState);
		activity = this.getActivity();
			rootView = inflater.inflate(R.layout.emergency_tab, container,
					false);

			SharedPreferences prefs = activity.getSharedPreferences("Prefs",
					Activity.MODE_PRIVATE);
			String name = prefs.getString("userName", "");
			TextView textView = (TextView) rootView.findViewById(R.id.emergencyTabLabel);
			textView.setText(name + ", Find your emergency numbers below.");
			String phone = prefs.getString("phone", "");
			String userQuery = "/fetchUser?phone="+phone;
			UserWebServiceClientem userRestClient = new UserWebServiceClientem(context);
			userRestClient.execute(new String[] { userQuery});
			return rootView;
	}
	public class UserWebServiceClientem extends AsyncTask<String, Integer, String> {

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
			String path = WTPConstants.SERVICE_PATH+params[0];
			query = params[0];
			
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
			SharedPreferences prefs = activity.getSharedPreferences("Prefs",
					Activity.MODE_PRIVATE);
			if (response != null && query.contains("fetchUser")) {
				    System.out.println("EmergencyTab : response" + response);
				    XStream userXs = new XStream();
					userXs.alias("UserInformation", User.class);
					userXs.alias("centers", String.class);
					userXs.addImplicitCollection(User.class, "centers",
							"centers", String.class);
					User user = (User) userXs.fromXML(response);
					if (user != null && user.getName() != null) {
						 Log.i("EmergencyTab", user.getName());
						 SharedPreferences.Editor editor = prefs.edit();
							/*editor.putString("userName", user.getName());
							editor.putString("phone", user.getPhone());
							editor.putString("dob", user.getDob());
							editor.putString("gender", user.getSex());
							editor.putString("bloodGrp", user.getBloodGroup());
							editor.putString("Address", user.getAddress());*/
							editor.putString("doctorPhone", user.getPrimaryDoctorId());
							editor.putString("centerPhone", user.getPrimaryCenterId());
							editor.apply();
							System.out.println("centerPhone in WebS " + user.getPrimaryCenterId());
							}
			}
			pDlg.dismiss();
		}

	}
	public void setActivity(Activity activity) {
		this.activity = activity;
	}
}