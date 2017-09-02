package kashmirr.social.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by USER on 2016-07-20.
 */
public class CoinsResponse {

    @SerializedName("success")
    public boolean success;
    @SerializedName("coins")
    public int coins;
    @SerializedName("coinsDeducateForGlobal")
    public int coinsDeducateForGlobal;
}
