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
}
