package ink.va.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ink.va.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.va.interfaces.RequestCallback;
import ink.va.utils.Constants;
import ink.va.utils.ErrorCause;
import ink.va.utils.Retrofit;
import okhttp3.ResponseBody;

public class ForgotPassword extends BaseActivity {
    @BindView(R.id.resetPasswordText)
    TextView resetPasswordText;
    @BindView(R.id.loginContainer)
    LinearLayout loginContainer;
    @BindView(R.id.loginField)
    EditText loginField;
    @BindView(R.id.securityQuestionContainer)
    LinearLayout securityQuestionContainer;
    @BindView(R.id.securityQuestionField)
    EditText securityQuestionField;
    @BindView(R.id.questionHolder)
    TextView questionHolder;
    @BindView(R.id.forgotPasswordResultContainer)
    LinearLayout forgotPasswordResultContainer;
    @BindView(R.id.resultPasswordHolder)
    TextView resultPasswordHolder;
    @BindView(R.id.forgotPasswordProgress)
    ProgressBar forgotPasswordProgress;
    @BindView(R.id.proceedLogin)
    Button proceedButton;
    @BindView(R.id.submitSecurityAnswer)
    Button submitSecurityAnswer;
    private Animation scaleOut;
    private Animation scaleIn;
    private Animation bounceAnimation;
    private String securityQuestion;
    private String securityAnswer;
    private String inputLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        ButterKnife.bind(this);
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
        makeRequest(Retrofit.getInstance().getInkService().getUserLogin(login, Constants.USER_LOGIN_TOKEN), forgotPasswordProgress, true, new RequestCallback() {
            @Override
            public void onRequestSuccess(Object result) {
                try {
                    String responseBody = ((ResponseBody) result).string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    setSecurityQuestionContainerEnabled(true);
                    securityQuestion = jsonObject.optString("securityQuestion");
                    securityAnswer = jsonObject.optString("securityAnswer");
                    if (success) {
                        if (securityQuestion != null && !securityQuestion.isEmpty()) {
                            inputLogin = login;
                            startScaleOutAnimation(loginContainer, securityQuestionContainer);
                            questionHolder.setText(securityQuestion);
                        } else {
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
            public void onRequestFailed(Object[] result) {

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
        makeRequest(Retrofit.getInstance().getInkService().getTemporaryPassword(inputLogin, Constants.USER_LOGIN_TOKEN), forgotPasswordProgress, true, new RequestCallback() {
            @Override
            public void onRequestSuccess(Object result) {
                try {
                    String responseBody = ((ResponseBody)result).string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
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
            public void onRequestFailed(Object[] result) {

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
}
