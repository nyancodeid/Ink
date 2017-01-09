package ink.va.activities;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

import com.ink.va.R;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import ink.va.models.UserModel;
import ink.va.utils.Retrofit;
import ink.va.utils.SharedHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExploreVipActivity extends BaseActivity {

    @Bind(R.id.exploreVipRootTitle)
    TextView exploreVipRootTitle;
    @Bind(R.id.noVipUsers)
    TextView noVipUsers;

    private String chosenMembership;
    private Typeface typeface;
    private SharedHelper sharedHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore_vip);
        ButterKnife.bind(this);
        sharedHelper = new SharedHelper(this);
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
        Call<List<UserModel>> vipMembersCall = Retrofit.getInstance().getInkService().getVipMembers(sharedHelper.getUserId());
        vipMembersCall.enqueue(new Callback<List<UserModel>>() {
            @Override
            public void onResponse(Call<List<UserModel>> call, Response<List<UserModel>> response) {
                List<UserModel> userModels = response.body();
                hideVipLoading();
                if (userModels.isEmpty()) {
                    changeNoUserTVVisibility(true);
                } else {
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
    }
}
