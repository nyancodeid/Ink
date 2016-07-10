package ink.models;

/**
 * Created by USER on 2016-07-11.
 */
public class RequestsModel {
    private String groupOwnerId;
    private String requesterId;
    private String requesterName;
    private String requesterImage;
    private String requestedGroupId;
    private String requestId;
    private String groupName;


    public RequestsModel(String groupOwnerId, String requesterId,
                         String requesterName, String requesterImage,
                         String requestedGroupId, String requestId,String groupName) {
        this.groupOwnerId = groupOwnerId;
        this.groupName = groupName;
        this.requesterId = requesterId;
        this.requesterName = requesterName;
        this.requesterImage = requesterImage;
        this.requestedGroupId = requestedGroupId;
        this.requestId = requestId;
    }

    public String getGroupOwnerId() {
        return groupOwnerId;
    }

    public void setGroupOwnerId(String groupOwnerId) {
        this.groupOwnerId = groupOwnerId;
    }

    public String getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(String requesterId) {
        this.requesterId = requesterId;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }

    public String getRequesterImage() {
        return requesterImage;
    }

    public void setRequesterImage(String requesterImage) {
        this.requesterImage = requesterImage;
    }

    public String getRequestedGroupId() {
        return requestedGroupId;
    }

    public void setRequestedGroupId(String requestedGroupId) {
        this.requestedGroupId = requestedGroupId;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
