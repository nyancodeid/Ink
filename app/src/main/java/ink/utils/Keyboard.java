package ink.utils;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by PC-Comp on 8/11/2016.
 */
public class Keyboard {
    public static void hideKeyboard(Context context, View viewForToken) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(viewForToken.getWindowToken(), 0);
    }
}
