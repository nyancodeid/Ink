package ink.va.utils;

import android.content.Context;

import com.ink.va.R;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by USER on 2016-06-26.
 */
public class Time {
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

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

    public static String getTimeInHumanFormat(Context context, String time, SimpleDateFormat dateFormatToParse) {

        Date date;
        int minutes;
        int hours;
        int days;
        int month;
        int year;
        int seconds;
        try {
            date = dateFormatToParse.parse(Time.convertToLocalTime(time));
            Period period = new Period(new DateTime(new Date()), new DateTime(date), PeriodType.dayTime());
            seconds = period.getSeconds();
            minutes = period.getMinutes();
            hours = period.getHours();
            days = period.getDays();
            month = period.getMonths();
            year = period.getYears();
        } catch (ParseException e) {
            e.printStackTrace();
            return "N/A";
        }
        String appendableString = context.getString(R.string.seconds);
        int lastTime = seconds;
        if (minutes != 0) {
            lastTime = minutes;
            appendableString = context.getString(R.string.minutes);
        }
        if (year != 0) {
            appendableString = context.getString(R.string.years);
            lastTime = year;
        } else if (month != 0) {
            appendableString = context.getString(R.string.month);
            lastTime = month;
        } else if (days != 0) {
            appendableString = context.getString(R.string.days);
            lastTime = days;
        } else if (hours != 0) {
            appendableString = context.getString(R.string.hours);
            lastTime = hours;
        }

        return Math.abs(lastTime) + " " + appendableString;
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

    /**
     * Return date in specified format.
     *
     * @param milliSeconds Date in milliseconds
     * @return String representing date in specified format
     */
    public static Date convertMillisToDate(long milliSeconds) throws ParseException {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        String parsedDate = formatter.format(calendar.getTime());
        Date dateObject = formatter.parse(parsedDate);
        return dateObject;
    }


    public static Date parseDate(String dateInput) {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);
        try {
            date = simpleDateFormat.parse(dateInput);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static Date parseDate(String dateInput, String dateFormat) {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
        try {
            date = simpleDateFormat.parse(dateInput);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }
}
