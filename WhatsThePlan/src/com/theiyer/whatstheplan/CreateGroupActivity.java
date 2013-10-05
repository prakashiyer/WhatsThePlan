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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.theiyer.whatstheplan.entity.Group;
import com.thoughtworks.xstream.XStream;

public class CreateGroupActivity extends Activity {

	private static final int PICK_IMAGE = 1;
	private ImageView imgView;
	private String filePath;

	private Bitmap bitmap;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.create_group);
		ActionBar aBar = getActionBar();
		Resources res = getResources();
		Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
		aBar.setBackgroundDrawable(actionBckGrnd);
		aBar.setTitle(" Group Creation Form");

		SharedPreferences prefs = getSharedPreferences("Prefs",
				Activity.MODE_PRIVATE);
		String userName = prefs.getString("userName", "New User");
		TextView userNameValue = (TextView) findViewById(R.id.welcomeCreateGroupLabel);
		userNameValue.setText(userName + ", Create a new group here!");
		
		imgView = (ImageView) findViewById(R.id.groupPicView);

	}

	/** Called when the user clicks the Create Group button */
	public void goFromCreateGroupToViewGroups(View view) {
		Button button = (Button) findViewById(R.id.registerGroupButton);
		button.setTextColor(getResources().getColor(R.color.click_button_1));
		
		EditText groupNameEditText = (EditText) findViewById(R.id.newGroupNameValue);
		String groupName = groupNameEditText.getText().toString();
		
		TextView errorFieldValue = (TextView) findViewById(R.id.createGroupErrorField);
		errorFieldValue.setText("");

		if (groupName != null && !groupName.isEmpty()) {
			SharedPreferences prefs = getSharedPreferences("Prefs",
					Activity.MODE_PRIVATE);
			String emailId = prefs.getString("emailId", "");

			String insertQuery = "/addGroup?groupName="
					+ groupName.replace(" ", "%20") + "&emailId=" + emailId;

			RestWebServiceClient restClient = new RestWebServiceClient(this);
			try {
				String response = restClient.execute(
						new String[] { insertQuery }).get();
				if(response!=null){
					XStream xstream = new XStream();
					xstream.alias("Group", Group.class);
					
					xstream.alias("memberEmailIds", String.class);
					xstream.addImplicitCollection(Group.class, "memberEmailIds","memberEmailIds",String.class);
					xstream.alias("planNames", String.class);
					xstream.addImplicitCollection(Group.class, "planNames","planNames",String.class);
					xstream.alias("pendingMembers", String.class);
					xstream.addImplicitCollection(Group.class, "pendingMembers","pendingMembers",String.class);
					Group group = (Group) xstream.fromXML(response);
					if (group != null && groupName.equals(group.getName())) {
						
						
						if (bitmap == null) {
							Toast.makeText(getApplicationContext(), "You can use menu to upload image later.",
									Toast.LENGTH_SHORT).show();
						} else {
							
								ImageRestWebServiceClient imageRestClient = new ImageRestWebServiceClient(
										this);

								byte[] imageResponse = imageRestClient.execute(
										new String[] { "uploadGroupImage", groupName, filePath }).get();
								if (imageResponse != null) {
									Bitmap img = BitmapFactory.decodeByteArray(imageResponse, 0,
											imageResponse.length);

									imgView.setImageBitmap(img);
									Toast.makeText(getApplicationContext(),
											"Selected Photo has been set", Toast.LENGTH_LONG)
											.show();
								}	
						}

						Intent intent = new Intent(this, GroupsListActivity.class);
						startActivity(intent);
					} else {
						errorFieldValue
								.setText("This name is not available. Enter a unique valid group name!");
					}
				} else {
					errorFieldValue
							.setText("This name is not available. Enter a unique valid group name!");
				}
				
			} catch (InterruptedException e) {
				
				errorFieldValue
						.setText("Apologies for any inconvenience caused. There is a problem with the service!");
			} catch (ExecutionException e) {
				
				errorFieldValue
						.setText("Apologies for any inconvenience caused. There is a problem with the service!");
			}

		} else {
			setContentView(R.layout.create_group);
			errorFieldValue.setText("Enter a valid group name!");
		}

	}
	
	public void selectGroupImage(View view) {
		Button button = (Button) findViewById(R.id.selectGroupImageButton);
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
	
	@Override
	public void onBackPressed() {
	    Intent intent = new Intent(this, HomePlanActivity.class);
	    startActivity(intent);
	}

}
