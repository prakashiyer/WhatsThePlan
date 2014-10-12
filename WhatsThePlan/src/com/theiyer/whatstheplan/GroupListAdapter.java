package com.theiyer.whatstheplan;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.theiyer.whatstheplan.entity.Center;

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
	    private List<Map<String, Center>> data; 
	    public List<Map<String, Center>> getData() {
			return data;
		}

		public void setData(List<Map<String, Center>> groupsList) {
			this.data = groupsList;
		}

		private static LayoutInflater inflater=null;
	 
		public GroupListAdapter(Activity a) {
	        activity = a;
	        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    }
		
	    public GroupListAdapter(Activity a, List<Map<String, Center>> d) {
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
				view = inflater.inflate(R.layout.groups_grid_row, null);
			}
			ImageView imgView = (ImageView) view.findViewById(R.id.groupGridPicThumbnail);
			TextView textView = (TextView) view.findViewById(R.id.groupGridNameField);
			
			Map<String,Center> selectedMap = data.get(position);
            for(Entry<String,Center> entry: selectedMap.entrySet()){
            	Center group = entry.getValue();
            	textView.setText(group.getName());
            	
            	byte[] image = group.getImage();
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
