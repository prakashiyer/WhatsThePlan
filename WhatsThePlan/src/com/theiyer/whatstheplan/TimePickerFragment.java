package com.theiyer.whatstheplan;

import java.util.Calendar;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.TextView;
import android.widget.TimePicker;

public class TimePickerFragment extends DialogFragment implements
		TimePickerDialog.OnTimeSetListener {

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the current time as the default values for the picker
		final Calendar c = Calendar.getInstance();
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);

		// Create a new instance of TimePickerDialog and return it
		return new TimePickerDialog(getActivity(), this, hour, minute, false);
//				DateFormat.is24HourFormat(getActivity()));
	}

	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		TextView planTimeEditText = (TextView) getActivity().findViewById(R.id.newPlanTimeValue);
		String hour = String.valueOf(hourOfDay);
		String min = String.valueOf(minute);
		
		if(hourOfDay<10){
			hour = "0"+hour;
		}
		if(minute<10){
			min = "0"+min;
		}
		int hourInt = Integer.valueOf(hour);
    	String ampm = "AM";
    	if(hourInt > 12){
    		hour = String.valueOf(hourInt - 12);
    		if(Integer.valueOf(hour) < 10){
    			hour = "0"+hour;
    		}
    		
    		ampm = "PM";
    	}
		planTimeEditText.setText(hour+":"+min+" "+ampm);
	}
}
