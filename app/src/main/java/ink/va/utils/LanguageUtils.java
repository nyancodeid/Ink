package ink.va.utils;

import android.content.Context;

/**
 * Created by PC-Comp on 4/20/2017.
 */

public class LanguageUtils {

    public static String getLocalLanguage(Context context) {
        String localeLanguage;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            localeLanguage = context.getResources().getConfiguration().getLocales().get(0).getLanguage();
        } else {
            localeLanguage = context.getResources().getConfiguration().locale.getLanguage();
        }
        return localeLanguage;
    }
}
