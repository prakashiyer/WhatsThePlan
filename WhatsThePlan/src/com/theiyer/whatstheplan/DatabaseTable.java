package com.theiyer.whatstheplan;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

public class DatabaseTable {

	 private static final String TAG = "DictionaryDatabase";

	    //The columns we'll include in the dictionary table
	    public static final String COL_EMAIL = "EMAIL";
	    public static final String COL_NAME = "NAME";

	    private static final String DATABASE_NAME = "CONTACTS";
	    private static final String FTS_VIRTUAL_TABLE = "FTS";
	    private static final int DATABASE_VERSION = 1;
	    private Context context;
	    
	    private Map<String, String> contactsMap;

	    private final DatabaseOpenHelper mDatabaseOpenHelper;

	    public DatabaseTable(Context context, Map<String, String> contactsMap) {
	    	this.context = context;
	        mDatabaseOpenHelper = new DatabaseOpenHelper(context);
	        this.contactsMap = contactsMap;
	    }
	    
	    public Cursor getEmailMatches(String query, String[] columns) {
	    	mDatabaseOpenHelper.loadContacts();
	        String selection = COL_EMAIL + " = ?";
	        String[] selectionArgs = new String[] {query};

	        return query(selection, selectionArgs, new String[]{COL_EMAIL, COL_NAME});
	    }
	    
	    public String getName(String query){
	    	return contactsMap.get(query);
	    }

	    private Cursor query(String selection, String[] selectionArgs, String[] columns) {
	        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
	        builder.setTables(FTS_VIRTUAL_TABLE);

	        Cursor cursor = builder.query(mDatabaseOpenHelper.getReadableDatabase(),
	                columns, selection, selectionArgs, null, null, null);

	        if (cursor == null) {
	            return null;
	        } else if (!cursor.moveToFirst()) {
	            cursor.close();
	            return null;
	        }
	        return cursor;
	    }

	    private class DatabaseOpenHelper extends SQLiteOpenHelper {

	        private final Context mHelperContext;
	        private SQLiteDatabase mDatabase;

	        private static final String FTS_TABLE_CREATE =
	                    "CREATE VIRTUAL TABLE " + FTS_VIRTUAL_TABLE +
	                    " USING fts3 (" +
	                    COL_EMAIL + ", " +
	                    COL_NAME + ")";

	        DatabaseOpenHelper(Context context) {
	            super(context, DATABASE_NAME, null, DATABASE_VERSION);
	            mHelperContext = context;
	        }

	        @Override
	        public void onCreate(SQLiteDatabase db) {
	            mDatabase = db;
	            db.execSQL(FTS_TABLE_CREATE);
	        }

	        @Override
	        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
	                    + newVersion + ", which will destroy all old data");
	            db.execSQL("DROP TABLE IF EXISTS " + FTS_VIRTUAL_TABLE);
	            onCreate(db);
	        }
	        
	        public void loadContacts() {
	            new Thread(new Runnable() {
	                public void run() {
	                    try {
	                        loadEmails();
	                    } catch (IOException e) {
	                        throw new RuntimeException(e);
	                    }
	                }
	            }).start();
	        }

	    private void loadEmails() throws IOException {
	    	if (contactsMap != null && !contactsMap.isEmpty()) {
				for(Entry<String, String> entry: contactsMap.entrySet()){
					addData(entry.getKey(), entry.getValue());
				}
			}
	    }

	    public long addData(String email, String name) {
	    	SQLiteDatabase db = this.getWritableDatabase();
	        ContentValues initialValues = new ContentValues();
	        if(email.equals("prakashk16@gmail.com")){
			}
	        initialValues.put(COL_EMAIL, email);
	        initialValues.put(COL_NAME, name);

	        return db.insert(FTS_VIRTUAL_TABLE, null, initialValues);
	        
	    }
	    }
}
