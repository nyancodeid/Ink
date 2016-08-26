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
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.FacebookRequestError;
import com.facebook.GraphRequestBatch;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.ink.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import ink.StartupApplication;
import ink.friendsmash.integration.FacebookLogin;
import ink.friendsmash.integration.FacebookLoginPermission;
import ink.friendsmash.integration.FriendSmashEventsLogger;
import ink.friendsmash.integration.GameRequest;
import ink.friendsmash.integration.GraphAPICall;
import ink.friendsmash.integration.GraphAPICallback;

/**
 *  Entry point for the app that represents the home screen with the Play button etc. and
 *  also the login screen for the social version of the app - these screens will switch
 *  within this activity using Fragments.
 */
public class HomeActivity extends FragmentActivity {
    private static final int FB_LOGGED_OUT_HOME = 0;
    private static final int HOME = 1;
    private static final int FRAGMENT_COUNT = HOME + 1;
    private Fragment[] fragments = new Fragment[FRAGMENT_COUNT];

    private FriendSmashEventsLogger eventsLogger;

    public FriendSmashEventsLogger getEventsLogger() {
        return eventsLogger;
    }

    private FacebookLogin facebookLogin;

    public FacebookLogin getFacebookLogin() {
        return facebookLogin;
    }

    private GameRequest gameRequest;

