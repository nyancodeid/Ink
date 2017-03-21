package ink.va.models;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by PC-Comp on 3/21/2017.
 */

public class ParticipantModelWithoutUser {

    @SerializedName("id")
    @Setter
    @Getter
    private String id;

    @SerializedName("role")
    @Setter
    @Getter
    private String role;

    @SerializedName("eliminated")
    @Setter
    @Getter
    private boolean eliminated;

    @SerializedName("participant_id")
    @Setter
    @Getter
    private String participantId;

    @SerializedName("room_id")
    @Setter
    @Getter
    private String roomId;
}
