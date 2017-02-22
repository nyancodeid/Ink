package ink.va.models;

import io.realm.RealmObject;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by USER on 2016-12-21.
 */

public class NotificationModel extends RealmObject {
    @Setter
    @Getter
    int opponentId;
    @Setter
    @Getter
    String message;


    public NotificationModel() {

    }
}
