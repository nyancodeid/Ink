package kashmirr.social.models;

/**
 * Created by USER on 2016-11-09.
 */

public class RandomChatModel {
    private String message;
    private boolean isMine;

    public RandomChatModel(String message, boolean isMine) {
        this.message = message;
        this.isMine = isMine;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isMine() {
        return isMine;
    }

    public void setMine(boolean mine) {
        isMine = mine;
    }
}
