package ink.va.activities;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.ink.va.R;

import butterknife.OnClick;
import ink.va.interfaces.FingerprintCallback;
import ink.va.utils.FingerPrintManager;
import ink.va.utils.PermissionsChecker;

public class SecurityActivity extends BaseActivity implements FingerprintCallback {
    private static final int FINGERPRINT_REQUEST_CODE = 5;
    private FingerPrintManager fingerPrintManager;
    private Button attachFingerPrintButton;
    private Button attachPinButton;
    private boolean isFingerPrintSucceed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finger_print);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            fingerPrintManager = new FingerPrintManager(this);
            fingerPrintManager.setOnFingerprintCallback(this);
        }

    }

    @Override
    public void onLockScreenNotSecured() {
        Toast.makeText(this, "not secure", Toast.LENGTH_SHORT).show();
        isFingerPrintSucceed = false;
    }

    @Override
    public void onPermissionNeeded() {
        Toast.makeText(this, "permission needed", Toast.LENGTH_SHORT).show();
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.USE_FINGERPRINT}, FINGERPRINT_REQUEST_CODE);
        isFingerPrintSucceed = false;
    }

    @Override
    public void onNoFingerPrints() {
        Toast.makeText(this, "no fingerprint", Toast.LENGTH_SHORT).show();
        isFingerPrintSucceed = false;
    }

    @Override
    public void onAuthenticationError(int errMsgId, CharSequence errString) {
        Toast.makeText(this, "authentication error" + errString, Toast.LENGTH_SHORT).show();
        isFingerPrintSucceed = false;
    }

    @Override
    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        Toast.makeText(this, "authentication help" + helpString, Toast.LENGTH_SHORT).show();
        isFingerPrintSucceed = false;
    }

    @Override
    public void onAuthenticationFailed() {
        Toast.makeText(this, "authentication failed", Toast.LENGTH_SHORT).show();
        isFingerPrintSucceed = false;
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        Toast.makeText(this, "authentication succeed", Toast.LENGTH_SHORT).show();
        isFingerPrintSucceed = true;
        if (attachFingerPrintButton != null) {
            attachFingerPrintButton.setAlpha(1);
        }
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

    @OnClick(R.id.fingerprintWrapper)
    public void fingerprintWrapperClicked() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.fingerprint_view);
        attachFingerPrintButton = (Button) dialog.findViewById(R.id.attachFingerPrintButton);
        attachFingerPrintButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFingerPrintSucceed) {

                }
            }
        });
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog = null;
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                dialog = null;
            }
        });
        dialog.show();
    }

    @OnClick(R.id.pinWrapper)
    public void pinWrapperClicked() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.pin_view);
        attachPinButton = (Button) dialog.findViewById(R.id.attachPinButton);
        attachPinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog = null;
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                dialog = null;
            }
        });
        dialog.show();
    }
}
