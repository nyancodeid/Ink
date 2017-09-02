package kashmirr.social.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by USER on 2016-07-23.
 */
public class UserStatus {
    @SerializedName("success")
    public boolean success;
    @SerializedName("isOnline")
    public boolean isOnline;
    @SerializedName("time")
    public String lastSeenTime;
}
