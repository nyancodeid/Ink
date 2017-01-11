package ink.va.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.ink.va.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import ink.va.adapters.VipGlobalChatAdapter;
import ink.va.interfaces.ItemClickListener;
import ink.va.interfaces.VipGlobalChatClickListener;
import ink.va.models.VipGlobalChatModel;
import ink.va.models.VipGlobalChatResponseModel;
import ink.va.utils.Constants;
import ink.va.utils.DialogUtils;
import ink.va.utils.Retrofit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GlobalVipChat extends BaseActivity implements VipGlobalChatClickListener {

    @Bind(R.id.globalChatRecycler)
    RecyclerView globalChatRecycler;
    private String chosenMembership;
    private VipGlobalChatAdapter vipGlobalChatAdapter;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_global_vip_chat);
        ButterKnife.bind(this);
        gson = new Gson();
        vipGlobalChatAdapter = new VipGlobalChatAdapter(this);
        vipGlobalChatAdapter.setVipGlobalChatClickListener(this);
        chosenMembership = getIntent().getExtras() != null ? getIntent().getExtras().getString("membershipType") : null;
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        globalChatRecycler.setLayoutManager(linearLayoutManager);
        globalChatRecycler.setAdapter(vipGlobalChatAdapter);
        getMessages();
    }


    private void getMessages() {
        vipGlobalChatAdapter.clear();

        Call<VipGlobalChatResponseModel> getMessages = Retrofit.getInstance().getInkService().vipGlobalChatAction(null, null, null, Constants.VIP_GLOBAL_CHAT_TYPE_GET);
        getMessages.enqueue(new Callback<VipGlobalChatResponseModel>() {
            @Override
            public void onResponse(Call<VipGlobalChatResponseModel> call, Response<VipGlobalChatResponseModel> response) {
                VipGlobalChatResponseModel vipGlobalChatResponseModel = response.body();

                if (vipGlobalChatResponseModel.isSuccess()) {
                    if (vipGlobalChatResponseModel.getVipGlobalChatModels().isEmpty()) {
                        vipGlobalChatAdapter.setChatModels(vipGlobalChatResponseModel.getVipGlobalChatModels());
                    } else {
                        showNoChat();
                    }
                } else {
                    Snackbar.make(globalChatRecycler, getString(R.string.serverErrorText), Snackbar.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<VipGlobalChatResponseModel> call, Throwable t) {
                Snackbar.make(globalChatRecycler, getString(R.string.serverErrorText), Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            RemoteMessage remoteMessage = extras.getParcelable("data");
            VipGlobalChatResponseModel vipGlobalChatResponseModel = gson.fromJson(remoteMessage.getData().get("data"), VipGlobalChatResponseModel.class);
        }
    };

    @Override
    protected void onStart() {
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(getPackageName() + ".GlobalVipChat"));
        super.onStart();
    }

    private void showNoChat() {

    }

    @Override
    public void onBackPressed() {
        finish();
        overrideActivityAnimation();
    }

    @Override
    public void onItemClicked(VipGlobalChatModel vipGlobalChatModel) {

    }

    @Override
    public void onMoreIconClicked(View clickedView, final VipGlobalChatModel vipGlobalChatModel) {
        DialogUtils.showPopUp(this, clickedView, new ItemClickListener<MenuItem>() {
            @Override
            public void onItemClick(MenuItem clickedItem) {
                switch (clickedItem.getItemId()) {
                    case 0:
                        deleteMessage(vipGlobalChatModel);
                        break;
                }
            }
        }, getString(R.string.delete));
    }

    private void deleteMessage(final VipGlobalChatModel vipGlobalChatModel) {
        Call<VipGlobalChatResponseModel> deleteCall = Retrofit.getInstance().getInkService().vipGlobalChatAction(String.valueOf(vipGlobalChatModel.getMessageId()),
                null, null, null);
        deleteCall.enqueue(new Callback<VipGlobalChatResponseModel>() {
            @Override
            public void onResponse(Call<VipGlobalChatResponseModel> call, Response<VipGlobalChatResponseModel> response) {
                if (response.body().isSuccess()) {
                    vipGlobalChatAdapter.removeItem(vipGlobalChatModel);
                    Snackbar.make(globalChatRecycler, getString(R.string.messageDeleted), Snackbar.LENGTH_SHORT).setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    }).show();
                } else {
                    Snackbar.make(globalChatRecycler, getString(R.string.messagedeleteError), Snackbar.LENGTH_SHORT).setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    }).show();
                }
            }

            @Override
            public void onFailure(Call<VipGlobalChatResponseModel> call, Throwable t) {
                Snackbar.make(globalChatRecycler, getString(R.string.serverErrorText), Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
}
