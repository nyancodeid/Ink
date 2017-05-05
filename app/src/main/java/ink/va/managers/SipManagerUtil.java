package ink.va.managers;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.sip.SipAudioCall;
import android.net.sip.SipErrorCode;
import android.net.sip.SipException;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.sip.SipRegistrationListener;
import android.support.annotation.Nullable;

import java.text.ParseException;

import ink.va.callbacks.GeneralCallback;
import ink.va.models.SipResponse;
import ink.va.receivers.IncomingCallReceiver;
import ink.va.utils.Retrofit;
import lombok.Getter;
import lombok.Setter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static ink.va.utils.Constants.SIP_DOMAIN_URL;
import static ink.va.utils.Constants.SIP_OUTBOUND_PROXY_URL;

/**
 * Created by PC-Comp on 5/5/2017.
 */

/**
 * Sip manager class to help initializing the Sip.
 * Don't forget to initialize the Context of the caller from the first after you have the reference to the singleton.
 */

class SipManagerUtil implements SipRegistrationListener {

    @Setter
    private SipCallback sipCallback;
    @Getter
    private static SipManagerUtil manager = new SipManagerUtil();
    @Setter
    private Context context;
    private SipManager sipManager;
    private SipProfile sipProfile;
    private Thread workerThread;
    private String sipUsername;
    private String sipPassword;
    private SipProfile.Builder profileBuilder;
    private PendingIntent pendingIntent;
    private String displayName;
    private SipAudioCall incomingCallInstance;
    private String userId;

    private SipManagerUtil() {
    }

    private void initSip() {
        if (sipManager == null && context != null) {
            sipManager = SipManager.newInstance(context);
        }
    }

