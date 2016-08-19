package ink.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.io.File;
import java.net.URISyntaxException;

/**
 * Created by USER on 2016-07-04.
 */
public class FileUtils {

    public static final String[] IMAGE_TYPES = new String[]{"jpg", "png", "gif", "jpeg"};

    public static String getPath(Context context, Uri uri) throws URISyntaxException {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {"_data"};
            Cursor cursor = null;

            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                // Eat it
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static void clearApplicationData(Context context) {

        File cacheDirectory = context.getCacheDir();
        File applicationDirectory = new File(cacheDirectory.getParent());
        if (applicationDirectory.exists()) {

            String[] fileNames = applicationDirectory.list();

            for (String fileName : fileNames) {

                if (!fileName.equals("lib")) {
                    deleteRecursiveFile(new File(applicationDirectory, fileName));

                }

            }

        }
    }

    public static boolean isImageType(String fileName) {
        boolean success = false;
        int lastIndex = fileName.lastIndexOf(".");
        String fileExtension = fileName.substring(lastIndex + 1, fileName.length());
        for (int i = 0; i < IMAGE_TYPES.length; i++) {
            if (fileExtension.equals(IMAGE_TYPES[i])) {
                success = true;
                break;
            }
        }
        return success;
    }

    public static boolean deleteRecursiveFile(File file) {

        boolean deletedAll = true;

        if (file != null) {
            if (file.isDirectory()) {
                String[] children = file.list();

                for (int i = 0; i < children.length; i++) {
                    deletedAll = deleteRecursiveFile(new File(file, children[i])) && deletedAll;
                }

            } else {
                deletedAll = file.delete();

            }

        }

        return deletedAll;

    }


    public static void deleteDirectoryTree(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteDirectoryTree(child);
            }
        }

        fileOrDirectory.delete();
    }

}
