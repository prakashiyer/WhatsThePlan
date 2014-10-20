package com.theiyer.whatstheplan;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.theiyer.whatstheplan.ViewGroupMembersActivity.WebImageRetrieveRestWebServiceClient;
import com.theiyer.whatstheplan.entity.Center;
import com.theiyer.whatstheplan.entity.CenterList;
import com.theiyer.whatstheplan.entity.Plan;
import com.theiyer.whatstheplan.entity.PlanList;
import com.theiyer.whatstheplan.entity.User;
import com.theiyer.whatstheplan.entity.UserList;
import com.theiyer.whatstheplan.util.WTPConstants;
import com.thoughtworks.xstream.XStream;

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
}
