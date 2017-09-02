package kashmirr.social.interfaces;

import org.json.JSONObject;

/**
 * Created by USER on 2017-02-15.
 */

public interface SocketListener {
    void onSocketConnected();

    void onSocketDisconnected();

    void onSocketConnectionError();

    void onUserStoppedTyping(JSONObject jsonObject);

    void onUserTyping(JSONObject jsonObject);

    void onNewMessageReceived(JSONObject messageJson);

    void onMessageSent(JSONObject chatJson);

    void onOnlineStatusReceived(boolean isOnline, String friendlyMessage);
}
