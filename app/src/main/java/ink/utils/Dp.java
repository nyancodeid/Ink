package ink.utils;

import android.content.Context;

/**
 * Created by USER on 2016-07-23.
 */
public class Dp {

    public static int toDps(final Context context, final float px) {
        return (int) (px / context.getResources().getDisplayMetrics().density);
    }
}
