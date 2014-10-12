package com.theiyer.whatstheplan;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.RadioButton;

public class NewRegistrationPage extends Activity implements OnItemSelectedListener{
	
	private String radioSelected;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
	if(haveInternet(this)){
		setContentView(R.layout.doc_patient);
		ActionBar aBar = getActionBar();
		Resources res = getResources();
		Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
		aBar.setBackgroundDrawable(actionBckGrnd);
		aBar.setTitle(" Welcome to Health Meet");
	    } else {
			Intent intent = new Intent(this, RetryActivity.class);
			startActivity(intent);
		}
	}
	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}
	public void onRadioButtonClicked(View view) {
	    // Is the button now checked?
	    boolean checked = ((RadioButton) view).isChecked();
	    
	    // Check which radio button was clicked
	    switch(view.getId()) {
	        case R.id.invidual:
	            if (checked) {
	                System.out.println("invidual ****");
	                setRadioSelected("invidual");
	        }
	            break;
	        case R.id.health_centre:
	            if (checked) {
	            System.out.println("HEALTH CENTER ****");
	            setRadioSelected("health_centre");
	        }
	            break;
	    }
	}
	public void proceedToRegister (View view) {
		if (getRadioSelected() != null && getRadioSelected() != "") {
			if (getRadioSelected() == "invidual") {
		    Intent intent = new Intent(this, NewUserSignUpActivity.class);
		    startActivity(intent);
			} else if (getRadioSelected() == "health_centre") {
				Intent intent = new Intent(this, NewHealthCenterSignUpActivity	.class);
			    startActivity(intent);
			}
		} else {
			Toast.makeText(getApplicationContext(),
					"Can I please know who you are?", Toast.LENGTH_LONG).show();
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
	public String getRadioSelected() {
		return radioSelected;
	}
	public void setRadioSelected(String radioSelected) {
		this.radioSelected = radioSelected;
	}

}
