package com.theiyer.whatstheplan;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.theiyer.whatstheplan.util.WTPConstants;

public class ImageRetrieveRestWebServiceClient extends AsyncTask<String, Integer, byte[]> {

	private Context mContext;
	private ProgressDialog pDlg;

	public ImageRetrieveRestWebServiceClient(Context mContext) {
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
	protected byte[] doInBackground(String... params) {
		String method = params[0];
		String path = WTPConstants.SERVICE_PATH+"/"+method;

		if("fetchUserImage".equals(method)){
        	path = path+"?phone="+params[1];
        } else {
        	path = path+"?groupName="+params[1];
        }
		//HttpHost target = new HttpHost(TARGET_HOST);
		HttpHost target = new HttpHost(WTPConstants.TARGET_HOST, 8080);
		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(path);
		HttpEntity results = null;

		try {
			
			HttpResponse response = client.execute(target, get);
			results = response.getEntity(); 
			byte[] byteresult = EntityUtils.toByteArray(results);
			return byteresult;
		} catch (Exception e) {
		}
		return null;
	}

	@Override
	protected void onPostExecute(byte[] response) {
		
		
		if(response != null){
			Bitmap img = BitmapFactory.decodeByteArray(response, 0, response.length);

			Activity activity = (Activity) mContext;
			
			
			//For ViewMyGroupActivity
			ImageView imgView = (ImageView) activity.findViewById(R.id.selectedGroupPicThumbnail);
			if(imgView != null){
				imgView.setImageBitmap(img);
			}
			
		}
		
		pDlg.dismiss();
	}

}
