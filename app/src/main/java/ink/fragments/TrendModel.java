package ink.fragments;

/**
 * Created by PC-Comp on 9/12/2016.
 */
public class TrendModel {
    private String title;
    private String content;
    private String imageUrl;
    private String externalUrl;
    private String category;
    private String id;
    private boolean isTop;

    public TrendModel(String title, String content, String imageUrl, String externalUrl, String category, String id, boolean isTop) {
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
        this.externalUrl = externalUrl;
        this.category = category;
        this.id = id;
        this.isTop = isTop;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getExternalUrl() {
        return externalUrl;
    }

    public void setExternalUrl(String externalUrl) {
        this.externalUrl = externalUrl;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isTop() {
        return isTop;
    }

    public void setTop(boolean top) {
        isTop = top;
    }
}
