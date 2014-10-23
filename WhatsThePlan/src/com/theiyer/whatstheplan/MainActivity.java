package com.theiyer.whatstheplan;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.theiyer.whatstheplan.util.WTPConstants;

public class MainActivity extends Activity {

	private static final String TAG = "MainActivity";
	private Context context;
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setTheme(R.style.AppTheme);

		setContentView(R.layout.activity_main);
		ActionBar aBar = getActionBar();
		Resources res = getResources();
		Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
		aBar.setBackgroundDrawable(actionBckGrnd);
		aBar.setTitle(" Network for doctors and patient");

		context = this;	
		
		AccountManager am = AccountManager.get(context); // "this" references the current Context
   		Account[] accounts = am.getAccountsByType(WTPConstants.ACCOUNT_ADDRESS);
   		if(accounts != null && accounts.length > 0){
   			Account account = accounts[0];
   			SharedPreferences prefs = getSharedPreferences("Prefs", Activity.MODE_PRIVATE);
               SharedPreferences.Editor editor = prefs.edit();
               editor.putString("userName", am.getUserData(account, "userName"));
               editor.putString("docFlag", am.getUserData(account, "doctor"));
               editor.putString("centerFlag", am.getUserData(account, "center"));
               editor.putString("phone", account.name);
               editor.apply();
               setTheme(R.style.AppTheme);
               if (am.getUserData(account, "center") == "Y") {
            	   Log.i(TAG, "Logging as an existing center: "+account.name);
            	   Intent intent = new Intent(context, HomePlanGroupFragmentActivity.class);
                   startActivity(intent);
               } else {
            	   Log.i(TAG, "Logging as an existing user: "+account.name);
                   Intent intent = new Intent(context, HomePlanGroupFragmentActivity.class);
                   startActivity(intent);
               }
       	} else {
       		Log.i(TAG, "New User logs in");
       		
       		Intent intent = new Intent(context, NewRegistrationPage.class);
    		startActivity(intent);
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
}
