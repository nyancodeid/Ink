package ink.va.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.google.gson.Gson;
import com.ink.va.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import ink.va.activities.Chat;
import ink.va.callbacks.GeneralCallback;
import ink.va.interfaces.SocketListener;
import ink.va.models.ChatModel;
import ink.va.utils.Notification;
import ink.va.utils.RealmHelper;
import ink.va.utils.SharedHelper;

import static com.github.nkzawa.socketio.client.Socket.EVENT_CONNECT;
import static com.github.nkzawa.socketio.client.Socket.EVENT_CONNECT_ERROR;
import static com.github.nkzawa.socketio.client.Socket.EVENT_DISCONNECT;
import static ink.va.utils.Constants.EVENT_ADD_USER;
import static ink.va.utils.Constants.EVENT_MESSAGE_RECEIVED;
import static ink.va.utils.Constants.EVENT_NEW_MESSAGE;
import static ink.va.utils.Constants.EVENT_STOPPED_TYPING;
import static ink.va.utils.Constants.EVENT_TYPING;
import static ink.va.utils.Constants.NOTIFICATION_MESSAGE_BUNDLE_KEY;
import static ink.va.utils.Constants.SOCKET_URL;

public class MessageService extends Service {
    private SharedHelper sharedHelper;
    private String currentUserId;
    LocalBinder mBinder = new LocalBinder();
    private SocketListener onSocketListener;

    public MessageService() {
    }

    private com.github.nkzawa.socketio.client.Socket mSocket;

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sharedHelper = new SharedHelper(this);
        currentUserId = sharedHelper.getUserId();

