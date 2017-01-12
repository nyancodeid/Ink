package ink.va.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by PC-Comp on 12/21/2016.
 */

public class ServerInformationModel {

    @SerializedName("banned")
    private boolean banned;
    @SerializedName("hasContent")
    private boolean hasContent;
    @SerializedName("singleLoad")
    private boolean singleLoad;
    @SerializedName("content")
    private String content;
    @SerializedName("newsId")
    private String newsId;
    @SerializedName("odlAppSupport")
    private boolean odlAppSupport;
    @SerializedName("serverAppVersion")
    private int serverAppVersion;
    @SerializedName("warningText")
    private String warningText;


    public String getNewsId() {
        return newsId;
    }

    public String getWarningText() {
        return warningText;
    }

    public void setWarningText(String warningText) {
        this.warningText = warningText;
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

    public boolean isOdlAppSupport() {
        return odlAppSupport;
    }

    public void setOdlAppSupport(boolean odlAppSupport) {
        this.odlAppSupport = odlAppSupport;
    }

    public int getServerAppVersion() {
        return serverAppVersion;
    }

    public void setServerAppVersion(int serverAppVersion) {
        this.serverAppVersion = serverAppVersion;
    }
}
