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
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.theiyer.whatstheplan.entity.User;
import com.theiyer.whatstheplan.util.WTPConstants;
import com.thoughtworks.xstream.XStream;

public class ViewMemberProfileActivity extends Activity {
	private static final String TAG = "Health Meet/ViewProfileActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (haveInternet(this)) {
			SharedPreferences prefs = getSharedPreferences("Prefs",
					Activity.MODE_PRIVATE);
			setContentView(R.layout.view_profile);
			ActionBar aBar = getActionBar();
			Resources res = getResources();
			Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
			aBar.setBackgroundDrawable(actionBckGrnd);
			aBar.setTitle(" Profile Details");

			String phone = prefs.getString("memberPhone", "");

			String userQuery = "/fetchUser?phone=" + phone;
			UserWebServiceClient userRestClient = new UserWebServiceClient(this);
			userRestClient.execute(new String[] { userQuery });

		} else {
			Intent intent = new Intent(this, RetryActivity.class);
			startActivity(intent);
		}

	}

	public class UserWebServiceClient extends
			AsyncTask<String, Integer, String> {

		private Context mContext;
		private ProgressDialog pDlg;
		private String query;

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
			if (response != null && query.contains("fetchUser")) {
				Log.i(TAG, response);
				XStream userXs = new XStream();
				userXs.alias("UserInformation", User.class);
				userXs.alias("centers", String.class);
				userXs.addImplicitCollection(User.class, "centers", "centers",
						String.class);
				User user = (User) userXs.fromXML(response);
				if (user != null && user.getName() != null) {
					TextView phoneValue = (TextView) findViewById(R.id.viewProfilePhone);
					phoneValue.setText("Phone: " + user.getPhone());
					TextView userNameValue = (TextView) findViewById(R.id.viewProfileName);
					userNameValue.setText("Name: " + user.getName());
					TextView dobValue = (TextView) findViewById(R.id.viewProfileDob);
					dobValue.setText("Date of Birth: " + user.getDob());
					TextView genderValue = (TextView) findViewById(R.id.viewProfilegender);
					genderValue.setText("Gender: " + user.getSex());
					TextView bloodGrpValue = (TextView) findViewById(R.id.viewProfilebloodGrp);
					bloodGrpValue.setText("Blood Group: "
							+ user.getBloodGroup());
					TextView addressValue = (TextView) findViewById(R.id.viewProfileaddress);
					addressValue.setText("Address: " + user.getAddress());
					ImageView imgView = (ImageView) findViewById(R.id.viewProfilePicThumbnail);
					byte[] image = user.getImage();
					if (image != null) {
						Bitmap img = BitmapFactory.decodeByteArray(image, 0,
								image.length);
						imgView.setImageBitmap(img);
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
