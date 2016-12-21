package ink.va.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by PC-Comp on 12/21/2016.
 */

public class ServerInformationModel {

    @SerializedName("banned")
    boolean banned;
    @SerializedName("hasContent")
    boolean hasContent;
    @SerializedName("singleLoad")
    boolean singleLoad;
    @SerializedName("content")
    String content;
    @SerializedName("newsId")
    String newsId;


    public String getNewsId() {
        return newsId;
    }

    public void setNewsId(String newsId) {
        this.newsId = newsId;
    }

    public boolean isBanned() {
        return banned;
    }

    public void setBanned(boolean banned) {
        this.banned = banned;
    }

    public boolean HasContent() {
        return hasContent;
    }

    public void setHasContent(boolean hasContent) {
        this.hasContent = hasContent;
    }

    public boolean isSingleLoad() {
        return singleLoad;
    }

    public void setSingleLoad(boolean singleLoad) {
        this.singleLoad = singleLoad;
    }


    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
