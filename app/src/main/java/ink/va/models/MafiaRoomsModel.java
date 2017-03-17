package ink.va.models;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.List;

import ink.va.utils.User;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by PC-Comp on 3/1/2017.
 */

@Parcel
public class MafiaRoomsModel implements Comparable<MafiaRoomsModel> {

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

    @SerializedName("gameStarted")
    @Setter
    @Getter
    private boolean gameStarted;

    @SerializedName("joinedUsers")
    @Setter
    @Getter
    private List<ParticipantModel> joinedUsers;

    @SerializedName("maxPlayers")
    @Setter
    @Getter
    private int maxPlayers;

    @SerializedName("gameStartDate")
    @Setter
    @Getter
    private String gameStartDate;

    @SerializedName("currentDayType")
    @Setter
    @Getter
    private String currentDayType;

    @SerializedName("willSelfDestruct")
    @Setter
    @Getter
    private boolean willSelfDestruct;

    @SerializedName("gameEnded")
    @Getter
    @Setter
    private boolean gameEnded;

    @SerializedName("daysPast")
    @Getter
    @Setter
    private int daysLeft;

    @SerializedName("hoursPast")
    @Getter
    @Setter
    private int hoursLeft;

    @SerializedName("minutesPast")
    @Getter
    @Setter
    private int minutesLeft;

    @SerializedName("currentServerDate")
    @Getter
    @Setter
    private String currentServerDate;

    @Override
    public int compareTo(MafiaRoomsModel o) {
        boolean isParticipant = false;
        String currentUserId = User.get().getUserId();
        for (ParticipantModel eachUserId : getJoinedUsers()) {
            if (eachUserId.getUser().getUserId().equals(currentUserId)) {
                isParticipant = true;
                break;
            }
        }

        if (isParticipant) {
            return -1;
        }
        return 0;
    }

}
