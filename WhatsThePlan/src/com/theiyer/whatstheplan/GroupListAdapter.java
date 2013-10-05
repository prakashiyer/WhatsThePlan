package com.theiyer.whatstheplan;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class GroupListAdapter extends BaseAdapter {
		 
	    private Activity activity;
	    private List<Map<String, byte[]>> data; 
	    private static LayoutInflater inflater=null;
	 
	    public GroupListAdapter(Activity a, List<Map<String, byte[]>> d) {
	        activity = a;
	        data=d;
	        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    }

		@Override
		public int getCount() {
			return data.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			if(view==null){
				view = inflater.inflate(R.layout.group_list_row, null);
			}
			ImageView imgView = (ImageView) view.findViewById(R.id.groupPicThumbnail);
			TextView textView = (TextView) view.findViewById(R.id.groupNameField);
			
			Map<String,byte[]> selectedMap = data.get(position);
            for(Entry<String,byte[]> entry: selectedMap.entrySet()){
            	textView.setText(entry.getKey());
            	
            	byte[] image = entry.getValue();
            	if (image != null) {
					Bitmap img = BitmapFactory.decodeByteArray(image, 0,
							image.length);
					imgView.setImageBitmap(img);
            	} else {
            		imgView.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_launcher));
            	}
			}
			return view;
		}

}
