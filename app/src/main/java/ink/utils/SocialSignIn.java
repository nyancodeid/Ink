package ink.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import ink.callbacks.GeneralCallback;

/**
 * Created by USER on 2016-08-07.
 */
public class SocialSignIn {

    private GoogleApiClient mGoogleApiClient;
    private static final SocialSignIn socialSignIn = new SocialSignIn();

    public void googleSignIn(Activity context, int requestCode) {
        // Configure sign-in to request the user's ID, email address, and basic profile. ID and
// basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build();

// Build a GoogleApiClient with access to SocialSignIn.API and the options above.
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        mGoogleApiClient.connect();
        context.startActivityForResult(signInIntent, requestCode);

    }


    public GoogleApiClient socialGooglePlusSignIn(Activity context, int requestCode) {
        // Configure sign-in to request the user's ID, email address, and basic profile. ID and
// basic profile are included in DEFAULT_SIGN_IN.

// Build a GoogleApiClient with access to SocialSignIn.API and the options above.
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Plus.API, null)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        mGoogleApiClient.connect();
        context.startActivityForResult(signInIntent, requestCode);
        return mGoogleApiClient;
    }

    public void facebookLogin(Context context, CallbackManager callbackManager,
                              @Nullable final GeneralCallback<Map<String, String>> resultCallback) {
        final Map<String, String> resultMap = new HashMap<>();

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        GraphRequest request = GraphRequest.newMeRequest(
                                loginResult.getAccessToken(),
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(
                                            JSONObject object,
                                            GraphResponse response) {
                                        Log.d("fsafsafsaf", "onCompleted: " + object.toString());
                                        String userLink = object.optString("link");
                                        String userName = object.optString("name");
                                        String email = object.optString("email");
                                        JSONObject jsonObject = object.optJSONObject("picture");
                                        JSONObject dataObject = jsonObject.optJSONObject("data");
                                        String imageUrl = dataObject.optString("url");

                                        resultMap.clear();
                                        resultMap.put("link", userLink);
                                        resultMap.put("name", userName);
                                        resultMap.put("email", email);
                                        resultMap.put("imageUrl", imageUrl);

                                        if (resultCallback != null) {
                                            resultCallback.onSuccess(resultMap);
                                        }

                                    }
                                });
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "link,name,picture.type(large),email");
                        request.setParameters(parameters);
                        request.executeAsync();

                    }

                    @Override
                    public void onCancel() {
                        resultCallback.onFailure(resultMap);
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        resultCallback.onFailure(resultMap);
                    }
                });
        LoginManager.getInstance().logInWithReadPermissions((Activity) context, Arrays.asList("public_profile", "email"));
    }

    public static SocialSignIn get() {
        return socialSignIn;
    }
}
