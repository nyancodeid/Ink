package ink.va.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ink.va.activities.HomeActivity;
import ink.va.utils.Notification;

import static ink.va.utils.Constants.NOTIFICATION_AUTO_REDIRECT_BUNDLE_KEY;
import static ink.va.utils.Constants.NOTIFICATION_MESSAGE_BUNDLE_KEY;

/**
 * Created by PC-Comp on 2/28/2017.
 */

public class NotificationBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Notification.get().isAppAlive()) {
            Notification.get().setCheckLock(true);
        } else {
            Notification.get().setCheckLock(false);
        }
        String jsonObject = intent.getExtras().getString(NOTIFICATION_MESSAGE_BUNDLE_KEY);
        Intent chatIntent = new Intent(context, HomeActivity.class);
        chatIntent.putExtra(NOTIFICATION_MESSAGE_BUNDLE_KEY, jsonObject.toString());
        chatIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        chatIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        chatIntent.putExtra(NOTIFICATION_AUTO_REDIRECT_BUNDLE_KEY, true);
        context.startActivity(chatIntent);
    }
}
