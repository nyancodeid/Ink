package kashmirr.social.fragments;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by PC-Comp on 9/12/2016.
 */
public class TrendModel {
    @Getter
    @Setter
    private String title;
    @Getter
    @Setter
    private String content;
    @Getter
    @Setter
    private String imageUrl;
    @Getter
    @Setter
    private String externalUrl;
    @Getter
    @Setter
    private String category;
    @Getter
    @Setter
    private String id;
    @Getter
    @Setter
    private boolean isTop;
    @Getter
    @Setter
    private String creatorId;

    public TrendModel(String creatorId,String title, String content, String imageUrl, String externalUrl, String category, String id, boolean isTop) {
        this.title = title;
        this.creatorId = creatorId;
        this.content = content;
        this.imageUrl = imageUrl;
        this.externalUrl = externalUrl;
        this.category = category;
        this.id = id;
        this.isTop = isTop;
    }

}
