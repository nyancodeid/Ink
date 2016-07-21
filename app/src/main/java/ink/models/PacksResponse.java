package ink.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by USER on 2016-07-21.
 */
public class PacksResponse {
    @SerializedName("success")
    public boolean success;
    @SerializedName("result")
    public ArrayList<PacksModel> packsModels;
}
