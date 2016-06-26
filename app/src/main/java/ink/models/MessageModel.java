package ink.models;

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
}
