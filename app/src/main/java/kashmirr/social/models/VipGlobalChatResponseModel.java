package kashmirr.social.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by PC-Comp on 1/11/2017.
 */

public class VipGlobalChatResponseModel {
    @SerializedName("result")
    private List<VipGlobalChatModel> vipGlobalChatModels;
    @SerializedName("success")
    private boolean success;

    public List<VipGlobalChatModel> getVipGlobalChatModels() {
        return vipGlobalChatModels;
    }

    public boolean isSuccess() {
        return success;
    }
}
