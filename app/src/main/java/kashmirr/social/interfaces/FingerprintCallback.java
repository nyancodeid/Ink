package kashmirr.social.interfaces;

import android.hardware.fingerprint.FingerprintManager;

/**
 * Created by PC-Comp on 2/27/2017.
 */

public interface FingerprintCallback {
    void onLockScreenNotSecured();

    void onPermissionNeeded();

    void onNoFingerPrints();

    void onAuthenticationError(int errMsgId, CharSequence errString);

    void onAuthenticationHelp(int helpMsgId, CharSequence helpString);

    void onAuthenticationFailed();

    void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result);
}
