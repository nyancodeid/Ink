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
    private String deliveryStatus;
    private String userImage;
    private String opponentImage;


    public ChatModel(String messageId, String userId,
                     String opponentId, String message,
                     boolean isClickable, String deliveryStatus,
                     String userImage, String opponentImage) {
        this.userImage = userImage;
        this.opponentImage = opponentImage;
        this.isClickable = isClickable;
        this.deliveryStatus = deliveryStatus;
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

    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(String deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
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
