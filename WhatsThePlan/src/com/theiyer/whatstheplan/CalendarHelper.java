package com.theiyer.whatstheplan;

import java.util.Calendar;
import java.util.TimeZone;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.util.Log;

public class CalendarHelper extends AsyncTask<String, String, String> {

	// Projection array. Creating indices for this array instead of doing
	// dynamic lookups improves performance.
	public static final String[] EVENT_PROJECTION = new String[] {
	    Calendars._ID,                           // 0
	    Calendars.ACCOUNT_NAME,                  // 1
	    Calendars.CALENDAR_DISPLAY_NAME,         // 2
	    Calendars.OWNER_ACCOUNT                  // 3
	};
	  
	
	
	private Context mContext;
	private ProgressDialog pDlg;
	private static final String DEBUG_TAG = "CalendarActivity";

	public CalendarHelper(Context mContext) {
		this.mContext = mContext;
	}

	private void showProgressDialog() {

		pDlg = new ProgressDialog(mContext);
		pDlg.setMessage("Processing ....");
		pDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		pDlg.setCancelable(false);
		pDlg.show();

	}
	
	@Override
	protected void onPreExecute() {
		showProgressDialog();

	}

	@Override
	protected String doInBackground(String... params) {
		
		// Run query
		Cursor cur = null;
		ContentResolver cr = mContext.getContentResolver();
		Uri uri = Events.CONTENT_URI;   
		/*String selection = "((" + Calendars.ACCOUNT_NAME + " = ?) AND (" 
		                        + Calendars.ACCOUNT_TYPE + " = ?) AND ("
		                        + Calendars.OWNER_ACCOUNT + " = ?))";*/
		/*String[] selectionArgs = new String[] {params[1], "com.google",
				params[1]}; */
		// Submit the query and get a Cursor object back. 
		//cur = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);
		
		uri = asSyncAdapter(uri, params[4],"com.google");
		
		
		
		
		
		
		
		
		
        ContentValues values = new ContentValues();
        String date = params[0];
        Calendar calendar = Calendar.getInstance();
        calendar.set(Integer.valueOf(date.substring(0,4)),
        		(Integer.valueOf(date.substring(5,7)) - 1), 
        		Integer.valueOf(date.substring(8,10)), 
        		Integer.valueOf(date.substring(11,13)), 
        		Integer.valueOf(date.substring(14,16)));
        long setDate = calendar.getTimeInMillis();
        calendar.add(Calendar.HOUR, 5);
        long end = calendar.getTimeInMillis();
        
        values.put(Events.DTSTART, setDate);
        values.put(Events.DTEND, end);
        //values.put(Events.DURATION, "PT1H");
        values.put(Events.TITLE, params[1]);
        values.put(Events.DESCRIPTION, params[2]);
        values.put(Events.CALENDAR_ID, Integer.valueOf(params[3]));

        values.put(Events.ALL_DAY, false);

        //values.put(Events.HAS_ALARM, true);

        //Get current timezone
        values.put(Events.EVENT_TIMEZONE,TimeZone.getDefault().getID());
        Log.i(DEBUG_TAG, "Timezone retrieved=>"+TimeZone.getDefault().getID());
        uri = cr.insert(uri, values);
        Log.i(DEBUG_TAG, "Uri returned=>"+uri.toString());
        // get the event ID that is the last element in the Uri
        long eventID = Long.parseLong(uri.getLastPathSegment());

        /*ContentValues reminders = new ContentValues();
        reminders.put(Reminders.EVENT_ID, eventID);
        reminders.put(Reminders.METHOD, Reminders.METHOD_ALERT);
        reminders.put(Reminders.MINUTES, 15);
        cr.insert(Reminders.CONTENT_URI, reminders);*/
        return String.valueOf(eventID);
	}
	
	@Override
	protected void onPostExecute(String response) {
		pDlg.dismiss();
	}
	
	private static Uri asSyncAdapter(Uri uri, String account, String accountType) {
	    return uri.buildUpon()
	        .appendQueryParameter(android.provider.CalendarContract.CALLER_IS_SYNCADAPTER,"true")
	        .appendQueryParameter(Calendars.ACCOUNT_NAME, account)
	        .appendQueryParameter(Calendars.ACCOUNT_TYPE, accountType).build();
	 }

}
