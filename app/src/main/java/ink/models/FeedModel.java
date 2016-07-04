package ink.models;

/**
 * Created by USER on 2016-06-20.
 */
public class FeedModel {
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


    public FeedModel(String id, String userImage, String fileName, String content, String posterId,
                     String address,
                     String datePosted, String firstName,
                     String lastName, boolean isLiked, String likesCount) {
        this.id = id;
        this.likesCount = likesCount;
        this.isLiked = isLiked;
        this.firstName = firstName;
        this.fileName = fileName;
        this.lastName = lastName;
        this.userImage = userImage;
        this.fileName = fileName;
        this.content = content;
        this.posterId = posterId;
        this.address = address;
        this.datePosted = datePosted;
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
}
