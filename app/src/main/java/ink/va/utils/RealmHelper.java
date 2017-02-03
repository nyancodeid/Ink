package ink.va.utils;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import ink.va.callbacks.GeneralCallback;
import ink.va.models.MessageModel;
import ink.va.models.NotificationModel;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

/**
 * Created by USER on 2016-06-26.
 */
public class RealmHelper {
    private static RealmHelper ourInstance = new RealmHelper();
    private Realm mRealm;
    private List<MessageModel> mModelArray = new ArrayList<>();
    private RealmConfiguration mRealmConfiguration;
    private boolean exists = false;
    private Handler handler;
    private boolean clearDBSuccess;
    private boolean isMessageExist;
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
                        .name("messages.realm")
                        .deleteRealmIfMigrationNeeded()
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

    public boolean clearDatabase(final Context context) {

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
                              final boolean hasGif, final String gifUrl, final boolean animated) {
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
                                            messageModel.setHasGif(queriedMessageModel.hasGif());
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
                                    messageModel.setHasGif(queriedMessageModel.hasGif());
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

    public void removeMessage(final String messageId) {
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
                                }
                            });
                        }

                        @Override
                        public void onFailure(Object o) {

                        }
                    });
                } else {

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
                                            .equalTo("id", lastPosition).findAllSorted("date");
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
                                    .equalTo("id", lastPosition).findAllSorted("date");
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

    public void getMessagesCount(@Nullable final QueryReadyListener queryReadyListener) {
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

    public void removeMessageCount(final int opponentId) {
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

    public void putNotificationCount(final int opponentId) {
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
                                }
                            });
                        }

                        @Override
                        public void onFailure(Object o) {

                        }
                    });
                } else {

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

    public interface QueryReadyListener<T> {
        void onQueryReady(T result);
    }

}
