package ink.va.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ink.va.service.MessageService;

/**
 * Created by PC-Comp on 2/20/2017.
 */

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent myIntent = new Intent(context, MessageService.class);
        context.startService(myIntent);

    }
}