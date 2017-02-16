package ink.va.models;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by USER on 2016-06-24.
 */

public class ChatModel {
    @SerializedName("messageId")
    @Setter
    @Getter
    private String messageId;
    @SerializedName("userId")
    @Setter
    @Getter
    private String userId;
    @SerializedName("firstName")
    @Setter
    @Getter
    private String firstName;
    @SerializedName("lastName")
    @Setter
    @Getter
    private String lastName;
    @SerializedName("opponentId")
    @Setter
    @Getter
    private String opponentId;
    @SerializedName("message")
    @Setter
    @Getter
    private String message;
    @SerializedName("date")
    @Setter
    @Getter
    private String date;
    @SerializedName("stickerChosen")
    @Setter
    @Getter
    private boolean stickerChosen;
    @SerializedName("stickerUrl")
    @Setter
    @Getter
    private String stickerUrl;
    @SerializedName("isSocialAccount")
    @Setter
    @Getter
    private boolean isSocialAccount;
    @SerializedName("opponentImage")
    @Setter
    @Getter
    private String opponentImage;


}
