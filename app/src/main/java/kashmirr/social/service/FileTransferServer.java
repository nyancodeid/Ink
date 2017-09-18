package kashmirr.social.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

import static kashmirr.social.utils.Constants.EVENT_FILE_TRANSFER_SERVER_READY;
import static kashmirr.social.utils.Constants.EVENT_ON_NO_FILE_EXIST;
import static kashmirr.social.utils.Constants.FILE_TRANSFER_EXTRA_KEY;

public class FileTransferServer extends Service {
    private ServerSocket serverSocket;
    private int socketServerPORT = 8080;
    private ServerSocketThread serverSocketThread;
    private File file;
    private SocketService socketService;
    private Intent intent;


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.intent = intent;
        Intent socketIntent = new Intent(this, SocketService.class);
        if (socketService == null) {
            bindService(socketIntent, mConnection, BIND_AUTO_CREATE);
        }
        return START_NOT_STICKY;
    }

    private void startServer(Intent intent) throws JSONException {
        String json = intent.getExtras().getString(FILE_TRANSFER_EXTRA_KEY);
        JSONObject data = new JSONObject(json);
        String requesterId = data.optString("requesterId");
        String filePath = data.optString("filePath");
        file = new File(filePath);
        if (file.exists()) {
            String ipAddress = getIpAddress();
            serverSocketThread = new ServerSocketThread();
            serverSocketThread.start();
            JSONObject jsonToSend = new JSONObject();
            jsonToSend.put("hostAddress", ipAddress);
            jsonToSend.put("hostPort", socketServerPORT);
            jsonToSend.put("filePath", filePath);
            jsonToSend.put("destinationId", requesterId);
            socketService.emit(EVENT_FILE_TRANSFER_SERVER_READY, jsonToSend);
        } else {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("destinationId", requesterId);
            socketService.emit(EVENT_ON_NO_FILE_EXIST, jsonObject);
        }

    }

    private String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip = inetAddress.getHostAddress();
                    }

                }

            }

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }

        return ip;
    }


    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            SocketService.LocalBinder binder = (SocketService.LocalBinder) service;
            socketService = binder.getService();
            try {
                startServer(intent);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {

        }
    };

    public class ServerSocketThread extends Thread {

        @Override
        public void run() {
            Socket socket = null;

            try {
                serverSocket = new ServerSocket(socketServerPORT);

                while (true) {
                    socket = serverSocket.accept();
                    FileTxThread fileTxThread = new FileTxThread(socket);
                    fileTxThread.start();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    public class FileTxThread extends Thread {
        Socket socket;

        FileTxThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {

            byte[] bytes = new byte[(int) file.length()];
            BufferedInputStream bis;
            try {
                bis = new BufferedInputStream(new FileInputStream(file));
                bis.read(bytes, 0, bytes.length);
                OutputStream os = socket.getOutputStream();
                os.write(bytes, 0, bytes.length);
                os.flush();
                socket.close();

            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        }
    }
}
