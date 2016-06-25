package ink.models;

/**
 * Created by USER on 2016-06-24.
 */
public class ChatModel {
    private String messageId;
    private String userId;
    private String opponentId;
    private String message;
    private boolean isClickable;


    public ChatModel(String messageId, String userId, String opponentId, String message, boolean isClickable) {
        this.isClickable = isClickable;
        this.messageId = messageId;
        this.userId = userId;
        this.opponentId = opponentId;
        this.message = message;
    }


    public boolean isClickable() {
        return isClickable;
    }

    public void setClickable(boolean clickable) {
        isClickable = clickable;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOpponentId() {
        return opponentId;
    }

    public void setOpponentId(String opponentId) {
        this.opponentId = opponentId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
