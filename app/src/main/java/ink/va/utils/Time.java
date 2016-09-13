package ink.va.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by USER on 2016-06-26.
 */
public class Time {
    public static String getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        String finalFormat = year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + second;
        if (month < 10) {
            String finalMonth = "0" + month;
            finalFormat = year + "-" + finalMonth + "-" + day + " " + hour + ":" + minute + ":" + second;
        }

        return finalFormat;
    }

    public static String convertToLocalTime(String timeToConvert) {
        if (Constants.SERVER_TIME_ZONE.equals(TimeZone.getDefault())) {
            return timeToConvert;
        }
        SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sourceFormat.setTimeZone(TimeZone.getTimeZone(Constants.SERVER_TIME_ZONE));
        Date parsed = null;
        try {
            parsed = sourceFormat.parse(timeToConvert);
        } catch (ParseException e) {
            return "N/A";
        }

        TimeZone currentTimeZone = TimeZone.getDefault();
        SimpleDateFormat destFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        destFormat.setTimeZone(currentTimeZone);

        String result = destFormat.format(parsed);
        return result;
    }

    public static Date convertToLocalDate(String timeToConvert) {
        SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (Constants.SERVER_TIME_ZONE.equals(TimeZone.getDefault())) {
            try {
                return sourceFormat.parse(timeToConvert);
            } catch (ParseException e) {
                e.printStackTrace();
                return new Date();
            }
        }

        sourceFormat.setTimeZone(TimeZone.getTimeZone(Constants.SERVER_TIME_ZONE));
        Date parsed = null;
        try {
            parsed = sourceFormat.parse(timeToConvert);
        } catch (ParseException e) {
            return new Date();
        }

        TimeZone tz = TimeZone.getDefault();
        SimpleDateFormat destFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        destFormat.setTimeZone(tz);

        try {
            return destFormat.parse(timeToConvert);
        } catch (ParseException e) {
            return new Date();
        }
    }

    public static String getTimeZone() {
        return TimeZone.getDefault().getID();
    }
}
