package ink.va.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import ink.va.models.MessageModel;
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
        mSharedHelper = new SharedHelper(this);
        getMyMessages(mSharedHelper.getUserId());
        return super.onStartCommand(intent, flags, startId);
    }

    private void getMyMessages(final String userId) {
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
                    List<MessageModel> messageModels = new LinkedList<>();

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
                            boolean isAnimated = eachObject.optBoolean("isAnimated");
                            String hasSound = eachObject.optString("hasSound");

                            MessageModel messageModel = new MessageModel();
                            if (mSharedHelper.getUserId().equals(userId)) {
                                messageModel.setUserId(userId);
                                messageModel.setOpponentId(opponentId);
                                messageModel.setMessage(message);
                                messageModel.setMessageId(messageId);
                                messageModel.setDate(Time.convertToLocalTime(date));
                                messageModel.setDeliveryStatus(deliveryStatus);
                                messageModel.setUserImage(userIdImage);
                                messageModel.setOpponentImage(opponentImage);
                                messageModel.setDeleteOpponentId(deleteOpponentId);
                                messageModel.setDeleteUserId(deleteUserId);
                                messageModel.setHasGif(hasGif);
                                messageModel.setGifUrl(gifUrl);
                                messageModel.setAnimated(isAnimated);
                            } else {
                                messageModel.setUserId(userId);
                                messageModel.setOpponentId(opponentId);
                                messageModel.setMessage(message);
                                messageModel.setMessageId(messageId);
                                messageModel.setDate(date);
                                messageModel.setDeliveryStatus(deliveryStatus);
                                messageModel.setUserImage(userIdImage);
                                messageModel.setOpponentImage(opponentImage);
                                messageModel.setDeleteOpponentId(deleteOpponentId);
                                messageModel.setDeleteUserId(deleteUserId);
                                messageModel.setHasGif(hasGif);
                                messageModel.setGifUrl(gifUrl);
                                messageModel.setAnimated(isAnimated);
                            }
                            messageModels.add(messageModel);
                        }
                        realmHelper.insertMessage(messageModels);
                    }else{
                        RealmHelper.getInstance().deleteMessages();
                    }
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
