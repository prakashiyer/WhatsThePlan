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
        .setContentTitle("Just Meet")
        .setStyle(new NotificationCompat.BigTextStyle()
        .bigText(msg))
        .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS)
        .setContentText(msg);
        SharedPreferences prefs = getSharedPreferences("Prefs",
				Activity.MODE_PRIVATE);
        
        if (msg.contains("A new plan has been added")) {
        	String planName = null;
        	String temp[] = msg.split("'");
        	planName = temp[1];
        	System.out.println("***** Plan Name in GCM (Added): " + planName);
        	SharedPreferences.Editor editor = prefs.edit();
    		editor.putString("selectedPlan", planName);
    		editor.apply();
        	contentIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, ViewMyNewPlansActivity.class), 0);
        } else if(msg.contains("deleted")) {
        	contentIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, HomePlanGroupFragmentActivity.class), 0);
        } else if(msg.contains("attending")) {
        	String planName = null;
        	String temp[] = msg.split("'");
        	planName = temp[1];
        	System.out.println("***** Plan Name in GCM (RSVP): " + planName);
        	SharedPreferences.Editor editor = prefs.edit();
    		editor.putString("selectedPlan", planName);
    		editor.apply();
        	contentIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, ViewMyNewPlansActivity.class), 0);
        } else if (msg.contains("edited")) {
        	String planName = null;
        	String temp[] = msg.split("'");
        	planName = temp[1];
        	System.out.println("***** Plan Name in GCM (edited): " + planName);
        	SharedPreferences.Editor editor = prefs.edit();
    		editor.putString("selectedPlan", planName);
    		editor.apply();
        	contentIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, ViewMyNewPlansActivity.class), 0);
        } else if (msg.contains("left the group")) {
        	String groupName = null;
        	String temp[] = msg.split("'");
        	groupName = temp[1];
        	System.out.println("***** Group Name in GCM (left): " + groupName);
        	SharedPreferences.Editor editor = prefs.edit();
    		editor.putString("selectedGroup", groupName);
    		editor.apply();
    		contentIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, ViewGroupMembersActivity.class), 0);
        } else if (msg.contains("expense")) {
        	String groupName = null;
        	String planName = null;
        	String temp[] = msg.split("'");
        	planName = temp[1];
        	groupName = temp[3];
        	System.out.println("***** Plan Name in GCM (expense): " + planName);
        	System.out.println("***** Group Name in GCM (expense): " + groupName);
        	SharedPreferences.Editor editor = prefs.edit();
    		editor.putString("selectedGroup", groupName);
    		editor.putString("selectedPlan", planName);
    		editor.apply();
    		contentIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, ExpenseReportActivity.class), 0);
        }

        System.out.println("Print notification !!!!");
        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
