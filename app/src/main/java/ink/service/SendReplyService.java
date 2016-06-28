package ink.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import ink.callbacks.QueCallback;
import ink.utils.Constants;
import ink.utils.QueHelper;
import ink.utils.RealmHelper;
import ink.utils.SharedHelper;

/**
 * Created by USER on 2016-06-27.
 */
public class SendReplyService extends Service {
    private SharedHelper sharedHelper;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sharedHelper = new SharedHelper(this);
        Bundle extras = intent.getExtras();
        if (extras != null) {
            attemptToQue(extras.getString("message"),
                    extras.getString("opponentId"), extras.getString("currentUserId"),
                    extras.getString("userImage"), extras.getString("opponentImage"));
        }
        return super.onStartCommand(intent, flags, startId);
    }


    private void attemptToQue(String message, final String mOpponentId,
                              final String mCurrentUserId,
                              String userImage, String mOpponentImage) {
        int uniqueId = sharedHelper.getUniqueId();
        int finalId = uniqueId++;
        sharedHelper.putUniqueId(finalId);
        RealmHelper.getInstance().insertMessage(mCurrentUserId, mOpponentId,
                message, "0", "",
                String.valueOf(finalId), Constants.STATUS_NOT_DELIVERED, userImage, mOpponentImage);

        QueHelper queHelper = new QueHelper();
        queHelper.attachToQue(mCurrentUserId, mOpponentId, message, finalId,
                new QueCallback() {
                    @Override
                    public void onMessageSent(String response, int sentItemLocation) {
                        System.gc();
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean success = jsonObject.optBoolean("success");
                            if (success) {
                                String messageId = jsonObject.optString("message_id");
                                RealmHelper.getInstance().updateMessages(messageId,
                                        Constants.STATUS_DELIVERED, String.valueOf(sentItemLocation),
                                        mOpponentId);

                            } else {
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onMessageSentFail(QueHelper failedHelperInstance, String failedMessage, int failedItemLocation) {
                        failedHelperInstance.attachToQue(mCurrentUserId, mOpponentId, failedMessage, failedItemLocation, this);
                    }
                });
    }


}
