package ink.service;

import android.app.Service;
import android.content.Intent;
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
 * Created by USER on 2016-07-13.
 */
public class RemoveChatRouletteService extends Service {
    private SharedHelper sharedHelper;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sharedHelper = new SharedHelper(this);
        attemptToQue();
        return super.onStartCommand(intent, flags, startId);
    }


    private void attemptToQue() {
        Call<ResponseBody> waitersQueActionCall = Retrofit.getInstance().getInkService().waitersQueAction(sharedHelper.getUserId(),
                sharedHelper.getFirstName() + " " + sharedHelper.getLastName(), Constants.STATUS_WAITING_NOT_AVAILABLE,
                Constants.ACTION_DELETE);
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
                        stopSelf();
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
}
