package kashmirr.social.models;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

import kashmirr.social.utils.Time;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by USER on 2016-07-01.
 */
public class UserMessagesModel implements Comparable<UserMessagesModel> {

    @SerializedName("user_id")
    @Setter
    @Getter
    private String userId;
    @SerializedName("opponent_id")
    @Setter
    @Getter
    private String opponentId;
    @SerializedName("message_id")
    @Setter
    @Getter
    private String messageId;
    @SerializedName("message")
    @Setter
    @Getter
    private String message;
    @SerializedName("firstName")
    @Setter
    @Getter
    private String firstName;
    @SerializedName("lastName")
    @Setter
    @Getter
    private String lastName;
    @SerializedName("imageName")
    @Setter
    @Getter
    private String imageName;
    @SerializedName("isSocialAccount")
    @Setter
    @Getter
    private boolean isSocialAccount;
    @Setter
    @Getter
    @SerializedName("isFriend")
    private boolean isFriend;


    @SerializedName("date")
    @Setter
    @Getter
    private String date;

    @SerializedName("filePath")
    @Setter
    @Getter
    private String filePath;

    @Setter
    @Getter
    private boolean hasRead;

    @Override
    public int compareTo(UserMessagesModel o) {
        try {
            long firstMillis = Long.valueOf(getMessageId());
            long secondMillis = Long.valueOf(o.getMessageId());
            Date firstDate = Time.convertMillisToDate(firstMillis);
            Date secondDate = Time.convertMillisToDate(secondMillis);
            int dateCompare = firstDate.compareTo(secondDate);

            if (dateCompare == 0) {
                return 0;
            } else if (dateCompare > 0) {
                return -1;
            } else if (dateCompare < 0) {
                return 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        return 0;
    }
}
