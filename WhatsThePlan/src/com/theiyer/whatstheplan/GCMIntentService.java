package com.theiyer.whatstheplan;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService {
 
    private static final String PROJECT_ID = "358164918628";
     
    private static final String TAG = "GCMIntentService";
     
    public GCMIntentService()
    {
        super(PROJECT_ID);
        Log.d(TAG, "GCMIntentService init");
    }
     
 
    @Override
    protected void onError(Context ctx, String sError) {
        // TODO Auto-generated method stub
        Log.d(TAG, "Error: " + sError);
         
    }
 
    @Override
    protected void onMessage(Context ctx, Intent intent) {
        String message = intent.getStringExtra("message");
        Log.d(TAG, "Message Received: "+message);
        sendNotification(message); 
        //sendGCMIntent(ctx, message);
         
    }
     
     
    @Override
    protected void onRegistered(Context ctx, String regId) {
        // TODO Auto-generated method stub
        // send regId to your server
        Log.d(TAG, regId);
         
    }
 
    @Override
    protected void onUnregistered(Context ctx, String regId) {
        // TODO Auto-generated method stub
        // send notification to your server to remove that regId
         
    }
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);
        System.out.println("Got Notification!!!!");
        /*PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);*/
        PendingIntent contentIntent = null;

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
        .setSmallIcon(R.drawable.ic_launcher)
        .setContentTitle("Health Meet")
        .setStyle(new NotificationCompat.BigTextStyle()
        .bigText(msg))
        .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS)
        .setAutoCancel(true).setContentText(msg);
        SharedPreferences prefs = getSharedPreferences("Prefs",
				Activity.MODE_PRIVATE);
        
        if (msg.contains("has been created")) {
        	String selectedPlanIndex = null;
        	String temp[] = msg.split(",");
        	selectedPlanIndex = temp[1];
        	SharedPreferences.Editor editor = prefs.edit();
    		editor.putString("selectedPlanIndex", selectedPlanIndex);
    		editor.apply();
        	contentIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, ViewMyNewPlansActivity.class), 0);
        } else if(msg.contains("cancelled")) {
        	contentIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, HomePlanGroupFragmentActivity.class), 0);
        } else if(msg.contains("declined appointment") || msg.contains("accepted appointment")) {
        	String selectedPlanIndex = null;
        	String temp[] = msg.split(",");
        	selectedPlanIndex = temp[1];
        	SharedPreferences.Editor editor = prefs.edit();
    		editor.putString("selectedPlanIndex", selectedPlanIndex);
    		editor.apply();
        	contentIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, ViewMyNewPlansActivity.class), 0);
        } else if (msg.contains("Center edited")) {
        	String centerPhone = null;
        	String temp[] = msg.split(",");
        	centerPhone = temp[1];
        	SharedPreferences.Editor editor = prefs.edit();
    		editor.putString("selectedCenterPhone", centerPhone);
    		editor.apply();
        	contentIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, ViewCenterUpcomingPlansActivity.class), 0);
        } else if (msg.contains("left center")) {
    		contentIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, ViewGroupMembersFragment.class), 0);
        } else if (msg.contains("joined center")) {
    		contentIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, ViewGroupMembersFragment.class), 0);
        }

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
