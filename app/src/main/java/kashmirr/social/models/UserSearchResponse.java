package kashmirr.social.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by PC-Comp on 8/15/2016.
 */
public class UserSearchResponse {
    @SerializedName("success")
    public boolean success;
    @SerializedName("cause")
    public String cause;
    @SerializedName("result")
    public ArrayList<UserSearchResult> userSearchResults;

}
