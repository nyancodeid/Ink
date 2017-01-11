package ink.va.activities;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;

import com.ink.va.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import ink.va.models.VipGlobalChatResponseModel;
import ink.va.utils.Constants;
import ink.va.utils.Retrofit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GlobalVipChat extends BaseActivity {

    @Bind(R.id.globalChatRecycler)
    RecyclerView globalChatRecycler;
    private String chosenMembership;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_global_vip_chat);
        ButterKnife.bind(this);
        chosenMembership = getIntent().getExtras() != null ? getIntent().getExtras().getString("membershipType") : null;
        getMessages();
    }


    private void getMessages() {
        Call<VipGlobalChatResponseModel> getMessages = Retrofit.getInstance().getInkService().vipGlobalChatAction(null, null, Constants.VIP_GLOBAL_CHAT_TYPE_GET);
        getMessages.enqueue(new Callback<VipGlobalChatResponseModel>() {
            @Override
            public void onResponse(Call<VipGlobalChatResponseModel> call, Response<VipGlobalChatResponseModel> response) {
                VipGlobalChatResponseModel vipGlobalChatResponseModel = response.body();

                if (vipGlobalChatResponseModel.isSuccess()) {
                    if (vipGlobalChatResponseModel.getVipGlobalChatModels().isEmpty()) {

                    } else {

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

    @Override
    public void onBackPressed() {
        finish();
        overrideActivityAnimation();
    }
}
