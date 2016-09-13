package ink.va.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

import org.json.JSONArray;
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
 * Created by USER on 2016-06-26.
 */
public class BackgroundTaskService extends Service {
    private SharedHelper mSharedHelper;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle extras = intent.getExtras();
        mSharedHelper = new SharedHelper(this);
        getMyMessages(mSharedHelper.getUserId());
        return super.onStartCommand(intent, flags, startId);
    }

    private void getMyMessages(final String userId) {
        RealmHelper.getInstance().clearDatabase(this);
        Call<ResponseBody> myMessagesResponse = Retrofit.getInstance().getInkService().getChatMessages(userId);
        myMessagesResponse.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    getMyMessages(userId);
                    return;
                }
                if (response.body() == null) {
                    getMyMessages(userId);
                    return;
                }
                try {
                    String responseString = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseString);
                    JSONArray messagesArray = jsonObject.optJSONArray("messages");
                    RealmHelper realmHelper = RealmHelper.getInstance();
                    if (messagesArray.length() > 0) {
                        for (int i = 0; i < messagesArray.length(); i++) {
                            JSONObject eachObject = messagesArray.optJSONObject(i);
                            String userId = eachObject.optString("user_id");
                            String opponentId = eachObject.optString("opponent_id");
                            String message = eachObject.optString("message");
                            String messageId = eachObject.optString("message_id");
                            String date = eachObject.optString("date");
                            String deliveryStatus = Constants.STATUS_DELIVERED;
                            String userIdImage = eachObject.optString("user_id_image");
                            String opponentImage = eachObject.optString("opponent_id_image");
                            String deleteUserId = eachObject.optString("delete_user_id");
                            String deleteOpponentId = eachObject.optString("delete_opponent_id");

                            boolean hasGif = eachObject.optBoolean("hasGif");
                            String gifUrl = eachObject.optString("gifUrl");
                            String isAnimated = eachObject.optString("isAnimated");
                            String hasSound = eachObject.optString("hasSound");

                            if (mSharedHelper.getUserId().equals(userId)) {

                                realmHelper.insertMessage(userId,
                                        opponentId, message, messageId, Time.convertToLocalTime(date), messageId,
                                        deliveryStatus,
                                        userIdImage, opponentImage, deleteOpponentId, deleteUserId, hasGif, gifUrl);
                            } else {

                                realmHelper.insertMessage(userId,
                                        opponentId, message, messageId, date, messageId,
                                        deliveryStatus,
                                        userIdImage, opponentImage, deleteOpponentId, deleteUserId, hasGif, gifUrl);
                            }
                        }
                    }
//                    mSharedHelper.setMessagesDownloaded();
                    stopSelf();

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                getMyMessages(userId);
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
