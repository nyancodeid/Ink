package ink.va.models;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by PC-Comp on 4/11/2017.
 */

public class FriendModel {
    @SerializedName("friend_id")
    @Setter
    @Getter
    private String friendId;

    @SerializedName("first_name")
    @Setter
    @Getter
    private String firstName;


    @SerializedName("last_name")
    @Setter
    @Getter
    private String last_name;

    @SerializedName("badge_name")
    @Setter
    @Getter
    private String badgeName;

    @SerializedName("gender")
    @Setter
    @Getter
    private String gender;

    @SerializedName("phone_number")
    @Setter
    @Getter
    private String phoneNumber;

    @SerializedName("facebook_profile")
    @Setter
    @Getter
    private String facebookProfile;

    @SerializedName("image_link")
    @Setter
    @Getter
    private String imageLink;

    @SerializedName("isSocialAccount")
    @Setter
    @Getter
    private String isSocialAccount;
}
