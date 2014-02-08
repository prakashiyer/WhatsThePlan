package com.theiyer.whatstheplan;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.theiyer.whatstheplan.util.WTPConstants;

public class NewRestClient extends Activity{
	
	 AsyncHttpClient client = new AsyncHttpClient();
	 String[] allowedContentTypes = new String[] { "image/png", "image/jpeg" };
	private Context context;
	private byte[] imageClient;
	public NewRestClient(Context context){
		this.context = context;
	}

	public void get(String method, RequestParams params, AsyncHttpResponseHandler responseHandler) {
	      //client.get(WTPConstants.SERVICE_PATH+"/"+method, params, responseHandler);
		
	      client.get(WTPConstants.SERVICE_PATH+"/"+method, new BinaryHttpResponseHandler(allowedContentTypes) {
	    	    @Override
	    	    public void onSuccess(byte[] image) {
	    	    	if(image != null){
						//Log.i(TAG,"IN HERE 1");
	    	    		
	    	    		imageClient = image;
						
					}
	    	    }
	    	});
	      
	      Bitmap img = BitmapFactory.decodeByteArray(imageClient, 0, imageClient.length);

			ImageView imgView = (ImageView) findViewById(R.id.selectedGroupPicThumbnail);
			imgView.setImageBitmap(img);
			
	}
}
