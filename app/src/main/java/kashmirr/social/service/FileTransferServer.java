package kashmirr.social.service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.kashmirr.social.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

import kashmirr.social.utils.SharedHelper;

import static com.github.nkzawa.socketio.client.Socket.EVENT_CONNECT;
import static kashmirr.social.utils.Constants.EVENT_COMPLETE_TRANSFER;
import static kashmirr.social.utils.Constants.EVENT_FILE_TRANSFER_SERVER_READY;
import static kashmirr.social.utils.Constants.EVENT_NO_FILE_EXIST;
import static kashmirr.social.utils.Constants.EVENT_ON_FILE_TRANSFER_CLIENT_READY;
import static kashmirr.social.utils.Constants.EVENT_TRANSFER_BYTES;
import static kashmirr.social.utils.Constants.FILE_SHARING_URL;
import static kashmirr.social.utils.Constants.FILE_TRANSFER_EXTRA_KEY;
import static kashmirr.social.utils.NotificationUtils.sendNotification;

public class FileTransferServer extends Service {
    private File file;
    private SocketService socketService;
    private Intent intent;
    private JSONObject jsonToSend;
    private com.github.nkzawa.socketio.client.Socket socket;
    private SharedHelper sharedHelper;
    private String requesterId;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    private String requesterFirstName;
    private String requesterLastName;


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.intent = intent;
        destroySocket();
        sharedHelper = new SharedHelper(getApplicationContext());
        Intent socketIntent = new Intent(this, SocketService.class);
        if (socketService == null) {
            bindService(socketIntent, mConnection, BIND_AUTO_CREATE);
        } else {
            try {
                startServer(intent);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return START_NOT_STICKY;
    }

    private void startServer(Intent intent) throws JSONException, URISyntaxException {
        socket = IO.socket(FILE_SHARING_URL);
        socket.on(EVENT_CONNECT, onSocketConnected);
        socket.on(EVENT_ON_FILE_TRANSFER_CLIENT_READY, onFileTransferClientReady);
        socket.connect();

        String json = intent.getExtras().getString(FILE_TRANSFER_EXTRA_KEY);
        JSONObject data = new JSONObject(json);
        requesterId = data.optString("requesterId");
        String filePath = data.optString("filePath");

        requesterFirstName = data.optString("requesterFirstName");
        requesterLastName = data.optString("requesterLastName");


        file = new File(filePath);
        if (file.exists()) {
            jsonToSend = new JSONObject();
            jsonToSend.put("filePath", filePath);
            jsonToSend.put("serverUserId", sharedHelper.getUserId());
            jsonToSend.put("destinationId", requesterId);
            buildNotification();
        } else {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("destinationId", requesterId);
            socketService.emit(EVENT_NO_FILE_EXIST, jsonObject);
        }

    }

    private void destroyBinder() {
        try {
            unbindService(mConnection);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void destroySocket() {
        if (socket != null) {
            socket.off(EVENT_CONNECT, onSocketConnected);
            socket.off(EVENT_ON_FILE_TRANSFER_CLIENT_READY, onFileTransferClientReady);
            socket.disconnect();
            socket.close();
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            SocketService.LocalBinder binder = (SocketService.LocalBinder) service;
            socketService = binder.getService();
            try {
                startServer(intent);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {

        }
    };


    public class FileTxThread extends Thread {


        @Override
        public void run() {
            try {

                final int BUFFER_SIZE = 1024 * 1024;
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("destinationId", requesterId);
                jsonObject.put("fileLength", file.length());

                FileInputStream fileInputStream = new FileInputStream(file);
                byte[] buffer = new byte[BUFFER_SIZE];

                while (fileInputStream.read(buffer) > 0) {
                    jsonObject.put("bytes", buffer);
                    jsonObject.put("remainingBytesLength", fileInputStream.available());
                    socket.emit(EVENT_TRANSFER_BYTES, jsonObject);
                }
                fileInputStream.close();
                socket.emit(EVENT_COMPLETE_TRANSFER, jsonObject);
                sendNotification(0, getApplicationContext(), "Transfer of " + file.getName() + " done", "The transfer of " + file.getName() + " is completed for " + requesterFirstName + " " + requesterLastName);
                destroySocket();
                destroyBinder();
                stopSelf();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
            }

        }
    }


    private Emitter.Listener onSocketConnected = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            socketService.emit(EVENT_FILE_TRANSFER_SERVER_READY, jsonToSend);
        }
    };

    private Emitter.Listener onFileTransferClientReady = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            String serverUserId = data.optString("serverUserId");
            if (serverUserId.equals(sharedHelper.getUserId())) {
                new FileTxThread().start();
            }
        }
    };

    private void buildNotification() {
        String text = "The " + file.getName() + " file is being uploaded by the the request of " + requesterFirstName + " " + requesterLastName;
        mNotifyManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle("The " + file.getName() + " file Upload")
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setSmallIcon(R.mipmap.ic_launcher);
        mBuilder.setProgress(0, 0, true);
        mNotifyManager.notify(0, mBuilder.build());
    }
}
