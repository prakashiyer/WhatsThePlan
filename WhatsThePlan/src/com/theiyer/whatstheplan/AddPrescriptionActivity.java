package com.theiyer.whatstheplan;

import android.app.ActionBar;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AddPrescriptionActivity extends Fragment {
	private View rootView;
	private Activity activity;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity = this.getActivity();
		ActionBar aBar =activity.getActionBar();
		Resources res = getResources();
		Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
		aBar.setBackgroundDrawable(actionBckGrnd);
		aBar.setTitle(" Add Prescriptions");
		
		rootView = inflater.inflate(R.layout.create_prescription, container, false);
		return rootView;
	}

	
	
	public void setActivity(Activity activity) {
		this.activity = activity;
	}
}
