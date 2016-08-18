package ink.activities;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatCheckBox;
import android.text.TextUtils;
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
import com.ink.R;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
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

import ink.callbacks.GeneralCallback;
import ink.utils.Constants;
import ink.utils.Retrofit;
import ink.utils.SharedHelper;
import ink.utils.SocialSignIn;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A login screen that offers login via email/password.
 */
public class Login extends BaseActivity implements View.OnClickListener {

    private static final int GOOGLE_SIGN_IN_REQUEST_CODE = 1;
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
        mCallbackManager = CallbackManager.Factory.create();
        // Set up the login form.
        mSharedHelper = new SharedHelper(this);
        FirebaseInstanceId.getInstance().getToken();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.logging));
        progressDialog.setMessage(getString(R.string.loggingPleasWait));
        if (!checkPlayServices()) {
            return;
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
            VKScope.EMAIL
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

    private void proceedLogin() {
        final Call<ResponseBody> responseBodyCall = Retrofit.getInstance().getInkService().login(mLoginView.getText().toString().toString(),
                mPasswordView.getText().toString());
        responseBodyCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                mProgressView.setVisibility(View.GONE);
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
                            String userId = jsonObject.optString("user_id");
                            mSharedHelper.putFirstName(jsonObject.optString("first_name"));
                            mSharedHelper.putLastName(jsonObject.optString("last_name"));
                            mSharedHelper.putUserId(userId);
                            mSharedHelper.putShouldShowIntro(false);
                            mSharedHelper.putIsSocialAccount(false);
                            String imageLink = jsonObject.optString("imageLink");
                            if (imageLink != null && !imageLink.isEmpty()) {
                                mSharedHelper.putImageLink(imageLink);
                            }
                            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                            finish();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
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
        AlertDialog alertDialog = null;
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
        final AppCompatCheckBox appCompatCheckBox = (AppCompatCheckBox) optionsView.findViewById(R.id.privacyCheckBox);
        TextView acceptPrivacyText = (TextView) optionsView.findViewById(R.id.acceptPrivacyText);

        acceptPrivacyText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

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
                        String imageUrl = resultMap.get("imageUrl");
                        String[] nameParts = name.split("\\s");
                        String firstName = nameParts[0];
                        String lastName = nameParts[1];
                        loginUser(email, firstName, lastName, imageUrl, link, name, Constants.SOCIAL_TYPE_FACEBOOK);
                    }

                    @Override
                    public void onFailure(Map<String, String> stringStringMap) {

                    }
                });
            }
        });
        vkSignInWrapper.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
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
                    loginUser(email, firstName, lastName, accountImageUri.toString(), "", "", Constants.SOCIAL_TYPE_GOOGLE);
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
                            String userId = userJson.optString("id");
                            String firstName = userJson.optString("first_name");
                            String lastName = userJson.optString("last_name");
                            String userPhoto = userJson.optString("photo_max_orig");
                            loginUser(userId, firstName, lastName, userPhoto, "", "", Constants.SOCIAL_TYPE_VK);
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

    private void loginUser(final String login,
                           final String firstName,
                           final String lastName,
                           final String imageUrl,
                           final String userLink,
                           final String facebookName, final String loginType) {
        Call<ResponseBody> socialLoginCall = Retrofit.getInstance().getInkService().socialLogin(
                login,
                firstName,
                lastName,
                imageUrl,
                mSharedHelper.getToken(),
                loginType,
                userLink, facebookName);

        socialLoginCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    loginUser(login, firstName, lastName, imageUrl, userLink, facebookName, loginType);
                    return;
                }
                if (response.body() == null) {
                    loginUser(login, firstName, lastName, imageUrl, userLink, facebookName, loginType);
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
                        if (isRegistered) {
                            isSocial = jsonObject.optBoolean("isSocialAccount");
                            saveSocialLoginInfo(jsonObject.optString("firstName"), jsonObject.optString("lastName"), userId, jsonObject.optString("imageUrl"), isRegistered, isSocial);
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


