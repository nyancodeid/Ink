package kashmirr.social.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import kashmirr.social.service.SocketService;

/**
 * Created by PC-Comp on 2/20/2017.
 */

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            context.stopService(new Intent(context, SocketService.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Intent myIntent = new Intent(context, SocketService.class);
        context.startService(myIntent);

    }
}