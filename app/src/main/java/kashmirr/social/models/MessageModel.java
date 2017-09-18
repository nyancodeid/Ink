package kashmirr.social.models;

import io.realm.RealmObject;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by USER on 2016-06-26.
 */
public class MessageModel extends RealmObject {
    @Setter
    @Getter
    String id;
    @Setter
    @Getter
    String messageId;
    @Setter
    @Getter
    String userId;
    @Setter
    @Getter
    String opponentId;
    @Setter
    @Getter
    String message;
    @Setter
    @Getter
    String date;
    @Setter
    @Getter
    String deliveryStatus;
    @Setter
    @Getter
    String userImage;
    @Setter
    @Getter
    String opponentImage;
    @Setter
    @Getter
    String deleteUserId;
    @Setter
    @Getter
    String deleteOpponentId;
    @Setter
    @Getter
    boolean hasGif;
    @Setter
    @Getter
    boolean isAnimated;
    @Setter
    @Getter
    String gifUrl;
    @Setter
    @Getter
    String firstName;

    @Setter
    @Getter
    String lastName;

    @Setter
    @Getter
    String filePath;

    @Setter
    @Getter
    String opponentFirstName;
    @Setter
    @Getter
    String opponentLastName;

    @Setter
    @Getter
    boolean isSocialAccount;

    @Setter
    @Getter
    boolean currentUserSocial;

    @Setter
    @Getter
    boolean hasRead;
}
