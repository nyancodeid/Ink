package ink.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by echessa on 6/17/15.
 */
public class Track {
    @SerializedName("title")
    public String mTitle;

    @SerializedName("id")
    public int mID;

    @SerializedName("stream_url")
    public String mStreamURL;

    @SerializedName("artwork_url")
    public String mArtworkURL;


}
