package kashmirr.social.utils;

import android.app.*;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

import com.kashmirr.social.R;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by Vladimir_Arevshatyan on 9/19/2017.
 */

public class NotificationUtils {

    public static void sendNotification(int notificationId, Context context, String title, String text) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle(title);
        builder.setContentText(text);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(text));
        android.app.Notification notification = builder.build();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, notification);
    }
}
