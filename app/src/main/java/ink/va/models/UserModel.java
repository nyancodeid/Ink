package ink.va.models;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by PC-Comp on 12/27/2016.
 */

@Parcel
public class UserModel {

    @SerializedName("coins")
    @Setter
    @Getter
    private int coins;
    @SerializedName("user_id")
    @Setter
    @Getter
    private String userId;
    @Setter
    @Getter
    @SerializedName("isSocialAccount")
    private boolean isSocialAccount;
    @Setter
    @Getter
    @SerializedName("login")
    private String login;
    @Setter
    @Getter
    @SerializedName("first_name")
    private String firstName;
    @Setter
    @Getter
    @SerializedName("last_name")
    private String lastName;
    @Setter
    @Getter
    @SerializedName("gender")
    private String gender;
    @Setter
    @Getter
    @SerializedName("phoneNumber")
    private String phoneNumber;
    @Setter
    @Getter
    @SerializedName("facebook_profile")
    private String facebookProfile;
    @Setter
    @Getter
    @SerializedName("image_link")
    private String imageUrl;
    @Setter
    @Getter
    @SerializedName("skype")
    private String skype;
    @Setter
    @Getter
    @SerializedName("address")
    private String address;
    @Setter
    @Getter
    @SerializedName("relationship")
    private String relationship;
    @SerializedName("status")
    @Setter
    @Getter
    private String status;
    @Setter
    @Getter
    @SerializedName("facebook_name")
    private String facebookName;
    @Setter
    @Getter
    @SerializedName("last_visited")
    private String lastVisited;
    @Setter
    @Getter
    @SerializedName("banned")
    private boolean banned;
    @Setter
    @Getter
    @SerializedName("isHidden")
    private boolean isHidden;
    @Setter
    @Getter
    @SerializedName("isIncognito")
    private boolean isIncognito;
    @Setter
    @Getter
    @SerializedName("isIncognitoBought")
    private boolean isIncognitoBought;
    @SerializedName("isHiddenBought")
    @Setter
    @Getter
    private boolean isHiddenBought;
    @Setter
    @Getter
    @SerializedName("isVip")
    private boolean isVip;
    @Setter
    @Getter
    @SerializedName("isFirstVipLogin")
    private boolean isFirstVipLogin;
    @Setter
    @Getter
    @SerializedName("vipMembershipType")
    private String vipMembershipType;
    @Setter
    @Getter
    @SerializedName("'badge_name'")
    private String badgeName;

}
