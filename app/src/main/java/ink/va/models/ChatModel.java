package ink.va.models;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

import ink.va.utils.Time;
import lombok.Getter;
import lombok.Setter;

import static ink.va.utils.Constants.STATUS_NOT_DELIVERED;

/**
 * Created by USER on 2016-06-24.
 */

public class ChatModel implements Comparable<ChatModel> {
    @SerializedName("messageId")
    @Setter
    @Getter
    private String messageId;
    @SerializedName("userId")
    @Setter
    @Getter
    private String userId;
    @SerializedName("firstName")
    @Setter
    @Getter
    private String firstName;
    @SerializedName("lastName")
    @Setter
    @Getter
    private String lastName;
    @SerializedName("opponentFirstName")
    @Setter
    @Getter
    private String opponentFirstName;
    @SerializedName("opponentLastName")
    @Setter
    @Getter
    private String opponentLastName;
    @SerializedName("opponentId")
    @Setter
    @Getter
    private String opponentId;
    @SerializedName("message")
    @Setter
    @Getter
    private String message;
    @SerializedName("date")
    @Setter
    @Getter
    private String date;
    @SerializedName("stickerChosen")
    @Setter
    @Getter
    private boolean stickerChosen;
    @SerializedName("stickerUrl")
    @Setter
    @Getter
    private String stickerUrl;
    @SerializedName("isSocialAccount")
    @Setter
    @Getter
    private boolean isSocialAccount;

    @SerializedName("isCurrentUserSocial")
    @Setter
    @Getter
    private boolean isCurrentUserSocial;

    @SerializedName("currentUserImage")
    @Setter
    @Getter
    private String currentUserImage;

    @SerializedName("opponentImage")
    @Setter
    @Getter
    private String opponentImage;

    @Setter
    @Getter
    private String deliveryStatus = STATUS_NOT_DELIVERED;

    @Override
    public int compareTo(ChatModel o) {
        try {
            long firstMillis = Long.valueOf(getMessageId());
            long secondMillis = Long.valueOf(o.getMessageId());
            Date firstDate = Time.convertMillistToDate(firstMillis);
            Date secondDate = Time.convertMillistToDate(secondMillis);
            return firstDate.compareTo(secondDate);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }


//    @Override
//    public boolean equals(Object obj) {
//        if (obj != null && obj instanceof ChatModel) {
//            return ((ChatModel) obj).messageId.equals(messageId);
//        }
//        return false;
//    }
//
//    @Override
//    public int hashCode() {
//        return messageId.hashCode();
//    }
}
