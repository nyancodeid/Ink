package ink.activities;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
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
import ink.utils.Constants;
import ink.utils.ErrorCause;
import ink.utils.Retrofit;
import ink.utils.SharedHelper;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePassword extends BaseActivity {

    @Bind(R.id.currentPassword)
    EditText currentPassword;
    @Bind(R.id.newPassword)
    EditText newPassword;
    @Bind(R.id.repeatPassword)
    EditText repeatPassword;
    private ink.utils.ProgressDialog progressDialog;
    private SharedHelper sharedHelper;
    private String newPasswordString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        ButterKnife.bind(this);
        sharedHelper = new SharedHelper(this);
        progressDialog = ink.utils.ProgressDialog.get().buildProgressDialog(this, getString(R.string.connecting), getString(R.string.connectingToServer), false);
        ButterKnife.bind(this);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @OnClick(R.id.changePasswordButton)
    public void changePasswordButton() {
        if (currentPassword.getText().toString().isEmpty()) {
            currentPassword.setError(getString(R.string.emptyField));
        } else if (newPassword.getText().toString().isEmpty()) {
            newPassword.setError(getString(R.string.emptyField));
        } else if (repeatPassword.getText().toString().isEmpty()) {
            repeatPassword.setError(getString(R.string.emptyField));
        } else if (!newPassword.getText().toString().equals(repeatPassword.getText().toString())) {
            repeatPassword.setError(getString(R.string.doesnotMatch));
            newPassword.setError(getString(R.string.doesnotMatch));
        } else if(currentPassword.getText().toString().equals(repeatPassword.getText().toString())) {
           Snackbar.make(repeatPassword,getString(R.string.passwordDifferentError),Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
               @Override
               public void onClick(View view) {

               }
           }).show();
        }else{
            progressDialog.show();
            newPasswordString = newPassword.getText().toString();
            doPasswordCheckRequest(currentPassword.getText().toString());
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

    private void doPasswordCheckRequest(final String userCurrentPassword) {
        Call<ResponseBody> getPasswordCall = Retrofit.getInstance().getInkService().getUserPassword(sharedHelper.getUserId(), Constants.PASSWORD_REQUEST_TOKEN);
        getPasswordCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    doPasswordCheckRequest(currentPassword.getText().toString());
                    return;
                }
                if (response.body() == null) {
                    doPasswordCheckRequest(currentPassword.getText().toString());
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    if (success) {
                        String password = jsonObject.optString("password");
                        if (userCurrentPassword.equals(password)) {
                            progressDialog.setTitle(getString(R.string.changingPassword));
                            progressDialog.setMessage(getString(R.string.passwordBeingChanged));
                            changePassword(newPasswordString);
                        } else {
                            progressDialog.hide();
                            Snackbar.make(repeatPassword, getString(R.string.passwordWrongForTisLogin), Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                }
                            }).show();
                        }
                    } else {
                        String cause = jsonObject.optString("cause");
                        if (cause.equals(ErrorCause.NO_USER_FOUND)) {
                            progressDialog.hide();
                            Snackbar.make(repeatPassword, getString(R.string.noUserFoundPasswordError), Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                }
                            }).show();
                        } else {
                            progressDialog.hide();
                            Snackbar.make(repeatPassword, getString(R.string.couldNotConnectToServer), Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                }
                            }).show();
                        }

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    doPasswordCheckRequest(currentPassword.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                doPasswordCheckRequest(currentPassword.getText().toString());
            }
        });
    }

    private void changePassword(final String newPassword) {
        Call<ResponseBody> changePasswordCall = Retrofit.getInstance().getInkService().changePassword(sharedHelper.getUserId(), Constants.PASSWORD_REQUEST_TOKEN, newPassword);
        changePasswordCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    changePassword(newPassword);
                    return;
                }
                if (response.body() == null) {
                    changePassword(newPassword);
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    if (success) {
                        progressDialog.hide();
                        Snackbar.make(repeatPassword, getString(R.string.passwordChanged), Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                            }
                        }).show();
                    } else {
                        Snackbar.make(repeatPassword, getString(R.string.couldNotConnectToServer), Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                            }
                        }).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                    changePassword(newPassword);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                changePassword(newPassword);
            }
        });
    }
}
