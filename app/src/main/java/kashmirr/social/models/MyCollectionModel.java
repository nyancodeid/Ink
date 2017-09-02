package kashmirr.social.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by PC-Comp on 12/22/2016.
 */

public class MyCollectionModel {
    @SerializedName("id")
    String id;
    @SerializedName("userId")
    String userId;
    @SerializedName("stickerUrl")
    String stickerUrl;
    @SerializedName("isAnimated")
    boolean isAnimated;
    @SerializedName("hasSound")
    boolean hasSound;
    @SerializedName("packName")
    String packName;


    public String getPackName() {
        return packName;
    }

    public void setPackName(String packName) {
        this.packName = packName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStickerUrl() {
        return stickerUrl;
    }

    public void setStickerUrl(String stickerUrl) {
        this.stickerUrl = stickerUrl;
    }

    public boolean isAnimated() {
        return isAnimated;
    }

    public void setAnimated(boolean animated) {
        isAnimated = animated;
    }

    public boolean isHasSound() {
        return hasSound;
    }

    public void setHasSound(boolean hasSound) {
        this.hasSound = hasSound;
    }
}
