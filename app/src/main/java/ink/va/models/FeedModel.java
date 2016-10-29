package ink.va.models;

/**
 * Created by USER on 2016-06-20.
 */
public class FeedModel {
    private String groupName;
    private String id;
    private String userImage;
    private String fileName;
    private String content;
    private String posterId;
    private String address;
    private String datePosted;
    private String firstName;
    private String lastName;
    private boolean isLiked;
    private String likesCount;
    private boolean isSocialAccount;
    private boolean hasAddress;
    private boolean hasAttachment;
    private boolean isPostOwner;
    private boolean isFriend;
    private String type;
    private String count;
    private String ownerImage;
    private String groupOwnerId;
    private String groupDescription;
    private String groupImage;
    private String groupColor;
    private String groupOwnerName;
    private String commentsCount;
    private String groupMessageFileName;

    public FeedModel(boolean isFriend, boolean isSocialAccount, String id, String userImage, String fileName, String content, String posterId,
                     String address,
                     String datePosted, String firstName,
                     String lastName, boolean isLiked, String likesCount, String type, String groupName, String count, String ownerImage, String groupOwnerId, String groupDescription,
                     String groupImage, String groupColor, String groupOwnerName,String commentsCount,String groupMessageFileName) {
        this.id = id;
        this.groupOwnerName = groupOwnerName;
        this.count = count;
        this.commentsCount = commentsCount;
        this.ownerImage = ownerImage;
        this.groupMessageFileName = groupMessageFileName;
        this.groupOwnerId = groupOwnerId;
        this.groupDescription = groupDescription;
        this.groupImage = groupImage;
        this.groupColor = groupColor;
        this.groupName = groupName;
        this.isSocialAccount = isSocialAccount;
        this.likesCount = likesCount;
        this.isFriend = isFriend;
        this.isLiked = isLiked;
        this.firstName = firstName;
        this.fileName = fileName;
        this.lastName = lastName;
        this.userImage = userImage;
        this.fileName = fileName;
        this.content = content;
        this.posterId = posterId;
        this.type = type;
        this.address = address;
        this.datePosted = datePosted;
    }

    public String getGroupOwnerName() {
        return groupOwnerName;
    }

    public String getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(String commentsCount) {
        this.commentsCount = commentsCount;
    }

    public void setGroupOwnerName(String groupOwnerName) {
        this.groupOwnerName = groupOwnerName;
    }

    public String getOwnerImage() {
        return ownerImage;
    }

    public void setOwnerImage(String ownerImage) {
        this.ownerImage = ownerImage;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public String getGroupOwnerId() {
        return groupOwnerId;
    }

    public void setGroupOwnerId(String groupOwnerId) {
        this.groupOwnerId = groupOwnerId;
    }

    public String getGroupMessageFileName() {
        return groupMessageFileName;
    }

    public void setGroupMessageFileName(String groupMessageFileName) {
        this.groupMessageFileName = groupMessageFileName;
    }

    public String getGroupDescription() {
        return groupDescription;
    }

    public void setGroupDescription(String groupDescription) {
        this.groupDescription = groupDescription;
    }

    public String getGroupImage() {
        return groupImage;
    }

    public void setGroupImage(String groupImage) {
        this.groupImage = groupImage;
    }

    public String getGroupColor() {
        return groupColor;
    }

    public void setGroupColor(String groupColor) {
        this.groupColor = groupColor;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserImage() {
        return userImage;
    }

    public boolean isSocialAccount() {
        return isSocialAccount;
    }

    public void setSocialAccount(boolean socialAccount) {
        isSocialAccount = socialAccount;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPosterId() {
        return posterId;
    }

    public void setPosterId(String posterId) {
        this.posterId = posterId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDatePosted() {
        return datePosted;
    }

    public void setDatePosted(String datePosted) {
        this.datePosted = datePosted;
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

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    public String getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(String likesCount) {
        this.likesCount = likesCount;
    }

    public boolean hasAddress() {
        return hasAddress;
    }

    public void setHasAddress(boolean hasAddress) {
        this.hasAddress = hasAddress;
    }

    public boolean hasAttachment() {
        return hasAttachment;
    }

    public void setHasAttachment(boolean hasAttachment) {
        this.hasAttachment = hasAttachment;
    }

    public boolean isPostOwner() {
        return isPostOwner;
    }

    public boolean isFriend() {
        return isFriend;
    }

    public void setFriend(boolean friend) {
        isFriend = friend;
    }

    public void setPostOwner(boolean postOwner) {
        isPostOwner = postOwner;
    }
}
