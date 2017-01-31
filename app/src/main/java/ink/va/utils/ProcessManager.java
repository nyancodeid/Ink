package ink.va.utils;

import android.app.ActivityManager;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * Created by PC-Comp on 1/31/2017.
 */

public class ProcessManager {
    public static List<String> getRunningProcesses(Context context) {
        ArrayList<String> processes = new ArrayList<>();
        ActivityManager activityManager = (ActivityManager)
                context.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();

        for (ActivityManager.RunningAppProcessInfo runningProInfo : procInfos) {
            processes.add(runningProInfo.processName);
        }
        return processes;
    }
}
