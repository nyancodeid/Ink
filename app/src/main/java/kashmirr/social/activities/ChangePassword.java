package kashmirr.social.activities;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.kashmirr.social.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import kashmirr.social.interfaces.RequestCallback;
import kashmirr.social.utils.Constants;
import kashmirr.social.utils.ErrorCause;
import kashmirr.social.utils.Retrofit;
import kashmirr.social.utils.SharedHelper;
import okhttp3.ResponseBody;

public class ChangePassword extends BaseActivity {

    @BindView(R.id.currentPassword)
    EditText currentPassword;
    @BindView(R.id.newPassword)
    EditText newPassword;
    @BindView(R.id.repeatPassword)
    EditText repeatPassword;
    private kashmirr.social.utils.ProgressDialog progressDialog;
    private SharedHelper sharedHelper;
    private String newPasswordString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        ButterKnife.bind(this);
        sharedHelper = new SharedHelper(this);
        progressDialog = kashmirr.social.utils.ProgressDialog.get().buildProgressDialog(this, false);
        progressDialog.setTitle(getString(R.string.connecting));
        progressDialog.setMessage(getString(R.string.connectingToServer));
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
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
        } else if (currentPassword.getText().toString().equals(repeatPassword.getText().toString())) {
            Snackbar.make(repeatPassword, getString(R.string.passwordDifferentError), Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            }).show();
        } else {
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
        makeRequest(Retrofit.getInstance().getInkService().getUserPassword(sharedHelper.getUserId(), Constants.PASSWORD_REQUEST_TOKEN), null, new RequestCallback() {
            @Override
            public void onRequestSuccess(Object result) {
                try {
                    String responseBody = ((ResponseBody) result).string();
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
            public void onRequestFailed(Object[] result) {
                progressDialog.hide();
            }
        });
    }

    private void changePassword(final String newPassword) {
        makeRequest(Retrofit.getInstance().getInkService().changePassword(sharedHelper.getUserId(), Constants.PASSWORD_REQUEST_TOKEN, newPassword), null, new RequestCallback() {
            @Override
            public void onRequestSuccess(Object result) {
                try {
                    String responseBody = ((ResponseBody) result).string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    progressDialog.hide();
                    if (success) {
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
            public void onRequestFailed(Object[] result) {
                progressDialog.hide();
            }
        });

    }
}
