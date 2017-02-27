package ink.va.activities;

import android.Manifest;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.ink.va.R;

import ink.va.interfaces.FingerprintCallback;
import ink.va.utils.FingerPrintManager;
import ink.va.utils.PermissionsChecker;

public class SecurityActivity extends BaseActivity implements FingerprintCallback {
    private static final int FINGERPRINT_REQUEST_CODE = 5;
    private FingerPrintManager fingerPrintManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finger_print);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            fingerPrintManager = new FingerPrintManager(this);
            fingerPrintManager.setOnFingerprintCallback(this);
            fingerPrintManager.startAuthentication();
        }

    }

    @Override
    public void onLockScreenNotSecured() {
        Toast.makeText(this, "not secure", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPermissionNeeded() {
        Toast.makeText(this, "permission needed", Toast.LENGTH_SHORT).show();
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.USE_FINGERPRINT}, FINGERPRINT_REQUEST_CODE);
    }

    @Override
    public void onNoFingerPrints() {
        Toast.makeText(this, "no fingerprint", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAuthenticationError(int errMsgId, CharSequence errString) {
        Toast.makeText(this, "authentication error" + errString, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        Toast.makeText(this, "authentication help" + helpString, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAuthenticationFailed() {
        Toast.makeText(this, "authentication failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        Toast.makeText(this, "authentication succeed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case FINGERPRINT_REQUEST_CODE:
                if (!PermissionsChecker.isFingerprintPermissionGranted(this)) {
                    Toast.makeText(this, "not granted yet", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "granted", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
