package ink.va.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import ink.va.callbacks.GeneralCallback;
import ink.va.models.ChatModel;
import ink.va.models.MessageModel;
import ink.va.models.NotificationModel;
import ink.va.models.UserMessagesModel;
import io.realm.DynamicRealm;
import io.realm.FieldAttribute;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.RealmSchema;
import io.realm.Sort;
import io.realm.internal.IOException;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;
import static ink.va.utils.Constants.REALM_DB_NAME;

/**
 * Created by USER on 2016-06-26.
 */
public class RealmHelper {

    private static RealmHelper ourInstance = new RealmHelper();
    private Realm mRealm;
    private List<MessageModel> mModelArray = new ArrayList<>();
    private RealmConfiguration mRealmConfiguration;
    private Handler handler;
    private boolean clearDBSuccess;
    private HandlerThread mHandlerThread;

    public static RealmHelper getInstance() {
        return ourInstance;
    }

    private RealmHelper() {
        mHandlerThread = new HandlerThread("HandlerThread");
        mHandlerThread.setPriority(THREAD_PRIORITY_BACKGROUND);
        mHandlerThread.start();
        handler = new Handler(mHandlerThread.getLooper());
    }

    public void initRealm(final Context context) {

        handler.post(new Runnable() {
            @Override
            public void run() {
                Realm.init(context);
                mRealmConfiguration = new RealmConfiguration.Builder()
                        .name(REALM_DB_NAME)
                        .migration(new RealmMigration() {
                            @Override
                            public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
                                RealmSchema realmSchema = realm.getSchema();
                                Log.d("oldVersion", "migrate: " + oldVersion);
                                if (oldVersion == 0) {
                                    RealmObjectSchema messageSchema = realmSchema.get("MessageModel");
                                    if (!messageSchema.hasField("isSocialAccount")) {
                                        messageSchema.addField("isSocialAccount", boolean.class, FieldAttribute.REQUIRED);
                                    }
                                    if (!messageSchema.hasField("firstName")) {
                                        messageSchema.addField("firstName", String.class, null);
                                    }

                                    if (!messageSchema.hasField("lastName")) {
                                        messageSchema.addField("lastName", String.class, null);
                                    }
                                    oldVersion++;
                                }
                            }
                        })
                        .build();
                mRealm = Realm.getInstance(mRealmConfiguration);
            }
        });
    }

    public void openRealm(final GeneralCallback generalCallback) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                mRealm = Realm.getInstance(mRealmConfiguration);
                generalCallback.onSuccess(mRealm);
            }
        });

    }

    public boolean clearDatabase(@Nullable final GeneralCallback onDatabaseDeleted) {

        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mRealm != null) {
                    try {
                        mRealm.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Realm.deleteRealm(mRealmConfiguration);
                    clearDBSuccess = true;
                } else {
                    clearDBSuccess = false;
                    Realm.deleteRealm(mRealmConfiguration);
                }
                if (onDatabaseDeleted != null) {
                    onDatabaseDeleted.onSuccess(null);

                }
            }
        });

        return clearDBSuccess;
    }


    public void insertMessage(final String userId, final String opponentId, final String message,
                              final String messageId, final String date,
                              final String id, final String deliveryStatus,
                              final String userImage,
                              final String opponentImage, final String deleteOpponentId,
                              final String deleteUserId,
                              final boolean hasGif, final String gifUrl,
                              final boolean animated, @Nullable final GeneralCallback generalCallback) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mRealm.isClosed()) {
                    openRealm(new GeneralCallback() {
                        @Override
                        public void onSuccess(Object o) {
                            mRealm.executeTransactionAsync(new Realm.Transaction() {
                                @Override
                                public void execute(final Realm realm) {
                                    RealmQuery<MessageModel> query = realm.where(MessageModel.class)
                                            .equalTo("messageId", messageId);
                                    if (query.count() == 0) {
                                        MessageModel messageModel = realm.createObject(MessageModel.class);

                                        messageModel.setId(id);
                                        messageModel.setMessageId(messageId);
                                        messageModel.setMessage(message);
                                        messageModel.setUserId(userId);
                                        messageModel.setOpponentId(opponentId);
                                        messageModel.setAnimated(animated);
                                        messageModel.setDeliveryStatus(deliveryStatus);
                                        messageModel.setUserImage(userImage);
                                        messageModel.setOpponentImage(opponentImage);
                                        messageModel.setDate(date);
                                        messageModel.setHasGif(hasGif);
                                        messageModel.setGifUrl(gifUrl);
                                        messageModel.setDeleteUserId(deleteUserId);
                                        messageModel.setDeleteOpponentId(deleteOpponentId);
                                    }
                                    if (generalCallback != null) {
                                        generalCallback.onSuccess(true);
                                    }

                                }
                            });
                        }

                        @Override
                        public void onFailure(Object o) {
                            if (generalCallback != null) {
                                generalCallback.onFailure(false);
                            }
                        }
                    });
                } else {
                    mRealm.executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(final Realm realm) {
                            RealmQuery<MessageModel> query = realm.where(MessageModel.class)
                                    .equalTo("messageId", messageId);
                            if (query.count() == 0) {
                                MessageModel messageModel = realm.createObject(MessageModel.class);

                                messageModel.setId(id);
                                messageModel.setMessageId(messageId);
                                messageModel.setMessage(message);
                                messageModel.setUserId(userId);
                                messageModel.setOpponentId(opponentId);
                                messageModel.setAnimated(animated);
                                messageModel.setDeliveryStatus(deliveryStatus);
                                messageModel.setUserImage(userImage);
                                messageModel.setOpponentImage(opponentImage);
                                messageModel.setDate(date);
                                messageModel.setHasGif(hasGif);
                                messageModel.setGifUrl(gifUrl);
                                messageModel.setDeleteUserId(deleteUserId);
                                messageModel.setDeleteOpponentId(deleteOpponentId);
                            }
                            if (generalCallback != null) {
                                generalCallback.onSuccess(false);
                            }

                        }
                    });
                }

            }
        });
    }

    public void getUserMessages(final String currentUserId, final GeneralCallback<List<UserMessagesModel>> messagesLoadedCallback) {
        final List<UserMessagesModel> messageModels = new LinkedList<>();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mRealm.isClosed()) {
                    openRealm(new GeneralCallback() {
                        @Override
                        public void onSuccess(Object o) {
                            mRealm.executeTransactionAsync(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    RealmResults<MessageModel> messages = realm.where(MessageModel.class).findAll();
                                    HashMap<String, String> opponentIds = new HashMap<>();

                                    for (MessageModel queryModel : messages) {
                                        String opponentId = queryModel.getOpponentId();
                                        String userId = queryModel.getUserId();
                                        String deleteOpponentId = queryModel.getDeleteOpponentId();
                                        String deleteUserId = queryModel.getDeleteUserId();

                                        if (opponentIds.containsKey(userId) & opponentIds.containsValue(opponentId) | opponentIds.containsKey(opponentId) & opponentIds.containsValue(userId) |
                                                !deleteOpponentId.equals("0") | !deleteUserId.equals("0")) {
                                            continue;
                                        }

                                        opponentIds.put(userId, opponentId);
                                    }

                                    for (String key : opponentIds.keySet()) {
                                        String userId = key;
                                        String opponentId = opponentIds.get(key);

                                        MessageModel queryModel = realm.where(MessageModel.class).equalTo("userId", userId).equalTo("opponentId", opponentId)
                                                .or().equalTo("userId", opponentId).equalTo("opponentId", userId).
                                                        findAll().last();

                                        UserMessagesModel userMessagesModel = new UserMessagesModel();
                                        userMessagesModel.setFirstName(queryModel.getFirstName());
                                        userMessagesModel.setLastName(queryModel.getLastName());
                                        userMessagesModel.setDate(queryModel.getDate());
                                        userMessagesModel.setImageName(queryModel.getOpponentImage());
                                        userMessagesModel.setFriend(false);
                                        userMessagesModel.setMessage(queryModel.getMessage());
                                        userMessagesModel.setMessageId(queryModel.getMessageId());
                                        userMessagesModel.setOpponentId(queryModel.getOpponentId());
                                        userMessagesModel.setUserId(queryModel.getUserId());
                                        userMessagesModel.setSocialAccount(queryModel.isSocialAccount());
                                        messageModels.add(userMessagesModel);
                                    }
                                    messagesLoadedCallback.onSuccess(messageModels);

                                }
                            });
                        }

                        @Override
                        public void onFailure(Object o) {
                            messagesLoadedCallback.onFailure(null);
                        }
                    });
                } else {
                    mRealm.executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            RealmResults<MessageModel> messages = realm.where(MessageModel.class).findAll();
                            HashMap<String, String> opponentIds = new HashMap<>();
                            List<String> ids = new LinkedList<>();

                            for (MessageModel queryModel : messages) {
                                String opponentId = queryModel.getOpponentId();
                                String userId = queryModel.getUserId();
                                String deleteOpponentId = queryModel.getDeleteOpponentId();
                                String deleteUserId = queryModel.getDeleteUserId();

                                if (opponentIds.containsKey(userId) & opponentIds.containsValue(opponentId) | opponentIds.containsKey(opponentId) & opponentIds.containsValue(userId) |
                                        !deleteOpponentId.equals("0") | !deleteUserId.equals("0")) {
                                    continue;
                                }

                                if (opponentIds.containsKey(userId)) {
                                    opponentIds.put(opponentId, userId);
                                } else if (opponentIds.containsKey(opponentId)) {
                                    opponentIds.put(userId, opponentId);
                                } else {
                                    opponentIds.put(userId, opponentId);
                                }
                                String finalId;
                                if (userId.equals(currentUserId)) {
                                    finalId = opponentId;
                                } else {
                                    finalId = userId;
                                }
                                ids.add(finalId);
                            }

                            for (String opponentId : ids) {
                                String userId = currentUserId;

                                MessageModel queryModel = realm.where(MessageModel.class)
                                        .equalTo("userId", userId)
                                        .equalTo("opponentId", opponentId).or()
                                        .equalTo("userId", opponentId)
                                        .equalTo("opponentId", userId).
                                                findAll().last();

                                UserMessagesModel userMessagesModel = new UserMessagesModel();
                                userMessagesModel.setFirstName(queryModel.getFirstName());
                                userMessagesModel.setLastName(queryModel.getLastName());
                                userMessagesModel.setDate(queryModel.getDate());
                                userMessagesModel.setImageName(queryModel.getUserId().equals(currentUserId) ? queryModel.getOpponentImage() : queryModel.getUserImage());
                                userMessagesModel.setFriend(false);
                                userMessagesModel.setMessage(queryModel.getMessage());
                                userMessagesModel.setMessageId(queryModel.getMessageId());
                                userMessagesModel.setOpponentId(queryModel.getOpponentId());
                                userMessagesModel.setUserId(queryModel.getUserId());
                                userMessagesModel.setSocialAccount(queryModel.isSocialAccount());
                                messageModels.add(userMessagesModel);
                            }

                            messagesLoadedCallback.onSuccess(messageModels);
                        }
                    });
                }
            }
        });
    }

    public void insertMessage(final ChatModel chatModel, @Nullable final GeneralCallback generalCallback) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mRealm.isClosed()) {
                    openRealm(new GeneralCallback() {
                        @Override
                        public void onSuccess(Object o) {
                            mRealm.executeTransactionAsync(new Realm.Transaction() {
                                @Override
                                public void execute(final Realm realm) {
                                    RealmQuery<MessageModel> query = realm.where(MessageModel.class)
                                            .equalTo("messageId", chatModel.getMessageId());
                                    if (query.count() == 0) {
                                        MessageModel messageModel = realm.createObject(MessageModel.class);

                                        messageModel.setId(chatModel.getMessageId());
                                        messageModel.setFirstName(chatModel.getFirstName());
                                        messageModel.setLastName(chatModel.getLastName());
                                        messageModel.setMessageId(chatModel.getMessageId());
                                        messageModel.setMessage(chatModel.getMessage());
                                        messageModel.setUserId(chatModel.getUserId());
                                        messageModel.setOpponentId(chatModel.getOpponentId());
                                        messageModel.setAnimated(false);
                                        messageModel.setDeliveryStatus("NONE");
                                        messageModel.setUserImage(chatModel.getOpponentImage());
                                        messageModel.setSocialAccount(chatModel.isSocialAccount());
                                        messageModel.setOpponentImage(chatModel.getOpponentImage());
                                        messageModel.setDate(chatModel.getDate());
                                        messageModel.setHasGif(chatModel.isStickerChosen());
                                        messageModel.setGifUrl(chatModel.getStickerUrl());
                                        messageModel.setDeleteUserId("0");
                                        messageModel.setDeleteOpponentId("0");
                                    }
                                    if (generalCallback != null) {
                                        generalCallback.onSuccess(true);
                                    }

                                }
                            });
                        }

                        @Override
                        public void onFailure(Object o) {
                            if (generalCallback != null) {
                                generalCallback.onFailure(false);
                            }
                        }
                    });
                } else {
                    mRealm.executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(final Realm realm) {
                            RealmQuery<MessageModel> query = realm.where(MessageModel.class)
                                    .equalTo("messageId", chatModel.getMessageId());
                            if (query.count() == 0) {
                                MessageModel messageModel = realm.createObject(MessageModel.class);

                                messageModel.setId(chatModel.getMessageId());
                                messageModel.setMessageId(chatModel.getMessageId());
                                messageModel.setMessage(chatModel.getMessage());
                                messageModel.setFirstName(chatModel.getFirstName());
                                messageModel.setLastName(chatModel.getLastName());
                                messageModel.setUserId(chatModel.getUserId());
                                messageModel.setOpponentId(chatModel.getOpponentId());
                                messageModel.setAnimated(false);
                                messageModel.setDeliveryStatus("NONE");
                                messageModel.setUserImage(chatModel.getOpponentImage());
                                messageModel.setOpponentImage(chatModel.getOpponentImage());
                                messageModel.setDate(chatModel.getDate());
                                messageModel.setHasGif(chatModel.isStickerChosen());
                                messageModel.setSocialAccount(chatModel.isSocialAccount());
                                messageModel.setGifUrl(chatModel.getStickerUrl());
                                messageModel.setDeleteUserId("0");
                                messageModel.setDeleteOpponentId("0");
                            }
                            if (generalCallback != null) {
                                generalCallback.onSuccess(false);
                            }

                        }
                    });
                }

            }
        });
    }


    public void deleteSingleMessage(final String messageId, final GeneralCallback deleteCallback) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mRealm.isClosed()) {
                    openRealm(new GeneralCallback() {
                        @Override
                        public void onSuccess(Object o) {
                            mRealm.executeTransactionAsync(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    RealmResults<MessageModel> messageModels = realm.where(MessageModel.class).equalTo("messageId", messageId)
                                            .findAll();
                                    messageModels.deleteAllFromRealm();
                                    deleteCallback.onSuccess(null);
                                }
                            });
                        }

                        @Override
                        public void onFailure(Object o) {
                            deleteCallback.onFailure(null);
                        }
                    });
                } else {
                    mRealm.executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            RealmResults<MessageModel> messageModels = realm.where(MessageModel.class).equalTo("messageId", messageId)
                                    .findAll();
                            messageModels.deleteAllFromRealm();
                            deleteCallback.onSuccess(null);
                        }
                    });
                }
            }
        });
    }

    public void deleteMessageRow(final String currentUserId, final String opponentId,
                                 final GeneralCallback deleteCallback) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mRealm.isClosed()) {
                    openRealm(new GeneralCallback() {
                        @Override
                        public void onSuccess(Object o) {
                            mRealm.executeTransactionAsync(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    RealmResults<MessageModel> messageModels = realm.where(MessageModel.class)
                                            .equalTo("userId", currentUserId)
                                            .equalTo("opponentId", opponentId).or()
                                            .equalTo("userId", opponentId)
                                            .equalTo("opponentId", currentUserId).findAll();
                                    messageModels.deleteAllFromRealm();
                                    deleteCallback.onSuccess(null);
                                }
                            });
                        }

                        @Override
                        public void onFailure(Object o) {
                            deleteCallback.onFailure(null);
                        }
                    });
                } else {
                    mRealm.executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            RealmResults<MessageModel> messageModels = realm.where(MessageModel.class)
                                    .equalTo("userId", currentUserId)
                                    .equalTo("opponentId", opponentId).or()
                                    .equalTo("userId", opponentId)
                                    .equalTo("opponentId", currentUserId).findAll();
                            messageModels.deleteAllFromRealm();
                            deleteCallback.onSuccess(null);
                        }
                    });
                }
            }
        });

    }

    public void insertMessage(final List<MessageModel> messageModels) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mRealm.isClosed()) {
                    openRealm(new GeneralCallback() {
                        @Override
                        public void onSuccess(Object o) {
                            mRealm.executeTransactionAsync(new Realm.Transaction() {
                                @Override
                                public void execute(final Realm realm) {
                                    realm.deleteAll();
                                    for (MessageModel queriedMessageModel : messageModels) {
                                        RealmQuery<MessageModel> query = realm.where(MessageModel.class)
                                                .equalTo("messageId", queriedMessageModel.getMessageId());
                                        if (query.count() == 0) {
                                            MessageModel messageModel = realm.createObject(MessageModel.class);

                                            messageModel.setId(queriedMessageModel.getId());
                                            messageModel.setMessageId(queriedMessageModel.getMessageId());
                                            messageModel.setMessage(queriedMessageModel.getMessage());
                                            messageModel.setUserId(queriedMessageModel.getUserId());
                                            messageModel.setOpponentId(queriedMessageModel.getOpponentId());
                                            messageModel.setAnimated(queriedMessageModel.isAnimated());
                                            messageModel.setDeliveryStatus(queriedMessageModel.getDeliveryStatus());
                                            messageModel.setUserImage(queriedMessageModel.getUserImage());
                                            messageModel.setOpponentImage(queriedMessageModel.getOpponentImage());
                                            messageModel.setDate(queriedMessageModel.getDate());
                                            messageModel.setHasGif(queriedMessageModel.isHasGif());
                                            messageModel.setGifUrl(queriedMessageModel.getGifUrl());
                                            messageModel.setDeleteUserId(queriedMessageModel.getDeleteUserId());
                                            messageModel.setDeleteOpponentId(queriedMessageModel.getDeleteOpponentId());
                                        }

                                    }

                                }
                            });
                        }

                        @Override
                        public void onFailure(Object o) {

                        }
                    });
                } else {
                    mRealm.executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(final Realm realm) {
                            realm.deleteAll();
                            for (MessageModel queriedMessageModel : messageModels) {

                                RealmQuery<MessageModel> query = realm.where(MessageModel.class)
                                        .equalTo("messageId", queriedMessageModel.getMessageId());
                                if (query.count() == 0) {
                                    MessageModel messageModel = realm.createObject(MessageModel.class);

                                    messageModel.setId(queriedMessageModel.getId());
                                    messageModel.setMessageId(queriedMessageModel.getMessageId());
                                    messageModel.setMessage(queriedMessageModel.getMessage());
                                    messageModel.setUserId(queriedMessageModel.getUserId());
                                    messageModel.setOpponentId(queriedMessageModel.getOpponentId());
                                    messageModel.setAnimated(queriedMessageModel.isAnimated());
                                    messageModel.setDeliveryStatus(queriedMessageModel.getDeliveryStatus());
                                    messageModel.setUserImage(queriedMessageModel.getUserImage());
                                    messageModel.setOpponentImage(queriedMessageModel.getOpponentImage());
                                    messageModel.setDate(queriedMessageModel.getDate());
                                    messageModel.setHasGif(queriedMessageModel.isHasGif());
                                    messageModel.setGifUrl(queriedMessageModel.getGifUrl());
                                    messageModel.setDeleteUserId(queriedMessageModel.getDeleteUserId());
                                    messageModel.setDeleteOpponentId(queriedMessageModel.getDeleteOpponentId());
                                }

                            }

                        }
                    });
                }

            }
        });
    }


    public void removeMessage(final String opponentId, final String userId) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mRealm.isClosed()) {
                    openRealm(new GeneralCallback() {
                        @Override
                        public void onSuccess(Object o) {
                            mRealm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    realm.where(MessageModel.class).equalTo("opponentId", opponentId).equalTo("userId", userId)
                                            .or().equalTo("opponentId", userId).equalTo("userId", opponentId
                                    ).findAll().deleteAllFromRealm();
                                }
                            });

                        }

                        @Override
                        public void onFailure(Object o) {

                        }
                    });
                } else {
                    mRealm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.where(MessageModel.class).equalTo("opponentId", opponentId).equalTo("userId", userId)
                                    .or().equalTo("opponentId", userId).equalTo("userId", opponentId
                            ).findAll().deleteAllFromRealm();
                        }
                    });

                }

            }
        });

    }

    public void removeMessage(final String messageId, @Nullable final GeneralCallback<Boolean> deleteCallback) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mRealm.isClosed()) {
                    openRealm(new GeneralCallback() {
                        @Override
                        public void onSuccess(Object o) {
                            mRealm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    realm.where(MessageModel.class).equalTo("messageId", messageId).findAll().deleteAllFromRealm();
                                    if (deleteCallback != null) {
                                        deleteCallback.onSuccess(true);
                                    }
                                }
                            });
                        }

                        @Override
                        public void onFailure(Object o) {
                            if (deleteCallback != null) {
                                deleteCallback.onFailure(false);
                            }
                        }
                    });
                } else {
                    mRealm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.where(MessageModel.class).equalTo("messageId", messageId).findAll().deleteAllFromRealm();
                            if (deleteCallback != null) {
                                deleteCallback.onSuccess(true);
                            }
                        }
                    });
                }


            }
        });

    }


    public void updateMessages(final String messageId, final String deliveryStatus, final String lastPosition, final String opponentId) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mRealm.isClosed()) {
                    openRealm(new GeneralCallback() {
                        @Override
                        public void onSuccess(Object o) {
                            mRealm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    RealmResults<MessageModel> resultQuery = realm.where(MessageModel.class).equalTo("opponentId", opponentId)
                                            .equalTo("id", lastPosition).findAllSorted("id", Sort.ASCENDING);
                                    for (MessageModel messageResult : resultQuery) {
                                        messageResult.setMessageId(messageId);
                                        messageResult.setDeliveryStatus(deliveryStatus);
                                        messageResult.setDate(Time.getCurrentTime());
                                    }
                                }
                            });
                        }

                        @Override
                        public void onFailure(Object o) {

                        }
                    });
                } else {
                    mRealm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            RealmResults<MessageModel> resultQuery = realm.where(MessageModel.class).equalTo("opponentId", opponentId)
                                    .equalTo("id", lastPosition).findAllSorted("id", Sort.ASCENDING);
                            for (MessageModel messageResult : resultQuery) {
                                messageResult.setMessageId(messageId);
                                messageResult.setDeliveryStatus(deliveryStatus);
                                messageResult.setDate(Time.getCurrentTime());
                            }
                        }
                    });
                }


            }
        });

    }

    public boolean isMessageExist(final String messageId, Realm realm) {
        RealmQuery<MessageModel> query = realm.where(MessageModel.class)
                .equalTo("messageId", messageId);
        return query.count() != 0;
    }

    public void getNotificationCount(@Nullable final QueryReadyListener queryReadyListener) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mRealm.isClosed()) {
                    openRealm(new GeneralCallback() {
                        @Override
                        public void onSuccess(Object o) {
                            mRealm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    RealmResults<NotificationModel> resultQuery = realm.where(NotificationModel.class).findAll();
                                    if (queryReadyListener != null) {
                                        queryReadyListener.onQueryReady(resultQuery.size());
                                    }
                                }
                            });
                        }

                        @Override
                        public void onFailure(Object o) {

                        }
                    });
                } else {
                    mRealm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            RealmResults<NotificationModel> resultQuery = realm.where(NotificationModel.class).findAll();
                            if (queryReadyListener != null) {
                                queryReadyListener.onQueryReady(resultQuery.size());
                            }
                        }
                    });
                }


            }
        });
    }


    public void getNotificationCount(final int opponentId, @Nullable final QueryReadyListener queryReadyListener) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mRealm.isClosed()) {
                    openRealm(new GeneralCallback() {
                        @Override
                        public void onSuccess(Object o) {
                            mRealm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    RealmResults<NotificationModel> resultQuery = realm.where(NotificationModel.class).equalTo("opponentId", opponentId).findAll();
                                    if (queryReadyListener != null) {
                                        queryReadyListener.onQueryReady(resultQuery.size());
                                    }
                                }
                            });
                        }

                        @Override
                        public void onFailure(Object o) {

                        }
                    });
                } else {
                    mRealm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            RealmResults<NotificationModel> resultQuery = realm.where(NotificationModel.class).equalTo("opponentId", opponentId).findAll();
                            if (queryReadyListener != null) {
                                queryReadyListener.onQueryReady(resultQuery.size());
                            }
                        }
                    });
                }


            }
        });
    }

    public void removeNotificationCount(final int opponentId) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mRealm.isClosed()) {
                    openRealm(new GeneralCallback() {
                        @Override
                        public void onSuccess(Object o) {
                            mRealm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    boolean deleteAllFromRealm = realm.where(NotificationModel.class).equalTo("opponentId", opponentId)
                                            .findAll().deleteAllFromRealm();
                                }
                            });
                        }

                        @Override
                        public void onFailure(Object o) {

                        }
                    });
                } else {
                    mRealm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            boolean deleteAllFromRealm = realm.where(NotificationModel.class).equalTo("opponentId", opponentId)
                                    .findAll().deleteAllFromRealm();
                        }
                    });
                }

            }
        });
    }

    public void putNotificationCount(final int opponentId, @Nullable final QueryReadyListener queryReadyListener) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mRealm.isClosed()) {
                    openRealm(new GeneralCallback() {
                        @Override
                        public void onSuccess(Object o) {
                            mRealm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    NotificationModel notificationModel = realm.createObject(NotificationModel.class);
                                    notificationModel.setOpponentId(opponentId);
                                    if (queryReadyListener != null) {
                                        queryReadyListener.onQueryReady(null);
                                    }
                                }
                            });
                        }

                        @Override
                        public void onFailure(Object o) {

                        }
                    });
                } else {
                    mRealm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            NotificationModel notificationModel = realm.createObject(NotificationModel.class);
                            notificationModel.setOpponentId(opponentId);
                            if (queryReadyListener != null) {
                                queryReadyListener.onQueryReady(null);
                            }
                        }
                    });
                }

            }
        });
    }

    public void getMessages(final String opponentId, final String userId, final GeneralCallback<List<MessageModel>> listGeneralCallback) {
        mModelArray.clear();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mRealm.isClosed()) {
                    openRealm(new GeneralCallback() {
                        @Override
                        public void onSuccess(Object o) {
                            mRealm.executeTransactionAsync(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    RealmResults<MessageModel> realmResults = realm.where(MessageModel.class).equalTo("opponentId", opponentId).equalTo("userId", userId)
                                            .or().equalTo("opponentId", userId).equalTo("userId", opponentId
                                            ).findAllSorted("messageId", Sort.ASCENDING);
                                    for (MessageModel messageModel : realmResults) {
                                        mModelArray.add(messageModel);
                                    }

                                    listGeneralCallback.onSuccess(mModelArray);
                                }
                            });
                        }

                        @Override
                        public void onFailure(Object o) {

                        }
                    });
                } else {
                    mRealm.executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            RealmResults<MessageModel> realmResults = realm.where(MessageModel.class).equalTo("opponentId", opponentId).equalTo("userId", userId)
                                    .or().equalTo("opponentId", userId).equalTo("userId", opponentId
                                    ).findAllSorted("messageId", Sort.ASCENDING);
                            for (MessageModel messageModel : realmResults) {
                                mModelArray.add(messageModel);
                            }
                            listGeneralCallback.onSuccess(mModelArray);
                        }
                    });
                }

            }
        });
    }

    public void restore(final Context context, final File fileToCopy, final GeneralCallback<Object> fileCopyCallback) {
        if (!PermissionsChecker.isStoragePermissionGranted(context)) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            return;
        }
        clearDatabase(new GeneralCallback() {
            @Override
            public void onSuccess(Object o) {
                File file = new File(context.getFilesDir().getAbsolutePath() + "/" + REALM_DB_NAME);
                if (file.exists()) {
                    file.delete();
                }
                try {
                    org.apache.commons.io.FileUtils.copyFileToDirectory(fileToCopy, new File(context.getFilesDir().getAbsolutePath()));
                    fileCopyCallback.onSuccess(null);
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                    fileCopyCallback.onFailure(null);
                }
            }

            @Override
            public void onFailure(Object o) {

            }
        });
    }


    public void backup(final Context context, final GeneralCallback<File> fileReadyCallback) {
        if (!PermissionsChecker.isStoragePermissionGranted(context)) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            return;
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mRealm.isClosed()) {
                    openRealm(new GeneralCallback() {
                        @Override
                        public void onSuccess(Object o) {
                            File exportRealmFile = null;

                            File exportRealmPATH = Environment.getExternalStorageDirectory();


                            try {
                                // create a backup file
                                exportRealmFile = new File(exportRealmPATH, REALM_DB_NAME);

                                // if backup file already exists, delete it
                                exportRealmFile.delete();

                                // copy current realm to backup file
                                mRealm.writeCopyTo(exportRealmFile);
                                fileReadyCallback.onSuccess(exportRealmFile);
                            } catch (IOException e) {
                                e.printStackTrace();
                                fileReadyCallback.onFailure(null);
                            }

                        }

                        @Override
                        public void onFailure(Object o) {

                        }
                    });
                } else {
                    File exportRealmFile = null;

                    File exportRealmPATH = Environment.getExternalStorageDirectory();


                    try {
                        // create a backup file
                        exportRealmFile = new File(exportRealmPATH, REALM_DB_NAME);

                        // if backup file already exists, delete it
                        exportRealmFile.delete();

                        // copy current realm to backup file
                        mRealm.writeCopyTo(exportRealmFile);
                        fileReadyCallback.onSuccess(exportRealmFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                        fileReadyCallback.onFailure(null);
                    }

                }
            }
        });


    }


    public void getMessagesAsChatModel(final String opponentId, final String userId, final GeneralCallback<List<ChatModel>> listGeneralCallback) {
        final List<ChatModel> chatModels = new LinkedList<>();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mRealm.isClosed()) {
                    openRealm(new GeneralCallback() {
                        @Override
                        public void onSuccess(Object o) {
                            mRealm.executeTransactionAsync(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    RealmResults<MessageModel> realmResults = realm.where(MessageModel.class).equalTo("opponentId", opponentId).equalTo("userId", userId)
                                            .or().equalTo("opponentId", userId).equalTo("userId", opponentId
                                            ).findAll();
                                    for (MessageModel messageModel : realmResults) {
                                        ChatModel chatModel = new ChatModel();
                                        chatModel.setUserId(messageModel.getUserId());
                                        chatModel.setDate(messageModel.getDate());
                                        chatModel.setOpponentImage(messageModel.getOpponentImage());
                                        chatModel.setMessage(StringEscapeUtils.unescapeJava(messageModel.getMessage()));
                                        chatModel.setMessageId(messageModel.getMessageId());
                                        chatModel.setOpponentId(messageModel.getOpponentId());
                                        chatModel.setStickerChosen(messageModel.isHasGif());
                                        chatModel.setStickerUrl(messageModel.getGifUrl());

                                        chatModels.add(chatModel);
                                    }
                                    listGeneralCallback.onSuccess(chatModels);
                                }
                            });
                        }

                        @Override
                        public void onFailure(Object o) {

                        }
                    });
                } else {
                    mRealm.executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            RealmResults<MessageModel> realmResults = realm.where(MessageModel.class).equalTo("opponentId", opponentId).equalTo("userId", userId)
                                    .or().equalTo("opponentId", userId).equalTo("userId", opponentId
                                    ).findAll();

                            for (MessageModel messageModel : realmResults) {
                                ChatModel chatModel = new ChatModel();
                                chatModel.setUserId(messageModel.getUserId());
                                chatModel.setDate(messageModel.getDate());
                                chatModel.setOpponentImage(messageModel.getOpponentImage());
                                chatModel.setMessageId(messageModel.getMessageId());
                                chatModel.setMessage(StringEscapeUtils.unescapeJava(messageModel.getMessage()));
                                chatModel.setOpponentId(messageModel.getOpponentId());
                                chatModel.setStickerChosen(messageModel.isHasGif());
                                chatModel.setStickerUrl(messageModel.getGifUrl());
                                chatModels.add(chatModel);
                            }
                            listGeneralCallback.onSuccess(chatModels);
                        }
                    });
                }

            }
        });
    }

    public void deleteMessages() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mRealm.isClosed()) {
                    openRealm(new GeneralCallback() {
                        @Override
                        public void onSuccess(Object o) {
                            mRealm.executeTransactionAsync(new Realm.Transaction() {
                                @Override
                                public void execute(final Realm realm) {
                                    realm.deleteAll();
                                }
                            });
                        }

                        @Override
                        public void onFailure(Object o) {

                        }
                    });
                } else {
                    mRealm.executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(final Realm realm) {
                            realm.deleteAll();

                        }
                    });
                }

            }
        });
    }

    public interface QueryReadyListener<T> {
        void onQueryReady(T result);
    }

}
