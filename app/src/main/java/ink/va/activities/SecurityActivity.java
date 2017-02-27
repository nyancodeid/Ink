package ink.va.activities;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ink.va.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.va.interfaces.FingerprintCallback;
import ink.va.utils.FingerPrintManager;
import ink.va.utils.SharedHelper;

public class SecurityActivity extends BaseActivity implements FingerprintCallback {

    @BindView(R.id.divider)
    View divider;
    @BindView(R.id.removeFingerPrint)
    View removeFingerPrint;
    @BindView(R.id.removePin)
    View removePin;

    private static final int FINGERPRINT_REQUEST_CODE = 5;
    private FingerPrintManager fingerPrintManager;
    private Button attachFingerPrintButton;
    private Button attachPinButton;
    private boolean isFingerPrintSucceed;
    private SharedHelper sharedHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finger_print);
        ButterKnife.bind(this);
        sharedHelper = new SharedHelper(this);
        removeFingerPrint.setVisibility(sharedHelper.hasFingerprintAttached() ? View.VISIBLE : View.GONE);
        removePin.setVisibility(sharedHelper.hasPinAttached() ? View.VISIBLE : View.GONE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            fingerPrintManager = new FingerPrintManager(this);
            fingerPrintManager.setOnFingerprintCallback(this);
        }

    }

    @Override
    public void onLockScreenNotSecured() {
        Snackbar.make(divider, getString(R.string.noSecurityOnLockScreen), Snackbar.LENGTH_LONG).show();
        isFingerPrintSucceed = false;
    }

    @Override
    public void onPermissionNeeded() {
        Snackbar.make(divider, getString(R.string.fingerprint_permission_needed), Snackbar.LENGTH_INDEFINITE).setAction(getString(R.string.grant), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.requestPermissions(SecurityActivity.this, new String[]{Manifest.permission.USE_FINGERPRINT}, FINGERPRINT_REQUEST_CODE);
            }
        }).show();
        isFingerPrintSucceed = false;
    }

    @Override
    public void onNoFingerPrints() {
        Snackbar.make(divider, getString(R.string.attachFingerprintHint), Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        }).show();
        isFingerPrintSucceed = false;
    }

    @Override
    public void onAuthenticationError(int errMsgId, CharSequence errString) {
        Toast.makeText(this, getString(R.string.error) + "-" + errString, Toast.LENGTH_SHORT).show();
        isFingerPrintSucceed = false;
    }

    @Override
    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        Toast.makeText(this, getString(R.string.error) + "-" + helpString, Toast.LENGTH_SHORT).show();
        isFingerPrintSucceed = false;
    }

    @Override
    public void onAuthenticationFailed() {
        Toast.makeText(this, getString(R.string.authenticationFailed), Toast.LENGTH_SHORT).show();
        isFingerPrintSucceed = false;
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        Toast.makeText(this, getString(R.string.authenticationSucceeded), Toast.LENGTH_LONG).show();
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
                break;
        }
    }

    @OnClick(R.id.fingerprintWrapper)
    public void fingerprintWrapperClicked() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!fingerPrintManager.supportsFingerprint()) {
                Toast.makeText(this, getString(R.string.fingerprint_not_supported), Toast.LENGTH_SHORT).show();
            } else {
                if (sharedHelper.hasPinAttached()) {
                    Toast.makeText(this, getString(R.string.removePinFirst), Toast.LENGTH_SHORT).show();
                } else {
                    showFingerprintDialog();
                    fingerPrintManager.startAuthentication();
                }
            }
        } else {
            Toast.makeText(this, getString(R.string.fingerprint_not_supported), Toast.LENGTH_SHORT).show();
        }
    }


    @OnClick(R.id.pinWrapper)
    public void pinWrapperClicked() {
        if (sharedHelper.hasFingerprintAttached()) {
            Toast.makeText(this, getString(R.string.removeFingerprint), Toast.LENGTH_SHORT).show();
        } else {
            showPinDialog();
        }

    }

    @OnClick(R.id.removeFingerPrint)
    public void remvoeFingerprintClicked() {
        sharedHelper.putFingerPrintAttached(false);
        removeFingerPrint.setVisibility(View.GONE);
    }

    @OnClick(R.id.removePin)
    public void removePinClicked() {
        sharedHelper.putPinAttached(false);
        removePin.setVisibility(View.GONE);
    }

    private void showPinDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.pin_view);
        attachPinButton = (Button) dialog.findViewById(R.id.attachPinButton);
        final EditText attachPasswordED = (EditText) dialog.findViewById(R.id.attachPasswordED);
        final EditText confirmPasswordED = (EditText) dialog.findViewById(R.id.confirmPasswordED);

        attachPinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (attachPasswordED.getText().toString().trim().isEmpty()) {
                    Snackbar.make(attachPasswordED, getString(R.string.emptyPasswordError), Snackbar.LENGTH_SHORT).show();
                } else if (!attachPasswordED.getText().toString().trim().equals(confirmPasswordED.getText().toString().trim())) {
                    Snackbar.make(attachPasswordED, getString(R.string.not_equals_error), Snackbar.LENGTH_SHORT).show();
                } else {
                    if (attachPasswordED.getText().toString().trim().equals(confirmPasswordED.getText().toString().trim())) {
                        proceedPinAttachment(attachPasswordED.getText().toString().trim());
                    }
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

    private void proceedPinAttachment(String password) {
        sharedHelper.putPinAttached(true);
        sharedHelper.putPin(password);
        removePin.setVisibility(View.VISIBLE);
    }

    private void proceedFingerprintAttachment() {
        sharedHelper.putFingerPrintAttached(true);
        removeFingerPrint.setVisibility(View.VISIBLE);
    }

    private void showFingerprintDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.fingerprint_view);
        attachFingerPrintButton = (Button) dialog.findViewById(R.id.attachFingerPrintButton);
        attachFingerPrintButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFingerPrintSucceed) {
                    proceedFingerprintAttachment();
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

}
