package ink.va.models;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by PC-Comp on 3/23/2017.
 */

public class UserNotificationModel {

    @SerializedName("id")
    @Setter
    @Getter
    private String id;

    @SerializedName("userId")
    @Setter
    @Getter
    private String userId;

    @SerializedName("notificationTitle")
    @Setter
    @Getter
    private String notificationTitle;

    @SerializedName("notificationText")
    @Setter
    @Getter
    private String notificationText;

    @SerializedName("isSystemMessage")
    @Setter
    @Getter
    private boolean isSystemMessage;

    @SerializedName("methodToRun")
    @Setter
    @Getter
    private String methodToRun;

}
