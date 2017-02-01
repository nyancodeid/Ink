package ink.va.models;

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
    private boolean hasSticker;
    private String gifUrl;
    private boolean isAttachment;
    private boolean isAnimated;


    public ChatModel(boolean isAttachment, boolean hasSticker, String gifUrl, String messageId, String userId,
                     String opponentId, String message,
                     boolean isClickable, String deliveryStatus,
                     String userImage, String opponentImage, String date, boolean isAnimated) {
        this.userImage = userImage;
        this.isAnimated = isAnimated;
        this.date = date;
        this.hasSticker = hasSticker;
        this.gifUrl = gifUrl;
        this.opponentImage = opponentImage;
        this.isClickable = isClickable;
        this.isAttachment = isAttachment;
        this.deliveryStatus = deliveryStatus;
        this.messageId = messageId;
        this.userId = userId;
        this.opponentId = opponentId;
        this.message = message;
    }


    public boolean isAttachment() {
        return isAttachment;
    }

    public void setAttachment(boolean attachment) {
        isAttachment = attachment;
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

    public boolean hasSticker() {
        return hasSticker;
    }

    public void setHasSticker(boolean hasSticker) {
        this.hasSticker = hasSticker;
    }

    public String getStickerUrl() {
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

    public boolean isAnimated() {
        return isAnimated;
    }

    public void setAnimated(boolean animated) {
        isAnimated = animated;
    }

    public void setDate(String date) {
        this.date = date;
    }


    @Override
    public int hashCode() {
        return messageId.hashCode();
    }
}
