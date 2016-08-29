/**
 * Copyright (c) 2014-present, Facebook, Inc. All rights reserved.
 * <p/>
 * You are hereby granted a non-exclusive, worldwide, royalty-free license to use,
 * copy, modify, and distribute this software in source code or binary form for use
 * in connection with the web services and APIs provided by Facebook.
 * <p/>
 * As with any software that integrates with the Facebook platform, your use of
 * this software is subject to the Facebook Developer Principles and Policies
 * [http://developers.facebook.com/policy/]. This copyright notice shall be
 * included in all copies or substantial portions of the software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package ink.friendsmash;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallbacks;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.ink.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ink.callbacks.GeneralCallback;
import ink.utils.Constants;
import ink.utils.SharedHelper;
import ink.utils.SocialSignIn;

/**
 * Entry point for the app that represents the home screen with the Play button etc. and
 * also the login screen for the social version of the app - these screens will switch
 * within this activity using Fragments.
 */
public class HomeActivity extends FragmentActivity {
    private static final int FB_LOGGED_OUT_HOME = 0;
    private static final int HOME = 1;
    private static final int FRAGMENT_COUNT = HOME + 1;
    private Fragment[] fragments = new Fragment[FRAGMENT_COUNT];
    private static final int GOOGLE_ERROR_RESOLUTION_RESULT = 25552;

    private GoogleApiClient mGoogleApiClient;

    private RelativeLayout singInWithGoogle;
    private SharedHelper sharedHelper;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.home);
        sharedHelper = new SharedHelper(this);
        FragmentManager fm = getSupportFragmentManager();
        fragments[FB_LOGGED_OUT_HOME] = fm.findFragmentById(R.id.fbLoggedOutHomeFragment);
        fragments[HOME] = fm.findFragmentById(R.id.homeFragment);
        singInWithGoogle = (RelativeLayout) fragments[FB_LOGGED_OUT_HOME].getView().findViewById(R.id.singInWithGoogle);
        singInWithGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGoogleApiClient = SocialSignIn.get().buildGoogleApiClient(HomeActivity.this, GOOGLE_ERROR_RESOLUTION_RESULT, new GeneralCallback<JSONArray>() {
                    @Override
                    public void onSuccess(JSONArray jsonArray) {
                        loginUser(jsonArray);
                    }

                    @Override
                    public void onFailure(JSONArray jsonArray) {

                    }
                });
            }
        });
        FragmentTransaction transaction = fm.beginTransaction();
        for (int i = 0; i < fragments.length; i++) {
            transaction.hide(fragments[i]);
        }
        transaction.commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case GOOGLE_ERROR_RESOLUTION_RESULT:
                handleGoogleCircleResult();
                break;
        }
    }

    protected void onResumeFragments() {
        if (sharedHelper.isLoggedIntoGame()) {
            showFragment(HOME);
        } else {
            showFragment(FB_LOGGED_OUT_HOME);
        }


    }

    @Override
    public void onResume() {
        super.onResume();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void buyBombs() {

        if (FriendSmashHelper.get().getCoins() < (FriendSmashHelper.get().NUM_COINS_PER_BOMB)) {
            Toast.makeText(this, "Not enough coins.", Toast.LENGTH_LONG).show();
            return;
        }

        // TODO: 8/29/2016 set up bomb logic and save it to inventory
//        app.setBombs(app.getBombs() + 1);
//        app.setCoins(app.getCoins() - StartupApplication.NUM_COINS_PER_BOMB);
//
//        app.saveInventory();

        loadInventoryFragment();
    }

    private void showFragment(int fragmentIndex) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        for (int i = 0; i < fragments.length; i++) {
            if (i == fragmentIndex) {
                transaction.show(fragments[i]);
            } else {
                transaction.hide(fragments[i]);
            }
        }
        transaction.commit();

        switch (fragmentIndex) {
            case FB_LOGGED_OUT_HOME:
                if (fragments[FB_LOGGED_OUT_HOME] != null) {
                    // TODO: 8/26/2016  hide progress
                }
                // TODO: 8/29/2016  set user logged out

                break;
            case HOME:
                // TODO: 8/29/2016 set user logged in
                break;
        }
    }


    private void loginUser(JSONArray friendsArray) {
        FriendSmashHelper.get().setFriends(friendsArray);
        showFragment(HOME);

    }

    private void loadInventoryFragment() {
        ((HomeFragment) fragments[HOME]).loadInventory();
    }

    private void loadPersonalizedFragment() {
        ((HomeFragment) fragments[HOME]).personalizeHomeFragment();
        showFragment(HOME);
    }

    public void showError(String error) {
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
    }

    private void logout() {
        LoginManager.getInstance().logOut();
    }


    private void handleGoogleCircleResult() {
        Plus.PeopleApi.loadVisible(mGoogleApiClient, null
        ).setResultCallback(new ResultCallbacks<People.LoadPeopleResult>() {
            @Override
            public void onSuccess(@NonNull People.LoadPeopleResult loadPeopleResult) {
                int personCount = loadPeopleResult.getPersonBuffer().getCount();
                if (personCount != 0) {
                    JSONArray friendsArray = new JSONArray();
                    for (int i = 0; i < personCount; i++) {
                        Person eachPerson = loadPeopleResult.getPersonBuffer().get(i);
                        try {
                            JSONObject eachFriendObject = new JSONObject();
                            eachFriendObject.put("name", eachPerson.getDisplayName());
                            eachFriendObject.put("id", eachPerson.getId());
                            String imageUrl;
                            if (eachPerson.hasUrl()) {
                                imageUrl = eachPerson.getImage().getUrl().replaceAll("\\?sz=50", "");
                            } else {
                                imageUrl = Constants.MAIN_URL + Constants.USER_IMAGES_FOLDER + Constants.FUNNY_USER_IMAGE;
                            }
                            eachFriendObject.put("image", imageUrl);
                            friendsArray.put(eachFriendObject);
                            loginUser(friendsArray);
                            loadPeopleResult.getPersonBuffer().release();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    loadPeopleResult.getPersonBuffer().release();
                    Snackbar.make(singInWithGoogle, getString(R.string.noGoogleFriend), Snackbar.LENGTH_INDEFINITE).
                            setAction("OK", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                }
                            }).show();
                }

            }

            @Override
            public void onFailure(@NonNull Status status) {
                if (status.hasResolution()) {
                    try {
                        status.startResolutionForResult(HomeActivity.this, GOOGLE_ERROR_RESOLUTION_RESULT);
                    } catch (IntentSender.SendIntentException e) {
                        mGoogleApiClient.connect();
                    }
                }
                Toast.makeText(HomeActivity.this, getString(R.string.error_retriving_circles), Toast.LENGTH_SHORT).show();
            }

        });

    }

}
