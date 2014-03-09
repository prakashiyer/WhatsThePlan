package com.theiyer.whatstheplan;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class PlanListAdapter extends BaseAdapter {
		 
	    private Activity activity;
	    private List<Map<String, String>> data; 
	    public List<Map<String, String>> getData() {
			return data;
		}

		public void setData(List<Map<String, String>> data) {
			this.data = data;
		}

		private static LayoutInflater inflater=null;
		
		 public PlanListAdapter(Activity a) {
		        activity = a;
		        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		 }
	 
	    public PlanListAdapter(Activity a, List<Map<String, String>> d) {
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
				view = inflater.inflate(R.layout.plan_list_row, null);
			}
			TextView planName = (TextView) view.findViewById(R.id.planName);
			TextView planTime = (TextView) view.findViewById(R.id.planTime);
			
			Map<String,String> selectedMap = data.get(position);
            for(Entry<String,String> entry: selectedMap.entrySet()){
            	planName.setText("Plan  |    "+entry.getKey());
            	String date = entry.getValue().substring(0, 10);
            	String time = entry.getValue().substring(11, 16);
            	String hour = time.substring(0, 2);
            	String min = time.substring(3);
            	int hourInt = Integer.valueOf(hour);
            	String ampm = "AM";
            	if(hourInt > 12){
            		hour = String.valueOf(hourInt - 12);
            		if(Integer.valueOf(hour) < 10){
            			hour = "0"+hour;
            		}
            		ampm = "PM";
            	}
            	planTime.setText("When   |    "+date+" "+hour+":"+min+" "+ampm);
			}
			return view;
		}

}
