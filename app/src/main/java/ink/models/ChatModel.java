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
    private String date;
    private boolean hasGif;
    private String gifUrl;


    public ChatModel(boolean hasGif, String gifUrl, String messageId, String userId,
                     String opponentId, String message,
                     boolean isClickable, String deliveryStatus,
                     String userImage, String opponentImage, String date) {
        this.userImage = userImage;
        this.date = date;
        this.hasGif = hasGif;
        this.gifUrl = gifUrl;
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

    public boolean hasGif() {
        return hasGif;
    }

    public void setHasGif(boolean hasGif) {
        this.hasGif = hasGif;
    }

    public String getGifUrl() {
        return gifUrl;
    }

    public void setGifUrl(String gifUrl) {
        this.gifUrl = gifUrl;
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
