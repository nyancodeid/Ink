package kashmirr.social.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.GregorianCalendar;

/**
 * Created by USER on 2017-02-24.
 */

public class AlarmUtils {

    public static void scheduleAlarmWithMinutes(Context context, Class<?> broadcastReceiver, int minutes) {

        Intent intent = new Intent(context, broadcastReceiver);

        final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long finalMillis = new GregorianCalendar().getTimeInMillis() + minutes * 60 * 1000;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarm.setExact(AlarmManager.RTC, finalMillis, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarm.setExactAndAllowWhileIdle(AlarmManager.RTC, finalMillis, pendingIntent);
        } else {
            alarm.set(AlarmManager.RTC, finalMillis, pendingIntent);
        }
    }

    public static void scheduleAlarmWithSeconds(Context context, Class<?> broadcastReceiver, int seconds) {

        Intent intent = new Intent(context, broadcastReceiver);

        final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long finalMillis = new GregorianCalendar().getTimeInMillis() + seconds * 1000;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarm.setExact(AlarmManager.RTC, finalMillis, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarm.setExactAndAllowWhileIdle(AlarmManager.RTC, finalMillis, pendingIntent);
        } else {
            alarm.set(AlarmManager.RTC, finalMillis, pendingIntent);
        }
    }
}
