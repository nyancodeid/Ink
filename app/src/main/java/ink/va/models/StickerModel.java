package ink.va.models;

/**
 * Created by PC-Comp on 8/9/2016.
 */
public class StickerModel {
    private String gifId;
    private String ownerId;
    private String StickerUrl;
    private boolean isAnimated;
    private boolean hasSound;

    public StickerModel(String gifId, String ownerId, String StickerUrl, boolean isAnimated, boolean hasSound) {
        this.gifId = gifId;
        this.ownerId = ownerId;
        this.StickerUrl = StickerUrl;
        this.isAnimated = isAnimated;
        this.hasSound = hasSound;
    }

    public boolean isAnimated() {
        return isAnimated;
    }

    public void setAnimated(boolean animated) {
        isAnimated = animated;
    }

    public boolean hasSound() {
        return hasSound;
    }

    public void setHasSound(boolean hasSound) {
        this.hasSound = hasSound;
    }

    public String getGifId() {
        return gifId;
    }

    public void setGifId(String gifId) {
        this.gifId = gifId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getStickerUrl() {
        return StickerUrl;
    }

    public void setStickerUrl(String stickerUrl) {
        this.StickerUrl = stickerUrl;
    }

}
