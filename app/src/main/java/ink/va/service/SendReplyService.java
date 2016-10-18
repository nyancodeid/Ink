package ink.va.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

import ink.va.utils.Constants;
import ink.va.utils.QueHelper;
import ink.va.utils.RealmHelper;
import ink.va.utils.SharedHelper;

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
                    extras.getString("userImage"), extras.getString("opponentImage"),
                    extras.getString("delete_user_id"), extras.getString("delete_opponent_id"));
        }
        return super.onStartCommand(intent, flags, startId);
    }


    private void attemptToQue(String message, final String mOpponentId,
                              final String mCurrentUserId,
                              String userImage, String mOpponentImage, String deleteUserId,
                              String deleteOpponentId) {
        int uniqueId = sharedHelper.getUniqueId();
        int finalId = uniqueId++;
        sharedHelper.putUniqueId(finalId);
        RealmHelper.getInstance().insertMessage(mCurrentUserId, mOpponentId,
                message, "0", "",
                String.valueOf(finalId), Constants.STATUS_NOT_DELIVERED, userImage, mOpponentImage, deleteOpponentId,
                deleteUserId, false, "", false);

        QueHelper queHelper = new QueHelper();
        queHelper.attachToQue(mOpponentId, message, finalId, false, ""
                , getApplicationContext(), false);
    }
}
