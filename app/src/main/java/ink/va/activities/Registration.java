package ink.va.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.ink.va.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import ink.va.utils.ErrorCause;
import ink.va.utils.Keyboard;
import ink.va.utils.Retrofit;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Registration extends BaseActivity implements View.OnClickListener {

    @Bind(R.id.login)
    EditText mLogin;
    @Bind(R.id.password)
    EditText mPassword;
    @Bind(R.id.confirmPassword)
    EditText mConfirmPassword;
    @Bind(R.id.register)
    Button mRegister;
    @Bind(R.id.rootRegistrationLayout)
    LinearLayout mRootRegistrationLayout;
    @Bind(R.id.registrationScrollView)
    ScrollView registrationScrollView;
    private View mRegisterLoading;

    @Bind(R.id.firstName)
    EditText mFirstName;
    @Bind(R.id.lastName)
    EditText mLastName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(R.string.registerAccount));
        }

        mRegisterLoading = findViewById(R.id.registerLoading);
        mRegister.setOnClickListener(this);
    }

    /**
     * Register user to the server
     */
    private void register(final String login, final String password, final String firstName, final String lastName) {
        if (!mLogin.getText().toString().isEmpty() && mLogin.getText().toString().length() >= 5
                && !mPassword.getText().toString().isEmpty() && mPassword.getText().toString().length() >= 5
                && !mConfirmPassword.getText().toString().isEmpty()
                && mPassword.getText().toString().equals(mConfirmPassword.getText().toString())
                && !mFirstName.getText().toString().isEmpty()
                && !mLastName.getText().toString().isEmpty()
                && !mPassword.getText().toString().isEmpty()) {

            Keyboard.hideKeyboard(this, mRootRegistrationLayout);
            registrationScrollView.post(new Runnable() {
                @Override
                public void run() {
                    registrationScrollView.smoothScrollTo(0, 0);
                }
            });

            mRegisterLoading.setVisibility(View.VISIBLE);
            mRootRegistrationLayout.setEnabled(false);
            final Call<ResponseBody> register = Retrofit.getInstance().getInkService().register(login,
                    password, firstName, lastName);
            register.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    mRegisterLoading.setVisibility(View.GONE);
                    if (response == null) {
                        register(login, password, firstName, lastName);
                        return;
                    }
                    if (response.body() == null) {
                        register(login, password, firstName, lastName);
                        return;
                    }
                    try {
                        try {
                            String responseString = response.body().string();
                            JSONObject jsonObject = new JSONObject(responseString);
                            boolean success = jsonObject.optBoolean("success");
                            if (success) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(Registration.this);
                                builder.setTitle(getString(R.string.successRegistration));
                                builder.setMessage(getString(R.string.successRegistrationMessage));
                                builder.setCancelable(false);
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        finish();
                                    }
                                });
                                builder.show();
                            } else {
                                String cause = jsonObject.optString("cause");
                                if (cause.equals(ErrorCause.USER_ALREADY_EXIST)) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(Registration.this);
                                    builder.setTitle(getString(R.string.userExist));
                                    builder.setMessage(getString(R.string.userExistMessage));
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
                            e.printStackTrace();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    mRootRegistrationLayout.setEnabled(true);
                    mRegisterLoading.setVisibility(View.GONE);
                }
            });

        } else {
            if (mLogin.getText().toString().isEmpty()) {
                mLogin.setError(getString(R.string.error_field_required));
            }

            if (mLogin.getText().toString().length() < 5) {
                mLogin.setError(getString(R.string.login_too_short));
            }
            if (mPassword.getText().toString().length() < 5) {
                mPassword.setError(getString(R.string.password_too_short));
            }

            if (mFirstName.getText().toString().isEmpty()) {
                mFirstName.setError(getString(R.string.errorFirstName));
            }
            if (mLastName.getText().toString().isEmpty()) {
                mLastName.setError(getString(R.string.errorLastName));
            }
            if (!mPassword.getText().toString().isEmpty() && !mConfirmPassword.getText().toString().isEmpty()) {
                if (!mPassword.getText().toString().equals(mConfirmPassword.getText().toString())) {
                    mPassword.setError(getString(R.string.not_equals_error));
                    mConfirmPassword.setError(getString(R.string.not_equals_error));
                }

            } else {
                if (mPassword.getText().toString().isEmpty()) {
                    mPassword.setError(getString(R.string.error_field_required));
                }
                if (mConfirmPassword.getText().toString().isEmpty()) {
                    mConfirmPassword.setError(getString(R.string.error_field_required));
                }
            }
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.register:
                register(mLogin.getText().toString(), mPassword.getText().toString(),
                        mFirstName.getText().toString(), mLastName.getText().toString());
                break;
        }
    }
}