    public GameRequest getGameRequest() {
        return gameRequest;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        facebookLogin = new FacebookLogin(this);
        facebookLogin.init();
        eventsLogger = new FriendSmashEventsLogger(getApplicationContext());
        gameRequest = new GameRequest(this);

        setContentView(R.layout.home);

        FragmentManager fm = getSupportFragmentManager();
        fragments[FB_LOGGED_OUT_HOME] = fm.findFragmentById(R.id.fbLoggedOutHomeFragment);
        fragments[HOME] = fm.findFragmentById(R.id.homeFragment);

        FragmentTransaction transaction = fm.beginTransaction();
        for (int i = 0; i < fragments.length; i++) {
            transaction.hide(fragments[i]);
        }
        transaction.commit();

        if (savedInstanceState != null) {
            StartupApplication app = (StartupApplication) getApplication();
            boolean loggedInState = savedInstanceState.getBoolean(StartupApplication.LOGGED_IN_KEY, false);
            app.setLoggedIn(loggedInState);

            if (app.isLoggedIn() && (app.getFriends() == null || app.getCurrentFBUser() == null)) {
                try {
                    String currentFBUserJSONString = savedInstanceState.getString(StartupApplication.CURRENT_FB_USER_KEY);
                    if (currentFBUserJSONString != null) {
                        JSONObject currentFBUser = new JSONObject(currentFBUserJSONString);
                        app.setCurrentFBUser(currentFBUser);
                    }

                    // friends
                    ArrayList<String> friendsJSONStringArrayList = savedInstanceState.getStringArrayList(StartupApplication.FRIENDS_KEY);
                    if (friendsJSONStringArrayList != null) {
                        JSONArray friends = new JSONArray();
                        Iterator<String> friendsJSONStringArrayListIterator = friendsJSONStringArrayList.iterator();
                        while (friendsJSONStringArrayListIterator.hasNext()) {
                            friends.put(new JSONObject(friendsJSONStringArrayListIterator.next()));
                        }
                        app.setFriends(friends);
                    }
                } catch (JSONException e) {
                    Log.e(StartupApplication.TAG, e.toString());
                }
            }
        } else if (FacebookLogin.isAccessTokenValid()) {
            fetchUserInformationAndLogin();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        facebookLogin.getCallbackManager().onActivityResult(requestCode, resultCode, data);
    }

    protected void onResumeFragments() {
        StartupApplication application = (StartupApplication) getApplication();
        if (FacebookLogin.isAccessTokenValid() && application.getCurrentFBUser() != null) {
            showFragment(HOME);
        } else {
            showFragment(FB_LOGGED_OUT_HOME);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        AppEventsLogger.activateApp(this);
        facebookLogin.activate();
    }

    @Override
    public void onPause() {
        super.onPause();
        AppEventsLogger.deactivateApp(this);
        facebookLogin.deactivate();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        StartupApplication app = (StartupApplication) getApplication();

        outState.putBoolean(StartupApplication.LOGGED_IN_KEY, app.isLoggedIn());

        if (((StartupApplication) getApplication()).getCurrentFBUser() != null) {
            outState.putString(StartupApplication.CURRENT_FB_USER_KEY, app.getCurrentFBUser().toString());
        }

        if (((StartupApplication) getApplication()).getFriends() != null) {
            outState.putStringArrayList(StartupApplication.FRIENDS_KEY, app.getFriendsAsArrayListOfStrings());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        facebookLogin.deactivate();
    }

    public void buyBombs() {
        StartupApplication app = (StartupApplication) getApplication();
        if (app.getCoins() < StartupApplication.NUM_COINS_PER_BOMB) {
            Toast.makeText(this, "Not enough coins.", Toast.LENGTH_LONG).show();
            return;
        }

        app.setBombs(app.getBombs() + 1);
        app.setCoins(app.getCoins() - StartupApplication.NUM_COINS_PER_BOMB);

        app.saveInventory();

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
                ((StartupApplication) getApplication()).setLoggedIn(false);
                break;
            case HOME:
                ((StartupApplication) getApplication()).setLoggedIn(true);
                break;
        }
    }

    public void onLoginStateChanged(AccessToken oldToken, AccessToken currentToken) {
        boolean isLoggedIn = ((StartupApplication) getApplication()).isLoggedIn();
        if (FacebookLogin.isAccessTokenValid() && !isLoggedIn && fragments[HOME] != null) {
            fetchUserInformationAndLogin();
        } else if (!FacebookLogin.isAccessTokenValid() && isLoggedIn && fragments[FB_LOGGED_OUT_HOME] != null) {
            logout();
            showFragment(FB_LOGGED_OUT_HOME);
        } else if (FacebookLogin.testTokenHasPermission(currentToken, FacebookLoginPermission.USER_FRIENDS) &&
                !FacebookLogin.testTokenHasPermission(oldToken, FacebookLoginPermission.USER_FRIENDS)) {
            ((HomeFragment) fragments[HOME]).onUserFriendsGranted();
        } else if (FacebookLogin.testTokenHasPermission(currentToken, FacebookLoginPermission.PUBLISH_ACTIONS) &&
                !FacebookLogin.testTokenHasPermission(oldToken, FacebookLoginPermission.PUBLISH_ACTIONS)) {
            ((HomeFragment) fragments[HOME]).onPublishActionsGranted();
        }
    }

    private void fetchUserInformationAndLogin() {
        if (FacebookLogin.isAccessTokenValid()) {
            if (fragments[FB_LOGGED_OUT_HOME] != null) {
                // TODO: 8/26/2016  hide progress
            }

            GraphAPICall myFriendsCall = GraphAPICall.callMeFriends("name,first_name", new GraphAPICallback() {
                @Override
                public void handleResponse(GraphResponse response) {
                    JSONArray friendsData = GraphAPICall.getDataFromResponse(response);
                    ((StartupApplication) getApplication()).setFriends(friendsData);
                }

                @Override
                public void handleError(FacebookRequestError error) {
                    showError(error.toString());
                }
            });

            GraphAPICall meCall = GraphAPICall.callMe("first_name", new GraphAPICallback() {
                @Override
                public void handleResponse(GraphResponse response) {
                    JSONObject user = response.getJSONObject();
                    ((StartupApplication) getApplication()).setCurrentFBUser(user);
                    //saveUserToParse();
                    // it causes strange behaviour with AccessTokenTracker
                }

                @Override
                public void handleError(FacebookRequestError error) {
                    showError(error.toString());
                }
            });

            GraphAPICall meScoresCall = GraphAPICall.callMeScores(new GraphAPICallback() {
                @Override
                public void handleResponse(GraphResponse response) {
                    JSONObject data = GraphAPICall.getDataFromResponse(response).optJSONObject(0);
                    if (data != null) {
                        int score = data.optInt("score");
                        ((StartupApplication) getApplication()).setTopScore(score);
                    }
                }

                @Override
                public void handleError(FacebookRequestError error) {
                    showError(error.toString());
                }
            });

            // Create a RequestBatch and add a callback once the batch of requests completes
            GraphRequestBatch requestBatch = GraphAPICall.createRequestBatch(myFriendsCall, meCall, meScoresCall);

            requestBatch.addCallback(new GraphRequestBatch.Callback() {
                @Override
                public void onBatchCompleted(GraphRequestBatch batch) {
                    if (((StartupApplication) getApplication()).getCurrentFBUser() != null) {
                        loadPersonalizedFragment();
                    } else {
                        showError(getString(R.string.error_fetching_profile));
                    }
                }
            });

            requestBatch.executeAsync();
        }
    }

    private void saveUserToParse() {
        loadInventoryFragment();
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
}
