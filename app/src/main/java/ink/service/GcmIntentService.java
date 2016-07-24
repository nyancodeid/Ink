package ink.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.ink.R;

import java.util.Random;

import ink.broadcasts.CallReceiver;
import ink.utils.Notification;

/**
 * Created by USER on 2016-07-24.
 */
public class GcmIntentService extends IntentService implements ServiceConnection {

    public static final String PICKUP_ACTION = "pick_up";
    public static final String HANGUP_ACTION = "hang_up";
    private Intent mIntent;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle bundle = intent.getExtras();
        Log.d("shuffTest", "onHandleIntent: ");
        for (String key : bundle.keySet()) {
            Object value = bundle.get(key);
            Log.d("Fasfasfas", String.format("%s %s (%s)", key,
                    value.toString(), value.getClass().getName()));
        }
        if (bundle.containsKey("sinch")) {
            Log.d("shuffTest", "onHandleIntent: ");
            if (Notification.getInstance().isCallRemote()) {
                showIncomingCallNotification();
            }
        }
    }

    private void showIncomingCallNotification() {
        Random random = new Random();
        NotificationManager notificationManagerCompat = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
        final android.support.v7.app.NotificationCompat.Builder builder = new android.support.v7.app.NotificationCompat.Builder(getApplicationContext());
        Intent yesReceive = new Intent(getApplicationContext(), CallReceiver.class);
        yesReceive.setAction(PICKUP_ACTION);
        PendingIntent pendingIntentYes = PendingIntent.getBroadcast(this, 12345, yesReceive, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(R.drawable.pick_up_icon, getString(R.string.pickup), pendingIntentYes);

        Intent maybeReceive = new Intent(getApplicationContext(), CallReceiver.class);
        maybeReceive.setAction(HANGUP_ACTION);
        PendingIntent pendingIntentMaybe = PendingIntent.getBroadcast(this, 12345, maybeReceive, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(R.drawable.hang_up_icon, getString(R.string.hangup), pendingIntentMaybe);
        builder.setSmallIcon(R.drawable.incoming_call_icon);
        builder.setContentTitle("Incoming Call");
        android.app.Notification notification = builder.build();
        notificationManagerCompat.notify(Integer.valueOf(random.nextInt()), notification);

    }

    private void connectToService() {

    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
    }
}