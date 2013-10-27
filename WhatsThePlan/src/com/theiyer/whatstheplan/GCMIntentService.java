package com.theiyer.whatstheplan;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
        .setSmallIcon(R.drawable.ic_stat_name)
        .setContentTitle("GCM Notification")
        .setStyle(new NotificationCompat.BigTextStyle()
        .bigText(msg))
        .setContentText(msg);

        System.out.println("Print notification !!!!");
        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
