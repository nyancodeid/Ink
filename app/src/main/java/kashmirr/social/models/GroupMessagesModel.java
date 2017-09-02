package kashmirr.social.models;

/**
 * Created by USER on 2016-07-10.
 */
public class GroupMessagesModel {
    private String groupId;
    private String groupMessage;
    private String senderId;
    private String senderImage;
    private String senderName;
    private String groupMessageId;
    private boolean isRequested;
    private boolean isFriend;
    private boolean isIncognito;
    private String fileName;

    public GroupMessagesModel(boolean isFriend,boolean isIncognito, String groupId, String groupMessage,
                              String senderId, String senderImage,
                              String senderName, String groupMessageId,
                              boolean isRequested, String fileName) {
        this.groupId = groupId;
        this.isIncognito = isIncognito;
        this.fileName = fileName;
        this.isFriend = isFriend;
        this.isRequested = isRequested;
        this.groupMessage = groupMessage;
        this.senderId = senderId;
        this.senderImage = senderImage;
        this.senderName = senderName;
        this.groupMessageId = groupMessageId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isFriend() {
        return isFriend;
    }

    public void setFriend(boolean friend) {
        isFriend = friend;
    }

    public String getGroupId() {
        return groupId;
    }

    public boolean isIncognito() {
        return isIncognito;
    }

    public void setIncognito(boolean incognito) {
        isIncognito = incognito;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupMessage() {
        return groupMessage;
    }

    public boolean isRequested() {
        return isRequested;
    }

    public void setRequested(boolean requested) {
        isRequested = requested;
    }

    public void setGroupMessage(String groupMessage) {
        this.groupMessage = groupMessage;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderImage() {
        return senderImage;
    }

    public void setSenderImage(String senderImage) {
        this.senderImage = senderImage;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getGroupMessageId() {
        return groupMessageId;
    }

    public void setGroupMessageId(String groupMessageId) {
        this.groupMessageId = groupMessageId;
    }
}
