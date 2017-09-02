package kashmirr.social.models;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by USER on 2017-05-03.
 */

public class Config {
    @Setter
    @Getter
    @SerializedName("configId")
    private String configId;
    @Setter
    @Getter
    @SerializedName("configName")
    private String configName;
    @Setter
    @Getter
    @SerializedName("configArgument")
    private String configArgument;
}
