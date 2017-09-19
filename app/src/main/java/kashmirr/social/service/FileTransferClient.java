package kashmirr.social.service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.kashmirr.social.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;

import kashmirr.social.utils.SharedHelper;

import static com.github.nkzawa.socketio.client.Socket.EVENT_CONNECT;
import static kashmirr.social.utils.Constants.EVENT_FILE_TRANSFER_CLIENT_READY;
import static kashmirr.social.utils.Constants.EVENT_ON_TRANSFER_BYTES;
import static kashmirr.social.utils.Constants.EVENT_ON_TRANSFER_COMPLETED;
import static kashmirr.social.utils.Constants.FILE_SHARING_URL;
import static kashmirr.social.utils.Constants.FILE_TRANSFER_EXTRA_KEY;
import static kashmirr.social.utils.NotificationUtils.sendNotification;


public class FileTransferClient extends Service {
    private String fileName;
    private com.github.nkzawa.socketio.client.Socket socket;
    private String serverUserId;
    private SharedHelper sharedHelper;
    private OutputStream outStream;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    private boolean notified;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        destroySocket();
        sharedHelper = new SharedHelper(getApplicationContext());
        buildNotification();
        try {
            startClient(intent);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return START_NOT_STICKY;
    }

    private void startClient(Intent intent) throws JSONException, URISyntaxException {

        socket = IO.socket(FILE_SHARING_URL);
        socket.on(EVENT_CONNECT, onSocketConnected);
        socket.on(EVENT_ON_TRANSFER_BYTES, onTransferBytes);
        socket.on(EVENT_ON_TRANSFER_COMPLETED, onTransferCompleted);
        socket.connect();

        String json = intent.getExtras().getString(FILE_TRANSFER_EXTRA_KEY);
        JSONObject data = new JSONObject(json);
        String filePath = data.optString("filePath");
        serverUserId = data.optString("serverUserId");
        File file = new File(filePath);
        fileName = file.getName();

        File destinationFile = new File(Environment.getExternalStorageDirectory(), fileName);
        try {
            outStream = new FileOutputStream(destinationFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    private Emitter.Listener onSocketConnected = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = new JSONObject();
            try {
                data.put("serverUserId", serverUserId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            socket.emit(EVENT_FILE_TRANSFER_CLIENT_READY, data);

        }
    };

    private Emitter.Listener onTransferBytes = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject jsonObject = (JSONObject) args[0];
            long fileLength = jsonObject.optLong("fileLength");
            int remainingBytesLength = jsonObject.optInt("remainingBytesLength");

            String destinationId = jsonObject.optString("destinationId");
            if (destinationId.equals(sharedHelper.getUserId())) {
                if (mNotifyManager == null) {
                    buildNotification();
                }
                if (!notified) {
                    notified = true;
                    Intent data = new Intent(getPackageName() + ".Chat");
                    data.putExtra("success", true);
                    data.putExtra("action", "transferStarted");
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(data);
                }

                byte[] buffer = (byte[]) jsonObject.opt("bytes");
                try {
                    outStream.write(buffer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private Emitter.Listener onTransferCompleted = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject jsonObject = (JSONObject) args[0];
            String destinationId = jsonObject.optString("destinationId");
            if (destinationId.equals(sharedHelper.getUserId())) {
                if (outStream != null) {
                    try {
                        outStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Intent data = new Intent(getPackageName() + ".Chat");
                data.putExtra("success", true);
                data.putExtra("action", "downloadDone");
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(data);
                sendNotification(0, getApplicationContext(), "Transfer done", "The file was downloaded into internal storage");
                destroySocket();
                stopSelf();
            }
        }
    };


    private void destroySocket() {
        if (socket != null) {
            socket.off(EVENT_CONNECT, onSocketConnected);
            socket.off(EVENT_ON_TRANSFER_BYTES, onTransferBytes);
            socket.off(EVENT_ON_TRANSFER_COMPLETED, onTransferCompleted);
            socket.disconnect();
            socket.close();
        }
    }

    private void buildNotification() {
        mNotifyManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle("File Download")
                .setContentText("Download in progress")
                .setSmallIcon(R.mipmap.ic_launcher);
        mBuilder.setProgress(0, 0, true);
        mNotifyManager.notify(0, mBuilder.build());
    }

}