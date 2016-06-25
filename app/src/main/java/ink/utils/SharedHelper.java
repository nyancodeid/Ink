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
        return mSharedPreferences.contains("imageLink");
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

    public boolean isLoggedIn() {
        return mSharedPreferences.contains("user_id");
    }

    public void putToken(String token) {
        mEditor.putString("token", token);
        mEditor.commit();
    }

    public void shouldUpdateToken(boolean shouldUpdate) {
        mEditor.putBoolean("shouldUpdate", shouldUpdate);
        mEditor.commit();
    }

    public boolean shouldUpdateToken() {
        return mSharedPreferences.getBoolean("shouldUpdate", false);
    }

    public String getToken() {
        return mSharedPreferences.getString("token", "no token");
    }
}
