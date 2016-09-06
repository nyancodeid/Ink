package ink.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.ink.R;
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

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.callbacks.GeneralCallback;
import ink.utils.Constants;
import ink.utils.ErrorCause;
import ink.utils.ProgressDialog;
import ink.utils.Retrofit;
import ink.utils.SharedHelper;
import ink.utils.SocialSignIn;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPassword extends AppCompatActivity {
    @Bind(R.id.resetPasswordText)
    TextView resetPasswordText;
    @Bind(R.id.loginContainer)
    LinearLayout loginContainer;
    @Bind(R.id.loginField)
    EditText loginField;
    @Bind(R.id.securityQuestionContainer)
    LinearLayout securityQuestionContainer;
    @Bind(R.id.securityQuestionField)
    EditText securityQuestionField;
    @Bind(R.id.questionHolder)
    TextView questionHolder;
    @Bind(R.id.forgotPasswordResultContainer)
    LinearLayout forgotPasswordResultContainer;
    @Bind(R.id.resultPasswordHolder)
    TextView resultPasswordHolder;
    @Bind(R.id.forgotPasswordProgress)
    ProgressBar forgotPasswordProgress;
    @Bind(R.id.proceedLogin)
    Button proceedButton;
    @Bind(R.id.socialAccountWrapper)
    RelativeLayout socialAccountWrapper;
    @Bind(R.id.submitSecurityAnswer)
    Button submitSecurityAnswer;
    private Animation scaleOut;
    private Animation scaleIn;
    private Animation bounceAnimation;
    private SharedHelper sharedHelper;
    private String securityQuestion;
    private String securityAnswer;
    private String inputLogin;
    private CallbackManager mCallbackManager;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        ButterKnife.bind(this);
        mCallbackManager = CallbackManager.Factory.create();
        sharedHelper = new SharedHelper(this);
        progressDialog = ProgressDialog.get().buildProgressDialog(this, getString(R.string.connectingToServer), getString(R.string.fetchingProfileInfo), false);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        scaleOut = AnimationUtils.loadAnimation(this, R.anim.scale_out);
        scaleIn = AnimationUtils.loadAnimation(this, R.anim.scale_in);
        bounceAnimation = AnimationUtils.loadAnimation(this, R.anim.bounce_animation);
    }

    @OnClick(R.id.proceedLogin)
    public void proceedLogin() {
        if (!loginField.getText().toString().isEmpty()) {
            requestLogin(loginField.getText().toString());
        } else {
            Snackbar.make(forgotPasswordResultContainer, getString(R.string.emptyField), Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            }).show();
        }

    }

    @OnClick(R.id.socialAccountWrapper)
    public void socialAccountWrapper() {
        showOptions();
    }

    @OnClick(R.id.backToLoginContainer)
    public void backToLoginContainer() {
        startScaleOutAnimation(securityQuestionContainer, loginContainer);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void requestLogin(final String login) {
        setSecurityQuestionContainerEnabled(false);
        forgotPasswordProgress.setVisibility(View.VISIBLE);
        Call<ResponseBody> loginCall = Retrofit.getInstance().getInkService().getUserLogin(login, Constants.USER_LOGIN_TOKEN);
        loginCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    requestLogin(login);
                    return;
                }
                if (response.body() == null) {
                    requestLogin(login);
                }
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    forgotPasswordProgress.setVisibility(View.GONE);
                    setSecurityQuestionContainerEnabled(true);
                    securityQuestion = jsonObject.optString("securityQuestion");
                    securityAnswer = jsonObject.optString("securityAnswer");
                    if (success) {
                        if (securityQuestion != null && !securityQuestion.isEmpty()) {
                            inputLogin = login;
                            startScaleOutAnimation(loginContainer, securityQuestionContainer);
                            questionHolder.setText(securityQuestion);
                        } else {
                            System.gc();
                            AlertDialog.Builder builder = new AlertDialog.Builder(ForgotPassword.this);
                            builder.setTitle(getString(R.string.noSecurityQuestion));
                            builder.setMessage(getString(R.string.noSecurityQuestionMessage));
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });
                            builder.show();
                        }
                    } else {
                        String cause = jsonObject.optString("cause");
                        if (cause.equals(ErrorCause.NO_USER_FOUND)) {
                            Snackbar.make(forgotPasswordResultContainer, getString(R.string.noUserFound), Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                }
                            }).show();
                        } else {
                            Snackbar.make(forgotPasswordResultContainer, getString(R.string.serverErrorText), Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                }
                            }).show();
                        }
                    }
                } catch (IOException e) {
                    setSecurityQuestionContainerEnabled(true);
                    e.printStackTrace();
                } catch (JSONException e) {
                    setSecurityQuestionContainerEnabled(true);
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                requestLogin(login);
            }
        });
    }

    @OnClick(R.id.submitSecurityAnswer)
    public void submitSecurityAnswer() {
        if (securityQuestionField.getText().toString().trim().isEmpty()) {
            securityQuestionField.setError(getString(R.string.fieldEmptyError));
        } else {
            if (securityQuestionField.getText().toString().equals(securityAnswer)) {
                getTemporaryPassword();
            } else {
                Snackbar.make(forgotPasswordResultContainer, getString(R.string.wrongSecurityAnswer), Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                }).show();
            }
        }
    }

    private void getTemporaryPassword() {
        forgotPasswordProgress.setVisibility(View.VISIBLE);
        Call<ResponseBody> temporaryPasswordCall = Retrofit.getInstance().getInkService().getTemporaryPassword(inputLogin, Constants.USER_LOGIN_TOKEN);
        temporaryPasswordCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    getTemporaryPassword();
                    return;
                }
                if (response.body() == null) {
                    getTemporaryPassword();
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    forgotPasswordProgress.setVisibility(View.GONE);
                    if (success) {
                        String tempPassword = jsonObject.optString("tempPassword");
                        startResultAnimation();
                        resultPasswordHolder.setText(tempPassword);
                    } else {
                        Snackbar.make(forgotPasswordResultContainer, getString(R.string.cantGetTempPassword), Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                            }
                        }).show();
                    }
                } catch (IOException e) {
                    forgotPasswordProgress.setVisibility(View.GONE);
                    e.printStackTrace();
                    Snackbar.make(forgotPasswordResultContainer, getString(R.string.serverErrorText), Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    }).show();
                } catch (JSONException e) {
                    forgotPasswordProgress.setVisibility(View.GONE);
                    e.printStackTrace();
                    Snackbar.make(forgotPasswordResultContainer, getString(R.string.serverErrorText), Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    }).show();

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                getTemporaryPassword();
            }
        });
    }

    private void startResultAnimation() {
        securityQuestionField.setEnabled(false);
        submitSecurityAnswer.setEnabled(false);
        scaleOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                securityQuestionContainer.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        securityQuestionContainer.startAnimation(scaleOut);

        forgotPasswordResultContainer.startAnimation(bounceAnimation);
        bounceAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                forgotPasswordResultContainer.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    @OnClick(R.id.copyPasswordIcon)
    public void copyPasswordIcon() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("tempPassword", resultPasswordHolder.getText().toString());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(ForgotPassword.this, getString(R.string.copied), Toast.LENGTH_SHORT).show();
    }

    private void setLoginContainerEnabled(boolean enabled) {
        loginContainer.setFocusable(enabled);
        loginContainer.setClickable(enabled);
        loginContainer.setEnabled(enabled);
    }

    private void setSecurityQuestionContainerEnabled(boolean enabled) {
        loginField.setEnabled(enabled);
        proceedButton.setEnabled(enabled);
        socialAccountWrapper.setEnabled(enabled);
    }

    private void setForgotPasswordContainerEnabled(boolean enabled) {
        forgotPasswordResultContainer.setFocusable(enabled);
        forgotPasswordResultContainer.setClickable(enabled);
        forgotPasswordResultContainer.setEnabled(enabled);
    }

    private void startScaleOutAnimation(final View viewToScaleOut, View viewToScaleIn) {
        if (viewToScaleOut.getVisibility() == View.GONE) {
            viewToScaleOut.setVisibility(View.VISIBLE);
        }
        viewToScaleOut.setClickable(false);
        viewToScaleOut.setFocusable(false);
        scaleOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                viewToScaleOut.setVisibility(View.GONE);
                viewToScaleOut.setClickable(true);
                viewToScaleOut.setFocusable(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        viewToScaleOut.startAnimation(scaleOut);
        startScaleInAnimation(viewToScaleIn);
    }


    private void startScaleInAnimation(final View viewToScaleIn) {
        scaleIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if (viewToScaleIn.getVisibility() == View.GONE) {
                    viewToScaleIn.setVisibility(View.VISIBLE);
                }
                viewToScaleIn.setFocusable(false);
                viewToScaleIn.setClickable(false);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                viewToScaleIn.setFocusable(true);
                viewToScaleIn.setClickable(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        viewToScaleIn.startAnimation(scaleIn);
    }


    private void showOptions() {
        System.gc();
        AlertDialog alertDialog = null;
        View optionsView = getLayoutInflater().inflate(R.layout.social_sign_in_options, null);
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

        alertDialog = builder.show();
        final AlertDialog finalAlertDialog = alertDialog;
        googleSignInWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (finalAlertDialog != null) {
                    finalAlertDialog.dismiss();
                }
                SocialSignIn.get().googleSignIn(ForgotPassword.this, Login.GOOGLE_SIGN_IN_REQUEST_CODE);
            }
        });
        facebookSignInWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (finalAlertDialog != null) {
                    finalAlertDialog.dismiss();
                }
                SocialSignIn.get().facebookLogin(ForgotPassword.this, mCallbackManager, new GeneralCallback<Map<String, String>>() {
                    @Override
                    public void onSuccess(Map<String, String> resultMap) {
                        String email = resultMap.get("email");
                        requestLogin(email);
                    }

                    @Override
                    public void onFailure(Map<String, String> stringStringMap) {

                    }
                });
            }
        });
        vkSignInWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (finalAlertDialog != null) {
                    finalAlertDialog.dismiss();
                }
                openVkLogin();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from
        //   GoogleSignInApi.getSignInIntent(...);
        if (requestCode == Login.GOOGLE_SIGN_IN_REQUEST_CODE) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();

                String email = account.getEmail();
                requestLogin(email);
            }
        }
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(final VKAccessToken res) {
                String id = res.userId;
                VKRequest request = VKApi.users().get(VKParameters.from(VKApiConst.USER_ID, id, VKApiConst.FIELDS, "photo_max_orig", "has_photo"));
                request.executeWithListener(new VKRequest.VKRequestListener() {
                    @Override
                    public void onComplete(VKResponse response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response.responseString);
                            JSONArray result = jsonObject.optJSONArray("response");
                            JSONObject userJson = result.optJSONObject(0);
                            String userId = userJson.optString("id");
                            requestLogin(userId);
                        } catch (JSONException e) {
                            progressDialog.hide();
                            e.printStackTrace();
                        }
                        super.onComplete(response);
                    }

                    @Override
                    public void onError(VKError error) {
                        Snackbar.make(resultPasswordHolder, getString(R.string.vkLoginError), Snackbar.LENGTH_LONG).show();
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

    private static final String[] vkScopes = new String[]{
    };

    private void openVkLogin() {
        VKSdk.login(this, vkScopes);
    }
}
