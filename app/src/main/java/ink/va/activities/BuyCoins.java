package ink.va.activities;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.android.vending.billing.IInAppBillingService;
import com.google.gson.Gson;
import com.ink.va.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ink.va.models.CoinsModel;
import ink.va.models.CoinsPackResponse;
import ink.va.utils.Constants;
import ink.va.utils.Retrofit;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BuyCoins extends BaseActivity {

    private boolean isCoinsBought;
    private IInAppBillingService mService;
    public static final String TEST_PURCHASE_RESPONSE = "android.test.purchased";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_coins);
        getCoinsPack();

        Intent serviceIntent =
                new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }


    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name,
                                       IBinder service) {
            mService = IInAppBillingService.Stub.asInterface(service);
            try {
                Bundle buyIntentBundle = mService.getBuyIntent(3, getPackageName(),
                        "small_pack", "inapp", "bGoa+V7g/yqDXvKRqq+JTFn4uQZbPiQJo4pf9RzJ");
                PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                startIntentSenderForResult(pendingIntent.getIntentSender(),
                        1001, new Intent(), Integer.valueOf(0), Integer.valueOf(0),
                        Integer.valueOf(0));
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1001) {
            int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
            String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");

            if (resultCode == RESULT_OK) {
                try {
                    JSONObject jo = new JSONObject(purchaseData);
                    String sku = jo.getString("productId");
                    Log.d("fasfasfa", "onActivityResult: " + "item bought" + sku);
                } catch (JSONException e) {
                    Log.d("fasfasfa", "onActivityResult: " + "failed to purchase");
                    e.printStackTrace();
                }
            }
        }
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
    protected void onDestroy() {
        if (serviceConnection != null) {
            unbindService(serviceConnection);
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(Constants.COINS_BOUGHT_KEY, isCoinsBought);
        setResult(Constants.BUY_COINS_REQUEST_CODE, intent);
        finish();
    }
}
