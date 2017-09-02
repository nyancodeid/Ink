package kashmirr.social.models;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by PC-Comp on 3/13/2017.
 */

public class MafiaMessageModel {
    @SerializedName("id")
    @Setter
    @Getter
    private String id;

    @SerializedName("room_id")
    @Setter
    @Getter
    private int roomId;
    @SerializedName("message")
    @Setter
    @Getter
    private String message;
    @SerializedName("sender_id")
    @Setter
    @Getter
    private String senderId;
    @SerializedName("user")
    @Setter
    @Getter
    private UserModel user;
    @Setter
    @Getter
    @SerializedName("isSystemMessage")
    private boolean isSystemMessage;

    @SerializedName("isMafiaMessage")
    @Setter
    @Getter
    private boolean isMafiaMessage;

    @SerializedName("showMessage")
    @Setter
    @Getter
    private boolean showMessage;
}
