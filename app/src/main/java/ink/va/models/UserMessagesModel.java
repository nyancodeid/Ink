package ink.va.models;

/**
 * Created by USER on 2016-07-01.
 */
public class UserMessagesModel {
    private String userId;
    private String opponentId;
    private String messageId;
    private String message;
    private String firstName;
    private String lastName;
    private String imageName;
        private String imageLink;
    private boolean isSocialAccount;
    private boolean isFriend;


    public UserMessagesModel(boolean isSocialAccount,
                             boolean isFriend,
                             String userId, String opponentId,
                             String messageId, String message,
                             String firstName, String lastName,
                             String imageName,
                             String imageLink) {
        this.userId = userId;
        this.isFriend = isFriend;
        this.isSocialAccount = isSocialAccount;
        this.imageLink = imageLink;
        this.opponentId = opponentId;
        this.messageId = messageId;
        this.message = message;
        this.firstName = firstName;
        this.lastName = lastName;
        this.imageName = imageName;
    }


    public boolean isSocialAccount() {
        return isSocialAccount;
    }

    public void setSocialAccount(boolean socialAccount) {
        isSocialAccount = socialAccount;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOpponentId() {
        return opponentId;
    }

    public void setOpponentId(String opponentId) {
        this.opponentId = opponentId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public boolean isFriend() {
        return isFriend;
    }

    public void setFriend(boolean friend) {
        isFriend = friend;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }
}
