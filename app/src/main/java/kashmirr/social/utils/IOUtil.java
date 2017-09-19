package kashmirr.social.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import in.gauriinfotech.commons.Commons;

/**
 * Created by vladi on 17.09.2017.
 */

public class IOUtil {


    public static byte[] fileToBytes(String path) throws IOException {
        File file = new File(path);
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
            bufferedInputStream.read(bytes, 0, bytes.length);
            bufferedInputStream.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bytes;
    }

    public static void bytesToFile(String fileNameWithExt) throws IOException, ClassNotFoundException {
        File file = new File(Environment.getExternalStorageDirectory() + "/" + fileNameWithExt);

// convert File to byte[]
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(file);
        bos.close();
        oos.close();
        byte[] bytes = bos.toByteArray();

// convert byte[] to File
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bis);
        File fileFromBytes = (File) ois.readObject();
        bis.close();
        ois.close();
    }

    public static String getPathFromUri(Context context, Uri uri) {
        String fullPath = Commons.getPath(uri, context);
        return fullPath;
    }
}
