package ink.activities;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.ink.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.utils.ProgressDialog;
import ink.utils.Retrofit;
import ink.utils.SharedHelper;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SecurityQuestion extends BaseActivity {
    @Bind(R.id.ownSecurityQuestion)
    EditText ownSecurityQuestion;
    @Bind(R.id.ownAnswer)
    EditText ownAnswer;
    private SharedHelper sharedHelper;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_question);
        ButterKnife.bind(this);
        sharedHelper = new SharedHelper(this);
        progressDialog = ProgressDialog.get().buildProgressDialog(this, getString(R.string.connecting), getString(R.string.connectingToServer), false);
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
                    Log.d("Fasfsafsafsa", "onResponse: "+responseBody);
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
}
