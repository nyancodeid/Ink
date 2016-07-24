package ink.utils;

/**
 * Created by USER on 2016-06-26.
 */
public class Notification {
    private static Notification ourInstance = new Notification();
    private boolean sendingRemote = true;
    private boolean isCallRemote = true;

    public static Notification getInstance() {
        return ourInstance;
    }

    private Notification() {
    }

    public boolean isSendingRemote() {
        return sendingRemote;
    }

    public void setSendingRemote(boolean sendingRemote) {
        this.sendingRemote = sendingRemote;
    }

    public boolean isCallRemote() {
        return isCallRemote;
    }

    public void setCallRemote(boolean isCallRemote) {
        this.isCallRemote = isCallRemote;
    }
}
