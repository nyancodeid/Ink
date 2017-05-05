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
import com.romainpiel.shimmer.ShimmerTextView;

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

    @BindView(R.id.slideToAcceptCancelTV)
    ShimmerTextView slideToAcceptCancelTV;

    private Animation fadeOut;
    private Animation slideRightRotate;
    private Animation slideDown;
    private boolean shallDecline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call_screen2);
        ButterKnife.bind(this);
        if (!PermissionsChecker.isCallPermissionGranted(this)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 0);
        }
//        incomingIntent = (Intent) getIntent().getExtras().get("destinationIntent");
        slideToAcceptCancelTV.animate();

        fadeOut = AnimationUtils.loadAnimation(this, R.anim.com_adobe_image_fade_out_long);
        slideRightRotate = AnimationUtils.loadAnimation(this, R.anim.slide_rotate);
        slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);
    }

    @OnClick(R.id.acceptCallIcon)
    public void acceptCallIconClicked() {
        if (!shallDecline) {
            startAcceptAnimation();
        } else {
            finish();
        }
    }

    private void startAcceptAnimation() {
        shallDecline = true;
        acceptCallIcon.setImageResource(R.drawable.decline_call_icon);

        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                slideToAcceptCancelTV.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        slideToAcceptCancelTV.startAnimation(fadeOut);
        slideDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                declineCallIcon.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        declineCallIcon.startAnimation(slideDown);
    }

    @OnClick(R.id.declineCallIcon)
    public void declineCallIconClicked() {
        finish();
    }
}
