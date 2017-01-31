package ink.va.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by PC-Comp on 1/31/2017.
 */

public class BadgeModel {
    @SerializedName("badge_id")
    private String badgeId;
    @SerializedName("badge_name")
    private String badgeName;
    @SerializedName("badge_title")
    private String badgeTitle;
    @SerializedName("badge_price")
    private int badgePrice;

    public String getBadgeId() {
        return badgeId;
    }

    public String getBadgeName() {
        return badgeName;
    }

    public String getBadgeTitle() {
        return badgeTitle;
    }

    public int getBadgePrice() {
        return badgePrice;
    }
}
