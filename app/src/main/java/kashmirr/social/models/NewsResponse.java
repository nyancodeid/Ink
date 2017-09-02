package kashmirr.social.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by PC-Comp on 9/12/2016.
 */
public class NewsResponse {
    @SerializedName("objects")
    public ArrayList<NewsModel> newsModels;
    @SerializedName("meta")
    public NewsMeta  newsMeta;
}
