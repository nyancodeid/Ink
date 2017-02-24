package ink.va.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

/**
 * Created by USER on 2017-02-24.
 */

public class AlarmUtils {

    public static void scheduleAlarmWithMinutes(Context context, Class<?> broadcastReceiver, int minutes) {

        Intent intent = new Intent(context, broadcastReceiver);

        final PendingIntent pIntent = PendingIntent.getBroadcast(context, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        long firstMillis = System.currentTimeMillis();
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long finalMillis = minutes * 60 * 1000;
        alarm.setInexactRepeating(AlarmManager.RTC, firstMillis,
                finalMillis, pIntent);
    }

    public static void scheduleAlarmWithSeconds(Context context, Class<?> broadcastReceiver, int seconds) {

        Intent intent = new Intent(context, broadcastReceiver);

        final PendingIntent pIntent = PendingIntent.getBroadcast(context, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        long firstMillis = System.currentTimeMillis();
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long finalMillis = seconds * 1000;
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis,
                finalMillis, pIntent);
    }
}
