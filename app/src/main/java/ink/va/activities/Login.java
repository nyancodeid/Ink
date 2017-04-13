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
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.View;
import android.view.View.OnClickListener;
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

import butterknife.BindView;
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
import shem.com.materiallogin.MaterialLoginView;

import static ink.va.utils.Constants.PEOPLE_LINKEDIN_URL;

/**
 * A login screen that offers login via email/password.
 */
public class Login extends BaseActivity {

    public static final int GOOGLE_SIGN_IN_REQUEST_CODE = 1;
    private BroadcastReceiver mBroadcastReceiver;
    private SharedHelper mSharedHelper;
    private ProgressDialog progressDialog;
    private CallbackManager mCallbackManager;

    @BindView(R.id.loginMaterialForm)
    MaterialLoginView materialLoginView;
    @BindView(R.id.registrationFirstName)
    TextInputLayout registrationFirstName;

    @BindView(R.id.registrationLastName)
    TextInputLayout registrationLastName;

    @BindView(R.id.registrationLogin)
    TextInputLayout registrationLogin;

    @BindView(R.id.registrationPassword)
    TextInputLayout registrationPassword;

    @BindView(R.id.registrationConfirmPassword)
    TextInputLayout registrationConfirmPassword;

    @BindView(R.id.loginInput)
    TextInputLayout loginInput;

    @BindView(R.id.passwordInput)
    TextInputLayout passwordInput;

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
        setStatusBarColor(R.color.light_blue);

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
            startHomeActivity();
        }

        registrationFirstName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    registrationFirstName.setError(null);
                }
            }
        });

        registrationLastName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                registrationLastName.setError(null);
            }
        });

        registrationLogin.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                registrationLogin.setError(null);
            }
        });

        registrationPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                registrationPassword.setError(null);

            }
        });

        registrationConfirmPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                registrationConfirmPassword.setError(null);
            }
        });

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                finish();
            }
        };
        registerReceiver(mBroadcastReceiver, new IntentFilter("Login"));
    }


    private void openVkLogin() {
        VKSdk.login(this, vkScopes);
    }

    private static final String[] vkScopes = new String[]{
    };


    @OnClick(R.id.forgotPassword)
    public void forgotPassword() {
        startActivity(new Intent(getApplicationContext(), ForgotPassword.class));
    }

    @OnClick(R.id.register)
    public void registerClicked() {
        if (canProceed()) {
        }
    }

    private boolean canProceed() {
        if (registrationFirstName.getEditText().getText().toString().trim().isEmpty()) {
            registrationFirstName.setError(getString(R.string.errorFirstName));
            return false;
        }
        if (registrationLastName.getEditText().getText().toString().trim().isEmpty()) {
            registrationLastName.setError(getString(R.string.errorLastName));
            return false;
        }
        if (registrationLogin.getEditText().getText().toString().trim().isEmpty()) {
            registrationLogin.setError(getString(R.string.fieldsMandatory));
            return false;
        }

        if (registrationLogin.getEditText().getText().toString().trim().length() < 5) {
            registrationLogin.setError(getString(R.string.login_too_short));
            return false;
        }

        if (registrationPassword.getEditText().getText().toString().trim().isEmpty()) {
            registrationPassword.setError(getString(R.string.fieldsMandatory));
            return false;
        }

        if (registrationConfirmPassword.getEditText().getText().toString().trim().isEmpty()) {
            registrationConfirmPassword.setError(getString(R.string.fieldsMandatory));
            return false;
        }

        if (registrationPassword.getEditText().getText().toString().trim().length() < 5 || registrationConfirmPassword.getEditText().getText().toString().trim().length() < 5) {
            registrationPassword.setError(getString(R.string.password_too_short));
            return false;
        }

        if (!registrationPassword.getEditText().getText().toString().trim().equals(registrationConfirmPassword.getEditText().getText().toString().trim().isEmpty())) {
            registrationPassword.setError(getString(R.string.doesnotMatch));
            registrationConfirmPassword.setError(getString(R.string.doesnotMatch));
            return false;
        }

        return true;
    }

