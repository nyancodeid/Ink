package ink.va.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by PC-Comp on 1/31/2017.
 */

public class MyMessagesModel {
    @SerializedName("success")
    private boolean success;
    @SerializedName("messages")
    private List<UserMessagesModel> userMessagesModels;

    public boolean isSuccess() {
        return success;
    }

    public List<UserMessagesModel> getUserMessagesModels() {
        return userMessagesModels;
    }
}
