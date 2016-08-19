package ink.activities;

import android.os.Bundle;

import com.google.gson.Gson;
import com.ink.R;

import java.io.IOException;
import java.util.ArrayList;

import ink.models.CoinsModel;
import ink.models.CoinsPackResponse;
import ink.utils.Retrofit;
import ink.utils.SharedHelper;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BuyCoins extends BaseActivity {

    private SharedHelper sharedHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_coins);
        sharedHelper = new SharedHelper(this);
        getCoinsPack();
    }

    private void getCoinsPack() {
        Call<ResponseBody> coinsCall = Retrofit.getInstance().getInkService().getCoins();
        coinsCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    getCoinsPack();
                    return;
                }
                if (response.body() == null) {
                    getCoinsPack();
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    Gson gson = new Gson();
                    CoinsPackResponse coinsPackResponse = gson.fromJson(responseBody, CoinsPackResponse.class);
                    if (coinsPackResponse.success) {
                        ArrayList<CoinsModel> coinsModels = coinsPackResponse.coinsModels;
                        for (int i = 0; i < coinsModels.size(); i++) {
                            CoinsModel eachModel = coinsModels.get(i);
                        }
                    } else {
                        getCoinsPack();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                getCoinsPack();
            }
        });
    }
}
