package ink.models;

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


    public GroupMessagesModel(String groupId, String groupMessage,
                              String senderId, String senderImage,
                              String senderName, String groupMessageId) {
        this.groupId = groupId;
        this.groupMessage = groupMessage;
        this.senderId = senderId;
        this.senderImage = senderImage;
        this.senderName = senderName;
        this.groupMessageId = groupMessageId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupMessage() {
        return groupMessage;
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
