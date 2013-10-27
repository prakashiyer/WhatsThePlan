package com.theiyer.whatstheplan;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {
	
    @Override
    public void onReceive(Context context, Intent intent) {
        // Explicitly specify that GcmIntentService will handle the intent.
        ComponentName comp = new ComponentName(context.getPackageName(),
                GCMIntentService.class.getName());
        System.out.println("Received !!!!");
        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, (intent.setComponent(comp)));
        System.out.println("WAKEFUL !!!!");
        setResultCode(Activity.RESULT_OK);
    }
}
