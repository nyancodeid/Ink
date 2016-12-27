package ink.va.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by PC-Comp on 12/27/2016.
 */

public class UserModel {

    @SerializedName("user_id")
    private String userId;
    @SerializedName("isSocialAccount")
    private boolean isSocialAccount;
    @SerializedName("login")
    private String login;
    @SerializedName("first_name")
    private String firstName;
    @SerializedName("last_name")
    private String lastName;
    @SerializedName("gender")
    private String gender;
    @SerializedName("phone_number")
    private String phone_number;
    @SerializedName("facebook_profile")
    private String facebookProfile;
    @SerializedName("image_link")
    private String imageUrl;
    @SerializedName("skype")
    private String skype;
    @SerializedName("address")
    private String address;
    @SerializedName("relationship")
    private String relationship;
    @SerializedName("status")
    private String status;
    @SerializedName("facebook_name")
    private String facebookName;
    @SerializedName("last_visited")
    private String lastVisited;
    @SerializedName("banned")
    private boolean banned;
    @SerializedName("isHidden")
    private boolean isHidden;
    @SerializedName("isIncognito")
    private boolean isIncognito;
    @SerializedName("isIncognitoBought")
    private boolean isIncognitoBought;
    @SerializedName("isHiddenBought")
    private boolean isHiddenBought;
    @SerializedName("isVip")
    private boolean isVip;
    @SerializedName("isFirstVipLogin")
    private boolean isFirstVipLogin;
    @SerializedName("vipMembershipType")
    private boolean vipMembershipType;
}
