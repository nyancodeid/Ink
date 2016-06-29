package ink.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by USER on 2016-06-30.
 */
public class Debug {
    public static String getKeyHash(Context context) {
        try {
            String sign = null;
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                sign = Base64.encodeToString(md.digest(), Base64.DEFAULT);
            }
            return sign;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }
}
