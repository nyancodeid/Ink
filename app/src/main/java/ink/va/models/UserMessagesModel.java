package ink.va.models;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by USER on 2016-07-01.
 */
public class UserMessagesModel {

    @SerializedName("user_id")
    @Setter
    @Getter
    private String userId;
    @SerializedName("opponent_id")
    @Setter
    @Getter
    private String opponentId;
    @SerializedName("message_id")
    @Setter
    @Getter
    private String messageId;
    @SerializedName("message")
    @Setter
    @Getter
    private String message;
    @SerializedName("firstName")
    @Setter
    @Getter
    private String firstName;
    @SerializedName("lastName")
    @Setter
    @Getter
    private String lastName;
    @SerializedName("imageName")
    @Setter
    @Getter
    private String imageName;
    @SerializedName("isSocialAccount")
    @Setter
    @Getter
    private boolean isSocialAccount;
    @Setter
    @Getter
    @SerializedName("isFriend")
    private boolean isFriend;
    @SerializedName("date")
    @Setter
    @Getter
    private String date;


}
