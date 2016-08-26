package ink.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallbacks;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

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

    public synchronized GoogleApiClient buildGoogleApiClient(final Activity activity) {
        GoogleSignInOptions gGoogleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(Scopes.PROFILE))
                .requestScopes(new Scope(Scopes.PLUS_LOGIN))
                .requestProfile()
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(activity)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gGoogleSignInOptions)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        Plus.PeopleApi.loadVisible(mGoogleApiClient, "me").setResultCallback(new ResultCallbacks<People.LoadPeopleResult>() {
                            @Override
                            public void onSuccess(@NonNull People.LoadPeopleResult loadPeopleResult) {
                                Toast.makeText(activity, "", Toast.LENGTH_SHORT).show();
                                Person person = loadPeopleResult.getPersonBuffer().get(0);
                                Log.d("fasfafasfsafasfas", "onSuccess: " + person);
                            }

                            @Override
                            public void onFailure(@NonNull Status status) {
                                if (status.hasResolution()) {
                                    try {
                                        // !!!
                                        status.startResolutionForResult(activity, 100);
                                    } catch (IntentSender.SendIntentException e) {
                                        mGoogleApiClient.connect();
                                    }
                                }
                                Log.d("fasfafasfsafasfas", "onFailure: " + status);
                            }

                        });

                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Toast.makeText(activity, "fail", Toast.LENGTH_SHORT).show();
                    }
                })
                .addApi(Plus.API)
                .build();
        mGoogleApiClient.connect(GoogleApiClient.SIGN_IN_MODE_OPTIONAL);
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
