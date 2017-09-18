package kashmirr.social.service;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import static kashmirr.social.utils.Constants.FILE_TRANSFER_EXTRA_KEY;


public class FileTransferClient extends Service {

    private ClientThread clientThread;
    private String ipAddress;
    private int port;
    private String fileName;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            startClient(intent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return START_NOT_STICKY;
    }

    private void startClient(Intent intent) throws JSONException {
        String json = intent.getExtras().getString(FILE_TRANSFER_EXTRA_KEY);
        JSONObject data = new JSONObject(json);
        ipAddress = data.optString("hostAddress");
        port = data.optInt("hostPort");
        String filePath = data.optString("filePath");
        File file = new File(filePath);
        fileName = file.getName();

        clientThread = new ClientThread(ipAddress, port);
        clientThread.start();
    }


    private class ClientThread extends Thread {
        String dstAddress;
        int dstPort;

        ClientThread(String address, int port) {
            dstAddress = address;
            dstPort = port;
        }

        @Override
        public void run() {
            Socket socket = null;

            try {
                socket = new Socket(dstAddress, dstPort);

                File file = new File(
                        Environment.getExternalStorageDirectory(),
                        fileName);

                InputStream ois = socket.getInputStream();

                byte[] buffer = new byte[ois.available()];
                ois.read(buffer);
                OutputStream outStream = null;
                try {
                    outStream = new FileOutputStream(file);
                    outStream.write(buffer);
                } finally {
                    if (outStream != null) {
                        outStream.close();
                    }

                }
                Looper.prepare();
                Toast.makeText(getApplicationContext(), "transfer completed", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (socket != null) {
                    //                        socket.close();
                }
            }
        }
    }
}
