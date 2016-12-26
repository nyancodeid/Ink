package ink.va.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import ink.va.utils.Constants;
import ink.va.utils.RealmHelper;
import ink.va.utils.Retrofit;
import ink.va.utils.SharedHelper;
import ink.va.utils.Time;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by PC-Comp on 8/19/2016.
 */
public class SendMessageService extends Service {
    private SharedHelper sharedHelper;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sharedHelper = new SharedHelper(this);
        Bundle extras = intent.getExtras();
        if (extras != null) {
            final String mOpponentId = extras.getString("opponentId");
            final String message = extras.getString("message");
            final boolean hasGif = extras.getBoolean("hasGif");
            final String gifUrl = extras.getString("gifUrl");
            final int sentItemLocation = extras.getInt("sentItemLocation");
            boolean isAnimated = extras.getBoolean("isAnimated");
            final String mCurrentUserId = sharedHelper.getUserId();

            attachToQue(mOpponentId, message, hasGif, gifUrl, String.valueOf(sentItemLocation), mCurrentUserId, isAnimated);
        }
        return super.onStartCommand(intent, flags, startId);
    }


    private void attachToQue(final String mOpponentId,
                             final String message,
                             final boolean hasGif, final
                             String gifUrl,
                             final String sentItemLocation,
                             final String mCurrentUserId, final boolean isAnimated) {

        Call<ResponseBody> sendMessageResponse = Retrofit.getInstance().getInkService().sendMessage(mCurrentUserId,
                mOpponentId, message, Time.getTimeZone(), hasGif, gifUrl, String.valueOf(isAnimated));

        sendMessageResponse.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response == null) {
                        attachToQue(mOpponentId, message, hasGif, gifUrl, sentItemLocation, mCurrentUserId, isAnimated);
                        return;
                    }
                    if (response.body() == null) {
                        attachToQue(mOpponentId, message, hasGif, gifUrl, sentItemLocation, mCurrentUserId, isAnimated);
                        return;
                    }

                    String responseString = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseString);

                    String messageId = jsonObject.optString("message_id");

                    RealmHelper.getInstance().updateMessages(messageId,
                            Constants.STATUS_DELIVERED, String.valueOf(sentItemLocation),
                            mOpponentId);

                    Intent intent = new Intent(getPackageName() + ".Chat");
                    intent.putExtra("response", responseString);
                    intent.putExtra("sentItemLocation", sentItemLocation);
                    intent.putExtra("type", Constants.TYPE_MESSAGE_SENT);
                    LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
                    localBroadcastManager.sendBroadcast(intent);
                    stopSelf();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                attachToQue(mOpponentId, message, hasGif, gifUrl, sentItemLocation, mCurrentUserId, isAnimated);
            }
        });

    }
}
