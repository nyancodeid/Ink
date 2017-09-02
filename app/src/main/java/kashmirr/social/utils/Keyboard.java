package kashmirr.social.utils;

import android.app.Activity;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by PC-Comp on 8/11/2016.
 */
public class Keyboard {
    public static void hideKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (activity.getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }

    }
}
