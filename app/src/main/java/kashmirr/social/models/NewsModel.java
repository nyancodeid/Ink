package kashmirr.social.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by PC-Comp on 9/12/2016.
 */
public class NewsModel {
    @SerializedName("url_text")
    public String urlText;
    @SerializedName("topics")
    public ArrayList<NewsTopic> newsTopics;
    @SerializedName("url_external_link")
    public String urlExternalLink;
    @SerializedName("content")
    public String newsContent;
}
