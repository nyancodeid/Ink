package ink.models;

/**
 * Created by USER on 2016-07-05.
 */
public class CommentModel {
    private String commentId;
    private String commenterId;
    private String commenterImage;
    private String commentBody;
    private String postId;
    private String firstName;
    private String lastName;
    private boolean isSocialAccount;
    private boolean isFriend;


    public CommentModel(boolean isSocialAccount, boolean isFriend, String commentId, String commenterId,
                        String commenterImage,
                        String commentBody,
                        String postId, String firstName,
                        String lastName) {
        this.commentId = commentId;
        this.isFriend = isFriend;
        this.isSocialAccount = isSocialAccount;
        this.firstName = firstName;
        this.lastName = lastName;
        this.commenterId = commenterId;
        this.commenterImage = commenterImage;
        this.commentBody = commentBody;
        this.postId = postId;
    }

    public boolean isSocialAccount() {
        return isSocialAccount;
    }

    public void setSocialAccount(boolean socialAccount) {
        isSocialAccount = socialAccount;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getCommenterId() {
        return commenterId;
    }

    public void setCommenterId(String commenterId) {
        this.commenterId = commenterId;
    }

    public String getCommenterImage() {
        return commenterImage;
    }

    public void setCommenterImage(String commenterImage) {
        this.commenterImage = commenterImage;
    }

    public String getCommentBody() {
        return commentBody;
    }

    public void setCommentBody(String commentBody) {
        this.commentBody = commentBody;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
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

    public boolean isFriend() {
        return isFriend;
    }

    public void setFriend(boolean friend) {
        isFriend = friend;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
