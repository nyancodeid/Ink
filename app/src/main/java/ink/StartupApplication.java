package ink;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.adobe.creativesdk.foundation.AdobeCSDKFoundation;
import com.adobe.creativesdk.foundation.auth.IAdobeAuthClientCredentials;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.ink.R;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKAccessTokenTracker;
import com.vk.sdk.VKSdk;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import ink.friendsmash.ScoreboardEntry;
import ink.utils.RealmHelper;
import ink.utils.SharedHelper;
import ink.utils.User;

/**
 * Created by USER on 2016-06-26.
 */
public class StartupApplication extends MultiDexApplication implements IAdobeAuthClientCredentials {
    private static final String CREATIVE_SDK_CLIENT_ID = "2b5c43dc4f6d4f79a7d353433a972a4b";
    private static final String CREATIVE_SDK_CLIENT_SECRET = "11b233bb-98d2-45d3-8c80-2be66726f10b";
    private SharedHelper sharedHelper;

    // Tag used when logging all messages with the same tag (e.g. for demoing purposes)
    public static final String TAG = "FriendSmash";

    public static int NEW_USER_BOMBS = 5;
    public static int NEW_USER_COINS = 100;
    public static int NUM_BOMBS_ALLOWED_IN_GAME = 3;
    public static int NUM_COINS_PER_BOMB = 5;
    private int score = 0;
    private int bombs = 0;
    private int coins = 0;
    private int coinsCollected = 0;
    private int topScore = 0;

    private boolean loggedIn = false;
    public static final String LOGGED_IN_KEY = "logged_in";

    private JSONObject currentFBUser;
    public static final String CURRENT_FB_USER_KEY = "current_fb_user";

    private JSONArray friends;

    public static final String FRIENDS_KEY = "friends";

    private String lastFriendSmashedID = null;

    private String lastFriendSmashedName = null;

    private boolean hasDeniedFriendPermission = false;

    private ArrayList<ScoreboardEntry> scoreboardEntriesList = null;

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

    public void setCoins(int coins) {
        this.coins = coins;
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

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
        if (!loggedIn) {
            setScore(0);
            setCurrentFBUser(null);
            setFriends(null);
            setLastFriendSmashedID(null);
            setScoreboardEntriesList(null);
        }
    }

    public JSONObject getCurrentFBUser() {
        return currentFBUser;
    }

    public void setCurrentFBUser(JSONObject currentFBUser) {
        this.currentFBUser = currentFBUser;
    }

    public JSONArray getFriends() {
        return friends;
    }

    public ArrayList<String> getFriendsAsArrayListOfStrings() {
        ArrayList<String> friendsAsArrayListOfStrings = new ArrayList<String>();

        int numFriends = friends.length();
        for (int i = 0; i < numFriends; i++) {
            friendsAsArrayListOfStrings.add(getFriend(i).toString());
        }

        return friendsAsArrayListOfStrings;
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

    public boolean hasDeniedFriendPermission() {
        return hasDeniedFriendPermission;
    }

    public void setHasDeniedFriendPermission(boolean hasDeniedFriendPermission) {
        this.hasDeniedFriendPermission = hasDeniedFriendPermission;
    }

    public ArrayList<ScoreboardEntry> getScoreboardEntriesList() {
        return scoreboardEntriesList;
    }

    public void setScoreboardEntriesList(ArrayList<ScoreboardEntry> scoreboardEntriesList) {
        this.scoreboardEntriesList = scoreboardEntriesList;
    }

    public String getFBAppID() {
        return getString(R.string.facebook_app_id);
    }

    public void saveInventory() {
        // TODO: 2016-08-25  save to ivnentory
        getBombs();
        getCoins();

    }

    public void loadInventory() {
        setBombs(1);
        setCoins(User.get().getCoins());

    }


    @Override
    public void onCreate() {
        RealmHelper.getInstance().initRealm(getApplicationContext());
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        sharedHelper = new SharedHelper(this);
        AdobeCSDKFoundation.initializeCSDKFoundation(getApplicationContext());
        VKAccessTokenTracker vkAccessTokenTracker = new VKAccessTokenTracker() {
            @Override
            public void onVKAccessTokenChanged(@Nullable VKAccessToken oldToken, @Nullable VKAccessToken newToken) {
                sharedHelper.putVkAccessToken(newToken.accessToken);
            }
        };
        vkAccessTokenTracker.startTracking();
        VKSdk.initialize(this);
        super.onCreate();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        MultiDex.install(this);
    }


    @Override
    public String getClientID() {
        return CREATIVE_SDK_CLIENT_ID;
    }

    @Override
    public String getClientSecret() {
        return CREATIVE_SDK_CLIENT_SECRET;
    }
}
