package ink.va.activities;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatCheckBox;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.iid.FirebaseInstanceId;
import com.ink.va.R;
import com.linkedin.platform.APIHelper;
import com.linkedin.platform.LISessionManager;
import com.linkedin.platform.errors.LIApiError;
import com.linkedin.platform.errors.LIAuthError;
import com.linkedin.platform.listeners.ApiListener;
import com.linkedin.platform.listeners.ApiResponse;
import com.linkedin.platform.listeners.AuthListener;
import com.linkedin.platform.utils.Scope;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.va.callbacks.GeneralCallback;
import ink.va.utils.Constants;
import ink.va.utils.Retrofit;
import ink.va.utils.SharedHelper;
import ink.va.utils.SocialSignIn;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static ink.va.utils.Constants.PEOPLE_LINKEDIN_URL;

/**
 * A login screen that offers login via email/password.
 */
public class Login extends BaseActivity implements View.OnClickListener {

    public static final int GOOGLE_SIGN_IN_REQUEST_CODE = 1;
    // UI references.
    private AutoCompleteTextView mLoginView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private RelativeLayout mRegisterWrapper;
    private BroadcastReceiver mBroadcastReceiver;
    private SharedHelper mSharedHelper;
    private Button mLoginButton;
    private ProgressDialog progressDialog;
    private CallbackManager mCallbackManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this);
        VKSdk.initialize(getApplicationContext());
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        mCallbackManager = CallbackManager.Factory.create();
        // Set up the login form.
        mSharedHelper = new SharedHelper(this);
        FirebaseInstanceId.getInstance().getToken();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.logging));
        progressDialog.setProgressDrawable(ContextCompat.getDrawable(this, R.drawable.progress_dialog_circle));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.setIndeterminateDrawable(ContextCompat.getDrawable(this, R.drawable.progress_dialog_circle));
        progressDialog.setMessage(getString(R.string.loggingPleasWait));
        if (!checkPlayServices()) {

        }

        if (mSharedHelper.isLoggedIn()) {
            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
            finish();
        }
        mLoginView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mRegisterWrapper = (RelativeLayout) findViewById(R.id.anotherOption);
        mRegisterWrapper.setOnClickListener(this);
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                finish();
            }
        };
        registerReceiver(mBroadcastReceiver, new IntentFilter("Login"));
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mLoginButton = (Button) findViewById(R.id.signInButton);
        mLoginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mPasswordView.getText().toString().isEmpty() && !mLoginView.getText().toString().isEmpty()) {
                    attemptLogin();
                } else {
                    if (mPasswordView.getText().toString().isEmpty()) {
                        mPasswordView.setError(getString(R.string.emptyPasswordError));
                    }
                    if (mLoginView.getText().toString().isEmpty()) {
                        mLoginView.setError(getString(R.string.emptyLoginError));
                    }
                }
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.loading_progress);
    }


    private void openVkLogin() {
        VKSdk.login(this, vkScopes);
    }

    private static final String[] vkScopes = new String[]{
    };

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        disableButtons();
        // Reset errors.
        mLoginView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mLoginView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mLoginView.setError(getString(R.string.error_field_required));
            focusView = mLoginView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            enableButtons();
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            mProgressView.setVisibility(View.VISIBLE);
            proceedLogin();
        }
    }

    @OnClick(R.id.forgotPassword)
    public void forgotPassword() {
        startActivity(new Intent(getApplicationContext(), ForgotPassword.class));
    }

    private void proceedLogin() {
        final Call<ResponseBody> responseBodyCall = Retrofit.getInstance().getInkService().login(mLoginView.getText().toString().toString(),
                mPasswordView.getText().toString());
        responseBodyCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                enableButtons();
                if (response == null) {
                    proceedLogin();
                    return;
                }
                if (response.body() == null) {
                    proceedLogin();
                    return;
                }
                try {
                    try {
                        String responseString = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseString);
                        boolean success = jsonObject.optBoolean("success");
                        AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
                        if (!success) {
                            mProgressView.setVisibility(View.GONE);
                            builder.setTitle(getString(R.string.errorLogin));
                            builder.setMessage(getString(R.string.errorLoginMessage));
                            builder.setCancelable(false);
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();

                                }
                            });
                            builder.show();
                        } else {
                            mProgressView.setVisibility(View.GONE);
                            boolean banned = jsonObject.optBoolean("banned");
                            if (!banned) {
                                String userId = jsonObject.optString("user_id");
                                String securityQuestion = jsonObject.optString("securityQuestion");
                                mSharedHelper.putSecurityQuestionSet(securityQuestion != null && !securityQuestion.isEmpty());
                                mSharedHelper.putFirstName(jsonObject.optString("first_name"));
                                mSharedHelper.putIsAccountRecoverable(true);
                                mSharedHelper.putLastName(jsonObject.optString("last_name"));
                                mSharedHelper.putUserId(userId);
                                mSharedHelper.putShouldShowIntro(false);
                                mSharedHelper.putIsSocialAccount(false);
                                mSharedHelper.putIsAccountRecoverable(true);
                                String imageLink = jsonObject.optString("imageLink");
                                if (imageLink != null && !imageLink.isEmpty()) {
                                    mSharedHelper.putImageLink(imageLink);
                                }
                                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                                finish();
                            } else {
                                builder.setTitle(getString(R.string.ban_title));
                                builder.setMessage(getString(R.string.ban_message));
                                builder.setCancelable(false);
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                                builder.show();
                            }
                        }
                    } catch (JSONException e) {
                        attemptLogin();
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    attemptLogin();
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                attemptLogin();
            }
        });
    }


    private boolean isPasswordValid(String password) {
        return password.length() > 0;
    }

    /**
     * Handling on click events
     *
     * @param v the view which was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.anotherOption:
                showOptions();
                break;
        }
    }

    private void showOptions() {
        System.gc();
        AlertDialog alertDialog;
        View optionsView = getLayoutInflater().inflate(R.layout.sign_in_options_view, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.chooseYourOption));
        builder.setView(optionsView);
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        RelativeLayout googleSignInWrapper = (RelativeLayout) optionsView.findViewById(R.id.googleSignInWrapper);
        RelativeLayout facebookSignInWrapper = (RelativeLayout) optionsView.findViewById(R.id.facebookSignInWrapper);
        RelativeLayout vkSignInWrapper = (RelativeLayout) optionsView.findViewById(R.id.vkSignInWrapper);
        RelativeLayout linkedInWrapper = (RelativeLayout) optionsView.findViewById(R.id.linkedInSingInWrapper);

        final AppCompatCheckBox appCompatCheckBox = (AppCompatCheckBox) optionsView.findViewById(R.id.privacyCheckBox);
        TextView acceptPrivacyText = (TextView) optionsView.findViewById(R.id.acceptPrivacyText);

        acceptPrivacyText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Policy.class);
                startActivity(intent);
            }
        });

        RelativeLayout inkSignInWrapper = (RelativeLayout) optionsView.findViewById(R.id.inkSignInWrapper);
        alertDialog = builder.show();
        final AlertDialog finalAlertDialog = alertDialog;
        googleSignInWrapper.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!appCompatCheckBox.isChecked()) {
                    Snackbar.make(appCompatCheckBox, getString(R.string.youMustAccept), Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    }).show();
                    return;
                }
                if (finalAlertDialog != null) {
                    finalAlertDialog.dismiss();
                }
                SocialSignIn.get().googleSignIn(Login.this, GOOGLE_SIGN_IN_REQUEST_CODE);
            }
        });
        facebookSignInWrapper.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!appCompatCheckBox.isChecked()) {
                    Snackbar.make(appCompatCheckBox, getString(R.string.youMustAccept), Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    }).show();
                    return;
                }
                if (finalAlertDialog != null) {
                    finalAlertDialog.dismiss();
                }
                SocialSignIn.get().facebookLogin(Login.this, mCallbackManager, new GeneralCallback<Map<String, String>>() {
                    @Override
                    public void onSuccess(Map<String, String> resultMap) {
                        progressDialog.show();
                        String name = resultMap.get("name");
                        String link = resultMap.get("link");
                        String email = resultMap.get("email");
                        String id = resultMap.get("id");
                        String imageUrl = resultMap.get("imageUrl");
                        String[] nameParts = name.split("\\s");
                        String firstName = nameParts[0];
                        String lastName = nameParts[1];
                        loginUser(id, firstName, lastName, imageUrl, link, name, Constants.SOCIAL_TYPE_FACEBOOK, email);
                    }

                    @Override
                    public void onFailure(Map<String, String> stringStringMap) {

                    }
                });
            }
        });
        linkedInWrapper.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!appCompatCheckBox.isChecked()) {
                    Snackbar.make(appCompatCheckBox, getString(R.string.youMustAccept), Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    }).show();
                    return;
                }

                if (finalAlertDialog != null) {
                    finalAlertDialog.dismiss();
                }
                openLinkedInLogin();
            }
        });
        vkSignInWrapper.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!appCompatCheckBox.isChecked()) {
                    Snackbar.make(appCompatCheckBox, getString(R.string.youMustAccept), Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    }).show();
                    return;
                }

                if (finalAlertDialog != null) {
                    finalAlertDialog.dismiss();
                }
                openVkLogin();
            }
        });
        inkSignInWrapper.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!appCompatCheckBox.isChecked()) {
                    Snackbar.make(appCompatCheckBox, getString(R.string.youMustAccept), Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    }).show();
                    return;
                }
                if (finalAlertDialog != null) {
                    finalAlertDialog.dismiss();
                }
                startActivity(new Intent(getApplicationContext(), Registration.class));
            }
        });
    }

    private Scope buildScope() {
        return Scope.build(Scope.R_BASICPROFILE, Scope.W_SHARE);
    }

    private void openLinkedInLogin() {
        LISessionManager.getInstance(getApplicationContext()).init(this, buildScope(), new AuthListener() {
            @Override
            public void onAuthSuccess() {
                progressDialog.show();
                APIHelper apiHelper = APIHelper.getInstance(getApplicationContext());
                apiHelper.getRequest(Login.this, PEOPLE_LINKEDIN_URL, new ApiListener() {
                    @Override
                    public void onApiSuccess(ApiResponse s) {
                        Log.d("fsakljfasfasf", "onApiSuccess: "+s);
                    }

                    @Override
                    public void onApiError(LIApiError error) {
                        Toast.makeText(getApplicationContext(), getString(R.string.errorLogin) + error.toString(), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onAuthError(LIAuthError error) {
                Toast.makeText(getApplicationContext(), getString(R.string.errorLogin) + error.toString(), Toast.LENGTH_LONG).show();
            }
        }, true);
    }

    @Override
    protected void onDestroy() {
        try {
            unregisterReceiver(mBroadcastReceiver);
        } catch (IllegalArgumentException e) {

        }
        super.onDestroy();

    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, 1000).show();
            } else {
                finish();
            }
            return false;
        }
        return true;
    }


    private void disableButtons() {
        mLoginView.setEnabled(false);
        mPasswordView.setEnabled(false);
        mLoginButton.setEnabled(false);
        mRegisterWrapper.setEnabled(false);
    }

    private void enableButtons() {
        mLoginView.setEnabled(true);
        mPasswordView.setEnabled(true);
        mLoginButton.setEnabled(true);
        mRegisterWrapper.setEnabled(true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from
        //   GoogleSignInApi.getSignInIntent(...);
        if (requestCode == GOOGLE_SIGN_IN_REQUEST_CODE) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                getFriends();
                // Get account information
                progressDialog.show();
                String fullName = account.getDisplayName();
                String email = account.getEmail();
                String[] nameParts = fullName.split("\\s");
                try {
                    String firstName = nameParts[0];
                    String lastName = nameParts[1];
                    Uri accountImageUri = account.getPhotoUrl();
                    if (accountImageUri == null) {
                        accountImageUri = Uri.parse(Constants.MAIN_URL + Constants.USER_IMAGES_FOLDER + "no_image.png");
                    }
                    loginUser(email, firstName, lastName, accountImageUri.toString(), "", "", Constants.SOCIAL_TYPE_GOOGLE, "");
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                    Snackbar.make(mRegisterWrapper, getString(R.string.errorGoogleSignIn), Snackbar.LENGTH_INDEFINITE).setAction("OK", new OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    });
                }

            }
        }
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(final VKAccessToken res) {
                String id = res.userId;
                progressDialog.show();
                VKRequest request = VKApi.users().get(VKParameters.from(VKApiConst.USER_ID, id, VKApiConst.FIELDS, "photo_max_orig", "has_photo"));
                request.executeWithListener(new VKRequest.VKRequestListener() {
                    @Override
                    public void onComplete(VKResponse response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response.responseString);
                            JSONArray result = jsonObject.optJSONArray("response");
                            JSONObject userJson = result.optJSONObject(0);
                            String userId = userJson.optString("uid");

                            if (userId != null && !userId.isEmpty()) {

                            } else {
                                userId = userJson.optString("id");
                            }

                            String firstName = userJson.optString("first_name");
                            String lastName = userJson.optString("last_name");
                            String userPhoto = userJson.optString("photo_max_orig");
                            loginUser(userId, firstName, lastName, userPhoto, "", "", Constants.SOCIAL_TYPE_VK, "");
                        } catch (JSONException e) {
                            progressDialog.dismiss();
                            e.printStackTrace();
                        }
                        super.onComplete(response);
                    }

                    @Override
                    public void onError(VKError error) {
                        Snackbar.make(mRegisterWrapper, getString(R.string.vkLoginError), Snackbar.LENGTH_LONG).show();
                        super.onError(error);
                    }

                });
            }

            @Override
            public void onError(VKError error) {

            }
        }))
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void getFriends() {
//        Plus.People.List listPeople = plus.people().list(
//                "me", "visible");
//        listPeople.setMaxResults(5L);
//
//        PeopleFeed peopleFeed = listPeople.execute();
//        List<Person> people = peopleFeed.getItems();
//
//// Loop through until we arrive at an empty page
//        while (people != null) {
//            for (Person person : people) {
//                System.out.println(person.getDisplayName());
//            }
//
//            // We will know we are on the last page when the next page token is
//            // null.
//            // If this is the case, break.
//            if (peopleFeed.getNextPageToken() == null) {
//                break;
//            }
//
//            // Prepare the next page of results
//            listPeople.setPageToken(peopleFeed.getNextPageToken());
//
//            // Execute and process the next page request
//            peopleFeed = listPeople.execute();
//            people = peopleFeed.getItems();
    }

    private void loginUser(final String login,
                           final String firstName,
                           final String lastName,
                           final String imageUrl,
                           final String userLink,
                           final String facebookName, final String loginType, String email) {
        Call<ResponseBody> socialLoginCall = Retrofit.getInstance().getInkService().socialLogin(
                login,
                firstName,
                lastName,
                imageUrl,
                mSharedHelper.getToken(),
                loginType,
                userLink, facebookName, email);

        socialLoginCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    loginUser(login, firstName, lastName, imageUrl, userLink, facebookName, loginType, "");
                    return;
                }
                if (response.body() == null) {
                    loginUser(login, firstName, lastName, imageUrl, userLink, facebookName, loginType, "");
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    if (success) {
                        String userId = jsonObject.optString("userId");
                        boolean isSocial = true;
                        boolean isRegistered = jsonObject.optBoolean("isRegistered");
                        mSharedHelper.putIsAccountRecoverable(false);
                        if (isRegistered) {
                            isSocial = jsonObject.optBoolean("isSocialAccount");
                            saveSocialLoginInfo(jsonObject.optString("firstName"), jsonObject.optString("lastName"), userId, jsonObject.optString("imageUrl"), isRegistered, isSocial);
                            String securityQuestion = jsonObject.optString("securityQuestion");
                            mSharedHelper.putSecurityQuestionSet(securityQuestion != null && !securityQuestion.isEmpty());
                        } else {
                            saveSocialLoginInfo(firstName, lastName, userId, imageUrl, isRegistered, isSocial);
                        }
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(Login.this, getString(R.string.failedLogin), Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                    Toast.makeText(Login.this, getString(R.string.failedLogin), Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                    Toast.makeText(Login.this, getString(R.string.failedLogin), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    private void saveSocialLoginInfo(String firstName,
                                     String lastName,
                                     String userId,
                                     String imageUrl,
                                     boolean isRegistered,
                                     boolean isSocial) {
        mSharedHelper.putFirstName(firstName);
        mSharedHelper.putLastName(lastName);
        mSharedHelper.putUserId(userId);
        mSharedHelper.putShouldShowIntro(false);
        mSharedHelper.putIsRegistered(isRegistered);
        mSharedHelper.putIsSocialAccount(isSocial);
        mSharedHelper.putImageLink(imageUrl);
        progressDialog.dismiss();
        startActivity(new Intent(getApplicationContext(), HomeActivity.class));
        finish();
    }

}


