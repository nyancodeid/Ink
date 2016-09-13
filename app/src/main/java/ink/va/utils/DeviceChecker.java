package ink.va.utils;

import android.os.Build;

/**
 * Created by USER on 2016-07-22.
 */
public class DeviceChecker {
    public static String checkDevice() {
        return Build.MANUFACTURER;
    }

    public static boolean isHuawei() {
        return Build.MANUFACTURER.equals(Constants.HUAWEI_MODEL);
    }
}
