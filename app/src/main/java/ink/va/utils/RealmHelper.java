package ink.va.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import ink.va.callbacks.GeneralCallback;
import ink.va.models.CountBadgeModel;
import ink.va.models.MessageModel;
import ink.va.models.NotificationModel;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

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

    public static RealmHelper getInstance() {
        return ourInstance;
    }

    private RealmHelper() {
        handler = new Handler(Looper.getMainLooper());
    }

    public void initRealm(final Context context) {

        handler.post(new Runnable() {
            @Override
            public void run() {
                mRealmConfiguration = new RealmConfiguration.Builder(context)
                        .name("messages.realm")
                        .deleteRealmIfMigrationNeeded()
                        .build();
                mRealm = Realm.getInstance(mRealmConfiguration);
            }
        });


    }

    public boolean clearDatabase(final Context context) {

        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mRealm != null) {
                    mRealm.close();
                    Realm.deleteRealm(mRealmConfiguration);
                    initRealm(context);
                    clearDBSuccess = true;
                } else {
                    clearDBSuccess = false;
                }
            }
        });

        return clearDBSuccess;
    }

    public void insertMessageCont(final String userId) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                mRealm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        final CountBadgeModel countBadgeModel = realm.createObject(CountBadgeModel.class);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                mRealm.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        CountBadgeModel countBadgeFound = realm.where(CountBadgeModel.class).equalTo("userId", userId).findFirst();
                                        String count;
                                        if (countBadgeFound != null && !countBadgeFound.getCount().equals("0")) {
                                            count = countBadgeFound.getCount();
                                        } else {
                                            count = "0";
                                        }
                                        int actualCount = Integer.valueOf(count) + 1;

                                        countBadgeModel.setUserId(userId);
                                        countBadgeModel.setCount(String.valueOf(actualCount));
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });

    }

    public boolean checkIfExists(final String id) {
        isMessageExist = false;
        handler.post(new Runnable() {
            @Override
            public void run() {
                mRealm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        RealmQuery<MessageModel> query = realm.where(MessageModel.class)
                                .equalTo("messageId", id);
                        if (query.count() == 0) {
                            isMessageExist = false;
                        } else {
                            isMessageExist = true;
                        }
                    }
                });
            }
        });


        return isMessageExist;
    }

    public void insertMessage(final String userId, final String opponentId, final String message,
                              final String messageId, final String date,
                              final String id, final String deliveryStatus,
                              final String userImage,
                              final String opponentImage, final String deleteOpponentId,
                              final String deleteUserId,
                              final boolean hasGif, final String gifUrl, final boolean animated) {

        if (!isMessageExist(messageId)) {
            handler.post(new Runnable() {
                @Override
                public void run() {

                }
            });
            mRealm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
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
            });
        }


    }

    public void removeMessage(final String opponentId, final String userId) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                mRealm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.where(MessageModel.class).equalTo("opponentId", opponentId).equalTo("userId", userId)
                                .or().equalTo("opponentId", userId).equalTo("userId", opponentId
                        ).findAll().deleteAllFromRealm();
                    }
                });
            }
        });

    }

    public void removeMessage(final String messageId) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                mRealm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.where(MessageModel.class).equalTo("messageId", messageId).findAll().deleteAllFromRealm();
                    }
                });
            }
        });

    }


    public void updateMessages(final String messageId, final String deliveryStatus, final String lastPosition, final String opponentId) {
        handler.post(new Runnable() {
            @Override
            public void run() {
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
        });

    }

    public boolean isMessageExist(final String messageId) {
        exists = false;
        handler.post(new Runnable() {
            @Override
            public void run() {
                mRealm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        RealmResults<MessageModel> resultQuery = realm.where(MessageModel.class).equalTo("messageId", messageId).findAll();
                        if (resultQuery.size() > 0) {
                            exists = true;
                        }
                    }
                });
            }
        });

        return exists;
    }

    public void getMessagesCount(@Nullable final QueryReadyListener queryReadyListener) {
        handler.post(new Runnable() {
            @Override
            public void run() {
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
        });
    }

    public void removeMessageCount(final int opponentId) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                mRealm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        boolean deleteAllFromRealm = realm.where(NotificationModel.class).equalTo("opponentId", opponentId)
                                .findAll().deleteAllFromRealm();
                    }
                });
            }
        });
    }

    public void putNotificationCount(final int opponentId) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                mRealm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        NotificationModel notificationModel = realm.createObject(NotificationModel.class);
                        notificationModel.setOpponentId(opponentId);
                    }
                });
            }
        });
    }

    public void getMessages(final String opponentId, final String userId, final GeneralCallback<List<MessageModel>> listGeneralCallback) {
        mModelArray.clear();
        handler.post(new Runnable() {
            @Override
            public void run() {
                mRealm.executeTransaction(new Realm.Transaction() {
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
        });

    }

    public interface QueryReadyListener<T> {
        void onQueryReady(T result);
    }

}
