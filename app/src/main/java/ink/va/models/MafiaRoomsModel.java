package ink.va.models;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by PC-Comp on 3/1/2017.
 */

public class MafiaRoomsModel {
    @SerializedName("id")
    @Setter
    @Getter
    private int id;

    @SerializedName("roomName")
    @Setter
    @Getter
    private String roomName;

    @SerializedName("roomLanguage")
    @Setter
    @Getter
    private String roomLanguage;

    @SerializedName("gameType")
    @Setter
    @Getter
    private String gameType;

    @SerializedName("morningDuration")
    @Setter
    @Getter
    private String morningDuration;


    @SerializedName("morningDurationUnit")
    @Setter
    @Getter
    private String morningDurationUnit;


    @SerializedName("nightDuration")
    @Setter
    @Getter
    private String nightDuration;


    @SerializedName("nightDurationUnit")
    @Setter
    @Getter
    private String nightDurationUnit;

    @SerializedName("creator_id")
    @Setter
    @Getter
    private String creatorId;

}
