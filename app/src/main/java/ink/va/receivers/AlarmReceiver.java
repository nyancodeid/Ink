package ink.va.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ink.va.service.MafiaGameService;
import ink.va.service.SocketService;
import ink.va.utils.AlarmUtils;

/**
 * Created by USER on 2017-02-24.
 */

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, SocketService.class));
        context.startService(new Intent(context, MafiaGameService.class));
        AlarmUtils.scheduleAlarmWithMinutes(context, AlarmReceiver.class, 10);
    }
}
