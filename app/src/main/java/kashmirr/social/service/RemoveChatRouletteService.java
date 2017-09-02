package kashmirr.social.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import kashmirr.social.utils.Constants;
import kashmirr.social.utils.Retrofit;
import kashmirr.social.utils.SharedHelper;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by USER on 2016-07-13.
 */
public class RemoveChatRouletteService extends Service {
    private SharedHelper sharedHelper;
    private String opponentId;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            this.opponentId = extras.getString("opponentId");
        }
        sharedHelper = new SharedHelper(this);
        attemptToQue();
        return super.onStartCommand(intent, flags, startId);
    }


    private void attemptToQue() {
        if (opponentId == null) {
            opponentId = "0";
        }
        Call<ResponseBody> waitersQueActionCall = Retrofit.getInstance().getInkService().waitersQueAction(sharedHelper.getUserId(),
                sharedHelper.getFirstName() + " " + sharedHelper.getLastName(), Constants.STATUS_WAITING_NOT_AVAILABLE,
                Constants.ACTION_DELETE, opponentId);
        waitersQueActionCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    attemptToQue();
                    return;
                }
                if (response.body() == null) {
                    attemptToQue();
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    if (success) {
                        sendDisconnect(opponentId);
                    } else {
                        attemptToQue();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                attemptToQue();
            }
        });
    }

    private void sendDisconnect(final String foundOpponentId) {
        Call<ResponseBody> disconnectCall = Retrofit.getInstance().getInkService().sendDisconnectNotification(foundOpponentId);
        disconnectCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    sendDisconnect(foundOpponentId);
                    return;
                }
                if (response.body() == null) {
                    sendDisconnect(foundOpponentId);
                    return;
                }

                stopSelf();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                sendDisconnect(foundOpponentId);
            }
        });
    }
}
