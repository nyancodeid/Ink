package ink.utils;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import ink.models.MessageModel;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

/**
 * Created by USER on 2016-06-26.
 */
public class RealmHelper {
    private static RealmHelper ourInstance = new RealmHelper();
    private Realm mRealm;
    private List<MessageModel> mModelArray = new ArrayList<>();
    private RealmConfiguration mRealmConfiguration;

    public static RealmHelper getInstance() {
        return ourInstance;
    }

    private RealmHelper() {
    }

    public void initRealm(Context context) {
        mRealmConfiguration = new RealmConfiguration.Builder(context)
                .name("messages.realm")
                .deleteRealmIfMigrationNeeded()
                .build();
        mRealm = Realm.getInstance(mRealmConfiguration);
    }

    public boolean clearDatabase(Context context) {
        if (mRealm != null) {
            mRealm.close();
            Realm.deleteRealm(mRealmConfiguration);
            initRealm(context);
            return true;
        } else {
            return false;
        }
    }

    public void insertMessage(final String userId, final String opponentId, final String message,
                              final String messageId, final String date,
                              final String id, final String deliveryStatus,
                              final String userImage,
                              final String opponentImage, final String deleteOpponentId,
                              final String deleteUserId,
                              final boolean hasGif, final String gifUrl) {

        mRealm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                MessageModel messageModel = realm.createObject(MessageModel.class);

                messageModel.setId(id);
                messageModel.setMessageId(messageId);
                messageModel.setMessage(message);
                messageModel.setUserId(userId);
                messageModel.setOpponentId(opponentId);
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

    public void removeMessage(final String opponentId, final String userId) {
        mRealm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(MessageModel.class).equalTo("opponentId", opponentId).equalTo("userId", userId)
                        .or().equalTo("opponentId", userId).equalTo("userId", opponentId
                ).findAll().deleteAllFromRealm();
            }
        });
    }

    public void updateMessages(final String messageId, final String deliveryStatus, final String lastPosition, final String opponentId) {
        mRealm.executeTransactionAsync(new Realm.Transaction() {
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

    public List<MessageModel> getMessages(final String opponentId, final String userId) {
        mModelArray.clear();
        mRealm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<MessageModel> realmResults = realm.where(MessageModel.class).equalTo("opponentId", opponentId).equalTo("userId", userId)
                        .or().equalTo("opponentId", userId).equalTo("userId", opponentId
                        ).findAll();
                for (MessageModel messageModel : realmResults) {
                    mModelArray.add(messageModel);
                }
            }
        });
        return mModelArray;
    }

}
