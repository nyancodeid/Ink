package ink.friendsmash;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.activities.BaseActivity;
import ink.callbacks.GeneralCallback;
import ink.utils.Constants;
import ink.utils.DimDialog;
import ink.utils.SharedHelper;
import ink.utils.SocialSignIn;

public class FriendSmashLoginView extends BaseActivity {

    @Bind(R.id.singInWithGoogle)
    RelativeLayout singInWithGoogle;
    @Bind(R.id.googleAccountGameText)
    TextView googleAccountGameText;
    private GoogleApiClient mGoogleApiClient;
    private static final int GOOGLE_ERROR_RESOLUTION_RESULT = 25552;
    private SharedHelper sharedHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_smash_login_view);
        ButterKnife.bind(this);
        sharedHelper = new SharedHelper(this);
        if (sharedHelper.isLoggedIntoGame()) {
            googleAccountGameText.setText(getString(R.string.continueWithGoogle));
        }
    }


    @OnClick(R.id.singInWithGoogle)
    public void singInWithGoogle() {
        DimDialog.showDimDialog(this, getString(R.string.fetchingFriends));
        mGoogleApiClient = SocialSignIn.get().getGooglePlusCircles(FriendSmashLoginView.this, GOOGLE_ERROR_RESOLUTION_RESULT, new GeneralCallback<JSONArray>() {
            @Override
            public void onSuccess(JSONArray jsonArray) {
                loginUser(jsonArray);
            }

            @Override
            public void onFailure(JSONArray jsonArray) {

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void loginUser(JSONArray friendsArray) {
        FriendSmashHelper.get().setFriends(friendsArray);
        Intent intent = new Intent(getApplicationContext(), FriendSmashHomeView.class);
        if (DimDialog.isDialogAlive()) {
            DimDialog.hideDialog();
        }
        sharedHelper.putLoggedIntoGame(true);
        startActivity(intent);
        finish();
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
                        status.startResolutionForResult(FriendSmashLoginView.this, GOOGLE_ERROR_RESOLUTION_RESULT);
                    } catch (IntentSender.SendIntentException e) {
                        mGoogleApiClient.connect();
                    }
                }
                Toast.makeText(FriendSmashLoginView.this, getString(R.string.error_retriving_circles), Toast.LENGTH_SHORT).show();
            }

        });

    }
}
