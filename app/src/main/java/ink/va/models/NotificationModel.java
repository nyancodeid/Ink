package ink.va.models;

import io.realm.RealmObject;

/**
 * Created by USER on 2016-12-21.
 */

public class NotificationModel extends RealmObject {
    int opponentId;

    public int getOpponentId() {
        return opponentId;
    }

    public void setOpponentId(int opponentId) {
        this.opponentId = opponentId;
    }

    public NotificationModel() {

    }
}
