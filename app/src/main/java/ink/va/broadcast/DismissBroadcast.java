package ink.va.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import ink.va.utils.SharedHelper;

/**
 * Created by USER on 2016-08-20.
 */
public class DismissBroadcast extends BroadcastReceiver {
    private SharedHelper sharedHelper;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                sharedHelper = new SharedHelper(context);
                String notificationId = extras.getString("notificationId");
                sharedHelper.removeLastNotificationId(notificationId);
            }
        }
    }

}