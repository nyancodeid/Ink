package ink.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import ink.utils.Constants;
import ink.utils.Retrofit;
import ink.utils.SharedHelper;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by PC-Comp on 8/17/2016.
 */
public class LocationRequestSessionDestroyer extends Service {

    private String opponentId;
    private SharedHelper sharedHelper;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle extras = intent.getExtras();
        sharedHelper = new SharedHelper(this);
        if (extras != null) {
            opponentId = extras.getString("opponentId");
            destroySession();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void destroySession() {
        Call<ResponseBody> locationCall = Retrofit.getInstance().getInkService().requestFriendLocation(sharedHelper.getUserId(), opponentId, "", "", Constants.LOCATION_REQUEST_TYPE_DELETE);
        locationCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    destroySession();
                    return;
                }
                if (response.body() == null) {
                    destroySession();
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    if (success) {
                        stopSelf();
                    } else {
                        destroySession();
                    }
                } catch (IOException e) {
                    stopSelf();
                    e.printStackTrace();
                } catch (JSONException e) {
                    stopSelf();
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                destroySession();
            }
        });
    }
}
