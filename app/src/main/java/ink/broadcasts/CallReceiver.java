package ink.broadcasts;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;

import ink.callbacks.GeneralCallback;
import ink.service.GcmIntentService;
import ink.utils.SharedHelper;
import ink.utils.SinchHelper;

/**
 * Created by USER on 2016-07-24.
 */
public class CallReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        SinchClient sinchClient = SinchHelper.get().getSinchClient();
        SharedHelper sharedHelper = new SharedHelper(context);
        if (sinchClient == null) {
            SinchHelper.get().startSinch(context, sharedHelper.getUserId(),
                    sharedHelper.getFirstName() + " " + sharedHelper.getLastName(), new GeneralCallback<SinchClient>() {
                        @Override
                        public void onSuccess(SinchClient sinchClient) {
                            CallClient callClient = sinchClient.getCallClient();
                            Call call = callClient.getCall("");
                            if (GcmIntentService.PICKUP_ACTION.equals(action)) {
                                Log.v("shuffTest", "Pressed pickup");
                            } else if (GcmIntentService.HANGUP_ACTION.equals(action)) {
                                Log.v("shuffTest", "Pressed hangup");
                            }
                        }

                        @Override
                        public void onFailure(SinchClient sinchClient) {

                        }
                    });
        }
        if (!action.equals(GcmIntentService.PICKUP_ACTION) && !action.equals(GcmIntentService.HANGUP_ACTION)) {
            ComponentName comp = new ComponentName(context.getPackageName(), GcmIntentService.class.getName());
            startWakefulService(context, (intent.setComponent(comp)));
            setResultCode(Activity.RESULT_OK);
        }
    }
}
