package ink.va.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.RemoteInput;
import android.support.v4.content.LocalBroadcastManager;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.google.gson.Gson;
import com.ink.va.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import ink.va.activities.Chat;
import ink.va.activities.Comments;
import ink.va.activities.MafiaRoomActivity;
import ink.va.activities.ReplyView;
import ink.va.activities.RequestsView;
import ink.va.activities.SplashScreen;
import ink.va.callbacks.GeneralCallback;
import ink.va.interfaces.SocketListener;
import ink.va.models.ChatModel;
import ink.va.receivers.AlarmReceiver;
import ink.va.receivers.DeleteReceiver;
import ink.va.receivers.NotificationBroadcast;
import ink.va.receivers.ReplyIntentReceiver;
import ink.va.utils.AlarmUtils;
import ink.va.utils.Notification;
import ink.va.utils.RealmHelper;
import ink.va.utils.SharedHelper;
import me.leolin.shortcutbadger.ShortcutBadger;

import static com.github.nkzawa.socketio.client.Socket.EVENT_CONNECT;
import static com.github.nkzawa.socketio.client.Socket.EVENT_CONNECT_ERROR;
import static com.github.nkzawa.socketio.client.Socket.EVENT_CONNECT_TIMEOUT;
import static com.github.nkzawa.socketio.client.Socket.EVENT_DISCONNECT;
import static ink.va.utils.Constants.EVENT_ADD_USER;
import static ink.va.utils.Constants.EVENT_COMMENT_RECEIVED;
import static ink.va.utils.Constants.EVENT_FRIEND_REQUEST_ACCEPT_RECEIVED;
import static ink.va.utils.Constants.EVENT_FRIEND_REQUEST_DECLINE_RECEIVED;
import static ink.va.utils.Constants.EVENT_FRIEND_REQUEST_RECEIVED;
import static ink.va.utils.Constants.EVENT_MESSAGE_RECEIVED;
import static ink.va.utils.Constants.EVENT_MESSAGE_SENT;
import static ink.va.utils.Constants.EVENT_NEW_MESSAGE;
import static ink.va.utils.Constants.EVENT_ONLINE_STATUS;
import static ink.va.utils.Constants.EVENT_ON_COMMENT_ADDED;
import static ink.va.utils.Constants.EVENT_ON_FRIEND_REQUESTED;
import static ink.va.utils.Constants.EVENT_ON_FRIEND_REQUEST_ACCEPTED;
import static ink.va.utils.Constants.EVENT_ON_FRIEND_REQUEST_DECLINED;
import static ink.va.utils.Constants.EVENT_ON_GAME_CREATED;
import static ink.va.utils.Constants.EVENT_ON_POST_LIKED;
import static ink.va.utils.Constants.EVENT_ON_POST_MADE;
import static ink.va.utils.Constants.EVENT_POST_LIKE_RECEIVED;
import static ink.va.utils.Constants.EVENT_POST_MADE_RECEIVED;
import static ink.va.utils.Constants.EVENT_STOPPED_TYPING;
import static ink.va.utils.Constants.EVENT_TYPING;
import static ink.va.utils.Constants.NOTIFICATION_BUNDLE_EXTRA_KEY;
import static ink.va.utils.Constants.NOTIFICATION_MESSAGE_BUNDLE_KEY;
import static ink.va.utils.Constants.POST_TYPE_GLOBAL;
import static ink.va.utils.Constants.SOCKET_URL;
import static ink.va.utils.Constants.STATUS_DELIVERED;


public class SocketService extends Service {
    private static final String TAG = SocketService.class.getSimpleName();
    public static final String NOTIFICATION_REPLY = "notificationReply";
    private SharedHelper sharedHelper;
    private String currentUserId;
    private LocalBinder mBinder = new LocalBinder();
    private SocketListener onSocketListener;
    private List<Integer> socketListeners = new LinkedList<>();
    private GeneralCallback emitListener;
    public static SocketService socketService;

    public SocketService() {
        socketService = this;
    }

