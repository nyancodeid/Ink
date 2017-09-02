package kashmirr.social.activities;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.kashmirr.social.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import kashmirr.social.interfaces.FingerprintCallback;
import kashmirr.social.managers.FingerPrintManager;
import kashmirr.social.utils.Notification;
import kashmirr.social.utils.SharedHelper;

import static kashmirr.social.activities.SecurityActivity.FINGERPRINT_REQUEST_CODE;
import static kashmirr.social.activities.SplashScreen.LOCK_SCREEN_REQUEST_CODE;

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
            fingerPrintLayout.setVisibility(View.GONE);
            pinLayout.setVisibility(View.VISIBLE);
            passwordED.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        passwordInputLayout.setError(null);
                    }
                }
            });
            passwordED.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (passwordInputLayout.getError() != null) {
                        passwordInputLayout.setError(null);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        } else {
            pinLayout.setVisibility(View.GONE);
            fingerPrintLayout.setVisibility(View.VISIBLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                fingerPrintManager = new FingerPrintManager(this);
                fingerPrintManager.init();
                fingerPrintManager.setOnFingerprintCallback(this);
                fingerPrintManager.startAuthentication();
            }
        }
    }

    private void proceedUnlocking() {
        Intent intent = new Intent();
        intent.putExtra("hasUnlocked", true);
        setResult(LOCK_SCREEN_REQUEST_CODE, intent);
        finish();
        overridePendingTransition(R.anim.activity_scale_up, R.anim.activity_scale_down);
    }

    @OnClick(R.id.unlockPasswordButton)
    public void unlockPasswordClicked() {
        if (passwordED.getText().toString().isEmpty()) {
            passwordInputLayout.setError(getString(R.string.emptyPasswordError));
        } else if (!passwordED.getText().toString().trim().equals(mSharedHelper.getPin())) {
            passwordInputLayout.setError(getString(R.string.passwordWrong));
        } else if (passwordED.getText().toString().trim().equals(mSharedHelper.getPin())) {
            proceedUnlocking();
        }
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

    @Override
    public void onBackPressed() {
        Notification.get().setAppAlive(false);
        finishAffinity();
        super.onBackPressed();
    }
}
