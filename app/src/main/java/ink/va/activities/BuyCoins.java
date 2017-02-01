package ink.va.activities;

import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;

import com.android.vending.billing.IInAppBillingService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ink.va.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import ink.va.adapters.CoinsAdapter;
import ink.va.models.CoinsModel;
import ink.va.utils.Constants;
import ink.va.utils.ProcessManager;
import ink.va.utils.Retrofit;
import ink.va.utils.SharedHelper;
import ink.va.utils.User;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BuyCoins extends BaseActivity implements CoinsAdapter.ItemClick, SwipeRefreshLayout.OnRefreshListener {

    private boolean isCoinsBought;
    private IInAppBillingService mService;
    public static final String BIG_PACK = "big_pack";
    public static final String EXTRA_LARGE_PACK = "extra_large_pack";
    public static final String LARGE_PACK = "large_pack";
    public static final String MEDIUM_PACK = "medium_pack";
    public static final String SMALL_PACK = "small_pack";
    private String chosenItem;
    private Dialog mProgressDialog;
    private SharedHelper sharedHelper;
    private RecyclerView coinsRecycler;
    private SwipeRefreshLayout coinsRefresh;
    private CoinsAdapter coinsAdapter;
    private Gson gson;
    private boolean isFreedomRunning;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_coins);
        sharedHelper = new SharedHelper(this);
        gson = new Gson();
        coinsRecycler = (RecyclerView) findViewById(R.id.coinsRecycler);
        coinsRefresh = (SwipeRefreshLayout) findViewById(R.id.coinsRefresh);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        coinsRecycler.setLayoutManager(linearLayoutManager);
        getCoins();
        initializeDialog();
        ButterKnife.bind(this);
        coinsRefresh.post(new Runnable() {
            @Override
            public void run() {
                coinsRefresh.setRefreshing(true);
            }
        });
        Intent serviceIntent =
                new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

    }

    private void getCoins() {
        Call<ResponseBody> coisnCall = Retrofit.getInstance().getInkService().getCoins();
        coisnCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    return;
                }
                if (response.body() == null) {
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    Type sizesType = new TypeToken<List<CoinsModel>>() {
                    }.getType();
                    List<CoinsModel> coinsModels = gson.fromJson(jsonObject.optString("result"), sizesType);

                    coinsAdapter = new CoinsAdapter(coinsModels, BuyCoins.this);
                    coinsAdapter.setOnItemClickListener(BuyCoins.this);
                    coinsRecycler.setAdapter(coinsAdapter);
                    coinsAdapter.notifyDataSetChanged();
                    coinsRefresh.setRefreshing(false);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
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
                Bundle ownedItems = mService.getPurchases(3, getPackageName(), "inapp", null);
                int response = ownedItems.getInt("RESPONSE_CODE");

                if (response == 0) {
                    ArrayList<String> ownedSkus =
                            ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                    ArrayList<String> purchaseDataList =
                            ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
                    ArrayList<String> signatureList =
                            ownedItems.getStringArrayList("INAPP_DATA_SIGNATURE_LIST");
                    String continuationToken =
                            ownedItems.getString("INAPP_CONTINUATION_TOKEN");


                    for (int i = 0; i < purchaseDataList.size(); ++i) {
                        String purchaseData = purchaseDataList.get(i);
                        String signature = signatureList.get(i);
                        String sku = ownedSkus.get(i);

                        // do something with this purchase information
                        // e.g. display the updated list of products owned by user
                        JSONObject jsonObject = new JSONObject(purchaseData);
                        mService.consumePurchase(3, getPackageName(), jsonObject.optString("purchaseToken"));
                    }

                    // if continuationToken != null, call getPurchases again
                    // and pass in the token to retrieve more items
                }


            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (JSONException e) {
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
                isFreedomRunning = false;
                List<String> processes = ProcessManager.getRunningProcesses(this);
                for (String process : processes) {
                    if (process.contains("freedom")) {
                        isFreedomRunning = true;
                        break;
                    }
                }

                if (isFreedomRunning) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(BuyCoins.this);
                    builder.setTitle(getString(R.string.error));
                    builder.setMessage(getString(R.string.freedomRunning));
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.show();
                } else {
                    try {
                        JSONObject jo = new JSONObject(purchaseData);
                        String sku = jo.getString("productId");
                        switch (chosenItem) {
                            case SMALL_PACK:
                                showProgress();
                                isCoinsBought = true;
                                int currentUserCoins = Integer.valueOf(User.get().getCoins());
                                int finalCoins = currentUserCoins + 120;
                                User.get().setCoins(String.valueOf(finalCoins));
                                updateCoinsOnServer(String.valueOf(finalCoins));
                                break;
                            case MEDIUM_PACK:
                                showProgress();
                                isCoinsBought = true;
                                currentUserCoins = Integer.valueOf(User.get().getCoins());
                                finalCoins = currentUserCoins + 300;
                                User.get().setCoins(String.valueOf(finalCoins));
                                updateCoinsOnServer(String.valueOf(finalCoins));
                                break;
                            case BIG_PACK:
                                showProgress();
                                isCoinsBought = true;
                                currentUserCoins = Integer.valueOf(User.get().getCoins());
                                finalCoins = currentUserCoins + 500;
                                User.get().setCoins(String.valueOf(finalCoins));
                                updateCoinsOnServer(String.valueOf(finalCoins));
                                break;
                            case LARGE_PACK:
                                showProgress();
                                isCoinsBought = true;
                                currentUserCoins = Integer.valueOf(User.get().getCoins());
                                finalCoins = currentUserCoins + 800;
                                User.get().setCoins(String.valueOf(finalCoins));
                                updateCoinsOnServer(String.valueOf(finalCoins));
                                break;
                            case EXTRA_LARGE_PACK:
                                showProgress();
                                isCoinsBought = true;
                                currentUserCoins = Integer.valueOf(User.get().getCoins());
                                finalCoins = currentUserCoins + 1200;
                                User.get().setCoins(String.valueOf(finalCoins));
                                updateCoinsOnServer(String.valueOf(finalCoins));
                                break;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
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

    private void makePurchase(String purchaseId) {
        Bundle buyIntentBundle = null;
        try {
            buyIntentBundle = mService.getBuyIntent(3, getPackageName(),
                    purchaseId, "inapp", "bGoa+V7g/yqDXvKRqq+JTFn4uQZbPiQJo4pf9RzJ");
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


    private void updateCoinsOnServer(final String coinsCountToUpdate) {
        Call<okhttp3.ResponseBody> responseBodyCall = Retrofit.getInstance().getInkService().setCoins(sharedHelper.getUserId(),
                coinsCountToUpdate);
        responseBodyCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    updateCoinsOnServer(coinsCountToUpdate);
                    return;
                }
                if (response.body() == null) {
                    updateCoinsOnServer(coinsCountToUpdate);
                    return;
                }
                try {
                    String reponseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(reponseBody);
                    boolean success = jsonObject.optBoolean("success");
                    hideProgress();
                    if (success) {
                        Snackbar.make(coinsRefresh, getString(R.string.coins_bought), Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                            }
                        }).show();
                    } else {

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    public void showProgress() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressDialog.show();
            }
        });
    }

    public void hideProgress() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressDialog.dismiss();
            }
        });
    }

    private void initializeDialog() {
        mProgressDialog = new Dialog(BuyCoins.this);
        mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mProgressDialog.setContentView(R.layout.dialog_progress);
        mProgressDialog.setCancelable(false);
        mProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
    }

    @Override
    public void onItemClick(CoinsModel coinsModel) {
        chosenItem = coinsModel.coinsType;
        makePurchase(coinsModel.coinsType);
    }

    @Override
    public void onRefresh() {
        coinsRefresh.post(new Runnable() {
            @Override
            public void run() {
                coinsRefresh.setRefreshing(true);
            }
        });
        getCoins();
    }
}