    private com.github.nkzawa.socketio.client.Socket mSocket;

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initSocketFully();
        return START_STICKY;
    }

    public static SocketService get() {
        return socketService;
    }

    private void initSocketFully() {
        sharedHelper = new SharedHelper(this);
        currentUserId = sharedHelper.getUserId();
        destroySocket();
        try {
            mSocket = IO.socket(SOCKET_URL);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        initSocket();

    }


    /**
     * Socket Listeners
     */

    private Emitter.Listener onFriendRequestDeclined = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject jsonObject = (JSONObject) args[0];
            emit(EVENT_FRIEND_REQUEST_DECLINE_RECEIVED, jsonObject);

            String destinationId = jsonObject.optString("destinationId");
            String declinerFirstName = jsonObject.optString("declinerFirstName");
            String declinerLastName = jsonObject.optString("declinerLastName");
            String requestId = jsonObject.optString("requestId");

            sendGeneralNotification(getString(R.string.requestDenied), declinerFirstName + " " + declinerLastName + " " + getString(R.string.deniedYourRequest),
                    Integer.valueOf(requestId), getString(R.string.requestDenied),
                    declinerFirstName + " " + declinerLastName + " " + getString(R.string.deniedYourRequest), R.drawable.remove_friend_icon, SplashScreen.class, null, null);
        }
    };

    private Emitter.Listener onFriendRequestAccepted = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject jsonObject = (JSONObject) args[0];
            emit(EVENT_FRIEND_REQUEST_ACCEPT_RECEIVED, jsonObject);
            String destinationId = jsonObject.optString("destinationId");
            String acceptorFirstName = jsonObject.optString("acceptorFirstName");
            String acceptorLastName = jsonObject.optString("acceptorLastName");
            String requestId = jsonObject.optString("requestId");

            sendGeneralNotification(getString(R.string.requestAccepted), acceptorFirstName + " " + acceptorLastName + " " + getString(R.string.acceptedYourFriendRequest),
                    Integer.valueOf(requestId), getString(R.string.requestAccepted),
                    acceptorFirstName + " " + acceptorLastName + " " + getString(R.string.acceptedYourFriendRequest), R.drawable.friend_request_white_icon, SplashScreen.class, null, null);

        }
    };

    private Emitter.Listener onCommentAdded = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject jsonObject = (JSONObject) args[0];
            emit(EVENT_COMMENT_RECEIVED, sharedHelper.getUserId());

            Bundle bundle = new Bundle();
            bundle.putString("postId", jsonObject.optString("postId"));
            bundle.putString("userImage", jsonObject.optString("userImage"));
            bundle.putString("postBody", jsonObject.optString("postBody"));
            bundle.putString("attachment", jsonObject.optString("attachment"));
            bundle.putString("isSocialAccount", jsonObject.optString("isSocialAccount"));
            bundle.putString("location", jsonObject.optString("location"));
            bundle.putString("name", jsonObject.optString("name"));
            bundle.putString("date", jsonObject.optString("date"));
            bundle.putString("likesCount", jsonObject.optString("likesCount"));
            bundle.putString("isLiked", jsonObject.optString("isLiked"));
            bundle.putString("ownerId", jsonObject.optString("ownerId"));
            bundle.putString("attachmentPresent", jsonObject.optString("attachmentPresent"));
            bundle.putString("addressPresent", jsonObject.optString("addressPresent"));
            bundle.putString("attachmentName", jsonObject.optString("attachmentName"));
            bundle.putString("addressName", jsonObject.optString("addressName"));
            bundle.putString("postId", jsonObject.optString("postId"));
            bundle.putString("postBody", jsonObject.optString("postBody"));
            bundle.putString("isPostOwner", jsonObject.optString("isPostOwner"));
            bundle.putString("isFriend", jsonObject.optString("isFriend"));

            String postId = jsonObject.optString("postId");
            String commenterFirstName = jsonObject.optString("commenterFirstName");
            String commenterLastName = jsonObject.optString("commenterLastName");
            String commentBody = jsonObject.optString("commentBody");
            int commentId = Integer.valueOf(jsonObject.optString("commentId"));

            if (sharedHelper.hasCommented(postId)) {
                sendGeneralNotification(getString(R.string.commentAdded), commenterFirstName + " " + commenterLastName + " " + getString(R.string.commented_post),
                        commentId, commenterFirstName + " " + commenterLastName + " " + getString(R.string.commented_post),
                        commentBody, R.drawable.comment_icon_white, Comments.class, bundle, NOTIFICATION_BUNDLE_EXTRA_KEY);
            } else if (sharedHelper.hasPostLiked(postId)) {
                sendGeneralNotification(getString(R.string.commentAdded), commenterFirstName + " " + commenterLastName + " " + getString(R.string.commented_post),
                        commentId, commenterFirstName + " " + commenterLastName + " " + getString(R.string.commented_post),
                        commentBody, R.drawable.comment_icon_white, Comments.class, bundle, NOTIFICATION_BUNDLE_EXTRA_KEY);
            } else if (sharedHelper.postOwner(postId)) {
                sendGeneralNotification(getString(R.string.commentAdded), commenterFirstName + " " + commenterLastName + " " + getString(R.string.commented_post),
                        commentId, commenterFirstName + " " + commenterLastName + " " + getString(R.string.commented_post),
                        commentBody, R.drawable.comment_icon_white, Comments.class, bundle, NOTIFICATION_BUNDLE_EXTRA_KEY);
            }
        }
    };

    private Emitter.Listener onPostLiked = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject jsonObject = (JSONObject) args[0];
            emit(EVENT_POST_LIKE_RECEIVED, jsonObject);

            String likerFirstName = jsonObject.optString("likerFirstName");
            String likerLastName = jsonObject.optString("likerLastName");
            String likerId = jsonObject.optString("likerId");

            sendGeneralNotification(getString(R.string.postLiked), likerFirstName + " " + likerLastName + " " + getString(R.string.likedPostText), Integer.valueOf(likerId), getString(R.string.postLiked),
                    likerFirstName + " " + likerLastName + " " + getString(R.string.likedPostText), R.drawable.like_icon_white, SplashScreen.class, null, null);
        }
    };

    private Emitter.Listener onPostMade = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject jsonObject = (JSONObject) args[0];
            emit(EVENT_POST_MADE_RECEIVED, jsonObject);
            String posterId = jsonObject.optString("posterId");
            String insertedPostId = jsonObject.optString("insertedPostId");
            String posterFirstName = jsonObject.optString("posterFirstName");
            String posterLastName = jsonObject.optString("posterLastName");
            String postType = jsonObject.optString("postType");

            sendGeneralNotification(getString(R.string.newPost), posterFirstName + " " + posterLastName + " " + (postType.equals(POST_TYPE_GLOBAL) ? getString(R.string.madeGlobalPost) : getString(R.string.madePost)),

                    Integer.valueOf(insertedPostId), getString(R.string.newPost),
                    posterFirstName + " " + posterLastName + " " + (postType.equals(POST_TYPE_GLOBAL) ? getString(R.string.madeGlobalPost) : getString(R.string.madePost)), R.drawable.feed_white_icon, SplashScreen.class, null, null);

        }
    };

    private Emitter.Listener onFriendRequested = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject jsonObject = (JSONObject) args[0];
            emit(EVENT_FRIEND_REQUEST_RECEIVED, jsonObject);
            String requesterFirstName = jsonObject.optString("requesterFirstName");
            String requesterLastName = jsonObject.optString("requesterLastName");
            String requestedUserId = jsonObject.optString("requestedUserId");
            String requesterId = jsonObject.optString("requesterId");

            sendGeneralNotification(getString(R.string.friendRequested), requesterFirstName + " " + requesterLastName + " " + getString(R.string.sentFriendRequest),
                    Integer.valueOf(requesterId), getString(R.string.friendRequested),
                    requesterFirstName + " " + requesterLastName + " " + getString(R.string.sentFriendRequest), R.drawable.request_friend_icon, RequestsView.class, null, null);
        }
    };

    private Emitter.Listener onOnlineStatusReceived = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject jsonObject = (JSONObject) args[0];
            int minutesDifference = jsonObject.optInt("minutesDifference");
            int hoursDifference = jsonObject.optInt("hoursDifference");
            int daysDifference = jsonObject.optInt("daysDifference");
            boolean dateAvailable = jsonObject.optBoolean("dateAvailable");
            String finalString;
            String dateString = "";
            boolean isOnline;
            if (dateAvailable) {
                if (daysDifference != 0) {
                    dateString = daysDifference + " " + getString(R.string.days);
                } else if (hoursDifference != 0) {
                    dateString = hoursDifference + " " + getString(R.string.hours);
                } else if (minutesDifference != 0) {
                    dateString = minutesDifference + " " + getString(R.string.minutes);
                }

                finalString = getString(R.string.lastSeen, dateString);
                isOnline = minutesDifference < 1 ? true : false;
            } else {
                isOnline = false;
                finalString = getString(R.string.dateNotAvailable);
            }

            if (onSocketListener != null) {
                onSocketListener.onOnlineStatusReceived(isOnline, finalString);
            }
        }
    };

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
                                sendMessageNotification(getApplicationContext(), messageJson);
                            }
                        });
                    }

                    @Override
                    public void onFailure(Object o) {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                sendMessageNotification(getApplicationContext(), messageJson);
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

    private Emitter.Listener onMafiaGameCreated = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject jsonObject = (JSONObject) args[0];
            String firstName = jsonObject.optString("firstName");
            String lastName = jsonObject.optString("lastName");
            String roomId = jsonObject.optString("roomId");

            String messageTitle = getString(R.string.gameRoomCreated);
            String messageContent = getString(R.string.userCreatedRoom, firstName + " " + lastName);
            String bigTextSummary = getString(R.string.viewRoom);
            String bigTextContentBody = getString(R.string.userCreatedRoomLong, firstName + " " + lastName);
            if (sharedHelper.showMafiaNotification()) {
                sendGeneralNotification(messageTitle, messageContent, Integer.valueOf(roomId),
                        bigTextSummary, bigTextContentBody,
                        R.drawable.ic_gamepad_variant_white_24dp, MafiaRoomActivity.class, null, null);
            }
        }
    };

    private Emitter.Listener onMessageSent = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject jsonObject = (JSONObject) args[0];
            if (onSocketListener != null) {
                onSocketListener.onMessageSent(jsonObject);
            }
            if (emitListener != null) {
                emitListener.onSuccess(null);
            }
            String messageId = jsonObject.optString("messageId");
            updateRealmMessage(messageId);
        }

    };

    private void updateRealmMessage(String messageId) {
        RealmHelper.getInstance().updateMessageDeliveryStatus(messageId, STATUS_DELIVERED, null);
    }

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

    private Emitter.Listener onSocketTimeOut = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            mSocket.connect();
        }
    };

    private Emitter.Listener onSocketDisconnected = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if (onSocketListener != null) {
                onSocketListener.onSocketDisconnected();
            }
            if (mSocket != null) {
                mSocket.connect();
            }
        }
    };

    private Emitter.Listener onSocketConnectionError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if (onSocketListener != null) {
                onSocketListener.onSocketConnectionError();
            }
            mSocket.connect();
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
        mSocket.on(EVENT_MESSAGE_SENT, onMessageSent);
        mSocket.on(EVENT_CONNECT_TIMEOUT, onSocketTimeOut);
        mSocket.on(EVENT_ONLINE_STATUS, onOnlineStatusReceived);
        mSocket.on(EVENT_ON_GAME_CREATED, onMafiaGameCreated);
        mSocket.on(EVENT_ON_COMMENT_ADDED, onCommentAdded);
        mSocket.on(EVENT_ON_FRIEND_REQUEST_ACCEPTED, onFriendRequestAccepted);
        mSocket.on(EVENT_ON_FRIEND_REQUEST_DECLINED, onFriendRequestDeclined);
        mSocket.on(EVENT_ON_POST_LIKED, onPostLiked);
        mSocket.on(EVENT_ON_POST_MADE, onPostMade);
        mSocket.on(EVENT_ON_FRIEND_REQUESTED, onFriendRequested);

        mSocket.connect();
    }

    public class LocalBinder extends Binder {
        public SocketService getService() {
            return SocketService.this;
        }
    }

    public void setOnSocketListener(SocketListener onSocketListener, int id) {
        boolean addListener = true;

        for (int socketId : socketListeners) {
            if (socketId == id) {
                addListener = false;
                break;
            }
        }
        if (addListener) {
            this.onSocketListener = onSocketListener;
        }
    }

    public void destroyListener() {
        onSocketListener = null;
        socketListeners.clear();
    }

    public boolean isSocketConnected() {
        return mSocket.connected();
    }

    public void emit(String event, Object... args) {
        mSocket.emit(event, args);
    }

    public void setEmitListener(GeneralCallback emitListener) {
        this.emitListener = null;
        this.emitListener = emitListener;
    }

    public void destroyEmitListener() {
        emitListener = null;
    }

    public void connectSocket() {
        if (!mSocket.connected()) {
            mSocket.connect();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AlarmUtils.scheduleAlarmWithMinutes(this, AlarmReceiver.class, 10);
        destroySocket();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Notification.get().setSendingRemote(true);
        AlarmUtils.scheduleAlarmWithMinutes(getApplicationContext(), AlarmReceiver.class, 10);
        destroySocket();
        super.onTaskRemoved(rootIntent);
    }

    private void destroySocket() {
        if (mSocket != null) {
            mSocket.disconnect();
            mSocket.off(EVENT_CONNECT, onSocketConnected);
            mSocket.off(EVENT_ON_FRIEND_REQUEST_ACCEPTED, onFriendRequestAccepted);
            mSocket.off(EVENT_CONNECT_ERROR, onSocketConnectionError);
            mSocket.off(EVENT_ON_FRIEND_REQUEST_DECLINED, onFriendRequestDeclined);
            mSocket.off(EVENT_DISCONNECT, onSocketDisconnected);
            mSocket.off(EVENT_NEW_MESSAGE, onNewMessageReceived);
            mSocket.off(EVENT_STOPPED_TYPING, onUserStoppedTyping);
            mSocket.off(EVENT_ON_GAME_CREATED, onMafiaGameCreated);
            mSocket.off(EVENT_TYPING, onUserTyping);
            mSocket.off(EVENT_MESSAGE_SENT, onMessageSent);
            mSocket.off(EVENT_CONNECT_TIMEOUT, onSocketTimeOut);
            mSocket.off(EVENT_ONLINE_STATUS, onOnlineStatusReceived);
            mSocket.off(EVENT_ON_COMMENT_ADDED, onCommentAdded);
            mSocket.off(EVENT_ON_POST_LIKED, onPostLiked);
            mSocket.off(EVENT_ON_POST_MADE, onPostMade);
            mSocket.off(EVENT_ON_FRIEND_REQUESTED, onFriendRequested);
        }
    }


    public static void sendMessageNotification(final Context context, final JSONObject jsonObject) {
        final String firstName = jsonObject.optString("firstName");
        final String lastName = jsonObject.optString("lastName");
        final String message = jsonObject.optString("message");
        final String opponentId = jsonObject.optString("userId");
        final StringBuilder stringBuilder = new StringBuilder();


        RealmHelper.getInstance().getNotificationCount(Integer.valueOf(opponentId), new RealmHelper.QueryReadyListener() {
            @Override
            public void onQueryReady(Object... results) {
                int querySize = (int) results[0];
                if (querySize != 0) {
                    ShortcutBadger.applyCount(context, (querySize + 1));
                }
                List<String> messages = (List<String>) results[1];

                for (String message : messages) {
                    stringBuilder.append("\n" + (message.isEmpty() ? context.getString(R.string.sentSticker) : message));
                }
                stringBuilder.append(messages.isEmpty() ? message.isEmpty() ?
                        context.getString(R.string.sentSticker) : message
                        : "\n" + (message.isEmpty() ? context.getString(R.string.sentSticker) : message));


                Intent requestsViewIntent = new Intent(context, Chat.class);
                requestsViewIntent.putExtra(NOTIFICATION_MESSAGE_BUNDLE_KEY, jsonObject.toString());
                requestsViewIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                requestsViewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                requestsViewIntent.setAction(opponentId);

                Intent replyIntent = new Intent(context, ReplyView.class);
                replyIntent.putExtra(NOTIFICATION_MESSAGE_BUNDLE_KEY, jsonObject.toString());
                replyIntent.setAction(opponentId);

                Intent deleteIntent = new Intent(context, DeleteReceiver.class);
                deleteIntent.putExtra("notificationId", Integer.valueOf(opponentId));
                deleteIntent.setAction(opponentId);

                Intent nReplyIntent = new Intent(context, ReplyIntentReceiver.class);
                nReplyIntent.putExtra("notificationId", Integer.valueOf(opponentId));
                nReplyIntent.putExtra(NOTIFICATION_MESSAGE_BUNDLE_KEY, jsonObject.toString());
                nReplyIntent.setAction(opponentId);


                Intent broadcastIntent = new Intent(context, NotificationBroadcast.class);
                broadcastIntent.putExtra(NOTIFICATION_MESSAGE_BUNDLE_KEY, jsonObject.toString());
                broadcastIntent.setAction(opponentId);

                PendingIntent broadcastPendingIntent = PendingIntent.getBroadcast(context, Integer.valueOf(opponentId),
                        broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                PendingIntent replyPendingIntent = PendingIntent.getActivity(context,
                        Integer.valueOf(opponentId), replyIntent, PendingIntent.FLAG_UPDATE_CURRENT);


                PendingIntent deleteReceiver = PendingIntent.getBroadcast(context, Integer.valueOf(opponentId), deleteIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

                final NotificationManager notificationManagerCompat = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);


                RemoteInput remoteInput = new RemoteInput.Builder(NOTIFICATION_REPLY)
                        .setLabel(context.getString(R.string.reply))
                        .build();

                PendingIntent nReplyPendingIntent = PendingIntent.getBroadcast(context, 0, nReplyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationCompat.Action nReplyAction =
                        new NotificationCompat.Action.Builder(R.drawable.ic_send_white_24dp,
                                context.getString(R.string.reply), nReplyPendingIntent)
                                .addRemoteInput(remoteInput)
                                .build();

                android.support.v7.app.NotificationCompat.Builder builder = new android.support.v7.app.NotificationCompat.Builder(context);
                builder.setSmallIcon(R.drawable.ic_message_white_24dp);
                builder.setAutoCancel(true);
                builder.setDeleteIntent(deleteReceiver);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    builder.addAction(nReplyAction);
                } else {
                    builder.addAction(R.drawable.ic_send_white_24dp, context.getString(R.string.reply), replyPendingIntent);
                }
                builder.setContentTitle(querySize != 0 ? (querySize + 1) + " " + context.getString(R.string.newMessagesFrom) : context.getString(R.string.newMessageGlobal));
                builder.setContentText(querySize != 0 ? (querySize + 1) + " " +
                        context.getString(R.string.newMessagesFrom) + firstName + " " + lastName
                        : context.getString(R.string.newMessagesFrom) + firstName + " " + lastName);
                builder.setDefaults(android.app.Notification.DEFAULT_ALL);

                builder.setStyle(new NotificationCompat.BigTextStyle()
                        .setSummaryText(context.getString(R.string.newMessage))
                        .setBigContentTitle(context.getString(R.string.myMessages))
                        .bigText(stringBuilder.toString())
                );

                builder.setContentIntent(broadcastPendingIntent);

                builder.setShowWhen(true);
                final android.app.Notification notification = builder.build();

                RealmHelper.getInstance().putNotificationCount(Integer.valueOf(opponentId), message, new RealmHelper.QueryReadyListener() {
                    @Override
                    public void onQueryReady(Object... results) {
                        Intent intent = new Intent(context.getPackageName() + "Messages");
                        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);
                        localBroadcastManager.sendBroadcast(intent);
                        notificationManagerCompat.notify(Integer.valueOf(opponentId), notification);
                    }
                });

            }
        });
    }

    public void sendGeneralNotification(String messageTitle, String messageContent,
                                        int uniqueId, String bigTextSummary,
                                        String bigTextContent, int iconResourceId, Class<?> resultClass, @Nullable Bundle extras, @Nullable String extrasKey) {

        Intent requestsViewIntent = new Intent(getApplicationContext(), resultClass);
        requestsViewIntent.setAction(String.valueOf(uniqueId));
        if (extras != null) {
            requestsViewIntent.putExtra(extrasKey, extras);
        }

        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), uniqueId, requestsViewIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        final NotificationManager notificationManagerCompat = (NotificationManager)
                getApplicationContext().getSystemService(NOTIFICATION_SERVICE);

        android.support.v7.app.NotificationCompat.Builder builder = new android.support.v7.app.NotificationCompat.Builder(getApplicationContext());
        builder.setSmallIcon(iconResourceId);
        builder.setAutoCancel(true);
        builder.setContentTitle(messageTitle);
        builder.setContentText(messageContent);
        builder.setDefaults(android.app.Notification.DEFAULT_ALL);

        builder.setStyle(new NotificationCompat.BigTextStyle()
                .setSummaryText(bigTextSummary)
                .setBigContentTitle(bigTextSummary)
                .bigText(bigTextContent)
        );
        builder.setContentIntent(contentIntent);
        builder.setShowWhen(true);
        final android.app.Notification notification = builder.build();
        notificationManagerCompat.notify(Integer.valueOf(uniqueId), notification);
    }
}
