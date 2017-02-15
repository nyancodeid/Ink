package ink.va.models;

import com.google.gson.annotations.SerializedName;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by USER on 2016-06-24.
 */

@Builder
public class ChatModel {
    @SerializedName("messageId")
    @Setter
    @Getter
    private String messageId;
    @SerializedName("userId")
    @Setter
    @Getter
    private String userId;
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
    @SerializedName("hasSticker")
    @Setter
    @Getter
    private boolean hasSticker;
    @SerializedName("stickerUrl")
    @Setter
    @Getter
    private String stickerUrl;


}
