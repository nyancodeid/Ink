package ink.va.utils;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by USER on 2016-06-26.
 */
public class Notification {
    private static Notification notificationInstance = new Notification();
    @Setter
    @Getter
    private boolean sendingRemote = true;

    @Setter
    @Getter
    private boolean isCallRemote = true;

    @Setter
    @Getter
    public String activeOpponentId;

    @Setter
    @Getter
    private boolean checkLock;

    @Setter
    @Getter
    private boolean isAppAlive;

    public static Notification get() {
        return notificationInstance;
    }

    private Notification() {
    }


}
