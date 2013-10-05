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

public class InviteListAdapter extends BaseAdapter {
		 
	    private Activity activity;
	    private List<Map<String, String>> data; 
	    private static LayoutInflater inflater=null;
	 
	    public InviteListAdapter(Activity a, List<Map<String, String>> d) {
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
				view = inflater.inflate(R.layout.invite_list_row, null);
			}
			TextView contactName = (TextView) view.findViewById(R.id.contactName);
			TextView contactEmail = (TextView) view.findViewById(R.id.contactEmail);
			Map<String,String> emailMap = data.get(position);
            for(Entry<String,String> entry: emailMap.entrySet()){
            	contactName.setText("Name  |    "+entry.getKey());
            	contactEmail.setText("Email   |    "+entry.getValue());
			}
			return view;
		}
		
		

}
