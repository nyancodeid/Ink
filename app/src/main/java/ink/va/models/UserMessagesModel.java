package ink.va.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by USER on 2016-07-01.
 */
public class UserMessagesModel {

    @SerializedName("user_id")
    private String userId;
    @SerializedName("opponent_id")
    private String opponentId;
    @SerializedName("message_id")
    private String messageId;
    @SerializedName("message")
    private String message;
    @SerializedName("firstName")
    private String firstName;
    @SerializedName("lastName")
    private String lastName;
    @SerializedName("imageName")
    private String imageName;
    @SerializedName("isSocialAccount")
    private boolean isSocialAccount;
    @SerializedName("isFriend")
    private boolean isFriend;
    @SerializedName("date")
    private String date;


    public String getUserId() {
        return userId;
    }

    public String getOpponentId() {
        return opponentId;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getMessage() {
        return message;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getImageName() {
        return imageName;
    }


    public boolean isSocialAccount() {
        return isSocialAccount;
    }

    public boolean isFriend() {
        return isFriend;
    }
}
