package com.theiyer.whatstheplan;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;

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
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.theiyer.whatstheplan.entity.Center;
import com.theiyer.whatstheplan.util.WTPConstants;
import com.thoughtworks.xstream.XStream;

public class NewHealthCenterSignUpActivity extends FragmentActivity {

	private Context context;
	private GoogleCloudMessaging gcm;
	private String regid;
	private String filePath;
	private Bitmap bitmap;
	private ImageView imgView;
	private static final int PICK_IMAGE = 1;
	private static final String TAG = "Health Meet GCM";

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		if (haveInternet(this)) {
			setContentView(R.layout.new_healthcenter_registration);
			ActionBar aBar = getActionBar();
			Resources res = getResources();
			Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
			aBar.setBackgroundDrawable(actionBckGrnd);
			aBar.setTitle(" Health Centre Registration form");
			context = getApplicationContext();
			imgView = (ImageView) findViewById(R.id.healthcentrePicView);
		} else {
			Intent intent = new Intent(this, RetryActivity.class);
			startActivity(intent);
		}
	}

	public void selectGroupImage(View view) {
		try {
			Intent intent = new Intent();
			intent.setType("image/*");
			intent.setAction(Intent.ACTION_GET_CONTENT);
			startActivityForResult(
					Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), "Image selection failed",
					Toast.LENGTH_LONG).show();
			Log.e(e.getClass().getName(), e.getMessage(), e);
		}
	}

	public void onClickRegisterHealthCentre(View view) {
		Button button = (Button) findViewById(R.id.registerHealthButton);
		button.setTextColor(getResources().getColor(R.color.click_button_2));
		EditText healthNameText = (EditText) findViewById(R.id.healthNameText);
		String centreName = healthNameText.getText().toString();
		EditText adminNameText = (EditText) findViewById(R.id.adminName);
		String adminName = adminNameText.getText().toString();
		EditText adminPhoneText = (EditText) findViewById(R.id.healthCentrePhoneValue);
		String adminPhone = adminPhoneText.getText().toString();
		EditText adminAddressText = (EditText) findViewById(R.id.healthaddress);
		String adminAddress = adminAddressText.getText().toString();
		SharedPreferences prefs = getSharedPreferences("Prefs",
				Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("userName", centreName);
		editor.putString("phone", adminPhone);
		editor.putString("docFlag", "N");
		editor.putString("centerFlag", "Y");
		editor.apply();

		gcm = GoogleCloudMessaging.getInstance(context);
		Asyncer syncer = new Asyncer();
		syncer.execute(new String[] { adminPhone });

		WebImageRestWebServiceClient imageRestClient = new WebImageRestWebServiceClient(
				this);

		imageRestClient.execute(new String[] { "addCenter", centreName,
				adminName, adminPhone, adminAddress, "", filePath });
		
		AccountManager am = AccountManager.get(this);
		final Account account = new Account(adminPhone,
				WTPConstants.ACCOUNT_ADDRESS);
		final Bundle bundle = new Bundle();
		bundle.putString("userName", centreName);
		bundle.putString("phone", adminPhone);
		bundle.putString("centerFlag", "Y");
		bundle.putString(AccountManager.KEY_ACCOUNT_NAME,
				account.name);
		am.addAccountExplicitly(account, adminPhone,
				bundle);
		am.setAuthToken(account, "Full Access",
				adminPhone);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case PICK_IMAGE:
			if (resultCode == Activity.RESULT_OK) {
				Uri selectedImageUri = data.getData();

				try {
					// OI FILE Manager
					String filemanagerstring = selectedImageUri.getPath();

					// MEDIA GALLERY
					String selectedImagePath = getPath(selectedImageUri);

					if (selectedImagePath != null) {
						filePath = selectedImagePath;
					} else if (filemanagerstring != null) {
						filePath = filemanagerstring;
					} else {
						Toast.makeText(getApplicationContext(), "Unknown path",
								Toast.LENGTH_LONG).show();
						Log.e("Bitmap", "Unknown path");
					}

					if (filePath != null) {
						decodeFile(filePath);
					} else {
						bitmap = null;
					}
				} catch (Exception e) {
					Toast.makeText(getApplicationContext(), "Internal error",
							Toast.LENGTH_LONG).show();
					Log.e(e.getClass().getName(), e.getMessage(), e);
				}
			}
			break;
		default:
		}
	}

	public void decodeFile(String filePath) {
		try {
			File file = new File(filePath);
			FileBody fBody = new FileBody(file);
			BufferedInputStream bis = new BufferedInputStream(fBody.getInputStream());
			bis.mark(1024);
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(bis,null,opts);
			Log.i("optwidth",opts.outWidth+"");
			bis.reset();
			bitmap = BitmapFactory.decodeStream(bis);

			imgView.setImageBitmap(bitmap);
		} catch (IOException e) {
			Toast.makeText(getApplicationContext(), "Please select an image less than 1 MB",
					Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * For GCM registration and storage
	 * 
	 * @author Dell
	 * 
	 */
	private class Asyncer extends AsyncTask<String, Integer, String> {

		@Override
		protected void onPreExecute() {

		}

		@Override
		protected String doInBackground(String... params) {
			String msg = "";
			if (gcm == null) {
				gcm = GoogleCloudMessaging.getInstance(context);
			}
			try {
				Log.i(TAG, "Registering GCM");
				regid = gcm.register(WTPConstants.SENDER_ID);
			} catch (IOException ex) {
				msg = "Error :" + ex.getMessage();
				Log.e(TAG, msg);

				ex.printStackTrace();
				// If there is an error, don't just keep trying to register.
				// Require the user to click a button again, or perform
				// exponential back-off.
			}
			msg = "Device registered, registration ID=" + regid;
			Log.i(TAG, msg);

			if (regid != null && regid != "") {
				// Persist the regID - no need to register again.
				storeRegistrationId(context, regid);

				// Store the reg id in server

				String path = WTPConstants.SERVICE_PATH + "/addRegId?regId="
						+ regid + "&phone=" + params[0];

				// HttpHost target = new HttpHost(TARGET_HOST);
				HttpHost target = new HttpHost(WTPConstants.TARGET_HOST, 8080);
				HttpClient client = new DefaultHttpClient();
				HttpGet get = new HttpGet(path);
				try {
					client.execute(target, get);
				} catch (Exception e) {

				}
			}

			return msg;
		}

		@Override
		protected void onPostExecute(String msg) {

		}

	}

	/**
	 * Stores the registration ID and app versionCode in the application's
	 * {@code SharedPreferences}.
	 * 
	 * @param context
	 *            application's context.
	 * @param regId
	 *            registration ID
	 */
	private void storeRegistrationId(Context context, String regId) {
		SharedPreferences prefs = getSharedPreferences("Prefs",
				Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("regId", regId);
		editor.apply();
	}

	@SuppressWarnings("deprecation")
	public String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(uri, projection, null, null, null);
		if (cursor != null) {
			// HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
			// THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} else
			return null;
	}

	public class WebImageRestWebServiceClient extends
			AsyncTask<String, Integer, String> {

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
			String path = WTPConstants.SERVICE_PATH + "/" + method;

			// HttpHost target = new HttpHost(TARGET_HOST);
			HttpHost target = new HttpHost(WTPConstants.TARGET_HOST, 8080);
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(path);
			HttpEntity results = null;
			try {
				MultipartEntity entity = new MultipartEntity();
				entity.addPart("name", new StringBody(params[1].replace(" ", "%20")));
				entity.addPart("adminName", new StringBody(params[2].replace(" ", "%20")));
				entity.addPart("adminPhone", new StringBody(params[3]));
				entity.addPart("address", new StringBody(params[4].replace(" ", "%20")));
				entity.addPart("members", new StringBody(""));
				entity.addPart("image", new FileBody(new File(filePath)));

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
				XStream userXs = new XStream();
				userXs.alias("Center", Center.class);
				userXs.alias("members", String.class);
				userXs.addImplicitCollection(Center.class, "members",
						"members", String.class);
				Center center = (Center) userXs.fromXML(response);
				if (center != null) {
					
					Toast.makeText(getApplicationContext(),
							"Congratulations! Your center is active now.",
							Toast.LENGTH_LONG).show();
					Intent intent = new Intent(mContext,
							HomePlanGroupFragmentActivity.class);
					startActivity(intent);

				}

			}
			pDlg.dismiss();
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case (R.id.aboutUs):
			Intent intent = new Intent(this, AboutUsActivity.class);
			startActivity(intent);
			return true;
		default:
			return false;
		}
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
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
