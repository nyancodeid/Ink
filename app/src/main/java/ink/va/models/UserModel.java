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
    private int coins;
    @SerializedName("coins")
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
    private String vipMembershipType;


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isSocialAccount() {
        return isSocialAccount;
    }

    public void setSocialAccount(boolean socialAccount) {
        isSocialAccount = socialAccount;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getFacebookProfile() {
        return facebookProfile;
    }

    public void setFacebookProfile(String facebookProfile) {
        this.facebookProfile = facebookProfile;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getSkype() {
        return skype;
    }

    public void setSkype(String skype) {
        this.skype = skype;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFacebookName() {
        return facebookName;
    }

    public void setFacebookName(String facebookName) {
        this.facebookName = facebookName;
    }

    public String getLastVisited() {
        return lastVisited;
    }

    public void setLastVisited(String lastVisited) {
        this.lastVisited = lastVisited;
    }

    public boolean isBanned() {
        return banned;
    }

    public void setBanned(boolean banned) {
        this.banned = banned;
    }

    public boolean isHidden() {
        return isHidden;
    }

    public void setHidden(boolean hidden) {
        isHidden = hidden;
    }

    public boolean isIncognito() {
        return isIncognito;
    }

    public void setIncognito(boolean incognito) {
        isIncognito = incognito;
    }

    public boolean isIncognitoBought() {
        return isIncognitoBought;
    }

    public void setIncognitoBought(boolean incognitoBought) {
        isIncognitoBought = incognitoBought;
    }

    public boolean isHiddenBought() {
        return isHiddenBought;
    }

    public void setHiddenBought(boolean hiddenBought) {
        isHiddenBought = hiddenBought;
    }

    public boolean isVip() {
        return isVip;
    }

    public void setVip(boolean vip) {
        isVip = vip;
    }

    public boolean isFirstVipLogin() {
        return isFirstVipLogin;
    }

    public void setFirstVipLogin(boolean firstVipLogin) {
        isFirstVipLogin = firstVipLogin;
    }

    public String getVipMembershipType() {
        return vipMembershipType;
    }

    public void setVipMembershipType(String vipMembershipType) {
        this.vipMembershipType = vipMembershipType;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }
}
