package com.theiyer.whatstheplan;

import java.util.HashMap;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;

public class ContactsHelper extends AsyncTask<String, String, Map<String, String>> {

	private Context mContext;
	private ProgressDialog pDlg;

	public ContactsHelper(Context mContext) {
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
	protected Map<String, String> doInBackground(String... params) {
		// Create a projection that limits the result Cursor
		// to the required columns.
		String[] projection = { ContactsContract.Contacts._ID,
				ContactsContract.Contacts.DISPLAY_NAME
				//ContactsContract.CommonDataKinds.Email.DATA 
				};
		// Get a Cursor over the Contacts Provider.
		Cursor cursor = mContext.getContentResolver().query(
				ContactsContract.Contacts.CONTENT_URI, projection, null, null,
				null);
		// Get the index of the columns.
		int nameIdx = cursor
				.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME);
		int idIdx = cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID);
		
		// Iterate over the result Cursor.
		Map<String, String> emailMap = new HashMap<String, String>();
		while (cursor.moveToNext()) {
			String name = cursor.getString(nameIdx);
			// Extract the unique ID.
			String id = cursor.getString(idIdx);
			String emailId = retriveEmail(id);
			if(emailId != null){
				emailMap.put(emailId, name);
			}
			
		}
		// Close the Cursor.
		cursor.close();
		return emailMap;
	}

	@Override
	protected void onPostExecute(Map<String, String> response) {
		pDlg.dismiss();
	}

	private String retriveEmail(String id) {
		Cursor cursor = null;
		try {

			// query for everything email
			cursor = mContext.getContentResolver().query(Email.CONTENT_URI,
					null, Email.CONTACT_ID + "=?", new String[] { id }, null);

			int emailIdx = cursor.getColumnIndex(Email.DATA);

			// let's just get the first email
			if (cursor.moveToFirst()) {
				do {
					String email = cursor.getString(emailIdx);
					if(email.equals("prakashk16@gmail.com")){
					}
					
                    return email;
				} while (cursor.moveToNext());
			} else {
				//System.out.println("No results");
			}

		} catch (Exception e) {
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return null;
	}

}
