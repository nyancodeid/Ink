package ink.va.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by PC-Comp on 1/11/2017.
 */

public class VipGlobalChatModel {
    @SerializedName("senderId")
    private String senderId;
    @SerializedName("messageId")
    private int messageId;
    @SerializedName("message")
    private String message;
    @SerializedName("user")
    private UserModel user;


    public String getSenderId() {
        return senderId;
    }

    public int getMessageId() {
        return messageId;
    }

    public String getMessage() {
        return message;
    }

    public UserModel getUser() {
        return user;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setUser(UserModel user) {
        this.user = user;
    }
}
