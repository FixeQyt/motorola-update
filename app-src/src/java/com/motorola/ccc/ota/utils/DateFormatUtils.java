package com.motorola.ccc.ota.utils;

import android.content.Context;
import android.icu.text.DateIntervalFormat;
import android.icu.util.Calendar;
import android.text.format.DateFormat;
import com.motorola.ccc.ota.R;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class DateFormatUtils {
    public static String getCalendarString(Context context, long j) {
        String str = DateFormat.is24HourFormat(context) ? "Hm" : "hm";
        return context.getResources().getString(R.string.relative_date_time, DateFormat.format(DateFormat.getBestDateTimePattern(Locale.getDefault(), "dMMMM"), j), DateFormat.format(DateFormat.getBestDateTimePattern(Locale.getDefault(), str), j));
    }

    public static String getCalendarStringWithoutAt(Context context, long j) {
        return DateFormat.format(DateFormat.getBestDateTimePattern(Locale.getDefault(), "dMMMM".concat(DateFormat.is24HourFormat(context) ? "Hm" : "hm")), j).toString();
    }

    public static String getCalendarDate(long j) {
        return DateFormat.format(DateFormat.getBestDateTimePattern(Locale.getDefault(), "dMMMM"), j).toString();
    }

    public static String getHistoryDate(long j) {
        return DateFormat.format(DateFormat.getBestDateTimePattern(Locale.getDefault(), "dMMMMyyyy"), j).toString();
    }

    public static String getDateTime(long j) {
        if (j == 0) {
            return "";
        }
        return new SimpleDateFormat("dd MMM yy HH:mm").format(new Date(j));
    }

    public static String formatTimeContent(Context context, Calendar calendar) {
        return DateFormat.format(DateFormat.getBestDateTimePattern(Locale.getDefault(), DateFormat.is24HourFormat(context) ? "Hm" : "hm"), calendar.getTime()).toString();
    }

    public static String formatTimeContent(Context context, int i, int i2) {
        Calendar calendar = Calendar.getInstance();
        String bestDateTimePattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), DateFormat.is24HourFormat(context) ? "Hm" : "hm");
        calendar.set(11, i);
        calendar.set(12, i2);
        return DateFormat.format(bestDateTimePattern, calendar.getTime()).toString();
    }

    public static String generateTimeSlotContent(Context context, int i, int i2, int i3, int i4) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(11, i);
        calendar.set(12, i2);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.set(11, i3);
        calendar2.set(12, i4);
        return DateIntervalFormat.getInstance(DateFormat.is24HourFormat(context) ? "Hm" : "hm").format(calendar, calendar2, new StringBuffer(), new FieldPosition(0)).toString();
    }

    public static String getSecurityPatch() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return DateFormat.format(DateFormat.getBestDateTimePattern(Locale.getDefault(), "dMMMMyyyy"), simpleDateFormat.parse(BuildPropReader.getSecurityPatch())).toString();
        } catch (ParseException e) {
            Logger.error("OtaApp", "Date Format exception" + e);
            return null;
        }
    }
}
