package com.theiyer.whatstheplan;

import java.util.concurrent.ExecutionException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.theiyer.whatstheplan.entity.UserInformation;
import com.thoughtworks.xstream.XStream;

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
		welcomeStmnt.setText(userName + ", manage your profile here!");

		TextView userNameValue = (TextView) findViewById(R.id.viewProfileName);
		userNameValue.setText(userName);
		
		String emailId = prefs.getString("emailId", "");
		TextView emailIdValue = (TextView) findViewById(R.id.viewProfileEmailId);
		emailIdValue.setText("Email Id: " + emailId);

		ImageRetrieveRestWebServiceClient userImageClient = new ImageRetrieveRestWebServiceClient(
				this);
		try {
			ImageView imgView = (ImageView) findViewById(R.id.viewProfilePicThumbnail);
			byte[] userImage = userImageClient.execute(new String[] { "fetchUserImage", emailId }).get();
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

	/** Called when the user checks the change password */
	public void onChangePasswordChecked(View view) {
		CheckBox checkBox = (CheckBox) findViewById(R.id.changePasswordCheckBox);

		EditText oldPassword = (EditText) findViewById(R.id.changePasswordOldValue);
		EditText newPassword = (EditText) findViewById(R.id.changePasswordNewValue);
		EditText reTypePassword = (EditText) findViewById(R.id.retypePasswordNewValue);
		Button changePassButton = (Button) findViewById(R.id.changePasswordButton);
		if (checkBox.isChecked()) {
			oldPassword.setVisibility(EditText.VISIBLE);
			newPassword.setVisibility(EditText.VISIBLE);
			reTypePassword.setVisibility(EditText.VISIBLE);
			changePassButton.setVisibility(Button.VISIBLE);
		} else {
			oldPassword.setVisibility(EditText.INVISIBLE);
			newPassword.setVisibility(EditText.INVISIBLE);
			reTypePassword.setVisibility(EditText.INVISIBLE);
			changePassButton.setVisibility(Button.INVISIBLE);
		}

	}

	/** Called when the user clicks the change password button */
	public void changePassword(View view) {
		Button changePassButton = (Button) findViewById(R.id.changePasswordButton);
		changePassButton.setTextColor(getResources().getColor(R.color.click_button_2));
		SharedPreferences prefs = getSharedPreferences("Prefs",
				Activity.MODE_PRIVATE);
		String currentPass = prefs.getString("password", "");
		String emailId = prefs.getString("emailId", "");
		EditText oldPasswordValue = (EditText) findViewById(R.id.changePasswordOldValue);
		String oldPass = oldPasswordValue.getText().toString();
		TextView errorFieldValue = (TextView) findViewById(R.id.viewProfileErrorField);
		if (!currentPass.equals(oldPass)) {
			errorFieldValue.setText("Wrong existing password!");
		}
		errorFieldValue.setText("");
		EditText newPasswordValue = (EditText) findViewById(R.id.changePasswordNewValue);
		String newPass = newPasswordValue.getText().toString();
		EditText retypePasswordValue = (EditText) findViewById(R.id.retypePasswordNewValue);
		String retypePass = retypePasswordValue.getText().toString();

		if (newPass != null && newPass.equals(retypePass)) {
			String searchQuery = "/updatePassword?emailId=" + emailId
					+ "&password=" + newPass;

			RestWebServiceClient restClient = new RestWebServiceClient(this);
			try {
				String response = restClient.execute(
						new String[] { searchQuery }).get();

				if (response != null) {
					XStream xstream = new XStream();
					xstream.alias("UserInformation", UserInformation.class);
					xstream.alias("groupNames", String.class);
					xstream.addImplicitCollection(UserInformation.class, "groupNames","groupNames",String.class);
					xstream.alias("pendingGroupNames", String.class);
					xstream.addImplicitCollection(UserInformation.class, "pendingGroupNames","pendingGroupNames",String.class);
					UserInformation user = (UserInformation) xstream
							.fromXML(response);
					if (user != null) {
						AccountManager am = AccountManager.get(this); // "this" references the current Context
						Account[] accounts = am.getAccountsByType("com.theiyer.whatstheplan");
						if(accounts != null && accounts.length > 0){
							Account account = accounts[0];
							am.setPassword(account, newPass);
						}
						CheckBox checkBox = (CheckBox) findViewById(R.id.changePasswordCheckBox);
						checkBox.setChecked(false);
						
						oldPasswordValue.setVisibility(EditText.INVISIBLE);
						newPasswordValue.setVisibility(EditText.INVISIBLE);
						retypePasswordValue.setVisibility(EditText.INVISIBLE);
						oldPasswordValue.setText("");
						newPasswordValue.setText("");
						retypePasswordValue.setText("");
						changePassButton.setVisibility(Button.INVISIBLE);
						changePassButton.setTextColor(getResources().getColor(R.color.button_text));
					} else {
						errorFieldValue
								.setText("Apologies for any inconvenience caused. There is a problem with the service!");
					}
				} else {
					errorFieldValue
							.setText("Apologies for any inconvenience caused. There is a problem with the service!");
				}
			} catch (InterruptedException e) {
				
				errorFieldValue
						.setText("Apologies for any inconvenience caused. There is a problem with the service!");
			} catch (ExecutionException e) {
				
				errorFieldValue
						.setText("Apologies for any inconvenience caused. There is a problem with the service!");
			}
		}
	}
}
