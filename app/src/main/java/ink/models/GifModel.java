package ink.models;

/**
 * Created by PC-Comp on 8/9/2016.
 */
public class GifModel {
    private String gifId;
    private String ownerId;
    private String gifName;
    private boolean isAnimated;
    private boolean hasSound;

    public GifModel(String gifId, String ownerId, String gifName, boolean isAnimated, boolean hasSound) {
        this.gifId = gifId;
        this.ownerId = ownerId;
        this.gifName = gifName;
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

    public String getGifName() {
        return gifName;
    }

    public void setGifName(String gifName) {
        this.gifName = gifName;
    }

}
