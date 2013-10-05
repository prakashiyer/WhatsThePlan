package com.theiyer.whatstheplan;

import android.content.Context;
import android.content.Intent;

import com.google.android.gcm.GCMBaseIntentService;

public class MessageReceiver extends GCMBaseIntentService {

	@Override
	protected void onError(Context arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onMessage(Context arg0, Intent arg1) {
		System.out.println("Message Received: " +arg1.getStringExtra("message"));
		
	}

	@Override
	protected void onRegistered(Context arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onUnregistered(Context arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

}
