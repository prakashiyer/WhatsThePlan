package com.theiyer.whatstheplan;

import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;
import android.widget.TextView;

@SuppressLint("ValidFragment")
public class DateNewPickerFragment extends DialogFragment implements
		DatePickerDialog.OnDateSetListener {

	private String date;
	public DateNewPickerFragment (String date) {
		this.date = date;
	}
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the current date as the default date in the picker
		final Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int day = c.get(Calendar.DAY_OF_MONTH);

		// Create a new instance of DatePickerDialog and return it
		return new DatePickerDialog(getActivity(), this, year, month, day);
	}

	public void onDateSet(DatePicker view, int year, int month, int day) {
		if (date == "start") {
		TextView planDateEditText = (TextView) getActivity().findViewById(R.id.appointmentStartDate);
		String mon = String.valueOf(month+1);
		String date = String.valueOf(day);
		if(month+1 < 10){
			mon = "0"+mon;
		}
		if(day<10){
			date = "0"+date;
		}
		planDateEditText.setText(year+"-"+mon+"-"+date);
		}
		else if (date == "end") {
			TextView planEndDateEditText = (TextView) getActivity().findViewById(R.id.appointmentEndDate);
			String mon = String.valueOf(month+1);
			String date = String.valueOf(day);
			if(month+1 < 10){
				mon = "0"+mon;
			}
			if(day<10){
				date = "0"+date;
			}
			planEndDateEditText.setText(year+"-"+mon+"-"+date);
		}
		else if (date == "birth") {
			TextView planEndDateEditText = (TextView) getActivity().findViewById(R.id.dateOfBirth);
			String mon = String.valueOf(month+1);
			String date = String.valueOf(day);
			if(month+1 < 10){
				mon = "0"+mon;
			}
			if(day<10){
				date = "0"+date;
			}
			planEndDateEditText.setText(year+"-"+mon+"-"+date);
		} else if (date == "prescription") {
			TextView planEndDateEditText = (TextView) getActivity().findViewById(R.id.medDateValue);
			String mon = String.valueOf(month+1);
			String date = String.valueOf(day);
			if(month+1 < 10){
				mon = "0"+mon;
			}
			if(day<10){
				date = "0"+date;
			}
			planEndDateEditText.setText(year+"-"+mon+"-"+date);
		}
	}
}
