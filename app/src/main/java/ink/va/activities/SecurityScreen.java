package ink.va.activities;

import android.Manifest;
import android.graphics.Color;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.ink.va.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import ink.va.interfaces.FingerprintCallback;
import ink.va.utils.FingerPrintManager;
import ink.va.utils.SharedHelper;

import static ink.va.activities.SecurityActivity.FINGERPRINT_REQUEST_CODE;

public class SecurityScreen extends BaseActivity implements FingerprintCallback {

    @BindView(R.id.activity_security_screen)
    View securityScreenRoot;
    @BindView(R.id.fingerPrintLayout)
    View fingerPrintLayout;
    @BindView(R.id.pinLayout)
    View pinLayout;
    @BindView(R.id.passwordInputLayout)
    TextInputLayout passwordInputLayout;
    @BindView(R.id.passwordED)
    EditText passwordED;
    private SharedHelper mSharedHelper;
    private FingerPrintManager fingerPrintManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_screen);
        ButterKnife.bind(this);
        mSharedHelper = new SharedHelper(this);

        if (mSharedHelper.getFeedColor() != null) {
            securityScreenRoot.setBackgroundColor(Color.parseColor(mSharedHelper.getFeedColor()));
        }
        if (mSharedHelper.hasPinAttached()) {
            pinLayout.setVisibility(View.VISIBLE);
            if (passwordED.getText().toString().isEmpty()) {
                passwordInputLayout.setError(getString(R.string.emptyPasswordError));
            } else if (!passwordED.getText().toString().trim().equals(mSharedHelper.getPin())) {
                passwordInputLayout.setError(getString(R.string.passwordWrong));
            } else if (passwordED.getText().toString().trim().equals(mSharedHelper.getPin())) {
                proceedUnlocking();
            }
        } else {
            fingerPrintLayout.setVisibility(View.VISIBLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                fingerPrintManager = new FingerPrintManager(this);
                fingerPrintManager.setOnFingerprintCallback(this);
                fingerPrintManager.startAuthentication();
            }
        }
    }

    private void proceedUnlocking() {
        finish();
        overridePendingTransition(R.anim.activity_scale_up, R.anim.activity_scale_down);
    }

    @Override
    public void onLockScreenNotSecured() {

    }

    @Override
    public void onPermissionNeeded() {
        Snackbar.make(fingerPrintLayout, getString(R.string.fingerprint_permission_needed), Snackbar.LENGTH_INDEFINITE).setAction(getString(R.string.grant), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.requestPermissions(SecurityScreen.this, new String[]{Manifest.permission.USE_FINGERPRINT}, FINGERPRINT_REQUEST_CODE);
            }
        }).show();
    }

    @Override
    public void onNoFingerPrints() {

    }

    @Override
    public void onAuthenticationError(int errMsgId, CharSequence errString) {
        Toast.makeText(this, getString(R.string.error) + "-" + errString, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        Toast.makeText(this, getString(R.string.error) + "-" + helpString, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAuthenticationFailed() {
        Toast.makeText(this, getString(R.string.authenticationFailed), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        proceedUnlocking();
    }
}
