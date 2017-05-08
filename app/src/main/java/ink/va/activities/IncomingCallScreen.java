package ink.va.activities;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.ink.va.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.va.utils.PermissionsChecker;


public class IncomingCallScreen extends BaseActivity {

    private Intent incomingIntent;

    @BindView(R.id.callStateTV)
    TextView callStateTV;
    @BindView(R.id.callerNameTV)
    TextView callerNameTV;
    @BindView(R.id.callDurationTV)
    TextView callDurationTV;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call_screen2);
        ButterKnife.bind(this);
        if (!PermissionsChecker.isCallPermissionGranted(this)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 0);
        }

        fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        scaleInAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_in);
    }

    @OnClick(R.id.acceptCallIcon)
    public void acceptCallIconClicked() {
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
        finish();
    }

    @OnClick(R.id.hangupIV)
    public void hangupIV() {
        finish();
    }
}
