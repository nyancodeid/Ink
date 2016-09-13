package ink.va.callbacks;

import ink.va.utils.QueHelper;

/**
 * Created by USER on 2016-06-26.
 */
public abstract class QueCallback {
    public abstract void onMessageSent(String response, int sentItemLocation);

    public abstract void onMessageSentFail(QueHelper failedHelperInstance, String failedMessage, int failedItemLocation);
}
