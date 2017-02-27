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

package ink.va.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.ink.va.R;

import java.util.Map;

import ink.va.activities.HomeActivity;
import ink.va.activities.RequestsView;
import ink.va.activities.SplashScreen;
import ink.va.callbacks.GeneralCallback;
import ink.va.utils.Notification;
import ink.va.utils.RealmHelper;
import ink.va.utils.SharedHelper;

import static ink.va.utils.Constants.DELETE_MESSAGE_REQUESTED;
import static ink.va.utils.Constants.NOTIFICAITON_TYPE_GLOBAL_CHAT_MESSAGE;
import static ink.va.utils.Constants.NOTIFICATION_BUNDLE_EXTRA_KEY;
import static ink.va.utils.Constants.NOTIFICATION_POST_ID_KEY;
import static ink.va.utils.Constants.NOTIFICATION_TYPE_CALL;
import static ink.va.utils.Constants.NOTIFICATION_TYPE_CHAT_ROULETTE;
import static ink.va.utils.Constants.NOTIFICATION_TYPE_COMMENT_ADDED;
import static ink.va.utils.Constants.NOTIFICATION_TYPE_FRIEND_REQUEST;
import static ink.va.utils.Constants.NOTIFICATION_TYPE_FRIEND_REQUEST_ACCEPTED;
import static ink.va.utils.Constants.NOTIFICATION_TYPE_GROUP_REQUEST;
import static ink.va.utils.Constants.NOTIFICATION_TYPE_LOCATION_REQUEST_ACCEPTED;
import static ink.va.utils.Constants.NOTIFICATION_TYPE_LOCATION_REQUEST_DECLINED;
import static ink.va.utils.Constants.NOTIFICATION_TYPE_LOCATION_SESSION_ENDED;
import static ink.va.utils.Constants.NOTIFICATION_TYPE_LOCATION_UPDATES;
import static ink.va.utils.Constants.NOTIFICATION_TYPE_POSTED_IN_GROUP;
import static ink.va.utils.Constants.NOTIFICATION_TYPE_POST_LIKED;
import static ink.va.utils.Constants.NOTIFICATION_TYPE_POST_MADE;
import static ink.va.utils.Constants.NOTIFICATION_TYPE_REQUESTING_LOCATION;

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
        // Build the notification and add the action
        mSharedHelper = new SharedHelper(this);


        final Map<String, String> response = remoteMessage.getData();
        if (response.get("type") == null) {
            return;
        }
        String type = response.get("type");

        switch (type) {
            case NOTIFICATION_TYPE_GROUP_REQUEST:
                sendGroupRequestNotification(getApplicationContext(), response.get("requesterName"),
                        response.get("requestedGroup"), response.get("requestId"));
                break;

            case NOTIFICATION_TYPE_CHAT_ROULETTE:
                Intent intent = new Intent(getPackageName() + "WaitRoom");
                intent.putExtra("currentUserId", response.get("currentUserId"));
                intent.putExtra("opponentId", response.get("opponentId"));
                intent.putExtra("message", response.get("message"));
                intent.putExtra("isDisconnected", response.get("isDisconnected"));
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                break;
            case NOTIFICATION_TYPE_CALL:
                break;

            case NOTIFICAITON_TYPE_GLOBAL_CHAT_MESSAGE:
                intent = new Intent(getPackageName() + ".GlobalVipChat");
                intent.putExtra("data", remoteMessage);
                LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
                localBroadcastManager.sendBroadcast(intent);
                break;

            case NOTIFICATION_TYPE_FRIEND_REQUEST:
                Log.d(TAG, "onMessageReceived: " + "user with id " + response.get("requesterId") + " with the name " + response.get("requesterName") + " with the image"
                        + response.get("requesterImage") + " requested to be friend with you");

                sendFriendRequestNotification(getApplicationContext(), response.get("requesterName"), response.get("requestId"));
                break;

            case NOTIFICATION_TYPE_REQUESTING_LOCATION:
                String requesterName = response.get("requesterName");
                String requesterId = response.get("requesterId");
                sendLocationRequestNotification(getApplicationContext(), requesterId, requesterName);
                break;

            case NOTIFICATION_TYPE_LOCATION_SESSION_ENDED:
                requesterName = response.get("requesterName");
                intent = new Intent(getPackageName() + ".Chat");
                intent.putExtra("requesterName", requesterName);
                intent.putExtra("type", NOTIFICATION_TYPE_LOCATION_SESSION_ENDED);
                localBroadcastManager = LocalBroadcastManager.getInstance(this);
                localBroadcastManager.sendBroadcast(intent);
                break;

            case NOTIFICATION_TYPE_COMMENT_ADDED:
                if (mSharedHelper.showCommentNotification()) {
                    String postId = response.get("postId");
                    mSharedHelper.putPostId(postId);
                    String firstName = response.get("firstName");
                    String lastName = response.get("lastName");
                    String commentId = response.get("id");
                    String commentBody = response.get("commentBody");

                    Bundle bundle = new Bundle();
                    bundle.putString(NOTIFICATION_POST_ID_KEY, postId);

                    sendGeneralNotification(getApplicationContext(), commentId, firstName + " " + lastName + " " + getString(R.string.commented_post),
                            commentBody, bundle);
                    Intent commentIntent = new Intent(getPackageName() + "HomeActivity");
                    commentIntent.putExtra(NOTIFICATION_POST_ID_KEY, postId);
                    LocalBroadcastManager.getInstance(NotificationService.this).sendBroadcast(commentIntent);
                }
                break;


            case NOTIFICATION_TYPE_POSTED_IN_GROUP:
                if (mSharedHelper.showGroupNotification()) {
                    String name = response.get("name");
                    String id = response.get("id");
                    String groupName = response.get("groupName");
                    sendGeneralNotification(getApplicationContext(), id,
                            getString(R.string.group_post_title) + " " + groupName,
                            name + " " + getString(R.string.posted_text) + " " + groupName,
                            null);
                }
                LocalBroadcastManager.getInstance(NotificationService.this).sendBroadcast(new Intent(getPackageName() + "HomeActivity"));

                break;

            case NOTIFICATION_TYPE_POST_LIKED:
                if (mSharedHelper.showLikeNotification()) {
                    String firstName = response.get("firstName");
                    String lastName = response.get("lastName");
                    String postId = response.get("id");
                    sendGeneralNotification(getApplicationContext(), postId, getString(R.string.likeText),
                            firstName + " " + lastName + getString(R.string.likedPostText),
                            null);

                    sendLikeNotification(getApplicationContext(), firstName + " " + lastName, postId);
                }
                LocalBroadcastManager.getInstance(NotificationService.this).sendBroadcast(new Intent(getPackageName() + "HomeActivity"));
                break;


            case NOTIFICATION_TYPE_LOCATION_REQUEST_DECLINED:
                requesterName = response.get("requesterName");
                intent = new Intent(getPackageName() + ".Chat");
                intent.putExtra("requesterName", requesterName);
                intent.putExtra("type", "locationSession");
                intent.putExtra("hasAccepted", false);
                localBroadcastManager = LocalBroadcastManager.getInstance(this);
                localBroadcastManager.sendBroadcast(intent);
                break;

            case NOTIFICATION_TYPE_LOCATION_REQUEST_ACCEPTED:
                requesterName = response.get("requesterName");
                intent = new Intent(getPackageName() + ".Chat");
                intent.putExtra("requesterName", requesterName);
                intent.putExtra("type", "locationSession");
                intent.putExtra("hasAccepted", true);
                localBroadcastManager = LocalBroadcastManager.getInstance(this);
                localBroadcastManager.sendBroadcast(intent);
                break;
            case NOTIFICATION_TYPE_FRIEND_REQUEST_ACCEPTED:
                sendGeneralNotification(getApplicationContext(), response.get("requesterId"), response.get("requesterName") + " " +
                        getString(R.string.acceptedYourFriendRequest), "", null);
                break;
            case NOTIFICATION_TYPE_LOCATION_UPDATES:
                String latitude = response.get("latitude");
                String longitude = response.get("longitude");

                intent = new Intent(getPackageName() + ".Chat");
                intent.putExtra("latitude", latitude);
                intent.putExtra("longitude", longitude);
                intent.putExtra("type", NOTIFICATION_TYPE_LOCATION_UPDATES);
                localBroadcastManager = LocalBroadcastManager.getInstance(this);
                localBroadcastManager.sendBroadcast(intent);
                break;
            case NOTIFICATION_TYPE_POST_MADE:
                String postId = response.get("postId");
                String firstName = response.get("firstName");
                String lastName = response.get("lastName");
                sendGeneralNotification(getApplicationContext(), postId, getString(R.string.newPost),
                        firstName + " " + lastName + " " + getString(R.string.madePost), null);
                break;
            case DELETE_MESSAGE_REQUESTED:
                String messageId = response.get("messageId");
                if (Notification.get().isSendingLocal()) {
                    RealmHelper.getInstance().removeMessage(messageId, new GeneralCallback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean aBoolean) {

                        }

                        @Override
                        public void onFailure(Boolean aBoolean) {

                        }
                    });
                } else {
                    intent = new Intent(getPackageName() + ".Chat");
                    intent.putExtra("messageId", messageId);
                    intent.putExtra("type", DELETE_MESSAGE_REQUESTED);
                    localBroadcastManager = LocalBroadcastManager.getInstance(this);
                    localBroadcastManager.sendBroadcast(intent);
                }


                break;
            default:
                sendGeneralNotification(getApplicationContext(),
                        response.get("uniqueId"), response.get("title"),
                        response.get("content"), null);
        }

    }

    private void sendLikeNotification(Context context, String requesterName, String requestId) {

        NotificationManager notificationManagerCompat = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        Intent requestsViewIntent = new Intent(context, mSharedHelper.isLoggedIn() ? HomeActivity.class : SplashScreen.class);
        PendingIntent likeNotification = PendingIntent.getActivity(context, Integer.valueOf(requestId), requestsViewIntent, 0);

        android.support.v7.app.NotificationCompat.Builder builder = new android.support.v7.app.NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.ic_account_multiple_plus_white_24dp);
        builder.setAutoCancel(true);
        builder.setContentIntent(likeNotification);
        builder.setContentTitle(requesterName + getString(R.string.likedPostText));
        builder.setGroup(GROUP_KEY_MESSAGES);
        builder.setDefaults(android.app.Notification.DEFAULT_ALL);
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.toViewTheRequest)));
        builder.setShowWhen(true);
        android.app.Notification notification = builder.build();
        notificationManagerCompat.notify(Integer.valueOf(requestId), notification);
    }


    private void sendLocationRequestNotification(Context context, String requestId, String requesterName) {

        NotificationManager notificationManagerCompat = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        Intent requestsViewIntent = new Intent(context, RequestsView.class);
        requestsViewIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);


        PendingIntent requestsViewPending = PendingIntent.getActivity(context, Integer.valueOf(requestId), requestsViewIntent, 0);
        android.support.v7.app.NotificationCompat.Builder builder = new android.support.v7.app.NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.location_vector);
        builder.setAutoCancel(true);


        builder.setContentTitle(requesterName + " " + getString(R.string.requestedShareLocation));
        builder.setContentText(getString(R.string.requestedTextBody));
        builder.setGroup(GROUP_KEY_MESSAGES);
        builder.setDefaults(android.app.Notification.DEFAULT_ALL);
        builder.setContentIntent(requestsViewPending);
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.toViewTheRequest)));
        builder.setShowWhen(true);
        android.app.Notification notification = builder.build();
        notificationManagerCompat.notify(Integer.valueOf(requestId), notification);
    }

    public void sendGroupRequestNotification(Context context, String requesterName, String requestedGroup,
                                             String requestId) {

        NotificationManager notificationManagerCompat = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        Intent requestsViewIntent = new Intent(context, RequestsView.class);
        requestsViewIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);


        PendingIntent requestsViewPending = PendingIntent.getActivity(context, Integer.valueOf(requestId), requestsViewIntent, 0);
        android.support.v7.app.NotificationCompat.Builder builder = new android.support.v7.app.NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.ic_account_switch_white_24dp);
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
        builder.setSmallIcon(R.drawable.ic_account_multiple_plus_white_24dp);
        builder.setAutoCancel(true);


        builder.setContentTitle(requesterName + " " + getString(R.string.tobeFriend));
        builder.setContentText(getString(R.string.toViewTheRequest));
        builder.setGroup(GROUP_KEY_MESSAGES);
        builder.setDefaults(android.app.Notification.DEFAULT_ALL);
        builder.setContentIntent(requestsViewPending);
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.toViewTheRequest)));
        builder.setShowWhen(true);
        android.app.Notification notification = builder.build();
        notificationManagerCompat.notify(Integer.valueOf(requestId), notification);
    }

    private void sendGeneralNotification(Context context, String uniqueId,
                                         String title, String contentText,
                                         @Nullable Bundle extra) {

        NotificationManager notificationManagerCompat = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        android.support.v4.app.NotificationCompat.Builder builder = new android.support.v4.app.NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.ic_bell_ring_outline_white_24dp);
        builder.setAutoCancel(true);


        builder.setContentTitle(title);
        builder.setContentText(contentText);
        builder.setGroup(GROUP_KEY_MESSAGES);
        builder.setDefaults(android.app.Notification.DEFAULT_ALL);
        builder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText(contentText)
        );

        Intent requestsViewIntent = new Intent(context, mSharedHelper.isLoggedIn() ? HomeActivity.class : SplashScreen.class);
        if (extra != null) {
            requestsViewIntent.putExtra(NOTIFICATION_BUNDLE_EXTRA_KEY, extra);
        }
        requestsViewIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        requestsViewIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent requestsViewPending = PendingIntent.getActivity(context, Integer.valueOf(uniqueId), requestsViewIntent, 0);
        builder.setContentIntent(requestsViewPending);

        builder.setShowWhen(true);
        android.app.Notification notification = builder.build();
        notificationManagerCompat.notify(Integer.valueOf(uniqueId), notification);
    }
}