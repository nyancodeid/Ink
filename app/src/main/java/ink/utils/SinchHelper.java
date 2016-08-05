package ink.utils;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.sinch.android.rtc.ClientRegistration;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchClientListener;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallListener;

import java.util.List;

import ink.callbacks.GeneralCallback;
import ink.interfaces.CallCallback;

/**
 * Created by USER on 2016-07-24.
 */
public class SinchHelper {
    private static final String SINCH_KEY = "072173e7-f5d1-4ecd-8ab6-af04bec5317c";
    private static final String SINCH_SECRET = "I+BSL79arEOeBqDB7jeMGw==";
    private static final String SINCH_ENVIRONMENT = "clientapi.sinch.com";
    private static SinchHelper ourInstance = new SinchHelper();
    private SinchClient sinchClient;
    private boolean sinchStarted;
    private Thread callThread;
    private CallCallback callCallback;

    public static SinchHelper get() {
        return ourInstance;
    }

    private SinchHelper() {
    }

    public boolean startSinch(final Context context, final String userId, final String name,
                              @Nullable final GeneralCallback<SinchClient> generalCallback) {
        if (isSinchClientStarted()) {
            return true;
        } else {
            sinchClient = Sinch.getSinchClientBuilder().context(context)
                    .applicationKey(SINCH_KEY)
                    .applicationSecret(SINCH_SECRET)
                    .environmentHost(SINCH_ENVIRONMENT)
                    .userId(userId)
                    .callerIdentifier(name)
                    .build();


            sinchClient.setSupportCalling(true);
            sinchClient.setSupportManagedPush(true);
            sinchClient.setSupportActiveConnectionInBackground(true);

            sinchClient.addSinchClientListener(new SinchClientListener() {
                @Override
                public void onClientStarted(final SinchClient sinchClient) {
                    sinchStarted = true;
                    Log.d("fasfasfas", "onClientStarted: ");
                    sinchClient.startListeningOnActiveConnection();
                    if (generalCallback != null) {
                        generalCallback.onSuccess(sinchClient);
                    }
                    callThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (callThread.getState() == Thread.State.TERMINATED) {
                                Notification.getInstance().setCallRemote(true);
                            } else {
                                Notification.getInstance().setCallRemote(false);
                            }
                        }
                    });
                    callThread.start();
                }

                @Override
                public void onClientStopped(SinchClient sinchClient) {
                    Log.d("fasfasfas", "onClientStopped: ");
                    sinchStarted = false;
                }

                @Override
                public void onClientFailed(SinchClient sinchClient, SinchError sinchError) {
                    Log.d("fasfasfas", "onClientFailed: " + sinchError.getMessage());
                    startSinch(context, userId, name, generalCallback);
                    sinchStarted = false;
                }

                @Override
                public void onRegistrationCredentialsRequired(SinchClient sinchClient, ClientRegistration clientRegistration) {
                }

                @Override
                public void onLogMessage(int i, String s, String s1) {

                }
            });
            sinchClient.start();
        }
        return sinchStarted;
    }

    public SinchClient getSinchClient() {
        return sinchClient;
    }

    public Call makeCall(String userIdToCall) {
        if (!sinchStarted) {
            throw new RuntimeException("Sinch client not started yet");
        }
        CallClient callClient = sinchClient.getCallClient();
        com.sinch.android.rtc.calling.Call call = callClient.callUser(userIdToCall);
        call.addCallListener(new CallListener() {
            @Override
            public void onCallProgressing(com.sinch.android.rtc.calling.Call call) {
                if (callCallback != null) {
                    callCallback.onCallProgress(call);
                }
                Log.d("Fasfasfas", "onCallProgressing: " + call.getDetails().getEndCause());
            }

            @Override
            public void onCallEstablished(com.sinch.android.rtc.calling.Call call) {
                if (callCallback != null) {
                    callCallback.onCallEstablished(call);
                }
                Log.d("Fasfasfas", "onCallEstablished: " + call.getDetails().getEndCause());
            }

            @Override
            public void onCallEnded(com.sinch.android.rtc.calling.Call call) {
                if (callCallback != null) {
                    callCallback.onCallEnded(call);
                }
                Log.d("Fasfasfas", "onCallEnded: " + call.getDetails().getEndCause());
            }

            @Override
            public void onShouldSendPushNotification(com.sinch.android.rtc.calling.Call call, List<PushPair> list) {
                Log.d("Fasfasfas", "onShouldSendPushNotification: " + call.getDetails().getEndCause());
            }
        });
        return call;
    }

    public void setCallCallback(CallCallback callCallback) {
        this.callCallback = callCallback;
    }

    public boolean isSinchStarted() {
        return sinchStarted;
    }

    private boolean isSinchClientStarted() {
        return sinchClient != null && sinchClient.isStarted();
    }

    public void hangup(Call call) {
        call.hangup();
    }
}
