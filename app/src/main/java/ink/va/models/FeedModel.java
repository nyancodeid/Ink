package ink.va.models;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by USER on 2016-06-20.
 */
public class FeedModel {
    @SerializedName("id")
    @Setter
    @Getter
    private String id;

    @SerializedName("group_name")
    @Setter
    @Getter
    private String groupName;

    @SerializedName("image_link")
    @Setter
    @Getter
    private String userImage;

    @SerializedName("file_name")
    @Setter
    @Getter
    private String fileName;

    @SerializedName("post_body")
    @Setter
    @Getter
    private String content;

    @SerializedName("poster_id")
    @Setter
    @Getter
    private String posterId;

    @SerializedName("address")
    @Setter
    @Getter
    private String address;

    @SerializedName("date_posted")
    @Setter
    @Getter
    private String datePosted;

    @SerializedName("first_name")
    @Setter
    @Getter
    private String firstName;

    @SerializedName("last_name")
    @Setter
    @Getter
    private String lastName;

    @SerializedName("is_liked")
    @Setter
    @Getter
    private boolean isLiked;

    @SerializedName("likes_count")
    @Setter
    @Getter
    private String likesCount;

    @SerializedName("isSocialAccount")
    @Setter
    @Getter
    private boolean isSocialAccount;


    @Setter
    @Getter
    private boolean addressPresent;

    @Setter
    @Getter
    private boolean attachmentPresent;

    @Setter
    @Getter
    private boolean isPostOwner;

    @SerializedName("isFriend")
    @Setter
    @Getter
    private boolean isFriend;

    @SerializedName("type")
    @Setter
    @Getter
    private String type;

    @SerializedName("count")
    @Setter
    @Getter
    private String count;

    @SerializedName("ownerImage")
    @Setter
    @Getter
    private String ownerImage;

    @SerializedName("groupOwnerId")
    @Setter
    @Getter
    private String groupOwnerId;

    @SerializedName("groupDescription")
    @Setter
    @Getter
    private String groupDescription;

    @SerializedName("groupImage")
    @Setter
    @Getter
    private String groupImage;

    @SerializedName("groupColor")
    @Setter
    @Getter
    private String groupColor;

    @SerializedName("groupOwnerName")
    @Setter
    @Getter
    private String groupOwnerName;

    @SerializedName("commentsCount")
    @Setter
    @Getter
    private String commentsCount;

    @SerializedName("group_message_file_name")
    @Setter
    @Getter
    private String groupMessageFileName;

    @SerializedName("isMember")
    @Setter
    @Getter
    private boolean isMember;

    @SerializedName("isGlobalPost")
    @Setter
    @Getter
    private boolean isGlobalPost;

    @SerializedName("isReported")
    @Setter
    @Getter
    private boolean isReported;

}
