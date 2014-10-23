package com.theiyer.whatstheplan.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import android.annotation.SuppressLint;

public class WhatstheplanUtil {
	@SuppressLint("SimpleDateFormat")
	public static String[] createLocalToGmtTime(String dateTime) {
		String[] dateStr = new String[2];
		try {
			SimpleDateFormat gmtFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date dateTOConvert = gmtFormat.parse(dateTime);
			TimeZone gmtTime = TimeZone.getTimeZone("GMT");
			gmtFormat.setTimeZone(gmtTime);
			String gmtDate = gmtFormat.format(dateTOConvert);
			dateStr = gmtDate.split(" ");
		} catch (ParseException e) {
		}
		return dateStr;
	}
	
	@SuppressLint("SimpleDateFormat")
	public static String[] createGmtToLocalTime(String dateTime) {
		String[] dateStr = new String[2];
		try {
			SimpleDateFormat gmtFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			gmtFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
			Date dateTOConvert = gmtFormat.parse(dateTime);
			
			SimpleDateFormat localFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			localFormat.setTimeZone(TimeZone.getDefault());
			String localDate = localFormat.format(dateTOConvert);
			dateStr = localDate.split(" ");
		} catch (ParseException e) {
		}
		return dateStr;
	}
}
