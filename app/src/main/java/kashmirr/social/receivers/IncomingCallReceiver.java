package kashmirr.social.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import kashmirr.social.activities.IncomingCallScreen;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by PC-Comp on 5/5/2017.
 */

public class IncomingCallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent destinationIntent = new Intent(context, IncomingCallScreen.class);
        destinationIntent.setFlags(FLAG_ACTIVITY_NEW_TASK);
        destinationIntent.putExtra("destinationIntent", intent);
        context.startActivity(destinationIntent);

    }
}
