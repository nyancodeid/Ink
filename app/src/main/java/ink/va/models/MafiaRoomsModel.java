package ink.va.models;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by PC-Comp on 3/1/2017.
 */
@Parcel
public class MafiaRoomsModel {
    @SerializedName("id")
    @Setter
    @Getter
    public int id;

    @SerializedName("roomName")
    @Setter
    @Getter
    public String roomName;

    @SerializedName("roomLanguage")
    @Setter
    @Getter
    public String roomLanguage;

    @SerializedName("gameType")
    @Setter
    @Getter
    public String gameType;

    @SerializedName("morningDuration")
    @Setter
    @Getter
    public String morningDuration;


    @SerializedName("morningDurationUnit")
    @Setter
    @Getter
    public String morningDurationUnit;


    @SerializedName("nightDuration")
    @Setter
    @Getter
    public String nightDuration;


    @SerializedName("nightDurationUnit")
    @Setter
    @Getter
    public String nightDurationUnit;

    @SerializedName("creator_id")
    @Setter
    @Getter
    public String creatorId;

    @SerializedName("gameStarted")
    @Setter
    @Getter
    public boolean gameStarted;

    @SerializedName("joinedUsers")
    @Setter
    @Getter
    public List<String> joinedUserIds;

    @SerializedName("maxPlayers")
    @Setter
    @Getter
    public int maxPlayers;

}
