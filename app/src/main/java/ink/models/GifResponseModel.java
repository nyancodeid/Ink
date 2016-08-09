package ink.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by PC-Comp on 8/9/2016.
 */
public class GifResponseModel {
    @SerializedName("id")
    public String id;
    @SerializedName("userId")
    public String userId;
    @SerializedName("gifName")
    public String gifName;
    @SerializedName("isAnimated")
    public boolean isAnimated;
    @SerializedName("hasSound")
    public boolean hasSound;
}
