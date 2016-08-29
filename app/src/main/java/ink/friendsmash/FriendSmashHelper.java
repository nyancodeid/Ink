package ink.friendsmash;

import org.json.JSONArray;
import org.json.JSONObject;

import ink.utils.User;

/**
 * Created by PC-Comp on 8/29/2016.
 */
public class FriendSmashHelper {


    private static FriendSmashHelper friendSmashHelper = new FriendSmashHelper();
    public static int NUM_BOMBS_ALLOWED_IN_GAME = 3;
    public static int NUM_COINS_PER_BOMB = 5;
    private int score = 0;
    private int bombs = 0;
    private int coinsCollected = 0;
    private int topScore = 0;

    private JSONArray friends;
    private String lastFriendSmashedID = null;
    private String lastFriendSmashedName = null;

    public JSONArray getFriends() {
        return friends;
    }

    public static FriendSmashHelper get() {
        return friendSmashHelper;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getBombs() {
        return bombs;
    }

    public void setBombs(int bombs) {
        this.bombs = bombs;
    }

    public int getCoins() {
        return User.get().getCoins();
    }


    public int getCoinsCollected() {
        return coinsCollected;
    }

    public void setCoinsCollected(int coinsCollected) {
        this.coinsCollected = coinsCollected;
    }

    public int getTopScore() {
        return topScore;
    }

    public void setTopScore(int topScore) {
        this.topScore = topScore;
    }

    public JSONObject getFriend(int index) {
        JSONObject friend = null;
        if (friends != null && friends.length() > index) {
            friend = friends.optJSONObject(index);
        }
        return friend;
    }


    public void setFriends(JSONArray friends) {
        this.friends = friends;
    }

    public String getLastFriendSmashedID() {
        return lastFriendSmashedID;
    }

    public void setLastFriendSmashedID(String lastFriendSmashedID) {
        this.lastFriendSmashedID = lastFriendSmashedID;
    }

    public String getLastFriendSmashedName() {
        return lastFriendSmashedName;
    }

    public void setLastFriendSmashedName(String lastFriendSmashedName) {
        this.lastFriendSmashedName = lastFriendSmashedName;
    }

}
