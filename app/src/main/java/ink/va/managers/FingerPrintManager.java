package ink.va.managers;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import ink.va.interfaces.FingerprintCallback;
import ink.va.utils.PermissionsChecker;
import lombok.Setter;

import static android.content.Context.FINGERPRINT_SERVICE;
import static android.content.Context.KEYGUARD_SERVICE;

/**
 * Created by PC-Comp on 2/27/2017.
 */

@RequiresApi(api = Build.VERSION_CODES.M)
public class FingerPrintManager extends FingerprintManager.AuthenticationCallback {

    private static final String KEY_NAME = "key_storage_name";
    private FingerprintManager fingerprintManager;
    private KeyguardManager keyguardManager;
    private KeyStore keyStore;
    private KeyGenerator keyGenerator;
    private Cipher cipher;
    private FingerprintManager.CryptoObject cryptoObject;
    private Context context;
    private CancellationSignal cancellationSignal;
    @Setter
    private FingerprintCallback onFingerprintCallback;

    @RequiresApi(api = Build.VERSION_CODES.M)
    public FingerPrintManager(Context context) {
        this.context = context;
        keyguardManager =
                (KeyguardManager) context.getSystemService(KEYGUARD_SERVICE);
        fingerprintManager =
                (FingerprintManager) context.getSystemService(FINGERPRINT_SERVICE);
    }


    public void init() {

        if (!keyguardManager.isKeyguardSecure()) {
            if (onFingerprintCallback != null) {
                onFingerprintCallback.onLockScreenNotSecured();
            }
            return;
        }
        if (!PermissionsChecker.isFingerprintPermissionGranted(context)) {
            if (onFingerprintCallback != null) {
                onFingerprintCallback.onPermissionNeeded();
            }
            return;
        }


        try {
            if (!fingerprintManager.hasEnrolledFingerprints()) {
                if (onFingerprintCallback != null) {
                    onFingerprintCallback.onNoFingerPrints();
                }
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        generateKey();
    }

    private void generateKey() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    "AndroidKeyStore");
        } catch (NoSuchAlgorithmException |
                NoSuchProviderException e) {
            throw new RuntimeException(
                    "Failed to get KeyGenerator instance", e);
        }

        try {
            keyStore.load(null);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                keyGenerator.init(new
                        KeyGenParameterSpec.Builder(KEY_NAME,
                        KeyProperties.PURPOSE_ENCRYPT |
                                KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                        .setUserAuthenticationRequired(true)
                        .setEncryptionPaddings(
                                KeyProperties.ENCRYPTION_PADDING_PKCS7)
                        .build());
            }
            keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException |
                InvalidAlgorithmParameterException
                | CertificateException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean cipherInit() {
        try {
            cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException |
                NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher", e);
        }

        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME,
                    null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException
                | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void startAuthentication() {
        if (cipherInit()) {
            cryptoObject =
                    new FingerprintManager.CryptoObject(cipher);
            cancellationSignal = new CancellationSignal();

            if (ActivityCompat.checkSelfPermission(context,
                    Manifest.permission.USE_FINGERPRINT) !=
                    PackageManager.PERMISSION_GRANTED) {
                return;
            }
            fingerprintManager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
        }
    }


    @Override
    public void onAuthenticationError(int errMsgId, CharSequence errString) {
        if (onFingerprintCallback != null) {
            onFingerprintCallback.onAuthenticationError(errMsgId, errString);
        }
    }

    @Override
    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        if (onFingerprintCallback != null) {
            onFingerprintCallback.onAuthenticationHelp(helpMsgId, helpString);
        }
    }

    @Override
    public void onAuthenticationFailed() {
        if (onFingerprintCallback != null) {
            onFingerprintCallback.onAuthenticationFailed();
        }
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        if (onFingerprintCallback != null) {
            onFingerprintCallback.onAuthenticationSucceeded(result);
        }
    }

    public boolean supportsFingerprint() {
        try {
            return fingerprintManager.isHardwareDetected() && fingerprintManager.hasEnrolledFingerprints();
        } catch (SecurityException e) {
            e.printStackTrace();
            return false;
        }

    }
}
