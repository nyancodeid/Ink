package kashmirr.social.service;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import kashmirr.social.utils.SharedHelper;

/**
 * Created by USER on 2016-06-25.
 */
public class FirebaseInstanceService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseIIDService";
    private SharedHelper mSharedHelper;

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {

        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "onTokenRefresh: " + refreshedToken);
        mSharedHelper = new SharedHelper(this);
        mSharedHelper.putToken(refreshedToken);
        mSharedHelper.setTokenRefreshed(true);
        sendRegistrationToServer(refreshedToken);
    }
    // [END refresh_token]

    /**
     * Persist token to third-party servers.
     * <p>
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        // Add custom implementation, as needed.
    }

}