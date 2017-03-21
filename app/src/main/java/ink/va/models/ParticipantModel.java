package ink.va.models;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by PC-Comp on 3/17/2017.
 */

@Parcel
public class ParticipantModel implements Comparable<ParticipantModel> {

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

    @Setter
    @Getter
    private String roomCreatorId = "";

    @Setter
    @Getter
    private boolean victim;


    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof ParticipantModel) {
            if (((ParticipantModel) obj).getUser().getUserId().equals(user.getUserId())) {
                return true;
            }
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return user.getUserId().hashCode();
    }

    @Override
    public int compareTo(@NonNull ParticipantModel o) {
        if (o.getUser().getUserId().equals(roomCreatorId)) {
            return 1;
        }
        return -1;
    }
}
