/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ink.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.ink.R;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.Map;

import ink.activities.Chat;
import ink.activities.ReplyView;
import ink.activities.RequestsView;
import ink.utils.Constants;
import ink.utils.Notification;
import ink.utils.RealmHelper;
import ink.utils.SharedHelper;

public class NotificationService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private static final String GROUP_KEY_MESSAGES = "Messages_Group";
    private SharedHelper mSharedHelper;

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {

        // Build the notification and add the actio
//
        mSharedHelper = new SharedHelper(this);
        final Map<String, String> response = remoteMessage.getData();
        if (response.get("type") == null) {
            return;
        }
        String type = response.get("type");

        switch (type) {
            case Constants.NOTIFICATION_TYPE_MESSAGE:
                Looper looper = Looper.getMainLooper();
                Handler handler = new Handler(looper);
                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        RealmHelper.getInstance().insertMessage(response.get("user_id"), response.get("opponent_id"),
                                response.get("message"), response.get("message_id"), response.get("date"), response.get("message_id"),
                                Constants.STATUS_DELIVERED, response.get("user_image"), response.get("opponent_image"),
                                response.get("delete_opponent_id"), response.get("delete_user_id"), Boolean.valueOf(response.get("hasGif")), response.get("gifUrl"));
                    }
                });

                if (Notification.getInstance().isSendingRemote()) {
                    sendNotification("New Message", response.get("user_id"),
                            StringEscapeUtils.unescapeJava(response.get("message")), getApplicationContext(),
                            response.get("message_id"), response.get("opponent_id"),
                            response.get("opponent_image"), response.get("opponent_image").isEmpty() ? "" : response.get("opponent_image"), response.get("name"),
                            response.get("delete_user_id"), response.get("delete_opponent_id"), Boolean.valueOf(response.get("isSocialAccount")), response.get("lastName"), Boolean.valueOf(response.get("hasGif")));
                } else {
                    Intent intent = new Intent(getPackageName() + ".Chat");
                    intent.putExtra("data", remoteMessage);
                    intent.putExtra("type", "showMessage");
                    LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
                    localBroadcastManager.sendBroadcast(intent);
                }
                break;
            case Constants.NOTIFICATION_TYPE_GROUP_REQUEST:
                sendGroupRequestNotification(getApplicationContext(), response.get("requesterName"),
                        response.get("requestedGroup"), response.get("requestId"));
                break;

            case Constants.NOTIFICATION_TYPE_CHAT_ROULETTE:
                Intent intent = new Intent(getPackageName() + "WaitRoom");
                intent.putExtra("currentUserId", response.get("currentUserId"));
                intent.putExtra("opponentId", response.get("opponentId"));
                intent.putExtra("message", response.get("message"));
                intent.putExtra("isDisconnected", response.get("isDisconnected"));
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                break;
            case Constants.NOTIFICATION_TYPE_CALL:

                break;

            case Constants.NOTIFICATION_TYPE_FRIEND_REQUEST:
                Log.d(TAG, "onMessageReceived: " + "user with id " + response.get("requesterId") + " with the name " + response.get("requesterName") + " with the image"
                        + response.get("requesterImage") + " requested to be friend with you");

                sendFriendRequestNotification(getApplicationContext(),response.get("requesterName"),response.get("requestId"));
                break;
        }

    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    public void sendNotification(String title, String opponentId,
                                 String messageBody, final Context context,
                                 String messageId, String currentUserId,
                                 String userImage, final String opponentImage,
                                 String userName, String deleteUserId, String deleteOpponentId,
                                 boolean isSocialAccount, String lastName, boolean hasGif) {

        NotificationManager notificationManagerCompat = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        if (hasGif && !messageBody.trim().isEmpty()) {
            String oldMesssage = messageBody;
            messageBody = oldMesssage + "\n\n" + userImage + " " + getString(R.string.haveSentSticker);
        } else if (hasGif && messageBody.trim().isEmpty()) {
            messageBody = userName + " " + getString(R.string.haveSentSticker);
        }

        Log.d(TAG, "sendNotification: " + opponentImage);
        Intent chatIntent = new Intent(context, Chat.class);
        chatIntent.setAction(opponentId);
        chatIntent.putExtra("firstName", userName);
        chatIntent.putExtra("lastName", lastName);
        chatIntent.putExtra("isSocialAccount", isSocialAccount);
        chatIntent.putExtra("opponentId", opponentId);
        chatIntent.putExtra("opponentImage", opponentImage);

        chatIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);


        Intent intent = new Intent(context, ReplyView.class);
        intent.putExtra("message", messageBody);
        intent.putExtra("mOpponentId", opponentId);
        intent.putExtra("mCurrentUserId", currentUserId);
        intent.putExtra("userImage", userImage);
        intent.putExtra("opponentImage", opponentImage);
        intent.putExtra("username", userName);
        intent.putExtra("deleteUserId", deleteUserId);
        intent.putExtra("deleteOpponentId", deleteUserId);

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent chatPending = PendingIntent.getActivity(context, Integer.valueOf(opponentId), chatIntent, 0);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, Integer.valueOf(opponentId), intent, 0);
        final android.support.v7.app.NotificationCompat.Builder builder = new android.support.v7.app.NotificationCompat.Builder(context);
        builder.setSmallIcon(R.mipmap.ic_launcher);

        // TODO: 8/11/2016 check if we can add user image to notification
