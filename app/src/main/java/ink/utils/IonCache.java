package ink.utils;

import android.content.Context;

import com.koushikdutta.ion.Ion;

/**
 * Created by PC-Comp on 8/11/2016.
 */
public class IonCache {
    public static void clearIonCache(Context context) {
        Ion.getDefault(context).getCache().clear();
        Ion.getDefault(context).getBitmapCache().clear();
        Ion.getDefault(context).configure().getResponseCache().clear();
    }
}
