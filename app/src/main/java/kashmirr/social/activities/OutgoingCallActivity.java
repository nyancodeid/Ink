package kashmirr.social.activities;

import android.content.DialogInterface;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kashmirr.social.R;

import java.text.ParseException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import kashmirr.social.managers.SipManagerUtil;
import kashmirr.social.utils.Constants;
import kashmirr.social.utils.ImageLoader;

/**
 * Created by PC-Comp on 5/8/2017.
 */

public class OutgoingCallActivity extends BaseActivity implements SipManagerUtil.SipCallback {
    private String destination;
    private String opponentName;
    private boolean isSocial;
    private String imageUrl;

    @BindView(R.id.usernameTV)
    TextView usernameTV;
    @BindView(R.id.outgoingUserIV)
    ImageView outgoingUserIV;
    @BindView(R.id.outgoingCM)
    Chronometer outgoingCM;
    @BindView(R.id.callStateTV)
    TextView callStateTV;

    private SipManagerUtil sipManagerUtil;
    private boolean insideCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.outgoing_call_view);
        ButterKnife.bind(this);
        destination = getIntent().getExtras().getString("destination");
        opponentName = getIntent().getExtras().getString("opponentName");
        isSocial = getIntent().getExtras().getBoolean("isSocial");
        imageUrl = getIntent().getExtras().getString("imageUrl");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sipManagerUtil = SipManagerUtil.getManager();
        sipManagerUtil.setContext(this);
        sipManagerUtil.setSipCallback(this);
        usernameTV.setText(opponentName);
        callStateTV.setText(getString(R.string.calling));
        try {
            sipManagerUtil.call(destination);
        } catch (SipException e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show();
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show();
        }

        if (isSocial) {
            if (!imageUrl.isEmpty()) {
                ImageLoader.loadImage(this, false, false, imageUrl, 0, R.drawable.profile_background, outgoingUserIV, new ImageLoader.ImageLoadedCallback() {
                    @Override
                    public void onImageLoaded(Object result, Exception e) {
                        if (e != null) {
                            ImageLoader.loadImage(OutgoingCallActivity.this, false, false, null, R.drawable.profile_background, 0, outgoingUserIV, null);
                        }
                    }
                });
            } else {
                ImageLoader.loadImage(this, false, false, null, R.drawable.profile_background, 0, outgoingUserIV, null);
            }
        } else {
            if (!imageUrl.isEmpty()) {
                ImageLoader.loadImage(this, false, false, Constants.MAIN_URL + Constants.USER_IMAGES_FOLDER + imageUrl, 0, R.drawable.profile_background, outgoingUserIV, new ImageLoader.ImageLoadedCallback() {
                    @Override
                    public void onImageLoaded(Object result, Exception e) {
                        if (e != null) {
                            ImageLoader.loadImage(OutgoingCallActivity.this, false, false, null, R.drawable.profile_background, 0, outgoingUserIV, null);
                        }
                    }
                });
            } else {
                ImageLoader.loadImage(this, false, false, null, R.drawable.profile_background, 0, outgoingUserIV, null);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (insideCall) {
            showCallWarning(true);
        } else {
            try {
                sipManagerUtil.hangup();
            } catch (SipException e) {
                e.printStackTrace();
            }
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.outgoingHangupIV)
    public void outgoingHangupIVClicked() {
        showCallWarning(false);
    }

    @Override
    public void onRinging(String callerName) {

    }

    @Override
    public void onIncomingCallEstablished(SipAudioCall sipAudioCall) {

    }

    @Override
    public void onIncomingCallEnded(SipAudioCall sipAudioCall) {

    }

    @Override
    public void onIncomingCallError(SipAudioCall call, int errorCode, String errorMessage) {

    }

    @Override
    public void onOutgoingCalling() {
        callStateTV.setText(getString(R.string.calling));
    }

    @Override
    public void onOutgoingCallEstablished(SipAudioCall call) {
        insideCall = true;
        callStateTV.setText(getString(R.string.callEstablished));
        outgoingCM.start();
    }

    @Override
    public void onOutgoingCallEnded(SipAudioCall call) {
        insideCall = false;
        callStateTV.setText(getString(R.string.callEnded));
        outgoingCM.stop();
    }

    @Override
    public void onUserBusy() {
        callStateTV.setText(getString(R.string.userBusy));
    }

    @Override
    public void onOutgoingCallHeld() {

    }

    @Override
    public void onOutgoingCallError(SipAudioCall call, int errorCode, String errorMessage) {
        insideCall = false;
        callStateTV.setText(getString(R.string.error) + " " + errorMessage);
    }

    @Override
    public void onIncomingCallHeld() {

    }

    @Override
    public void onIncomingCallInstanceNull() {

    }

    private void showCallWarning(boolean leaving) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.warning));
        builder.setMessage(leaving ? getString(R.string.leavingCallWarning) : getString(R.string.endCallWarning));
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                try {
                    sipManagerUtil.hangup();
                } catch (SipException e) {
                    e.printStackTrace();
                }
                finish();
            }
        });
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }
}
