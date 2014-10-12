package com.theiyer.whatstheplan;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class EmergencyCallTabFragment extends Fragment {
	Activity activity;
	View rootView = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity = this.getActivity();
			rootView = inflater.inflate(R.layout.emergency_tab, container,
					false);

			SharedPreferences prefs = activity.getSharedPreferences("Prefs",
					Activity.MODE_PRIVATE);
			String name = prefs.getString("userName", "");
			TextView textView = (TextView) rootView.findViewById(R.id.emergencyTabLabel);
			textView.setText(name + ", Find your emergency numbers below.");
			return rootView;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}
}