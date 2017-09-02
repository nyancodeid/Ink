package kashmirr.social.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import kashmirr.social.utils.RealmHelper;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by USER on 2017-02-22.
 */

public class DeleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            final int notificationId = extras.getInt("notificationId");
            final NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    notificationManager.cancel(notificationId);
                    RealmHelper.getInstance().removeNotificationCount(context, notificationId);
                }
            });
        }
    }
}