//        if (opponentImage != null && !opponentImage.isEmpty()) {
//            Handler handler = new Handler(getMainLooper());
//            handler.post(new Runnable() {
//                @Override
//                public void run() {
//                    Ion.with(context).load(opponentImage).intoImageView(getTarget(builder));
//                }
//            });
//        }
        builder.setAutoCancel(true);


        builder.addAction(new NotificationCompat.Action(R.drawable.ic_send_black_24dp, context.getString(R.string.reply),
                pendingIntent));

        builder.setContentTitle(getString(R.string.newMessage) + " " + userName);
        builder.setContentText(messageBody);
        builder.setGroup(GROUP_KEY_MESSAGES);
        builder.setDefaults(android.app.Notification.DEFAULT_ALL);
        builder.setContentIntent(chatPending);
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(messageBody));
        builder.setShowWhen(true);
        android.app.Notification notification = builder.build();
        notificationManagerCompat.notify(Integer.valueOf(opponentId), notification);
    }

    public void sendGroupRequestNotification(Context context, String requesterName, String requestedGroup,
                                             String requestId) {

        NotificationManager notificationManagerCompat = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        Intent requestsViewIntent = new Intent(context, RequestsView.class);
        requestsViewIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);


        PendingIntent requestsViewPending = PendingIntent.getActivity(context, Integer.valueOf(requestId), requestsViewIntent, 0);
        android.support.v7.app.NotificationCompat.Builder builder = new android.support.v7.app.NotificationCompat.Builder(context);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setAutoCancel(true);


        builder.setContentTitle(requesterName + " " + getString(R.string.requestedText) + " " +
                "'" + requestedGroup + "'");
        builder.setContentText(getString(R.string.requestedTextBody));
        builder.setGroup(GROUP_KEY_MESSAGES);
        builder.setDefaults(android.app.Notification.DEFAULT_ALL);
        builder.setContentIntent(requestsViewPending);
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.requestedTextBody)));
        builder.setShowWhen(true);
        android.app.Notification notification = builder.build();
        notificationManagerCompat.notify(Integer.valueOf(requestId), notification);
    }

    public void sendFriendRequestNotification(Context context, String requesterName,
                                              String requestId) {

        NotificationManager notificationManagerCompat = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        Intent requestsViewIntent = new Intent(context, RequestsView.class);
        requestsViewIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);


        PendingIntent requestsViewPending = PendingIntent.getActivity(context, Integer.valueOf(requestId), requestsViewIntent, 0);
        android.support.v7.app.NotificationCompat.Builder builder = new android.support.v7.app.NotificationCompat.Builder(context);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setAutoCancel(true);


        builder.setContentTitle(requesterName + " " + getString(R.string.tobeFriend));
        builder.setContentText(getString(R.string.friendRequestInfo));
        builder.setGroup(GROUP_KEY_MESSAGES);
        builder.setDefaults(android.app.Notification.DEFAULT_ALL);
        builder.setContentIntent(requestsViewPending);
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.friendRequestInfo)));
        builder.setShowWhen(true);
        android.app.Notification notification = builder.build();
        notificationManagerCompat.notify(Integer.valueOf(requestId), notification);
    }

}