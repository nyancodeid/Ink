package ink.activities;

import android.content.Intent;
import android.os.Bundle;

import com.google.gson.Gson;
import com.ink.R;

import java.util.ArrayList;

import ink.models.CoinsModel;
import ink.models.CoinsPackResponse;
import ink.utils.Constants;
import ink.utils.Retrofit;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BuyCoins extends BaseActivity {

    private boolean isCoinsBought;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_coins);
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

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(Constants.COINS_BOUGHT_KEY, isCoinsBought);
        setResult(Constants.BUY_COINS_REQUEST_CODE, intent);
        finish();
    }
}
