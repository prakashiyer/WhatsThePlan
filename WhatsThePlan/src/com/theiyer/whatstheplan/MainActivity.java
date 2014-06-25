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
import android.os.Handler;
import android.util.Log;

import com.theiyer.whatstheplan.util.WTPConstants;

public class MainActivity extends Activity {

	private static final String TAG = "MainActivity";

	private static int SPLASH_TIME_OUT = 3000;
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
		aBar.setTitle(" Home of Groups and Plans");

		context = this;
		
		new Handler().postDelayed(new Runnable() {
			 
            /*
            * Showing splash screen with a timer. This will be useful when you
            * want to show case your app logo / company
            */

           @Override
           public void run() {
               // This method will be executed once the timer is over
               // Start your app main activity
           	AccountManager am = AccountManager.get(context); // "this" references the current Context
       		Account[] accounts = am.getAccountsByType(WTPConstants.ACCOUNT_ADDRESS);
       		if(accounts != null && accounts.length > 0){
       			Account account = accounts[0];
       			SharedPreferences prefs = getSharedPreferences("Prefs", Activity.MODE_PRIVATE);
                   SharedPreferences.Editor editor = prefs.edit();
                   editor.putString("userName", am.getUserData(account, "userName"));
                   editor.putString("phone", account.name);
                   editor.apply();
                   setTheme(R.style.AppTheme);
                   Log.i(TAG, "Logging as an existing user: "+account.name);
                   
                   
                   
                   Intent intent = new Intent(context, HomePlanGroupFragmentActivity.class);
                   startActivity(intent);
           	} else {
           		Log.i(TAG, "New User logs in");
           		
           		Intent intent = new Intent(context, NewUserSignUpActivity.class);
        		startActivity(intent);
       		}
           }
       }, SPLASH_TIME_OUT);

	}

}
