package ink.va.utils;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.Nullable;

import java.io.File;

/**
 * Created by PC-Comp on 4/17/2017.
 */

public class Download {

    public static void downloadFiled(Context context, String fullUrlToLoad, @Nullable DownloadCallback downloadCallback) {

        if (!PermissionsChecker.isStoragePermissionGranted(context)) {
            if (downloadCallback != null) {
                downloadCallback.onPermissionNeeded();
            }
            return;
        }

        File directory = new File(Environment.getExternalStorageDirectory()
                + "/" + FileUtils.INK_DIRECTORY_NAME);

        if (!directory.exists()) {
            directory.mkdirs();
        }
        String fileName = FileUtils.INK_FILE_PREFIX + "-" + System.currentTimeMillis();
        String fullFileName = fileName + "." + FileUtils.getExtension(fullUrlToLoad);

        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(
                Uri.parse(fullUrlToLoad));
        request.setTitle(fileName);
        request.setDestinationInExternalPublicDir("/" + FileUtils.INK_DIRECTORY_NAME, fullFileName);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setVisibleInDownloadsUi(true);
        downloadManager.enqueue(request);
    }

    public interface DownloadCallback {
        void onPermissionNeeded();

    }
}
