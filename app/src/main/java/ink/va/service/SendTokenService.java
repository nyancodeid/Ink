package ink.va.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import ink.va.utils.Retrofit;
import ink.va.utils.SharedHelper;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by USER on 2016-06-26.
 */
public class SendTokenService extends Service {
    private SharedHelper mSharedHelper;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mSharedHelper = new SharedHelper(this);
        sendToken(mSharedHelper.getToken());
        return super.onStartCommand(intent, flags, startId);
    }

    private void sendToken(final String token) {
        Call<ResponseBody> tokenResponse = Retrofit.getInstance().getInkService().registerToken(mSharedHelper.getUserId(), token);
        tokenResponse.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                mSharedHelper.setTokenRefreshed(false);
                stopSelf();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                sendToken(token);
            }
        });
    }


}
