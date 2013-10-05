package com.theiyer.whatstheplan;

import java.util.concurrent.ExecutionException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class DeactivateAccountActivity extends Activity {

	private Context context;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		setContentView(R.layout.delete_profile);
		ActionBar aBar = getActionBar();
		Resources res = getResources();
		Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
		aBar.setBackgroundDrawable(actionBckGrnd);
		aBar.setTitle(" De-activate Account Form");

		SharedPreferences prefs = getSharedPreferences("Prefs",
				Activity.MODE_PRIVATE);
		String userName = prefs.getString("userName", "New User");
		TextView welcomeStmnt = (TextView) findViewById(R.id.welcomeDeleteProfileLabel);
		welcomeStmnt.setText(userName + ", manage your profile here!");

		TextView userNameValue = (TextView) findViewById(R.id.deleteProfileName);
		userNameValue.setText("Name: " + userName);

		String emailId = prefs.getString("emailId", "");
		TextView emailIdValue = (TextView) findViewById(R.id.deleteProfileEmailId);
		emailIdValue.setText("Email Id: " + emailId);

	}

	/** Called when the user checks the delet account */
	public void onDeleteProfileChecked(View view) {
		CheckBox checkBox = (CheckBox) findViewById(R.id.deleteCheckBox);

		EditText oldPassword = (EditText) findViewById(R.id.deleteProfilePasswordValue);
		Button deleteButton = (Button) findViewById(R.id.deleteProfileButton);
		if (checkBox.isChecked()) {
			oldPassword.setVisibility(EditText.VISIBLE);
			deleteButton.setVisibility(Button.VISIBLE);
		} else {
			oldPassword.setVisibility(EditText.INVISIBLE);
			deleteButton.setVisibility(Button.INVISIBLE);
		}

	}

	/** Called when the user clicks the delete profile button */
	public void deleteProfile(View view) {
		Button changePassButton = (Button) findViewById(R.id.deleteProfileButton);
		changePassButton.setTextColor(getResources().getColor(R.color.click_button_2));
		SharedPreferences prefs = getSharedPreferences("Prefs",
				Activity.MODE_PRIVATE);
		String currentPass = prefs.getString("password", "");
		String emailId = prefs.getString("emailId", "");
		EditText oldPasswordValue = (EditText) findViewById(R.id.deleteProfilePasswordValue);
		String oldPass = oldPasswordValue.getText().toString();
		TextView errorFieldValue = (TextView) findViewById(R.id.deleteProfileErrorField);
		if (!currentPass.equals(oldPass)) {
			errorFieldValue.setText("Wrong existing password!");
		}
		errorFieldValue.setText("");
		

		String searchQuery = "/deleteAccount?emailId=" + emailId;

		RestWebServiceClient restClient = new RestWebServiceClient(this);
		try {
			restClient.execute(
					new String[] { searchQuery }).get();
			
			 AccountManager am = AccountManager.get(this);
			 Account[] accounts = am.getAccountsByType("com.theiyer.whatstheplan");
				if(accounts != null && accounts.length > 0){
					Account account = accounts[0];
					am.removeAccount(account, new OnTokenAcquired(),          // Callback called when a token is successfully acquired
						    new Handler());
				}
				
			changePassButton.setTextColor(getResources().getColor(R.color.button_text));
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
		} catch (InterruptedException e) {
			
			errorFieldValue
					.setText("Apologies for any inconvenience caused. There is a problem with the service!");
		} catch (ExecutionException e) {
			
			errorFieldValue
					.setText("Apologies for any inconvenience caused. There is a problem with the service!");
		}
	}
	
	private class OnTokenAcquired implements AccountManagerCallback<Boolean> {
	    @Override
	    public void run(AccountManagerFuture<Boolean> result) {
	        // Get the result of the operation from the AccountManagerFuture.
	    	Intent intent = new Intent(context, MainActivity.class);
			startActivity(intent);
	    }
	}
}
