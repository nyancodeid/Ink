package ink.va.models;

/**
 * Created by USER on 2016-11-18.
 */

public class WhoViewedModel {
    private String firstName;
    private String lastName;
    private String imageLink;
    private boolean isSocialAccount;
    private boolean isFriend;
    private String userId;
    private String timeViewed;


    public WhoViewedModel(String firstName, String lastName,
                          String imageLink, boolean isSocialAccount,
                          boolean isFriend, String userId, String timeViewed) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.imageLink = imageLink;
        this.isSocialAccount = isSocialAccount;
        this.isFriend = isFriend;
        this.userId = userId;
        this.timeViewed = timeViewed;
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

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    public boolean isSocialAccount() {
        return isSocialAccount;
    }

    public void setSocialAccount(boolean socialAccount) {
        isSocialAccount = socialAccount;
    }

    public boolean isFriend() {
        return isFriend;
    }

    public void setFriend(boolean friend) {
        isFriend = friend;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTimeViewed() {
        return timeViewed;
    }

    public void setTimeViewed(String timeViewed) {
        this.timeViewed = timeViewed;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
