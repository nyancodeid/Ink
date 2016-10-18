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
import com.ink.va.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.va.utils.Constants;

public class BuyCoins extends BaseActivity {

    private boolean isCoinsBought;
    private IInAppBillingService mService;
    public static final String TEST_PURCHASE_RESPONSE = "android.test.purchased";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_coins);
        ButterKnife.bind(this);

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
                Bundle ownedItems = mService.getPurchases(3, getPackageName(), "inapp", null);
                Log.d("Fasfsafasfasfa", "onServiceConnected: " + ownedItems);

                int response = ownedItems.getInt("RESPONSE_CODE");
                Log.d("Fasfsafasfasfa", "onServiceConnected: " + response);

                if (response == 0) {
                    ArrayList<String> ownedSkus =
                            ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                    ArrayList<String> purchaseDataList =
                            ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
                    ArrayList<String> signatureList =
                            ownedItems.getStringArrayList("INAPP_DATA_SIGNATURE_LIST");
                    String continuationToken =
                            ownedItems.getString("INAPP_CONTINUATION_TOKEN");


                    Log.d("Fasfsafasfasfa", "onServiceConnected: " + ownedSkus.size());
                    Log.d("Fasfsafasfasfa", "onServiceConnected: " + purchaseDataList.size());
                    Log.d("Fasfsafasfasfa", "onServiceConnected: " + signatureList.size());
                    Log.d("Fasfsafasfasfa", "onServiceConnected: " + continuationToken);

                    for (int i = 0; i < purchaseDataList.size(); ++i) {
                        String purchaseData = purchaseDataList.get(i);
                        String signature = signatureList.get(i);
                        String sku = ownedSkus.get(i);

                        // do something with this purchase information
                        // e.g. display the updated list of products owned by user
                        Log.d("Fasfsafasfasfa", "onServiceConnected: " + "purchased items are" + sku + " " + signature + " " + purchaseData);
                    }

                    // if continuationToken != null, call getPurchases again
                    // and pass in the token to retrieve more items
                }


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


    @OnClick(R.id.buy_zero_ninteen)
    public void zeroClicked() {

    }

    @OnClick(R.id.buy_one_ninteen)
    public void threeHundredClicked() {

    }

    @OnClick(R.id.buy_two_ninteen)
    public void twoClicked() {

    }


    @OnClick(R.id.buy_three_ninteen)
    public void threeClicked() {

    }


    @OnClick(R.id.buy_four_ninteen)
    public void fourClicked() {

    }


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
