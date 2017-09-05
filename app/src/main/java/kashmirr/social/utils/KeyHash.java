package kashmirr.social.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by vladi on 05.09.2017.
 */

public class KeyHash {
    public static String getKeyHash(Context context){
        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : packageInfo.signatures) {
                MessageDigest messageDigest;
                messageDigest = MessageDigest.getInstance("SHA");
                messageDigest.update(signature.toByteArray());
                String keyHash = new String(Base64.encode(messageDigest.digest(), 0));
                //String something = new String(Base64.encodeBytes(md.digest()));
             return keyHash;
            }
        } catch (PackageManager.NameNotFoundException e1) {
            Log.e("name not found", e1.toString());
        } catch (NoSuchAlgorithmException e) {
            Log.e("no such an algorithm", e.toString());
        } catch (Exception e) {
            Log.e("exception", e.toString());
        }
        return "No Key Hash Available";
    }
}
