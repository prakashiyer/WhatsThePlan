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

public class GroupImageChangeActivity extends Activity {

	private static final int PICK_IMAGE = 1;
	private ImageView imgView;
	private String filePath;

	private Bitmap bitmap;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.change_group_image);
		ActionBar aBar = getActionBar();
		Resources res = getResources();
		Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
		aBar.setBackgroundDrawable(actionBckGrnd);
		aBar.setTitle(" Group Photo Selection");

		imgView = (ImageView) findViewById(R.id.changeGroupPicView);
		
        ImageRetrieveRestWebServiceClient imageRetrieveClient = new ImageRetrieveRestWebServiceClient(this);
		
		try {
			SharedPreferences prefs = getSharedPreferences("Prefs",
					Activity.MODE_PRIVATE);
			String selectedGroup = prefs.getString("selectedGroup", "");
			byte[] response = imageRetrieveClient.execute(
					new String[] { "fetchGroupImage", selectedGroup}).get();
			if (response != null) {
				Bitmap img = BitmapFactory.decodeByteArray(response, 0,
						response.length);

				imgView.setImageBitmap(img);
			} else {
				imgView.setImageResource(R.drawable.ic_launcher);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			
		}

	}

	public void uploadChangeGroupImage(View view) {
		Button button = (Button) findViewById(R.id.uploadChangeGroupImageButton);
		button.setTextColor(getResources().getColor(R.color.click_button_2));
		if (bitmap == null) {
			Toast.makeText(getApplicationContext(), "Please select image",
					Toast.LENGTH_SHORT).show();
		} else {

			try {
				SharedPreferences prefs = getSharedPreferences("Prefs",
						Activity.MODE_PRIVATE);
				String selectedGroup = prefs.getString("selectedGroup", "");
				
				ImageRestWebServiceClient restClient = new ImageRestWebServiceClient(
						this);

				byte[] response = restClient.execute(
						new String[] { "uploadGroupImage", selectedGroup, filePath }).get();
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
		
	}

	public void skipChangeGroupImage(View view) {
		Button button = (Button) findViewById(R.id.skipChangeGroupImageButton);
		button.setTextColor(getResources().getColor(R.color.click_button_1));
		Toast.makeText(getApplicationContext(),
				"You can use the menu to change photo later.", Toast.LENGTH_LONG)
				.show();
		Intent intent = new Intent(this, ViewMyGroupActivity.class);
		startActivity(intent);
	}

	public void selectChangeGroupImage(View view) {
		Button button = (Button) findViewById(R.id.selectChangeGroupImageButton);
		button.setTextColor(getResources().getColor(R.color.click_button_2));
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
