package com.theiyer.whatstheplan;

import java.util.concurrent.ExecutionException;

import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class ViewProfileActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_profile);
		ActionBar aBar = getActionBar();
		Resources res = getResources();
		Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
		aBar.setBackgroundDrawable(actionBckGrnd);
		aBar.setTitle(" Profile Details");

		SharedPreferences prefs = getSharedPreferences("Prefs",
				Activity.MODE_PRIVATE);
		String userName = prefs.getString("userName", "New User");
		TextView welcomeStmnt = (TextView) findViewById(R.id.welcomeViewProfileLabel);
		welcomeStmnt.setText(userName + ", Your profile details!");

		TextView userNameValue = (TextView) findViewById(R.id.viewProfileName);
		userNameValue.setText(userName);
		
		String phone = prefs.getString("phone", "");
		TextView phoneValue = (TextView) findViewById(R.id.viewProfilePhone);
		phoneValue.setText("Phone: " + phone);

		ImageRetrieveRestWebServiceClient userImageClient = new ImageRetrieveRestWebServiceClient(
				this);
		try {
			ImageView imgView = (ImageView) findViewById(R.id.viewProfilePicThumbnail);
			byte[] userImage = userImageClient.execute(new String[] { "fetchUserImage", phone }).get();
			if (userImage != null) {
				Bitmap img = BitmapFactory.decodeByteArray(userImage, 0,
						userImage.length);

				imgView.setImageBitmap(img);
        	}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			
		}

	}
}
