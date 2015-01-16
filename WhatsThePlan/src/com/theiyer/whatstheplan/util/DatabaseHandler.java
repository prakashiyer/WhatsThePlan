package com.theiyer.whatstheplan.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHandler extends SQLiteOpenHelper {

	public static final String KEY_ID = "_id";

	public static final String USER_NAME = "USER_NAME";
	public static final String PASSWORD = "PASSWORD";
	public static final String EMAIL_ID = "EMAIL_ID";
	private static final String DATABASE_NAME = "whatsThePlan.db";
	private static final String DATABASE_TABLE = "UserInformation";
	private static final int DATABASE_VERSION = 1;

	// SQL Statement to create Database
	private static final String DATABASE_CREATE = "create table "
			+ DATABASE_TABLE + " (" + KEY_ID
			+ " integer primary key autoincrement, " + USER_NAME
			+ " text not null, " + EMAIL_ID + " text not null unique, "
			+ PASSWORD + " text not null);";

	public DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.w("Table Creation","Creating Table with query "+DATABASE_CREATE);
		db.execSQL(DATABASE_CREATE);
		Log.w("Table Creation","Table creation complete... ");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Log the version update
		Log.w("TaskDBAdapter", "Upgrading from version " + oldVersion + " to "
				+ newVersion + ", which will destroy all old data");

		db.execSQL("DROP TABLE IF IT EXISTS " + DATABASE_TABLE);
		onCreate(db);
	}

	/**
	 * All CRUD(Create, Read, Update, Delete) Operations
	 */

	// Adding new user
	public boolean addUserEntry(String inName, String inEmailId, String inPassword) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(USER_NAME, inName); // User Name
		values.put(EMAIL_ID, inEmailId); // User Email
		values.put(PASSWORD, inPassword); // User Password

		Log.w("Inserting New User","Details: "+inName+", "+inEmailId+", "+inPassword);
		// Inserting Row
		long id = db.insert(DATABASE_TABLE, null, values);
		db.close(); // Closing database connection
		if(id > -1){
			Log.w("Inserting New User","New User added succesfully.");
			return true;
		}
		Log.w("Inserting New User","New User addition failed.");
		return false;
	}

	// Authorize existing user
	public String isUserAuthorized(String inEmailId, String inPassword) {
		
		String[] result_columns = new String[] { KEY_ID, USER_NAME, EMAIL_ID,
				PASSWORD };

		String where = EMAIL_ID + "=?" + " AND " + PASSWORD + "=?";
		String[] whereArgs = { inEmailId, inPassword };
		String groupBy = null;
		String having = null;
		String order = null;

		SQLiteDatabase db = this.getWritableDatabase();
		Log.w("Checking Old User","Details: "+inEmailId+", "+inPassword);
		Cursor cursor = db.query(DATABASE_TABLE, result_columns, where,
				whereArgs, groupBy, having, order);
		if(cursor!=null && cursor.moveToFirst()){
			Log.w("Checking Old User","Found cursor successfully."+cursor.getColumnCount());
			int passwordColumnIndex = cursor.getColumnIndex(PASSWORD);
			Log.w("Checking Old User","Found password index."+passwordColumnIndex);
			if(passwordColumnIndex > -1){
				String passwordValue = cursor.getString(passwordColumnIndex);
				Log.w("Checking Old User","Found password: "+passwordValue);
				if(inPassword.equals(passwordValue)){
					int nameColumnIndex = cursor.getColumnIndex(USER_NAME);
					if(nameColumnIndex > -1){
						String userName = cursor.getString(nameColumnIndex);
						Log.w("Checking Old User","Found user name: "+userName);
						cursor.close();
						return userName;
					}
				}
			}
		}
		
		cursor.close();
		db.close();
		Log.w("Checking Old User","User Not authorized.");
		return null;
	}
	
	/**
	 * Check if the database already exist to avoid re-copying the file each time you open the  
	     application.
	 * @return true if it exists, false if it doesn't
	 *//*
	private boolean checkDataBase(){

	    SQLiteDatabase checkDB = null;

	    try{
	        String myPath = DATABASE_NAME;
	        checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

	    }catch(SQLiteException e){

	    }

	    if(checkDB != null){
	        checkDB.close();
	    }

	    return checkDB != null ? true : false;
	}*/

}
