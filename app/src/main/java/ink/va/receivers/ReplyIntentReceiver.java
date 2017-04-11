package ink.va.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.RemoteInput;
import android.widget.Toast;

import com.google.gson.Gson;
import com.ink.va.R;

import org.json.JSONException;
import org.json.JSONObject;

import ink.va.callbacks.GeneralCallback;
import ink.va.models.ChatModel;
import ink.va.service.SocketService;
import ink.va.utils.RealmHelper;
import ink.va.utils.SharedHelper;
import ink.va.utils.Time;
import lombok.Setter;

import static ink.va.service.SocketService.NOTIFICATION_REPLY;
import static ink.va.utils.Constants.EVENT_PING;
import static ink.va.utils.Constants.EVENT_SEND_MESSAGE;
import static ink.va.utils.Constants.NOTIFICATION_MESSAGE_BUNDLE_KEY;
import static ink.va.utils.Constants.STATUS_DELIVERED;

/**
 * Created by USER on 2017-03-01.
 */

public class ReplyIntentReceiver extends BroadcastReceiver {
    private SharedHelper sharedHelper;
    private SocketService socketService;
    @Setter
    private OnBindCallback onBindCallback;
    private Context context;
    private int notificationId;
    private Gson chatGSON;
    private String jsonExtra;
    private JSONObject receivedMessageJson;
    private String mOpponentId;
    private String mCurrentUserId;
    private boolean isSocialAccount;
    private String opponentImage;
    private String mFirstName;
    private String mLastName;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        sharedHelper = new SharedHelper(context);
        ping();
        String messageToSend = "";
        chatGSON = new Gson();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            jsonExtra = extras.getString(NOTIFICATION_MESSAGE_BUNDLE_KEY);
            notificationId = extras.getInt("notificationId");
        }

        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {
            CharSequence charSequence = remoteInput.getCharSequence(NOTIFICATION_REPLY);
            messageToSend = charSequence.toString().replaceAll(":\\)", "\u263A")
                    .replaceAll(":\\(", "\u2639").replaceAll(":D", "\uD83D\uDE00").trim();
        }

        try {
            receivedMessageJson = new JSONObject(jsonExtra);
            mOpponentId = receivedMessageJson.optString("userId");
            mCurrentUserId = receivedMessageJson.optString("opponentId");
            isSocialAccount = receivedMessageJson.optBoolean("isCurrentUserSocial");
            opponentImage = receivedMessageJson.optString("currentUserImage");
            mFirstName = receivedMessageJson.optString("firstName");
            mLastName = receivedMessageJson.optString("lastName");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String finalMessageToSend = messageToSend;

        if (socketService == null) {
            setOnBindCallback(new OnBindCallback() {
                @Override
                public void onBind() {
                    sendMessage(finalMessageToSend);
                }
            });
            Intent messageIntent = new Intent(context, SocketService.class);
            socketService = SocketService.get();
            if (socketService == null) {
                context.startService(messageIntent);
                socketService = SocketService.get();
            }
            sendMessage(finalMessageToSend);
        } else {
            sendMessage(finalMessageToSend);
        }

    }

    private void sendMessage(String finalMessageToSend) {
        socketService.connectSocket();
        final ChatModel chatModel = chatGSON.fromJson(receivedMessageJson.toString(), ChatModel.class);
        chatModel.setDate(Time.getCurrentTime());
        chatModel.setMessageId(String.valueOf(System.currentTimeMillis()));
        chatModel.setMessage(finalMessageToSend);
        chatModel.setUserId(mCurrentUserId);
        chatModel.setOpponentId(mOpponentId);
        chatModel.setFirstName(mFirstName);
        chatModel.setLastName(mLastName);
        chatModel.setOpponentFirstName(sharedHelper.getFirstName());
        chatModel.setOpponentLastName(sharedHelper.getLastName());
        chatModel.setStickerUrl("");
        chatModel.setDeliveryStatus(STATUS_DELIVERED);
        chatModel.setStickerChosen(false);
        chatModel.setSocialAccount(isSocialAccount);
        chatModel.setCurrentUserSocial(sharedHelper.isSocialAccount());
        chatModel.setCurrentUserImage(sharedHelper.getImageLink());
        chatModel.setOpponentImage(opponentImage);

        try {
            receivedMessageJson.put("messageId", System.currentTimeMillis());
            receivedMessageJson.put("userId", sharedHelper.getUserId());
            receivedMessageJson.put("opponentId", mOpponentId);
            receivedMessageJson.put("firstName", sharedHelper.getFirstName());
            receivedMessageJson.put("lastName", sharedHelper.getLastName());
            receivedMessageJson.put("opponentFirstName", mFirstName);
            receivedMessageJson.put("opponentLastName", mLastName);
            receivedMessageJson.put("opponentImage", opponentImage);
            receivedMessageJson.put("currentUserImage", sharedHelper.getImageLink());
            receivedMessageJson.put("isSocialAccount", isSocialAccount);
            receivedMessageJson.put("isCurrentUserSocial", sharedHelper.isSocialAccount());
            receivedMessageJson.put("message", finalMessageToSend);
            receivedMessageJson.put("date", Time.getCurrentTime());
            receivedMessageJson.put("stickerChosen", false);
            receivedMessageJson.put("stickerUrl", "");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        localMessageInsert(chatModel, receivedMessageJson);
    }

    protected void ping() {
        JSONObject pingJson = new JSONObject();
        try {
            pingJson.put("currentUserId", sharedHelper.getUserId());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (socketService != null) {
            socketService.emit(EVENT_PING, pingJson);
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            SocketService.LocalBinder binder = (SocketService.LocalBinder) service;
            socketService = binder.getService();
            onBindCallback.onBind();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {

        }
    };

    private void localMessageInsert(ChatModel chatModel, final JSONObject messageJson) {
        RealmHelper.getInstance().insertMessage(chatModel, new GeneralCallback() {
            @Override
            public void onSuccess(Object o) {
                socketService.setEmitListener(new GeneralCallback() {
                    @Override
                    public void onSuccess(Object o) {
                        socketService.destroyEmitListener();
                        removeNotificationIfNeeded();
                    }

                    @Override
                    public void onFailure(Object o) {

                    }
                });
                socketService.emit(EVENT_SEND_MESSAGE, messageJson);
            }

            @Override
            public void onFailure(Object o) {
                Toast.makeText(context, context.getString(R.string.failedToSent), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeNotificationIfNeeded() {
        final NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                notificationManager.cancel(Integer.valueOf(notificationId));
                RealmHelper.getInstance().removeNotificationCount(context, notificationId);
            }
        });

    }


    private interface OnBindCallback {
        void onBind();
    }
}
