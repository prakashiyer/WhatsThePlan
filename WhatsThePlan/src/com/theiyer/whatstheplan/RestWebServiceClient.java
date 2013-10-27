package com.theiyer.whatstheplan;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.theiyer.whatstheplan.util.WTPConstants;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public class RestWebServiceClient extends AsyncTask<String, Integer, String> {

	private Context mContext;
	private ProgressDialog pDlg;

	public RestWebServiceClient(Context mContext) {
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
		String path = WTPConstants.SERVICE_PATH+params[0];

		//HttpHost target = new HttpHost(TARGET_HOST);
		HttpHost target = new HttpHost(WTPConstants.TARGET_HOST, 8080);
		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(path);
		HttpEntity results = null;

		try {
			HttpResponse response = client.execute(target, get);
			results = response.getEntity(); 
			String result = EntityUtils.toString(results);
			return result;
		} catch (Exception e) {
			
		}
		return null;
	}

	@Override
	protected void onPostExecute(String response) {
		pDlg.dismiss();
	}

}
