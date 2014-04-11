package com.theiyer.whatstheplan;

import java.util.HashMap;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract;

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
		Map<String, String> phoneMap = new HashMap<String, String>();
		Cursor phones = mContext.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
		while (phones.moveToNext())
		{
		  String name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
		  String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
		  
		  phoneMap.put(phoneNumber, name);

		}
		phones.close();
		
		return phoneMap;
	}

	@Override
	protected void onPostExecute(Map<String, String> response) {
		pDlg.dismiss();
	}

}
