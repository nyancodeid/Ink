package ink.va.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by USER on 2016-07-21.
 */
public class CoinsModel {
    @SerializedName("coins_price")
    public String coinsPrice;
    @SerializedName("coins_count")
    public String coinsCount;
    @SerializedName("coins_name_en")
    public String coinsNameEn;
    @SerializedName("coins_name_ru")
    public String coinsNameRu;
    @SerializedName("coins_pack_id")
    public String coinsPackId;
}
