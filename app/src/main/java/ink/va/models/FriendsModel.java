package ink.va.models;

/**
 * Created by USER on 2016-06-22.
 */

public class FriendsModel {
    private String fullName;
    private String imageLink;
    private String phoneNumber;
    private String friendId;
    private String firstName;
    private String lastName;
    private boolean isFriend;
    private boolean isSocialAccount;
    private String badgeName;


    public FriendsModel(String badgeName,boolean isFriend, boolean isSocialAccount, String fullName, String imageLink, String phoneNumber, String friendId, String firstName, String lastName) {
        this.fullName = fullName;
        this.badgeName = badgeName;
        this.isFriend = isFriend;
        this.isSocialAccount = isSocialAccount;
        this.friendId = friendId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.imageLink = imageLink;
        this.phoneNumber = phoneNumber;
    }


    public String getBadgeName() {
        return badgeName;
    }

    public void setBadgeName(String badgeName) {
        this.badgeName = badgeName;
    }

    public String getFullName() {
        return fullName;
    }

    public String getFriendId() {
        return friendId;
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

    public void setFriendId(String friendId) {
        this.friendId = friendId;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isFriend() {
        return isFriend;
    }

    public boolean isSocialAccount() {
        return isSocialAccount;
    }

    public void setSocialAccount(boolean socialAccount) {
        isSocialAccount = socialAccount;
    }

    public void setFriend(boolean friend) {
        isFriend = friend;
    }
}
