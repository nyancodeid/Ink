package ink.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by USER on 2016-06-20.
 */
public class SharedHelper {
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;

    public SharedHelper(Context context) {
        mSharedPreferences = context.getSharedPreferences("ink_session", Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
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

    public boolean shouldShowIntro() {
        return mSharedPreferences.contains("shouldShowIntro");
    }

    public void putShouldShowIntro(boolean value) {
        mEditor.putBoolean("shouldShowIntro", value);
        mEditor.commit();
    }

    public void putUserId(String userId) {
        mEditor.putString("user_id", userId);
        mEditor.commit();
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

    public void clean() {
        mEditor.clear();
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

    public void putToken(String token) {
        mEditor.putString("token", token);
        mEditor.commit();
    }

    public void putVkAccessToken(String value) {
        mEditor.putString("vkAccessToken", value);
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

    public void putLastNotificationId(String id) {
        mEditor.putString(id, "notification_id");
        mEditor.commit();
    }

    public boolean isEditorHintShown() {
        return mSharedPreferences.getBoolean("editorHint", false);
    }

    public void putEditorHintShow(boolean value) {
        mEditor.putBoolean("editorHint", value);
        mEditor.commit();
    }

    public String getNotificationId(String id) {
        return mSharedPreferences.getString(id, null);
    }

    public void removeNotificationId(String id) {
        mEditor.remove(id);
        mEditor.commit();
    }
}
