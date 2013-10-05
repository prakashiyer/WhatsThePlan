package com.theiyer.whatstheplan;

import java.io.File;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.theiyer.whatstheplan.util.WTPConstants;

public class ImageRestWebServiceClient extends AsyncTask<String, Integer, byte[]> {

	private Context mContext;
	private ProgressDialog pDlg;

	public ImageRestWebServiceClient(Context mContext) {
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
		String path = "/WhatsThePlan/operation/"+method;

		//HttpHost target = new HttpHost(TARGET_HOST);
		HttpHost target = new HttpHost(WTPConstants.TARGET_HOST, 8080);
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(path);
		HttpEntity results = null;
		try {
	        MultipartEntity entity = new MultipartEntity();
	       
	        if("uploadUserImage".equals(method)){
	        	entity.addPart("emailId", new StringBody(params[1]));
	        } else {
	        	entity.addPart("groupName", new StringBody(params[1]));
	        }
	        
	        entity.addPart("image", new FileBody(new File(params[2])));
	        post.setEntity(entity);

	        HttpResponse response = client.execute(target, post);
	        results = response.getEntity(); 
			byte[] byteresult = EntityUtils.toByteArray(results);
			return byteresult;
		} catch (Exception e) {
			
		}
		return null;
	}

	@Override
	protected void onPostExecute(byte[] response) {
		pDlg.dismiss();
	}

}
