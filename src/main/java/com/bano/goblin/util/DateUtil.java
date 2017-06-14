package com.bano.goblin.util;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@SuppressLint("SimpleDateFormat")
public class DateUtil {
    private static final String TAG = "DateUtil";

    public static final String DATE_PATTERN_BD_TIMEZONE = "yyyy-MM-dd HH:mm:ss z";
    public static final String API_DATE_FORMAT_KEY = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String DATE_PATTERN_US = "yyyy-MM-dd";

    public static String getDateString(String pattern) {
        SimpleDateFormat df = new SimpleDateFormat(pattern);
        Date now = Calendar.getInstance().getTime();
        return df.format(now);
    }

    public static long getDifferenceInSeconds(Calendar higherDate, Calendar lowDate){
        if(higherDate == null || lowDate == null) return 0;
        return (higherDate.getTimeInMillis() - lowDate.getTimeInMillis()) / 1000;
    }

    public static long getDifference(Calendar higherDate, Calendar lowDate){
        if(higherDate == null || lowDate == null) return 0;
        return higherDate.getTimeInMillis() - lowDate.getTimeInMillis();
    }

    public static String getDifferenceInMinutes(Calendar higherDate, Calendar lowDate){
        if(higherDate == null || lowDate == null){
            return "0 min";
        }

        long diff = higherDate.getTimeInMillis() - lowDate.getTimeInMillis();
        long diffMinutes = diff / (60 * 1000);
        if(diffMinutes < 60){
            return diffMinutes % 60 + " min";
        }
        else{
            diffMinutes = diffMinutes % 60;

            //Hours
            long diffHours = diff / (60 * 60 * 1000);
            if(diffMinutes > 0) return diffHours + " h " + diffMinutes + " min";
            else return diffHours + " h";
        }
    }

    public static boolean isToday(Calendar c2){
        if(c2 == null) return false;

        Calendar c1 = Calendar.getInstance();

        return c2.get(Calendar.YEAR) == c1.get(Calendar.YEAR)
                && c2.get(Calendar.DAY_OF_YEAR) == c1.get(Calendar.DAY_OF_YEAR);
    }

    public static boolean isSameYear(Calendar c2){
        if(c2 == null) return false;

        Calendar c1 = Calendar.getInstance();
        return c2.get(Calendar.YEAR) == c1.get(Calendar.YEAR);
    }

    public static boolean isYesterday(Calendar c2){
        if(c2 == null) return false;

        Calendar c1 = Calendar.getInstance();
        c1.add(Calendar.DATE, -1);

        return c2.get(Calendar.YEAR) == c1.get(Calendar.YEAR)
                && c2.get(Calendar.DAY_OF_YEAR) == c1.get(Calendar.DAY_OF_YEAR);
    }

    public static String dateToString(Calendar calendar, String pattern) {
        if (calendar != null && pattern != null) {
            SimpleDateFormat format = new SimpleDateFormat(pattern);
            return format.format(calendar.getTime());
        }
        return null;
    }
}
