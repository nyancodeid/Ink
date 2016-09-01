package ink.utils;

import android.content.Context;
import android.os.Process;

import com.bumptech.glide.Glide;

/**
 * Created by PC-Comp on 8/11/2016.
 */
public class IonCache {
    public static void clearGlideCache(final Context context) {
        System.gc();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                Glide.get(context).clearDiskCache();
            }
        }).start();
    }
}
