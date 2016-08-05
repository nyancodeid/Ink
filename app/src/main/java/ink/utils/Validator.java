package ink.utils;

import android.text.TextUtils;

/**
 * Created by USER on 2016-08-06.
 */
public class Validator {
    public final static boolean isValidEmail(String target) {
        if (TextUtils.isEmpty(target)) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }
}
