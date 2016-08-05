package ink.utils;

import android.os.Build;

/**
 * Created by USER on 2016-08-06.
 */
public class Device {

    public static String getDeviceManufacturer() {
        String manufacturer = Build.MANUFACTURER;
        return manufacturer;
    }

    public static String getDeviceModel() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        return StringUtils.capitalize(model);
    }

    public static String getDeviceFullName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return StringUtils.capitalize(model);
        } else {
            return StringUtils.capitalize(manufacturer) + " " + model;
        }
    }

    public static String getNumberOfCores() {
        return String.valueOf(Runtime.getRuntime().availableProcessors());
    }

    public static String getMaximumHeapSize() {
        return String.valueOf(Runtime.getRuntime().maxMemory());
    }

    public static String getFreeMemoryInBytes() {
        return String.valueOf(Runtime.getRuntime().freeMemory());
    }
}
