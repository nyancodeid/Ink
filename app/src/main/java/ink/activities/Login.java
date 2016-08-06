package ink.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.ink.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import ink.utils.Retrofit;
import ink.utils.SharedHelper;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A login screen that offers login via email/password.
 */
public class Login extends AppCompatActivity implements View.OnClickListener {

    // UI references.
    private AutoCompleteTextView mLoginView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private RelativeLayout mRegisterWrapper;
    private BroadcastReceiver mBroadcastReceiver;
    private SharedHelper mSharedHelper;
    private Button mLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mSharedHelper = new SharedHelper(this);
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

        mLoginButton = (Button) findViewById(R.id.email_sign_in_button);
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
                Log.i("Fffsfasfas", "This device is not supported.");
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
}


