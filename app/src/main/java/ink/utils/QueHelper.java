package ink.utils;

import android.util.Log;

import java.io.IOException;

import ink.callbacks.QueCallback;
import ink.models.ChatModel;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by USER on 2016-06-26.
 */
public class QueHelper {
    private ChatModel chatModel;

    public QueHelper() {
    }

    public ChatModel getChatModel() {
        return chatModel;
    }

    public void attachToQue(final String mCurrentUserId,
                            final String mOpponentId,
                            final String message, final int sentItemLocation, final QueCallback queCallback) {
        Call<ResponseBody> sendMessageResponse = Retrofit.getInstance().getInkService().sendMessage(mCurrentUserId,
                mOpponentId, message, Time.getTimeZone());

        sendMessageResponse.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String responseString = response.body().string();
                    Log.d("fasfkjlasfas", "onResponse: "+responseString);
                    queCallback.onMessageSent(responseString, sentItemLocation);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                queCallback.onMessageSentFail(QueHelper.this, message, sentItemLocation);
            }
        });

    }
}
