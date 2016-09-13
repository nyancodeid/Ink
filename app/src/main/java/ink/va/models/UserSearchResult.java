package ink.va.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by PC-Comp on 8/15/2016.
 */
public class UserSearchResult {
    @SerializedName("firstName")
    public String firstName;
    @SerializedName("last_name")
    public String lastName;
    @SerializedName("imageLink")
    public String imageLink;
    @SerializedName("isSocialAccount")
    public String isSocialAccount;
    @SerializedName("isFriend")
    public boolean isFriend;
    @SerializedName("user_id")
    public String userId;
}