//    private void proceedLogin() {
//        final Call<ResponseBody> responseBodyCall = Retrofit.getInstance().getInkService().login(mLoginView.getText().toString().toString(),
//                mPasswordView.getText().toString());
//        responseBodyCall.enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                if (response == null) {
//                    proceedLogin();
//                    return;
//                }
//                if (response.body() == null) {
//                    proceedLogin();
//                    return;
//                }
//                AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
//                try {
//                    try {
//                        String responseString = response.body().string();
//                        JSONObject jsonObject = new JSONObject(responseString);
//                        boolean success = jsonObject.optBoolean("success");
//                        if (!success) {
//                            enableButtons();
//                            mProgressView.setVisibility(View.GONE);
//                            builder.setTitle(getString(R.string.errorLogin));
//                            builder.setMessage(getString(R.string.errorLoginMessage));
//                            builder.setCancelable(false);
//                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    dialog.dismiss();
//
//                                }
//                            });
//                            builder.show();
//                        } else {
//                            boolean banned = jsonObject.optBoolean("banned");
//                            if (!banned) {
//                                String userId = jsonObject.optString("user_id");
//                                String securityQuestion = jsonObject.optString("securityQuestion");
//                                mSharedHelper.putSecurityQuestionSet(securityQuestion != null && !securityQuestion.isEmpty());
//                                mSharedHelper.putFirstName(jsonObject.optString("first_name"));
//                                mSharedHelper.putIsAccountRecoverable(true);
//                                mSharedHelper.putLastName(jsonObject.optString("last_name"));
//                                mSharedHelper.setPassword(jsonObject.optString("password"));
//                                mSharedHelper.putUserId(userId);
//                                mSharedHelper.putShouldShowIntro(false);
//                                mSharedHelper.putLogin(jsonObject.optString("login"));
//                                mSharedHelper.putIsSocialAccount(false);
//                                mSharedHelper.putIsAccountRecoverable(true);
//                                String imageLink = jsonObject.optString("imageLink");
//                                if (imageLink != null && !imageLink.isEmpty()) {
//                                    mSharedHelper.putImageLink(imageLink);
//                                }
//                                startHomeActivity();
//                            } else {
//                                mProgressView.setVisibility(View.GONE);
//                                enableButtons();
//                                builder.setTitle(getString(R.string.ban_title));
//                                builder.setMessage(getString(R.string.ban_message));
//                                builder.setCancelable(false);
//                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        dialog.dismiss();
//                                    }
//                                });
//                                builder.show();
//                            }
//                        }
//                    } catch (JSONException e) {
//                        enableButtons();
//                        mProgressView.setVisibility(View.GONE);
//                        builder.setTitle(getString(R.string.errorLogin));
//                        builder.setMessage(getString(R.string.errorLoginMessage));
//                        builder.setCancelable(false);
//                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.dismiss();
//
//                            }
//                        });
//                        builder.show();
//                        e.printStackTrace();
//                    }
//                } catch (IOException e) {
//                    enableButtons();
//                    mProgressView.setVisibility(View.GONE);
//                    builder.setTitle(getString(R.string.errorLogin));
//                    builder.setMessage(getString(R.string.errorLoginMessage));
//                    builder.setCancelable(false);
//                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            dialog.dismiss();
//
//                        }
//                    });
//                    builder.show();
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ResponseBody> call, Throwable t) {
//                attemptLogin();
//            }
//        });
//    }

    private void startHomeActivity() {
        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

    }


    @OnClick(R.id.anotherOption)
    public void anotherOptionsClicked() {
        showOptions();
    }

    private void showOptions() {
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
                        try {
                            JSONObject jsonObject = new JSONObject(s.getResponseDataAsString());
                            String firstName = jsonObject.optString("firstName");
                            String lastName = jsonObject.optString("lastName");
                            String login = jsonObject.optString("id");
                            JSONObject pictureUrls = jsonObject.optJSONObject("pictureUrls");
                            int total = pictureUrls.optInt("_total");
                            String pictureUrl = Constants.NO_IMAGE_URL;
                            if (total > 0) {
                                JSONArray values = pictureUrls.optJSONArray("values");
                                pictureUrl = values.optString(0);
                            }
                            loginUser(login, firstName, lastName, pictureUrl, "", "", Constants.SOCIAL_TYPE_LINKEDIN, "");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(Login.this, getString(R.string.errorLogin), Toast.LENGTH_SHORT).show();
                        }
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


//    private void disableButtons() {
//        mLoginView.setEnabled(false);
//        mPasswordView.setEnabled(false);
//        mLoginButton.setEnabled(false);
//        mRegisterWrapper.setEnabled(false);
//    }
//
//    private void enableButtons() {
//        if (mLoginView != null) {
//            mLoginView.setEnabled(true);
//            mPasswordView.setEnabled(true);
//            mLoginButton.setEnabled(true);
//            mRegisterWrapper.setEnabled(true);
//        }
//
//    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LISessionManager.getInstance(getApplicationContext()).onActivityResult(this, requestCode, resultCode, data);
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
                    loginUser(email, firstName, lastName, accountImageUri.toString(), "", "", Constants.SOCIAL_TYPE_GOOGLE, "");
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                    Snackbar.make(materialLoginView, getString(R.string.errorGoogleSignIn), Snackbar.LENGTH_INDEFINITE).setAction("OK", new OnClickListener() {
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
                        Snackbar.make(materialLoginView, getString(R.string.vkLoginError), Snackbar.LENGTH_LONG).show();
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
//        startHomeActivity();
    }

}


