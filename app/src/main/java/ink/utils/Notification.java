package ink.utils;

/**
 * Created by USER on 2016-06-26.
 */
public class Notification {
    private static Notification notificationInstance = new Notification();
    private boolean sendingRemote = true;
    private boolean isCallRemote = true;
    public String activeOpponentId;

    public static Notification get() {
        return notificationInstance;
    }

    private Notification() {
    }

    public boolean isSendingRemote() {
        return sendingRemote;
    }

    public void setSendingRemote(boolean sendingRemote) {
        this.sendingRemote = sendingRemote;
    }

    public String getActiveOpponentId() {
        return activeOpponentId;
    }

    public void setActiveOpponentId(String activeOpponentId) {
        this.activeOpponentId = activeOpponentId;
    }
}
