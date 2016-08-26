package ink.utils;

/**
 * Created by USER on 2016-07-20.
 */
public class User {
    public static User user = new User();
    private int coins;
    private int bombCount;
    private String facebookUserId;

    public int getCoins() {
        return coins;
    }


    public static User get() {
        return user;
    }

    public int getBombCount() {
        return bombCount;
    }

    public void setBombCount(int bombCount) {
        this.bombCount = bombCount;
    }

    public String getFacebookUserId() {
        return facebookUserId;
    }

    public void setFacebookUserId(String facebookUserId) {
        this.facebookUserId = facebookUserId;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }
}
