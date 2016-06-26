package ink.utils;

import android.util.Log;

import java.util.Calendar;

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
            Log.d("finalMonth", "getCurrentTime: " + finalMonth);
            finalFormat = year + "-" + finalMonth + "-" + day + " " + hour + ":" + minute + ":" + second;
            Log.d("finalFormat", "getCurrentTime: " + finalFormat);
        }

        return finalFormat;
    }
}
