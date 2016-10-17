package ink.va.models;

import io.realm.RealmObject;

/**
 * Created by USER on 2016-06-26.
 */
public class MessageModel extends RealmObject {
    String id;
    String messageId;
    String userId;
    String opponentId;
    String message;
    String date;
    String deliveryStatus;
    String userImage;
    String opponentImage;
    String deleteUserId;
    String deleteOpponentId;
    boolean hasGif;
    boolean isAnimated;
    String gifUrl;

    public MessageModel() {

    }


    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public String getOpponentImage() {
        return opponentImage;
    }

    public void setOpponentImage(String opponentImage) {
        this.opponentImage = opponentImage;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public boolean isAnimated() {
        return isAnimated;
    }

    public void setAnimated(boolean animated) {
        isAnimated = animated;
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

    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(String deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    public String getDeleteUserId() {
        return deleteUserId;
    }

    public void setDeleteUserId(String deleteUserId) {
        this.deleteUserId = deleteUserId;
    }

    public String getDeleteOpponentId() {
        return deleteOpponentId;
    }

    public void setDeleteOpponentId(String deleteOpponentId) {
        this.deleteOpponentId = deleteOpponentId;
    }

    public boolean hasGif() {
        return hasGif;
    }

    public String getGifUrl() {
        return gifUrl;
    }

    public void setHasGif(boolean hasGif) {
        this.hasGif = hasGif;
    }

    public void setGifUrl(String gifUrl) {
        this.gifUrl = gifUrl;
    }
}
