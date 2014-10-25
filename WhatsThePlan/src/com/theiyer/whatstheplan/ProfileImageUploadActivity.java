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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.theiyer.whatstheplan.util.WTPConstants;

public class ProfileImageUploadActivity extends Activity {

	private static final int PICK_IMAGE = 1;
	private ImageView imgView;
	private String filePath;

	private Bitmap bitmap;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if(haveInternet(this)){
			setContentView(R.layout.upload_image);
			ActionBar aBar = getActionBar();
			Resources res = getResources();
			Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
			aBar.setBackgroundDrawable(actionBckGrnd);
			aBar.setTitle(" Profile Photo Selection");
			SharedPreferences prefs = getSharedPreferences("Prefs",
					Activity.MODE_PRIVATE);
			String phone = prefs.getString("phone", "");

			imgView = (ImageView) findViewById(R.id.profilePicView);
			WebImageRetrieveRestWebServiceClient userImageClient = new WebImageRetrieveRestWebServiceClient(
					this);
			userImageClient.execute(new String[] { "fetchUserImage", phone });
	        
		} else {
			Intent intent = new Intent(this, RetryActivity.class);
			startActivity(intent);
		}
		

	}

	public void uploadImage(View view) {
		Button button = (Button) findViewById(R.id.uploadImageButton);
		button.setTextColor(getResources().getColor(R.color.click_button_2));
		if (bitmap == null) {
			Toast.makeText(getApplicationContext(), "Please select image",
					Toast.LENGTH_SHORT).show();
			button.setTextColor(getResources().getColor(R.color.button_text));
		} else {

			SharedPreferences prefs = getSharedPreferences("Prefs",
					Activity.MODE_PRIVATE);
			String phone = prefs.getString("phone", "");
			
			WebImageRestWebServiceClient restClient = new WebImageRestWebServiceClient(
					this);

			restClient.execute(
					new String[] { "uploadUserImage", phone, filePath });
		}
		
	}

	public void skipImage(View view) {
		Button button = (Button) findViewById(R.id.skipImageButton);
		button.setTextColor(getResources().getColor(R.color.click_button_1));
		Toast.makeText(getApplicationContext(),
				"You can use the menu to change photo later.", Toast.LENGTH_LONG)
				.show();
		button.setTextColor(getResources().getColor(R.color.button_text));
		Intent intent = new Intent(this, HomePlanGroupFragmentActivity.class);
		startActivity(intent);
	}

	public void selectImage(View view) {
		Button button = (Button) findViewById(R.id.selectImageButton);
		button.setTextColor(getResources().getColor(R.color.click_button_2));
		try {
			Intent intent = new Intent();
			intent.setType("image/*");
			intent.setAction(Intent.ACTION_GET_CONTENT);
			startActivityForResult(
					Intent.createChooser(intent, "Select a Picture"), PICK_IMAGE);
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), "Image selection failed",
					Toast.LENGTH_LONG).show();
			Log.e(e.getClass().getName(), e.getMessage(), e);
		}
		button.setTextColor(getResources().getColor(R.color.button_text));
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
			
			
			if (response != null) {
				Bitmap img = BitmapFactory.decodeByteArray(response, 0,
						response.length);

				if(img!=null){
					imgView.setImageBitmap(img);
				}
				
			} else {
				imgView.setImageResource(R.drawable.ic_launcher);
			}
			
			pDlg.dismiss();
		}

	}
	
	private class WebImageRestWebServiceClient extends AsyncTask<String, Integer, byte[]> {

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
		protected byte[] doInBackground(String... params) {
			
			String method = params[0];
			String path = WTPConstants.SERVICE_PATH+"/"+method;

			//HttpHost target = new HttpHost(TARGET_HOST);
			HttpHost target = new HttpHost(WTPConstants.TARGET_HOST, 8080);
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(path);
			
			HttpEntity results = null;
			try {
		        MultipartEntity entity = new MultipartEntity();
		       
		        if("uploadUserImage".equals(method)){
		        	entity.addPart("phone", new StringBody(params[1]));
		        } else {
		        	entity.addPart("groupName", new StringBody(params[1]));
		        }
		        
		        entity.addPart("image", new FileBody(new File(params[2])));
		        post.setEntity(entity);

		        HttpResponse response = client.execute(target, post);
		        results = response.getEntity(); 
				byte[] byteresult = EntityUtils.toByteArray(results);
				return byteresult;
			} catch (Exception e) {
				
			}
			return null;
		}

		@Override
		protected void onPostExecute(byte[] response) {
			
			Button button = (Button) findViewById(R.id.uploadImageButton);
			
			if (response != null) {
				Bitmap img = BitmapFactory.decodeByteArray(response, 0,
						response.length);

				imgView.setImageBitmap(img);
				Toast.makeText(getApplicationContext(),
						"Selected Photo has been set", Toast.LENGTH_LONG)
						.show();
				
			} else {
				Toast.makeText(getApplicationContext(),
						"Photo upload failed. Please try again later.", Toast.LENGTH_LONG)
						.show();
			}
			button.setTextColor(getResources().getColor(R.color.button_text));
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
