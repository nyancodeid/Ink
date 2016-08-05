package ink.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.ink.R;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallEndCause;
import com.sinch.android.rtc.calling.CallListener;

import java.util.List;

import ink.service.SinchService;
import ink.utils.SharedHelper;

public class IncomingCallScreenActivity extends BaseActivity implements SinchService.StartFailedListener {
    private static final String TAG = IncomingCallScreenActivity.class.getSimpleName();
    private com.sinch.android.rtc.calling.Call call;
    private String mCallId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call_screen);
        findViewById(R.id.hangup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (call != null) {
                    call.hangup();
                }
            }
        });
    }

    @Override
    public void onStartFailed(SinchError error) {

    }

    @Override
    public void onStarted() {
        mCallId = getIntent().getStringExtra(SinchService.CALL_ID);
        Log.d("mcallId", "onStarted: " + mCallId);
        call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            Toast.makeText(IncomingCallScreenActivity.this, "Not null blinging call", Toast.LENGTH_SHORT).show();
            call.addCallListener(new SinchCallListener());
        } else {
            Toast.makeText(IncomingCallScreenActivity.this, "Started with invalid callId, aborting", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onServiceConnected(SinchService.SinchServiceInterface sinchServiceInterface) {
        mCallId = getIntent().getStringExtra(SinchService.CALL_ID);
        Log.d("mcallId", "onServiceConneted: " + mCallId);
        Log.d("sinchServiceInterface", "onServiceConnected: " + sinchServiceInterface);
        SharedHelper sharedHelper = new SharedHelper(this);
        if (!getSinchServiceInterface().isStarted()) {
            getSinchServiceInterface().setStartListener(this);
            getSinchServiceInterface().startClient(sharedHelper.getUserId());
        } else {
            call = sinchServiceInterface.getCall(mCallId);
            if (call != null) {
                Toast.makeText(IncomingCallScreenActivity.this, "Not null blinging call", Toast.LENGTH_SHORT).show();
                call.addCallListener(new SinchCallListener());
            } else {
                Toast.makeText(IncomingCallScreenActivity.this, "Started with invalid callId, aborting", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private class SinchCallListener implements CallListener {

        @Override
        public void onCallEnded(Call call) {
            CallEndCause cause = call.getDetails().getEndCause();
            Log.d(TAG, "Call ended, cause: " + cause.toString());
            finish();
        }

        @Override
        public void onCallEstablished(Call call) {
            Log.d(TAG, "Call established");
        }

        @Override
        public void onCallProgressing(Call call) {
            Log.d(TAG, "Call progressing");
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> pushPairs) {
            // Send a push through your push provider here, e.g. GCM
        }
    }
}
