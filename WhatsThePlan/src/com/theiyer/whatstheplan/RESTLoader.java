package com.theiyer.whatstheplan;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import com.theiyer.whatstheplan.RESTLoader.RESTResponse;
import com.theiyer.whatstheplan.util.WTPConstants;

public class RESTLoader extends AsyncTaskLoader<RESTResponse>{

	private String query;
	private String code;
	private String method;
	
	 public static class RESTResponse {
	        private String mData;
	        private String    mCode;
	        private byte[] mImage;
	        
	        public RESTResponse() {
	        }
	        
	        public RESTResponse(String data, String code, byte[] image) {
	            mData = data;
	            mCode = code;
	            mImage = image;
	        }
	        
	        public String getData() {
	            return mData;
	        }
	        
	        public String getCode() {
	            return mCode;
	        }
	        
	        public byte[] getImage(){
	        	return mImage;
	        }
	    }
    
    /**
     * @param context
     * @param mParams
     * @param mRestResponse
     */
    public RESTLoader(Context context, String query, String code, String method) {
		super(context);
		this.query = query;
		this.code = code;
		this.method = method;
	}
    
	@Override
	public RESTResponse loadInBackground() {
		Log.i("IN BACKGROUND LOADING: ",code);
		if(code.equals("image")){
			Log.i("CODE FOUND: ",code);
			String path = WTPConstants.SERVICE_PATH+"/"+method;

			if("fetchUserImage".equals(method)){
	        	path = path+"?phone="+query;
	        } else {
	        	path = path+"?groupName="+query;
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
				Log.i("GOT RESULT: ",byteresult.toString());
				return new RESTResponse(null, code, byteresult);
			} catch (Exception e) {
			}
			return null;
		} else {
			String path = WTPConstants.SERVICE_PATH+query;

			//HttpHost target = new HttpHost(TARGET_HOST);
			HttpHost target = new HttpHost(WTPConstants.TARGET_HOST, 8080);
			HttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet(path);
			HttpEntity results = null;

			try {
				HttpResponse response = client.execute(target, get);
				results = response.getEntity(); 
				String result = EntityUtils.toString(results);
				Log.i("GOT RESULT: ",result);
				return new RESTResponse(result, code, null);
			} catch (Exception e) {
				
			}
			return null;
		}
	}
	
	 @Override
	    public void deliverResult(RESTResponse data) {
	        // Here we cache our response.
	       // mRestResponse = data;
	        super.deliverResult(data);
	    }
	    
	    @Override
	    protected void onStartLoading() {
	        /*if (mRestResponse != null) {
	            // We have a cached result, so we can just
	            // return right away.
	            super.deliverResult(mRestResponse);
	        }
	        
	        // If our response is null or we have hung onto it for a long time,
	        // then we perform a force load.
	        if (mRestResponse == null || System.currentTimeMillis() - mLastLoad >= STALE_DELTA) forceLoad();
	        mLastLoad = System.currentTimeMillis();*/
	    }
	    
	    @Override
	    protected void onStopLoading() {
	        // This prevents the AsyncTask backing this
	        // loader from completing if it is currently running.
	        cancelLoad();
	    }
	    
	    @Override
	    protected void onReset() {
	        super.onReset();
	        
	        // Stop the Loader if it is currently running.
	        onStopLoading();
	        
	       /* // Get rid of our cache if it exists.
	        mRestResponse = null;
	        
	        // Reset our stale timer.
	        mLastLoad = 0;*/
	    }

}
