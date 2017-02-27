package ink.va.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.ink.va.R;

import java.util.Map;

import static ink.va.utils.Constants.SERVER_NOTIFICATION_SHARED_KEY;
import static ink.va.utils.Constants.SHOW_SERVER_NEWS_START_UP_KEY;


/**
 * Created by USER on 2016-06-20.
 */
public class SharedHelper {
    private static final String SHOW_SHIMMER = "show_shimmer_key";
    private static final String QB_USER_ID = "qb_user_id";
    private static final String QB_USER_LOGIN = "qb_user_login";
    private static final String QB_USER_PASSWORD = "qb_user_password";
    private static final String QB_USER_FULL_NAME = "qb_user_full_name";
    private static final String QB_USER_TAGS = "qb_user_tags";

    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;
    private Context context;
    private static SharedHelper instance;

    public SharedHelper(Context context) {
        instance = this;
        mSharedPreferences = context.getSharedPreferences("ink_session", Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
        this.context = context;
    }


    public boolean hasImage() {
        return mSharedPreferences.getString("imageLink", "").isEmpty() ? false : true;
    }

    public String getImageLink() {
        return mSharedPreferences.getString("imageLink", "");
    }

    public void putImageLink(String imageLink) {
        mEditor.putString("imageLink", imageLink);
        mEditor.commit();
    }


    public String getLastNotificationId(String notificationId) {
        return mSharedPreferences.getString(notificationId, null);
    }

    public int getLastNotificationCount(String notificationId) {
        return mSharedPreferences.getInt("notificationCount" + notificationId, 1);
    }

    public boolean hasQbUser() {
        return has(QB_USER_LOGIN);
    }


    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) mSharedPreferences.getAll().get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, T defValue) {
        T returnValue = (T) mSharedPreferences.getAll().get(key);
        return returnValue == null ? defValue : returnValue;
    }

    public boolean has(String key) {
        return mSharedPreferences.contains(key);
    }

    public void save(String key, Object value) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        } else if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        } else if (value instanceof Float) {
            editor.putFloat(key, (Float) value);
        } else if (value instanceof Long) {
            editor.putLong(key, (Long) value);
        } else if (value instanceof String) {
            editor.putString(key, (String) value);
        } else if (value instanceof Enum) {
            editor.putString(key, value.toString());
        } else if (value != null) {
            throw new RuntimeException("Attempting to save non-supported preference");
        }

        editor.commit();
    }

    public void putLastNotificationCount(String id) {
        mEditor.putInt("notificationCount" + id, getLastNotificationCount(id) + 1);
        mEditor.commit();
        putLastNotificationId(id);
    }

    public void putLastNotificationId(String id) {
        mEditor.putString(id, id);
        mEditor.commit();
    }

    public void removeLastNotificationId(String notificationId) {
        mEditor.remove(notificationId);
        mEditor.remove("notificationCount" + notificationId);
        mEditor.commit();
    }

    public boolean shouldShowIntro() {
        return mSharedPreferences.contains("shouldShowIntro");
    }

    public void putShouldShowIntro(boolean value) {
        mEditor.putBoolean("shouldShowIntro", value);
        mEditor.commit();
    }

    public void putShowCommentNotification(boolean value) {
        mEditor.putBoolean("commentNotification", value);
        mEditor.commit();
    }

    public boolean showCommentNotification() {
        return mSharedPreferences.getBoolean("commentNotification", true);
    }

    public void putShowGroupNotification(boolean value) {
        mEditor.putBoolean("showGroupNotification", value);
        mEditor.commit();
    }

    public boolean showGroupNotification() {
        return mSharedPreferences.getBoolean("showGroupNotification", true);
    }

    public void putShowLikeNotification(boolean value) {
        mEditor.putBoolean("showLikeNotification", value);
        mEditor.commit();
    }

    public boolean showLikeNotification() {
        return mSharedPreferences.getBoolean("showLikeNotification", true);
    }

    public boolean isRainbowMessageActivated() {
        return mSharedPreferences.getBoolean("isRainbowModeActivated", false);
    }

    public void putRainbowMessageActivated(boolean value) {
        mEditor.putBoolean("isRainbowModeActivated", value);
        mEditor.commit();
    }

    public void putSecurityQuestionSet(boolean value) {
        mEditor.putBoolean("securityWarning", value);
        mEditor.commit();
    }

    public boolean isSecurityQuestionSet() {
        return mSharedPreferences.getBoolean("securityWarning", false);
    }

    public void putUserId(String userId) {
        mEditor.putString("user_id", userId);
        mEditor.commit();
    }

    public void putShowSnow(boolean value) {
        mEditor.putBoolean("showSnow", value);
        mEditor.commit();
    }

    public boolean showSnow() {
        return mSharedPreferences.getBoolean("showSnow", true);
    }

    public String getUserId() {
        return mSharedPreferences.getString("user_id", null);
    }

    public void putLogin(String login) {
        mEditor.putString("login", login);
        mEditor.commit();
    }

    public String getLogin() {
        return mSharedPreferences.getString("login", null);
    }

    public String getUserPassword() {
        return mSharedPreferences.getString("password", null);
    }

    public void setPassword(String value) {
        mEditor.putString("password", value);
        mEditor.commit();
    }

    public void clean() {
        mEditor.clear();
        mEditor.commit();
    }


    public String getLastSessionUserId() {
        return mSharedPreferences.getString("lastSessionUserId", null);
    }

    public void removeLastSessionUserId() {
        mEditor.remove("lastSessionUserId");
        mEditor.commit();
    }

    public boolean shouldShowShowCase() {
        return mSharedPreferences.getBoolean("shouldShowShowCase", true);
    }

    public void setShouldShowShowCase(boolean value) {
        mEditor.putBoolean("shouldShowShowCase", value);
        mEditor.commit();
    }

    public boolean isLoggedIn() {
        return mSharedPreferences.contains("user_id");
    }

    public boolean isUserProfileCached() {
        return mSharedPreferences.contains("userStatus");
    }

    public String getUserStatus() {
        return mSharedPreferences.getString("userStatus", context.getString(R.string.noStatusText));
    }

    public void putUserStatus(String value) {
        mEditor.putString("userStatus", value);
        mEditor.commit();
    }

    public String getUserAddress() {
        return mSharedPreferences.getString("userAddress", context.getString(R.string.noAddress));
    }

    public void putUserAddress(String value) {
        mEditor.putString("userAddress", value);
        mEditor.commit();
    }

    public String getUserPhoneNumber() {
        return mSharedPreferences.getString("userPhoneNumber", context.getString(R.string.noPhone));
    }

    public void putUserPhoneNumber(String value) {
        mEditor.putString("userPhoneNumber", value);
        mEditor.commit();
    }

    public String getUserRelationship() {
        return mSharedPreferences.getString("userRelationship", context.getString(R.string.noRelationship));
    }

    public void putUserRelationship(String value) {
        mEditor.putString("userRelationship", value);
        mEditor.commit();
    }

    public String getUserGender() {
        return mSharedPreferences.getString("userGender", context.getString(R.string.noGender));
    }

    public void putUserGender(String value) {
        mEditor.putString("userGender", value);
        mEditor.commit();
    }

    public String getUserFacebookName() {
        return mSharedPreferences.getString("userFacebookName", context.getString(R.string.noFacebook));
    }

    public void putUserFacebookName(String value) {
        mEditor.putString("userFacebookName", value);
        mEditor.commit();
    }


    public String getUserFacebookLink() {
        return mSharedPreferences.getString("userFacebookLink", context.getString(R.string.noFacebook));
    }

    public void putUserFacebookLink(String value) {
        mEditor.putString("userFacebookLink", value);
        mEditor.commit();
    }

    public String getUserSkype() {
        return mSharedPreferences.getString("userSkype", context.getString(R.string.noSkype));
    }

    public void putUserSkype(String value) {
        mEditor.putString("userSkype", value);
        mEditor.commit();
    }

    public void putToken(String token) {
        mEditor.putString("token", token);
        mEditor.commit();
    }

    public void putVkAccessToken(String value) {
        mEditor.putString("vkAccessToken", value);
        mEditor.commit();
    }

    public boolean shouldLoadImage() {
        return mSharedPreferences.getBoolean("shouldLoadImage", true);
    }

    public void putShouldLoadImage(boolean value) {
        mEditor.putBoolean("shouldLoadImage", value);
        mEditor.commit();
    }

    public String getVkAccessToken() {
        return mSharedPreferences.getString("vkAccessToken", "noAccessToken");
    }

    public void setTokenRefreshed(boolean isRefreshed) {
        mEditor.putBoolean("refreshed", isRefreshed);
        mEditor.commit();
    }

    public void putIsRegistered(boolean value) {
        mEditor.putBoolean("isRegistered", value);
        mEditor.commit();
    }

    public boolean isRegistered() {
        return mSharedPreferences.getBoolean("isRegistered", false);
    }

    public void putIsSocialAccount(boolean value) {
        mEditor.putBoolean("isSocialAccount", value);
        mEditor.commit();
    }

    public void putIsAccountRecoverable(boolean value) {
        mEditor.putBoolean("isAccountRecoverable", value);
        mEditor.commit();
    }

    public boolean isAccountRecoverable() {
        return mSharedPreferences.getBoolean("isAccountRecoverable", false);
    }

    public boolean isSocialAccount() {
        return mSharedPreferences.getBoolean("isSocialAccount", false);
    }

    public boolean isTokenRefreshed() {
        return mSharedPreferences.getBoolean("refreshed", true);
    }

    public void shouldUpdateToken(boolean shouldUpdate) {
        mEditor.putBoolean("shouldUpdate", shouldUpdate);
        mEditor.commit();
    }

    public boolean shouldUpdateToken() {
        return mSharedPreferences.getBoolean("shouldUpdate", false);
    }

    public void setMessagesDownloaded() {
        mEditor.putBoolean("isMessagesDownloaded", true);
        mEditor.commit();
    }

    public boolean isMessagesDownloaded() {
        return mSharedPreferences.contains("isMessagesDownloaded");
    }


    public boolean showComments() {
        return !mSharedPreferences.getString("postId", "").isEmpty();
    }

    public String getPostId() {
        return mSharedPreferences.getString("postId", "");
    }

    public void putPostId(String value) {
        mEditor.putString("postId", value);
        mEditor.commit();
    }

    public String getToken() {
        return mSharedPreferences.getString("token", "no token");
    }

    public int getUniqueId() {
        return mSharedPreferences.getInt("uniqueId", 0);
    }

    public void putUniqueId(int id) {
        mEditor.putInt("uniqueId", id);
        mEditor.commit();
    }

    public void putFirstName(String fistName) {
        mEditor.putString("firstName", fistName);
        mEditor.commit();
    }

    public String getFirstName() {
        return mSharedPreferences.getString("firstName", "");
    }

    public void putLastName(String lastName) {
        mEditor.putString("lastName", lastName);
        mEditor.commit();
    }

    public boolean isDeviceWarned() {
        return mSharedPreferences.getBoolean("isWarned", false);
    }

    public void putWarned(boolean isWarned) {
        mEditor.putBoolean("isWarned", isWarned);
        mEditor.commit();
    }

    public String getLastName() {
        return mSharedPreferences.getString("lastName", "");
    }


    public boolean isEditorHintShown() {
        return mSharedPreferences.getBoolean("editorHint", false);
    }


    public void putEditorHintShow(boolean value) {
        mEditor.putBoolean("editorHint", value);
        mEditor.commit();
    }

    public boolean isAnimationHintShown() {
        return mSharedPreferences.getBoolean("isAnimationHintShown", false);
    }

    public void putAnimationHintShow(boolean value) {
        mEditor.putBoolean("isAnimationHintShown", value);
        mEditor.commit();
    }

    public void putLoggedIntoGame(boolean value) {
        mEditor.putBoolean("isLoggedIntoGame", value);
        mEditor.commit();
    }

    public String getOwnBubbleColor() {
        return mSharedPreferences.getString("ownBubbleColor", null);
    }

    public String getOpponentBubbleColor() {
        return mSharedPreferences.getString("opponentBubbleColor", null);
    }

    public String getActionBarColor() {
        return mSharedPreferences.getString("actionBarColor", null);
    }

    public String getMenuButtonColor() {
        return mSharedPreferences.getString("menuButtonColor", null);
    }

    public String getNotificationIconColor() {
        return mSharedPreferences.getString("notificationIconColor", null);
    }

    public String getShopIconColor() {
        return mSharedPreferences.getString("shopIconColor", null);
    }

    public String getLeftSlidingPanelHeaderColor() {
        return mSharedPreferences.getString("leftSlidingPanelHeaderColor", null);
    }

    public String getFeedColor() {
        return mSharedPreferences.getString("feedColor", null);
    }

    public String getSendButtonColor() {
        return mSharedPreferences.getString("sendButtonColor", null);
    }

    public String getStatusBarColor() {
        return mSharedPreferences.getString("statusBarColor", null);
    }

    public boolean hasPendingCustomizationsToSave() {
        return mSharedPreferences.getBoolean("hasCustomizationsToSave", false);
    }

    public String getHamburgerColor() {
        return mSharedPreferences.getString("hamburgerColor", null);
    }

    public String getChatFieldTextColor() {
        return mSharedPreferences.getString("chatFieldTextColor", null);
    }

    public String getOpponentTextColor() {
        return mSharedPreferences.getString("opponentTextColor", null);
    }

    public String getOwnTextColor() {
        return mSharedPreferences.getString("ownTextColor", null);
    }

    public String getFriendsColor() {
        return mSharedPreferences.getString("friendsColor", null);
    }

    public String getMessagesColor() {
        return mSharedPreferences.getString("messagesColor", null);
    }

    public String getChatColor() {
        return mSharedPreferences.getString("chatColor", null);
    }

    public String getTrendColor() {
        return mSharedPreferences.getString("trendColor", null);
    }

    public String getOpponentProfileColor() {
        return mSharedPreferences.getString("putOpponentColor", null);
    }

    public String getMyRequestColor() {
        return mSharedPreferences.getString("myRequestColor", null);
    }

    public void putOpponentBubbleColor(String value) {
        if (value == null || value.isEmpty()) {
            mEditor.remove("opponentBubbleColor");
            mEditor.commit();
        } else {
            mEditor.putString("opponentBubbleColor", value);
            mEditor.commit();
        }

    }

    public void putOwnBubbleColor(String value) {
        if (value == null || value.isEmpty()) {
            mEditor.remove("ownBubbleColor");
            mEditor.commit();
        } else {
            mEditor.putString("ownBubbleColor", value);
            mEditor.commit();
        }
    }

    public void putActionBarColor(String value) {
        if (value == null || value.isEmpty()) {
            mEditor.remove("actionBarColor");
            mEditor.commit();
        } else {
            mEditor.putString("actionBarColor", value);
            mEditor.commit();
        }
    }

    public void putMenuButtonColor(String value) {
        if (value == null || value.isEmpty()) {
            mEditor.remove("menuButtonColor");
            mEditor.commit();
        } else {
            mEditor.putString("menuButtonColor", value);
            mEditor.commit();
        }
    }


    public void putNotificationIconColor(String value) {
        if (value == null || value.isEmpty()) {
            mEditor.remove("notificationIconColor");
            mEditor.commit();
        } else {
            mEditor.putString("notificationIconColor", value);
            mEditor.commit();
        }
    }


    public void putShopIconColor(String value) {
        if (value == null || value.isEmpty()) {
            mEditor.remove("shopIconColor");
            mEditor.commit();
        } else {
            mEditor.putString("shopIconColor", value);
            mEditor.commit();
        }
    }

    public void putLeftSlidingPanelColor(String value) {
        if (value == null || value.isEmpty()) {
            mEditor.remove("leftSlidingPanelHeaderColor");
            mEditor.commit();
        } else {
            mEditor.putString("leftSlidingPanelHeaderColor", value);
            mEditor.commit();
        }
    }

    public void putFeedColor(String value) {
        if (value == null || value.isEmpty()) {
            mEditor.remove("feedColor");
            mEditor.commit();
        } else {
            mEditor.putString("feedColor", value);
            mEditor.commit();
        }
    }

    public void putHamburgerColor(String value) {
        if (value == null || value.isEmpty()) {
            mEditor.remove("hamburgerColor");
            mEditor.commit();
        } else {
            mEditor.putString("hamburgerColor", value);
            mEditor.commit();
        }
    }

    public void putFriendsColor(String value) {
        if (value == null || value.isEmpty()) {
            mEditor.remove("friendsColor");
            mEditor.commit();
        } else {
            mEditor.putString("friendsColor", value);
            mEditor.commit();
        }
    }

    public void putMessagesColor(String value) {
        if (value == null || value.isEmpty()) {
            mEditor.remove("messagesColor");
            mEditor.commit();
        } else {
            mEditor.putString("messagesColor", value);
            mEditor.commit();
        }
    }

    public void putSendButtonColor(String value) {
        if (value == null || value.isEmpty()) {
            mEditor.remove("sendButtonColor");
            mEditor.commit();
        } else {
            mEditor.putString("sendButtonColor", value);
            mEditor.commit();
        }
    }

    public void putHasPendingCustomizationsToSave(boolean value) {
        mEditor.putBoolean("hasCustomizationsToSave", value);
        mEditor.commit();
    }

    public void putChatColor(String value) {
        if (value == null || value.isEmpty()) {
            mEditor.remove("chatColor");
            mEditor.commit();
        } else {
            mEditor.putString("chatColor", value);
            mEditor.commit();
        }
    }


    public void putTrendColor(String value) {
        if (value == null || value.isEmpty()) {
            mEditor.remove("trendColor");
            mEditor.commit();
        } else {
            mEditor.putString("trendColor", value);
            mEditor.commit();
        }
    }

    public void putMyRequestColor(String value) {
        if (value == null || value.isEmpty()) {
            mEditor.remove("myRequestColor");
            mEditor.commit();
        } else {
            mEditor.putString("myRequestColor", value);
            mEditor.commit();
        }
    }


    public void putStatusBarColor(String value) {
        if (value == null || value.isEmpty()) {
            mEditor.remove("statusBarColor");
            mEditor.commit();
        } else {
            mEditor.putString("statusBarColor", value);
            mEditor.commit();
        }
    }

    public void putOpponentColor(String value) {
        if (value == null || value.isEmpty()) {
            mEditor.remove("putOpponentColor");
            mEditor.commit();
        } else {
            mEditor.putString("putOpponentColor", value);
            mEditor.commit();
        }
    }


    public void putOwnTextColor(String value) {
        if (value == null || value.isEmpty()) {
            mEditor.remove("ownTextColor");
            mEditor.commit();
        } else {
            mEditor.putString("ownTextColor", value);
            mEditor.commit();
        }
    }


    public void putOpponentTextColor(String value) {
        if (value == null || value.isEmpty()) {
            mEditor.remove("opponentTextColor");
            mEditor.commit();
        } else {
            mEditor.putString("opponentTextColor", value);
            mEditor.commit();
        }
    }


    public void putChatFieldTextColor(String value) {
        if (value == null || value.isEmpty()) {
            mEditor.remove("chatFieldTextColor");
            mEditor.commit();
        } else {
            mEditor.putString("chatFieldTextColor", value);
            mEditor.commit();
        }
    }

    public void resetCustomization() {
        mEditor.remove("opponentBubbleColor");
        mEditor.remove("ownBubbleColor");
        mEditor.remove("statusBarColor");
        mEditor.remove("menuButtonColor");
        mEditor.remove("notificationIconColor");
        mEditor.remove("shopIconColor");
        mEditor.remove("leftSlidingPanelHeaderColor");
        mEditor.remove("feedColor");
        mEditor.remove("friendsColor");
        mEditor.remove("messagesColor");
        mEditor.remove("chatColor");
        mEditor.remove("myRequestColor");
        mEditor.remove("hamburgerColor");
        mEditor.remove("actionBarColor");
        mEditor.remove("sendButtonColor");
        mEditor.remove("chatFieldTextColor");
        mEditor.remove("opponentTextColor");
        mEditor.remove("ownTextColor");
        mEditor.remove("trendColor");
        mEditor.remove("putOpponentColor");
        mEditor.commit();

    }

    public boolean isLoggedIntoGame() {
        return mSharedPreferences.getBoolean("isLoggedIntoGame", false);
    }

    public void putMessagesCount(int value) {
        mEditor.putInt("messagesCount", value);
        mEditor.commit();
    }

    public int getMessagesCount() {
        return mSharedPreferences.getInt("messagesCount", 0);
    }

    public boolean hasShownServerNews(String newsId) {
        return mSharedPreferences.getBoolean(SERVER_NOTIFICATION_SHARED_KEY + newsId, false);
    }

    public void putShownServerNews(String newsId) {
        mEditor.putBoolean(SERVER_NOTIFICATION_SHARED_KEY + newsId, true);
        mEditor.commit();
    }

    public Map getAllSharedPrefs() {
        return mSharedPreferences.getAll();
    }

    public void removeObject(String key) {
        mEditor.remove(key);
        mEditor.commit();
    }

    public boolean showServerNewsOnStartup() {
        return mSharedPreferences.getBoolean(SHOW_SERVER_NEWS_START_UP_KEY, true);
    }

    public void putServerNewsOnStartup(boolean value) {
        mEditor.putBoolean(SHOW_SERVER_NEWS_START_UP_KEY, value);
        mEditor.commit();
    }

    public boolean hasAnySecurityAttached() {
        return mSharedPreferences.getBoolean("fingerprintAttached", false) || mSharedPreferences.getBoolean("pinAttached", false);
    }

    public void putFingerPrintAttached(boolean value) {
        mEditor.putBoolean("fingerprintAttached", value);
        mEditor.commit();
    }

    public void putPinAttached(boolean value) {
        mEditor.putBoolean("pinAttached", value);
        mEditor.commit();
    }
}