    public void loginIntoSip(final String sipUsername,
                             final String sipPassword,
                             final String displayName,
                             final String userId) {
        initSip();
        this.userId = userId;
        this.sipUsername = sipUsername;
        this.sipPassword = sipPassword;
        this.displayName = displayName;
        if (sipManager == null) {
            Exception exception = new Exception("Sip  Manager is null");
            exception.printStackTrace();
            return;
        }
        if (workerThread != null) {
            workerThread = null;
        }
        workerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                closeSipProfile();
                profileBuilder = null;
                try {
                    profileBuilder = new SipProfile.Builder(sipUsername, SIP_DOMAIN_URL);
                    profileBuilder.setPassword(sipPassword);
                    profileBuilder.setProfileName(userId);
                    profileBuilder.setAuthUserName(sipUsername);
                    profileBuilder.setOutboundProxy(SIP_OUTBOUND_PROXY_URL);
                    profileBuilder.setDisplayName(displayName);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                sipProfile = profileBuilder.build();
                final Intent intent = new Intent(context, IncomingCallReceiver.class);

                pendingIntent = PendingIntent.getBroadcast(context, 0, intent, Intent.FILL_IN_DATA);
                try {
                    sipManager.open(sipProfile, pendingIntent, SipManagerUtil.this);
                } catch (SipException e) {
                    e.printStackTrace();
                }
            }
        });
        workerThread.start();


    }

    private void closeSipProfile() {
        if (sipManager == null) {
            return;
        }
        try {
            if (sipProfile != null) {
                sipManager.close(sipProfile.getUriString());
                sipManager.unregister(sipProfile, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRegistering(String localProfileUri) {

    }

    @Override
    public void onRegistrationDone(String localProfileUri, long expiryTime) {

    }

    @Override
    public void onRegistrationFailed(String localProfileUri, int errorCode, String errorMessage) {
        if (errorCode == SipErrorCode.IN_PROGRESS) {
            closeSipProfile();
            try {
                sipManager.open(sipProfile, pendingIntent, this);
            } catch (SipException e) {
                e.printStackTrace();
            }
        } else if (errorCode == SipErrorCode.CLIENT_ERROR) {
            try {
                sipManager.register(sipProfile, 10, this);
            } catch (SipException e) {
                loginIntoSip(sipUsername, sipPassword, displayName, userId);
                e.printStackTrace();
            }
        }
    }

    public void takeAudioCall(final Intent incomingIntent) throws SipException {
        incomingCallInstance = sipManager.takeAudioCall(incomingIntent, new SipAudioCall.Listener() {
            @Override
            public void onRinging(SipAudioCall call, SipProfile caller) {
                super.onRinging(call, caller);
                if (sipCallback != null) {
                    sipCallback.onRinging(caller.getDisplayName());
                }
            }

            @Override
            public void onCallEstablished(SipAudioCall call) {
                super.onCallEstablished(call);
                incomingCallInstance.startAudio();
                if (sipCallback != null) {
                    sipCallback.onIncomingCallEstablished(call);
                }
            }

            @Override
            public void onCallHeld(SipAudioCall call) {
                super.onCallHeld(call);
                if (sipCallback != null) {
                    sipCallback.onIncomingCallHeld();
                }
            }

            @Override
            public void onCallEnded(SipAudioCall call) {
                super.onCallEnded(call);
                if (sipCallback != null) {
                    sipCallback.onIncomingCallEnded(call);
                }
            }

            @Override
            public void onError(SipAudioCall call, int errorCode, String errorMessage) {
                super.onError(call, errorCode, errorMessage);
                if (sipCallback != null) {
                    sipCallback.onIncomingCallError(call, errorCode, errorMessage);
                }
            }

        });
    }

    public void destroy() {
        closeSipProfile();
        sipManager = null;
        if (workerThread != null) {
            workerThread.interrupt();
        }
        workerThread = null;
        sipProfile = null;
        manager = null;
        sipCallback = null;
        pendingIntent = null;
        context = null;
    }

    public void pickup() throws SipException {
        if (incomingCallInstance == null) {
            if (sipCallback != null) {
                sipCallback.onIncomingCallInstanceNull();
            }
        } else {
            incomingCallInstance.answerCall(10);
        }
    }

    public void hangup() throws SipException {
        if (incomingCallInstance == null) {
            if (sipCallback != null) {
                sipCallback.onIncomingCallInstanceNull();
            }
        } else {
            incomingCallInstance.endCall();
        }
    }

    public boolean toggleMute() {
        if (incomingCallInstance == null) {
            if (sipCallback != null) {
                sipCallback.onIncomingCallInstanceNull();
            }
        } else {
            incomingCallInstance.toggleMute();
        }
        return incomingCallInstance.isMuted();
    }

    public void holdCall() {
        if (incomingCallInstance == null) {
            if (sipCallback != null) {
                sipCallback.onIncomingCallInstanceNull();
            }
        } else {
            try {
                incomingCallInstance.holdCall(Integer.MAX_VALUE);
            } catch (SipException e) {
                e.printStackTrace();
            }
        }
    }

    public void resumeCall() {
        if (incomingCallInstance == null) {
            if (sipCallback != null) {
                sipCallback.onIncomingCallInstanceNull();
            }
        } else {
            try {
                incomingCallInstance.continueCall(10);
            } catch (SipException e) {
                e.printStackTrace();
            }
        }
    }

    public void toggleSpeaker(boolean on) {
        if (incomingCallInstance == null) {
            if (sipCallback != null) {
                sipCallback.onIncomingCallInstanceNull();
            }
        } else {
            incomingCallInstance.setSpeakerMode(on);
        }
    }


    public void call(String sipUsername) throws SipException, ParseException {
        SipProfile.Builder builder = new SipProfile.Builder(sipUsername, SIP_DOMAIN_URL);
        SipProfile sipProfile = builder.build();
        sipManager.makeAudioCall(this.sipProfile, sipProfile, new SipAudioCall.Listener() {
            @Override
            public void onCalling(SipAudioCall call) {
                super.onCalling(call);
                if (sipCallback != null) {
                    sipCallback.onOutgoingCalling();
                }
            }

            @Override
            public void onCallEstablished(SipAudioCall call) {
                super.onCallEstablished(call);
                call.startAudio();
                if (sipCallback != null) {
                    sipCallback.onOutgoingCallEstablished(call);
                }
            }

            @Override
            public void onCallEnded(SipAudioCall call) {
                super.onCallEnded(call);
                if (sipCallback != null) {
                    sipCallback.onOutgoingCallEnded(call);
                }
            }

            @Override
            public void onCallBusy(SipAudioCall call) {
                super.onCallBusy(call);
                if (sipCallback != null) {
                    sipCallback.onUserBusy();
                }
            }

            @Override
            public void onCallHeld(SipAudioCall call) {
                super.onCallHeld(call);
                if (sipCallback != null) {
                    sipCallback.onOutgoingCallHeld();
                }
            }

            @Override
            public void onError(SipAudioCall call, int errorCode, String errorMessage) {
                super.onError(call, errorCode, errorMessage);
                if (sipCallback != null) {
                    sipCallback.onOutgoingCallError(call, errorCode, errorMessage);
                }
            }
        }, 10);
    }

    private void registerSipAccount(String sipUsername, String sipPassword,
                                    String displayName, @Nullable final GeneralCallback generalCallback) {

        Retrofit.getInstance().getSipService().registerSipAccount(sipUsername, sipPassword, "dummy@email.com", displayName).enqueue(new Callback<SipResponse>() {
            @Override
            public void onResponse(Call<SipResponse> call, Response<SipResponse> response) {
                if (response.body().isSuccess()) {
                    if (generalCallback != null) {
                        generalCallback.onSuccess(null);
                    }
                } else {
                    String cause = response.body().getError();
                    if (generalCallback != null) {
                        generalCallback.onFailure(cause);
                    }
                }
            }

            @Override
            public void onFailure(Call<SipResponse> call, Throwable t) {
                if (generalCallback != null) {
                    generalCallback.onFailure(t.toString());
                }
            }
        });
    }

    public interface SipCallback {
        void onRinging(String callerName);

        void onIncomingCallEstablished(SipAudioCall sipAudioCall);

        void onIncomingCallEnded(SipAudioCall sipAudioCall);

        void onIncomingCallError(SipAudioCall call, int errorCode, String errorMessage);

        void onOutgoingCalling();

        void onOutgoingCallEstablished(SipAudioCall call);

        void onOutgoingCallEnded(SipAudioCall call);

        void onUserBusy();

        void onOutgoingCallHeld();

        void onOutgoingCallError(SipAudioCall call, int errorCode, String errorMessage);

        void onIncomingCallHeld();

        void onIncomingCallInstanceNull();
    }

}