        if (mSocket == null) {
            try {
                mSocket = IO.socket(SOCKET_URL);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            initSocket();
        } else if (!mSocket.connected()) {
            try {
                mSocket = IO.socket(SOCKET_URL);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            initSocket();
        }


        return START_STICKY;
    }


    /**
     * Socket Listeners
     */
    private Emitter.Listener onNewMessageReceived = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            final JSONObject messageJson = (JSONObject) args[0];

            Gson chatGSON = new Gson();

            final ChatModel chatModel = chatGSON.fromJson(messageJson.toString(), ChatModel.class);

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("id", currentUserId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            emit(EVENT_MESSAGE_RECEIVED, jsonObject);
            jsonObject = null;

            if (Notification.get().isSendingRemote()) {
                RealmHelper.getInstance().insertMessage(chatModel, new GeneralCallback() {
                    @Override
                    public void onSuccess(Object o) {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                sendGeneralNotification(getApplicationContext(), messageJson);
                            }
                        });
                    }

                    @Override
                    public void onFailure(Object o) {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                sendGeneralNotification(getApplicationContext(), messageJson);
                            }
                        });
                    }
                });
            } else {
                if (onSocketListener != null) {
                    onSocketListener.onNewMessageReceived(messageJson);
                }
            }
        }
    };


    private Emitter.Listener onUserTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            JSONObject jsonObject = (JSONObject) args[0];
            if (onSocketListener != null) {
                onSocketListener.onUserTyping(jsonObject);
            }
        }
    };

    private Emitter.Listener onSocketConnected = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            mSocket.emit(EVENT_ADD_USER, currentUserId);
            if (onSocketListener != null) {
                onSocketListener.onSocketConnected();
            }
        }
    };

    private Emitter.Listener onSocketDisconnected = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if (onSocketListener != null) {
                onSocketListener.onSocketDisconnected();
            }
        }
    };

    private Emitter.Listener onSocketConnectionError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if (onSocketListener != null) {
                onSocketListener.onSocketConnectionError();
            }
        }
    };

    private Emitter.Listener onUserStoppedTyping = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if (onSocketListener != null) {
                JSONObject jsonObject = (JSONObject) args[0];
                onSocketListener.onUserStoppedTyping(jsonObject);
            }
        }
    };

    private void initSocket() {
        mSocket.on(EVENT_CONNECT, onSocketConnected);
        mSocket.on(EVENT_CONNECT_ERROR, onSocketConnectionError);
        mSocket.on(EVENT_DISCONNECT, onSocketDisconnected);
        mSocket.on(EVENT_NEW_MESSAGE, onNewMessageReceived);
        mSocket.on(EVENT_STOPPED_TYPING, onUserStoppedTyping);
        mSocket.on(EVENT_TYPING, onUserTyping);
        mSocket.connect();
    }

    public class LocalBinder extends Binder {
        public MessageService getService() {
            return MessageService.this;
        }
    }

    public void setOnSocketListener(SocketListener onSocketListener) {
        this.onSocketListener = onSocketListener;
    }

    public boolean isSocketConnected() {
        return mSocket.connected();
    }

    public void emit(String event, Object... args) {
        mSocket.emit(event, args);
    }

    public void connectSocket() {
        mSocket.connect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        destroySocket();
    }

    private void destroySocket() {
        mSocket.disconnect();
        mSocket.off(EVENT_CONNECT, onSocketConnected);
        mSocket.off(EVENT_CONNECT_ERROR, onSocketConnectionError);
        mSocket.off(EVENT_DISCONNECT, onSocketDisconnected);
        mSocket.off(EVENT_NEW_MESSAGE, onNewMessageReceived);
        mSocket.off(EVENT_STOPPED_TYPING, onUserStoppedTyping);
        mSocket.off(EVENT_TYPING, onUserTyping);
    }

    public static void sendGeneralNotification(final Context context, final JSONObject jsonObject) {
        final String firstName = jsonObject.optString("firstName");
        final String lastName = jsonObject.optString("lastName");
        final String message = jsonObject.optString("message");
        final String opponentId = jsonObject.optString("userId");

        RealmHelper.getInstance().getNotificationCount(Integer.valueOf(opponentId), new RealmHelper.QueryReadyListener() {
            @Override
            public void onQueryReady(Object result) {
                int querySize = (int) result;


                Intent requestsViewIntent = new Intent(context, Chat.class);
                requestsViewIntent.putExtra(NOTIFICATION_MESSAGE_BUNDLE_KEY, jsonObject.toString());

                PendingIntent requestsViewPending = PendingIntent.getActivity(context,
                        Integer.valueOf(jsonObject.optInt("messageId")), requestsViewIntent, 0);


                final NotificationManager notificationManagerCompat = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

                android.support.v7.app.NotificationCompat.Builder builder = new android.support.v7.app.NotificationCompat.Builder(context);
                builder.setSmallIcon(R.drawable.ic_message_white_24dp);
                builder.setAutoCancel(true);


                builder.setContentTitle(querySize != 0 ? (querySize + 1) + " " + context.getString(R.string.newMessagesFrom) : context.getString(R.string.newMessageGlobal));
                builder.setContentText(querySize != 0 ? (querySize + 1) + " " +
                        context.getString(R.string.newMessagesFrom) + firstName + " " + lastName
                        : context.getString(R.string.newMessagesFrom) + firstName + " " + lastName);
                builder.setDefaults(android.app.Notification.DEFAULT_ALL);

                builder.setStyle(new NotificationCompat.BigTextStyle()
                        .setSummaryText(querySize != 0 ? (querySize + 1) + " " +
                                context.getString(R.string.newMessagesFrom) + firstName + " " + lastName
                                : context.getString(R.string.newMessagesFrom) + firstName + " " + lastName)
                        .setBigContentTitle(querySize != 0 ? (querySize + 1) + " " +
                                context.getString(R.string.newMessagesFrom) + firstName + " " + lastName
                                : context.getString(R.string.newMessagesFrom) + firstName + " " + lastName)
                        .bigText(querySize != 0 ? (querySize + 1) + " " + context.getString(R.string.newMessage) + "  " + context.getString(R.string.lastMessage) +
                                (message.isEmpty() ? context.getString(R.string.sentSticker) : message)
                                : message.isEmpty() ? context.getString(R.string.sentSticker) : message)
                );

                builder.setContentIntent(requestsViewPending);

                builder.setShowWhen(true);
                final android.app.Notification notification = builder.build();

                RealmHelper.getInstance().putNotificationCount(Integer.valueOf(opponentId), new RealmHelper.QueryReadyListener() {
                    @Override
                    public void onQueryReady(Object result) {
                        notificationManagerCompat.notify(Integer.valueOf(opponentId), notification);
                    }
                });

            }
        });

    }
}
