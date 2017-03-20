package ink.va.activities;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.ink.va.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.va.utils.Constants;
import ink.va.utils.ErrorCause;
import ink.va.utils.ProgressDialog;
import ink.va.utils.Retrofit;
import ink.va.utils.SharedHelper;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SecurityQuestion extends BaseActivity {
    @BindView(R.id.ownSecurityQuestion)
    EditText ownSecurityQuestion;
    @BindView(R.id.ownAnswer)
    EditText ownAnswer;
    private SharedHelper sharedHelper;
    private ProgressDialog progressDialog;
    @BindView(R.id.securityQuestionContainer)
    LinearLayout securityQuestionContainer;
    @BindView(R.id.currentPasswordContainer)
    LinearLayout currentPasswordContainer;
    @BindView(R.id.proceedPasswordCheck)
    Button proceedPasswordCheck;
    @BindView(R.id.currentPasswordField)
    EditText currentPasswordField;
    private Animation scaleOut;
    private Animation scaleIn;
    @BindView(R.id.securityQuestionProgress)
    ProgressBar securityQuestionProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_question);
        ButterKnife.bind(this);
        sharedHelper = new SharedHelper(this);
        scaleOut = AnimationUtils.loadAnimation(this, R.anim.scale_out);
        scaleIn = AnimationUtils.loadAnimation(this, R.anim.scale_in);
        progressDialog = ProgressDialog.get().buildProgressDialog(this, false);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setTitle(getString(R.string.connecting));
        progressDialog.setMessage(getString(R.string.connectingToServer));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @OnClick(R.id.submitSecurityQuestion)
    public void submitSecurityQuestion() {
        if (ownSecurityQuestion.getText().toString().isEmpty() || ownSecurityQuestion.getText().length() < 8) {
            ownSecurityQuestion.setError(getString(R.string.ownSecurityQuestionShort));
        } else if (ownAnswer.getText().toString().isEmpty() || ownAnswer.getText().length() < 4) {
            ownAnswer.setError(getString(R.string.ownSecurityAnswerShort));
        } else {
            progressDialog.show();
            setSecurityQuestion();
        }
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

    @OnClick(R.id.proceedPasswordCheck)
    public void proceedPasswordCheck() {
        if (currentPasswordField.getText().toString().isEmpty()) {
            currentPasswordField.setError(getString(R.string.emptyField));
        } else {
            doPasswordCheckRequest(currentPasswordField.getText().toString());
        }
    }

    private void setSecurityQuestion() {
        Call<ResponseBody> securityQuestionCall = Retrofit.getInstance().getInkService().setSecurityQuestion(sharedHelper.getUserId(), ownSecurityQuestion.getText().toString(),
                ownAnswer.getText().toString());
        securityQuestionCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    setSecurityQuestion();
                    return;
                }
                if (response.body() == null) {
                    setSecurityQuestion();
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    if (success) {
                        progressDialog.hide();
                        sharedHelper.putSecurityQuestionSet(true);
                        Snackbar.make(ownAnswer, getString(R.string.securityQuestionSet), Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                            }
                        }).show();
                    } else {
                        progressDialog.hide();
                        Snackbar.make(ownAnswer, getString(R.string.securityQuestionNotSet), Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                            }
                        }).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    progressDialog.hide();
                    setSecurityQuestion();
                } catch (JSONException e) {
                    e.printStackTrace();
                    progressDialog.hide();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                setSecurityQuestion();
            }
        });
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

    private void doPasswordCheckRequest(final String userCurrentPassword) {
        securityQuestionProgress.setVisibility(View.VISIBLE);
        Call<ResponseBody> getPasswordCall = Retrofit.getInstance().getInkService().getUserPassword(sharedHelper.getUserId(), Constants.PASSWORD_REQUEST_TOKEN);
        getPasswordCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    doPasswordCheckRequest(userCurrentPassword);
                    return;
                }
                if (response.body() == null) {
                    doPasswordCheckRequest(userCurrentPassword);
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    securityQuestionProgress.setVisibility(View.GONE);
                    if (success) {
                        String password = jsonObject.optString("password");
                        if (userCurrentPassword.equals(password)) {
                            proceedForward();
                        } else {
                            progressDialog.hide();
                            Snackbar.make(ownAnswer, getString(R.string.passwordWrong), Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                }
                            }).show();
                        }
                    } else {
                        String cause = jsonObject.optString("cause");
                        if (cause.equals(ErrorCause.NO_USER_FOUND)) {
                            progressDialog.hide();
                            Snackbar.make(ownAnswer, getString(R.string.noUserFoundPasswordError), Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                }
                            }).show();
                        } else {
                            progressDialog.hide();
                            Snackbar.make(ownAnswer, getString(R.string.couldNotConnectToServer), Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                }
                            }).show();
                        }

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    doPasswordCheckRequest(userCurrentPassword);
                } catch (JSONException e) {
                    e.printStackTrace();
                    securityQuestionProgress.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                doPasswordCheckRequest(userCurrentPassword);
            }
        });
    }

    private void proceedForward() {
        currentPasswordField.setEnabled(false);
        proceedPasswordCheck.setEnabled(false);
        startScaleOutAnimation(currentPasswordContainer, securityQuestionContainer);
    }

}
