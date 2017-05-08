package ink.va.activities;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;

import com.ink.va.R;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.va.interfaces.RequestCallback;
import ink.va.managers.SipManagerUtil;
import ink.va.utils.Constants;
import ink.va.utils.ImageLoader;
import ink.va.utils.PermissionsChecker;
import ink.va.utils.Retrofit;


public class IncomingCallScreen extends BaseActivity implements SipManagerUtil.SipCallback {

    private Intent incomingIntent;

    @BindView(R.id.callStateTV)
    TextView callStateTV;
    @BindView(R.id.callerNameTV)
    TextView callerNameTV;
    @BindView(R.id.callDurationCM)
    Chronometer callDurationCM;
    @BindView(R.id.callerImage)
    ImageView callerImage;
    @BindView(R.id.acceptCallIcon)
    ImageView acceptCallIcon;
    @BindView(R.id.declineCallIcon)
    ImageView declineCallIcon;
    @BindView(R.id.hangupIV)
    ImageView hangupIV;

    @BindView(R.id.slideToAcceptCancelTV)
    TextView acceptDeclineText;

    private Animation fadeOut;
    private Animation slideDown;
    private Animation scaleInAnimation;
    private SipManagerUtil sipManagerUtil;
    private SipAudioCall sipAudioCall;
    private String opponentImageUrl;
    private boolean isSocialAccount;
    private Intent destinationIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call_screen);
        ButterKnife.bind(this);
        if (!PermissionsChecker.isCallPermissionGranted(this)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 0);
        }
        sipManagerUtil = SipManagerUtil.getManager();
        sipManagerUtil.setContext(this);
        sipManagerUtil.setSipCallback(this);
        destinationIntent = (Intent) getIntent().getExtras().get("destinationIntent");
        try {
            sipAudioCall = sipManagerUtil.takeAudioCall(destinationIntent);
        } catch (SipException e) {
            e.printStackTrace();
        }

        callerNameTV.setText(sipAudioCall.getPeerProfile().getDisplayName());
        getSingleUserDetails(sipAudioCall.getPeerProfile().getProfileName());

        fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        scaleInAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_in);
    }

    @OnClick(R.id.acceptCallIcon)
    public void acceptCallIconClicked() {
        try {
            sipManagerUtil.pickup();
        } catch (SipException e) {
            e.printStackTrace();
        }
        startAcceptAnimation();
    }

    private void startAcceptAnimation() {
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                acceptDeclineText.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        acceptDeclineText.startAnimation(fadeOut);
        slideDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                declineCallIcon.setVisibility(View.GONE);
                acceptCallIcon.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        declineCallIcon.startAnimation(slideDown);
        acceptCallIcon.startAnimation(slideDown);
        hangupIV.setVisibility(View.VISIBLE);
        hangupIV.startAnimation(scaleInAnimation);
    }

    @OnClick(R.id.declineCallIcon)
    public void declineCallIconClicked() {
        try {
            sipManagerUtil.hangup();
        } catch (SipException e) {
            e.printStackTrace();
        }
        finish();
    }

    @OnClick(R.id.hangupIV)
    public void hangupIV() {
        try {
            sipManagerUtil.hangup();
        } catch (SipException e) {
            e.printStackTrace();
        }
        finish();
    }

    public void getSingleUserDetails(String opponentId) {
        makeRequest(Retrofit.getInstance().getInkService().getSingleUserDetails(opponentId, null), null, new RequestCallback() {
            @Override
            public void onRequestSuccess(Object result) {
                String responseBody = result.toString();
                JSONObject jsonObject;
                try {
                    jsonObject = new JSONObject(responseBody);

                    opponentImageUrl = jsonObject.optString("image_link");
                    isSocialAccount = jsonObject.optBoolean("isSocialAccount");
                    loadUserImage();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onRequestFailed(Object[] result) {
                ImageLoader.loadImage(IncomingCallScreen.this, false, true, null, R.drawable.profile_background, 0, null, null);
            }
        });
    }

    @Override
    public void onRinging(String callerName) {

    }

    @Override
    public void onIncomingCallEstablished(SipAudioCall sipAudioCall) {
        callStateTV.setText(getString(R.string.callEstablished));
        callDurationCM.start();
    }

    @Override
    public void onIncomingCallEnded(SipAudioCall sipAudioCall) {
        callStateTV.setText(getString(R.string.callEnded));
        callDurationCM.stop();
    }

    @Override
    public void onIncomingCallError(SipAudioCall call, int errorCode, String errorMessage) {
        callStateTV.setText(getString(R.string.error) + "-" + errorMessage);
    }

    @Override
    public void onOutgoingCalling() {

    }

    @Override
    public void onOutgoingCallEstablished(SipAudioCall call) {

    }

    @Override
    public void onOutgoingCallEnded(SipAudioCall call) {

    }

    @Override
    public void onUserBusy() {

    }

    @Override
    public void onOutgoingCallHeld() {

    }

    @Override
    public void onOutgoingCallError(SipAudioCall call, int errorCode, String errorMessage) {

    }

    @Override
    public void onIncomingCallHeld() {

    }

    @Override
    public void onIncomingCallInstanceNull() {

    }


    private void loadUserImage() {
        if (opponentImageUrl != null && !opponentImageUrl.isEmpty()) {
            if (isSocialAccount) {
                ImageLoader.loadImage(this, true, false, opponentImageUrl, 0, R.drawable.user_image_placeholder, callerImage, null);

            } else {
                String encodedImage = Uri.encode(opponentImageUrl);
                ImageLoader.loadImage(this, true, false, Constants.MAIN_URL + Constants.USER_IMAGES_FOLDER + encodedImage, 0, R.drawable.user_image_placeholder, callerImage, null);
            }
        } else {
            ImageLoader.loadImage(this, true, true, null, R.drawable.no_image, R.drawable.user_image_placeholder, callerImage, null);
        }
    }
}
