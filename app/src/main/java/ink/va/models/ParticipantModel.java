package ink.va.models;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by PC-Comp on 3/17/2017.
 */

@Parcel
public class ParticipantModel {

    @SerializedName("role")
    @Setter
    @Getter
    private String role;

    @SerializedName("eliminated")
    @Setter
    @Getter
    private boolean eliminated;

    @SerializedName("user")
    @Setter
    @Getter
    private UserModel user;
}
