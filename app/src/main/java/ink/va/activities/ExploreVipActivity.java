package ink.va.activities;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ink.va.R;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.va.adapters.VipMemberAdapter;
import ink.va.interfaces.VipMemberItemClickListener;
import ink.va.models.UserModel;
import ink.va.utils.Retrofit;
import ink.va.utils.SharedHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExploreVipActivity extends BaseActivity implements VipMemberItemClickListener {

    @Bind(R.id.exploreVipRootTitle)
    TextView exploreVipRootTitle;
    @Bind(R.id.noVipUsers)
    TextView noVipUsers;
    @Bind(R.id.refreshVipMembers)
    ImageView refreshVipMembers;
    @Bind(R.id.vipMemberRecycler)
    RecyclerView recyclerView;

    private String chosenMembership;
    private Typeface typeface;
    private SharedHelper sharedHelper;
    private VipMemberAdapter vipMemberAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore_vip);
        ButterKnife.bind(this);
        sharedHelper = new SharedHelper(this);
        vipMemberAdapter = new VipMemberAdapter(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(vipMemberAdapter);
        setStatusBarColor(R.color.vip_status_bar_color);
        hideActionBar();
        typeface = Typeface.createFromAsset(getAssets(), "fonts/vip_regular.ttf");
        exploreVipRootTitle.setTypeface(typeface);
        noVipUsers.setTypeface(typeface);
        showVipLoading();
        getVipMembers();
        chosenMembership = getIntent().getExtras() != null ? getIntent().getExtras().getString("membershipType") : null;
    }

    private void getVipMembers() {
        vipMemberAdapter.clear();
        Call<List<UserModel>> vipMembersCall = Retrofit.getInstance().getInkService().getVipMembers(sharedHelper.getUserId());
        vipMembersCall.enqueue(new Callback<List<UserModel>>() {
            @Override
            public void onResponse(Call<List<UserModel>> call, Response<List<UserModel>> response) {
                List<UserModel> userModels = response.body();
                hideVipLoading();
                if (userModels.isEmpty()) {
                    changeNoUserTVVisibility(true);
                } else {
                    vipMemberAdapter.setUsers(userModels);
                    changeNoUserTVVisibility(false);
                }
            }

            @Override
            public void onFailure(Call<List<UserModel>> call, Throwable t) {
                Snackbar.make(exploreVipRootTitle, getString(R.string.serverErrorText), Snackbar.LENGTH_LONG).show();
                hideVipLoading();
            }
        });
    }

    private void changeNoUserTVVisibility(boolean makeVisible) {
        noVipUsers.setVisibility(makeVisible ? View.VISIBLE : View.GONE);
        refreshVipMembers.setVisibility(makeVisible ? View.GONE : View.VISIBLE);
    }


    @Override
    public void onBackPressed() {
        finish();
        overrideActivityAnimation();
    }

    @OnClick(R.id.refreshVipMembers)
    public void refreshClicked() {
        showVipLoading();
        getVipMembers();
    }

    @Override
    public void onItemClicked(@Nullable UserModel userModel) {

    }

    @Override
    public void onSendCoinsClicked(@Nullable UserModel userModel) {

    }

    @Override
    public void onSendMessageClicked(@Nullable UserModel userModel) {

    }
}
