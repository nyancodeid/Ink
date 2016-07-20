package ink.utils;

/**
 * Created by USER on 2016-07-20.
 */
public class User {
    public static User user = new User();
    private int coins;

    public int getCoins() {
        return coins;
    }

    public static User get() {
        return user;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }
}
