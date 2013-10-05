package com.theiyer.whatstheplan;

import java.util.concurrent.ExecutionException;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class ProfileImageUploadActivity extends Activity {

	private static final int PICK_IMAGE = 1;
	private ImageView imgView;
	private String filePath;

	private Bitmap bitmap;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.upload_image);
		ActionBar aBar = getActionBar();
		Resources res = getResources();
		Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
		aBar.setBackgroundDrawable(actionBckGrnd);
		aBar.setTitle(" Profile Photo Selection");

		imgView = (ImageView) findViewById(R.id.profilePicView);
		
        ImageRetrieveRestWebServiceClient imageRetrieveClient = new ImageRetrieveRestWebServiceClient(this);
		
		try {
			SharedPreferences prefs = getSharedPreferences("Prefs",
					Activity.MODE_PRIVATE);
			String emailId = prefs.getString("emailId", "");
			byte[] response = imageRetrieveClient.execute(
					new String[] { "fetchUserImage", emailId}).get();
			if (response != null) {
				Bitmap img = BitmapFactory.decodeByteArray(response, 0,
						response.length);

				if(img!=null){
					imgView.setImageBitmap(img);
				}
				
			} else {
				imgView.setImageResource(R.drawable.ic_launcher);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			
		}

	}

	public void uploadImage(View view) {
		Button button = (Button) findViewById(R.id.uploadImageButton);
		button.setTextColor(getResources().getColor(R.color.click_button_2));
		if (bitmap == null) {
			Toast.makeText(getApplicationContext(), "Please select image",
					Toast.LENGTH_SHORT).show();
		} else {

			try {
				SharedPreferences prefs = getSharedPreferences("Prefs",
						Activity.MODE_PRIVATE);
				String emailId = prefs.getString("emailId", "");
				
				ImageRestWebServiceClient restClient = new ImageRestWebServiceClient(
						this);

				byte[] response = restClient.execute(
						new String[] { "uploadUserImage", emailId, filePath }).get();
				if (response != null) {
					Bitmap img = BitmapFactory.decodeByteArray(response, 0,
							response.length);

					imgView.setImageBitmap(img);
					Toast.makeText(getApplicationContext(),
							"Selected Photo has been set", Toast.LENGTH_LONG)
							.show();
					
				}

			} catch (InterruptedException e) {
				

			} catch (ExecutionException e) {
				

			}
		}
		button.setTextColor(getResources().getColor(R.color.button_text));
	}

	public void skipImage(View view) {
		Button button = (Button) findViewById(R.id.skipImageButton);
		button.setTextColor(getResources().getColor(R.color.click_button_1));
		Toast.makeText(getApplicationContext(),
				"You can use the menu to change photo later.", Toast.LENGTH_LONG)
				.show();
		button.setTextColor(getResources().getColor(R.color.button_text));
		Intent intent = new Intent(this, HomePlanActivity.class);
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
}
