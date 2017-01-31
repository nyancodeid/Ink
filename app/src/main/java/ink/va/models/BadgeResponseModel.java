package ink.va.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by PC-Comp on 1/31/2017.
 */

public class BadgeResponseModel {
    @SerializedName("success")
    boolean success;
    @SerializedName("result")
    List<BadgeModel> badgeModels;

    public boolean isSuccess() {
        return success;
    }

    public List<BadgeModel> getBadgeModels() {
        return badgeModels;
    }
}
