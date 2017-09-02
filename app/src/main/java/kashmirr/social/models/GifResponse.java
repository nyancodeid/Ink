package kashmirr.social.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by PC-Comp on 8/9/2016.
 */
public class GifResponse {
    @SerializedName("success")
    public boolean success;
    @SerializedName("cause")
    public String cause;
    @SerializedName("result")
    public  ArrayList<GifResponseModel> gifResponseModels;
}
