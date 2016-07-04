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
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.ink.R;
import com.squareup.picasso.Picasso;

import java.util.Map;

import ink.activities.Chat;
import ink.activities.NotificationView;
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
        final Map<String, String> response = remoteMessage.getData();
        Looper looper = Looper.getMainLooper();
        Handler handler = new Handler(looper);
        handler.post(new Runnable() {
            @Override
            public void run() {
                RealmHelper.getInstance().insertMessage(response.get("user_id"), response.get("opponent_id"),
                        response.get("message"), response.get("message_id"), response.get("date"), response.get("message_id"),
                        Constants.STATUS_DELIVERED, response.get("user_image"), response.get("opponent_image"),
                        response.get("delete_opponent_id"), response.get("delete_user_id"));
            }
        });

        mSharedHelper = new SharedHelper(this);
        if (Notification.getInstance().isSendingRemote()) {
            sendNotification("New Message", response.get("user_id"),
                    response.get("message"), getApplicationContext(),
                    response.get("message_id"), response.get("opponent_id"),
                    response.get("opponent_image"), response.get("user_image"), response.get("name"),
                    response.get("delete_user_id"), response.get("delete_opponent_id"));
        } else {
            Intent intent = new Intent(getPackageName() + ".Chat");
            intent.putExtra("data", remoteMessage);
            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
            localBroadcastManager.sendBroadcast(intent);
        }


    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    public void sendNotification(String title, String opponentId,
                                 String messageBody, Context context,
                                 String messageId, String currentUserId,
                                 String userImage, String opponentImage,
                                 String userName, String deleteUserId, String deleteOpponentId) {

        NotificationManager notificationManagerCompat = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        Intent chatIntent = new Intent(context, Chat.class);
        chatIntent.putExtra("firstName", userName);
        chatIntent.putExtra("opponentId", opponentId);
        chatIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);


        Intent intent = new Intent(context, NotificationView.class);
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
        android.support.v7.app.NotificationCompat.Builder builder = new android.support.v7.app.NotificationCompat.Builder(context);
        builder.setSmallIcon(R.mipmap.ic_launcher);

        if (opponentImage != null && !opponentImage.isEmpty()) {
            Picasso.with(context).load(opponentImage).into(getTarget(builder));
        }
        builder.setAutoCancel(true);


        builder.addAction(new NotificationCompat.Action(R.drawable.ic_send_black_24dp, context.getString(R.string.reply),
                pendingIntent));

        builder.setContentTitle("New Message from " + userName);
        builder.setContentText(messageBody);
        builder.setGroup(GROUP_KEY_MESSAGES);
        builder.setDefaults(android.app.Notification.DEFAULT_ALL);
        builder.setContentIntent(chatPending);
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(messageBody));
        builder.setShowWhen(true);
        android.app.Notification notification = builder.build();
        notificationManagerCompat.notify(Integer.valueOf(opponentId), notification);
    }


    private com.squareup.picasso.Target getTarget(final android.support.v7.app.NotificationCompat.Builder builder) {
        return new com.squareup.picasso.Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                builder.setLargeIcon(bitmap);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };
    }
}