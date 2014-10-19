package com.theiyer.whatstheplan;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.apache.james.mime4j.message.Multipart;

import com.theiyer.whatstheplan.CreateGroupActivity.WebImageRestWebServiceClient;
import com.theiyer.whatstheplan.JoinGroupActivity.WebServiceClient;
import com.theiyer.whatstheplan.entity.Center;
import com.theiyer.whatstheplan.entity.Group;
import com.theiyer.whatstheplan.util.WTPConstants;
import com.thoughtworks.xstream.XStream;

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
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class NewHealthCenterSignUpActivity extends FragmentActivity {

	private Context context;
	private String filePath;
	private Bitmap bitmap;
	private ImageView imgView;
	private static final int PICK_IMAGE = 1;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		if(haveInternet(this)){
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
		editor.putString("name", centreName);
		editor.putString("phone", adminPhone);
		editor.putString("docFlag", "N");
		editor.putString("centerFlag", "Y");
		editor.apply();
		WebImageRestWebServiceClient imageRestClient = new WebImageRestWebServiceClient(
				this);

		imageRestClient.execute(
				new String[] { "addCenter", centreName, adminName, 
						adminPhone, adminAddress, "", filePath });
		
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
		// Decode image size
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, o);

		// The new size we want to scale to
		final int REQUIRED_SIZE = 1024;

		// Find the correct scale value. It should be the power of 2.
		int width_tmp = o.outWidth, height_tmp = o.outHeight;
		int scale = 1;
		while (true) {
			if (width_tmp < REQUIRED_SIZE && height_tmp < REQUIRED_SIZE)
				break;
			width_tmp /= 2;
			height_tmp /= 2;
			scale *= 2;
		}

		// Decode with inSampleSize
		BitmapFactory.Options o2 = new BitmapFactory.Options();
		o2.inSampleSize = scale;
		bitmap = BitmapFactory.decodeFile(filePath, o2);

		imgView.setImageBitmap(bitmap);

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

			//HttpHost target = new HttpHost(TARGET_HOST);
			HttpHost target = new HttpHost(WTPConstants.TARGET_HOST, 8080);
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(path);
			HttpEntity results = null;
			try {
				MultipartEntity entity = new MultipartEntity();
		        entity.addPart("name", new StringBody(params[1]));
		        entity.addPart("adminName", new StringBody(params[2]));
		        entity.addPart("adminPhone", new StringBody(params[3]));
		        entity.addPart("address", new StringBody(params[4]));
		        entity.addPart("members",  new StringBody(""));
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
				System.out.println("**** response " + response);
				XStream userXs = new XStream();
			    userXs.alias("Center", Center.class);
				userXs.alias("members", String.class);
				userXs.addImplicitCollection(Center.class, "members",
						"members", String.class);
				Center center = (Center) userXs.fromXML(response);
				if (center != null) {
					AccountManager am = AccountManager.get(mContext);
					final Account account = new Account(center.getAdminPhone(),
							WTPConstants.ACCOUNT_ADDRESS);
					final Bundle bundle = new Bundle();
					bundle.putString("userName", center.getName());
					bundle.putString("phone", center.getAdminPhone());
					bundle.putString("center", "Y");
					bundle.putString(AccountManager.KEY_ACCOUNT_NAME,
							account.name);
					am.addAccountExplicitly(account, center.getAdminPhone(), bundle);
					am.setAuthToken(account, "Full Access", center.getAdminPhone());
					Toast.makeText(getApplicationContext(), "Congratulations! Your center is active now.",
							Toast.LENGTH_LONG).show();
					Intent intent = new Intent(mContext, HomePlanGroupFragmentActivity.class);
					startActivity(intent);
					
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
