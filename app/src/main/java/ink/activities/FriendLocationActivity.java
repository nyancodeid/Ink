package ink.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;

import com.ink.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import ink.service.LocationRequestSessionDestroyer;
import ink.utils.Retrofit;
import ink.utils.SharedHelper;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FriendLocationActivity extends BaseActivity {

    private static final String TAG = "fasfsafasfasfas";
    @Bind(R.id.requestStatus)
    TextView requestStatus;
    private SharedHelper sharedHelper;
    private String requestType;
    private String opponentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_location);
        ButterKnife.bind(this);
        sharedHelper = new SharedHelper(this);
        Bundle extras = getIntent().getExtras();
        String opponentName = getString(R.string.couldNotFetchName);
        if (extras != null) {
            opponentId = extras.getString("opponentId");
            opponentName = extras.getString("opponentName");
            requestType = extras.getString("requestType");
            requestLocation(opponentId, opponentName);
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.locationShareWith) + " " + opponentName);
        }
    }

    private void requestLocation(final String opponentId, final String opponentName) {
        Call<ResponseBody> friendLocationCall = Retrofit.getInstance().getInkService().requestFriendLocation(sharedHelper.getUserId(), opponentId,
                sharedHelper.getFirstName() + " " + sharedHelper.getLastName(), opponentName, requestType);
        friendLocationCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    requestLocation(opponentId, opponentName);
                    return;
                }
                if (response.body() == null) {
                    requestLocation(opponentId, opponentName);
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    if (success) {
                        requestStatus.setText(getString(R.string.requestSentWaiting));
                    } else {
                        Snackbar.make(requestStatus, getString(R.string.failedRequestLocation), Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                finish();
                            }
                        }).show();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                requestLocation(opponentId, opponentName);
            }
        });
    }



    @Override
    public void onBackPressed() {
        showWarning();
    }

    private void showWarning() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.warning));
        builder.setMessage(getString(R.string.leavingSession));
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                destroySession();
            }
        });
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    @Override
    protected void onDestroy() {
        destroySession();
        super.onDestroy();
    }

    private void destroySession() {
        Intent intent = new Intent(getApplicationContext(), LocationRequestSessionDestroyer.class);
        intent.putExtra("opponentId", opponentId);
        startService(intent);
        finish();
    }
}
