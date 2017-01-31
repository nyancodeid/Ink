package ink.va.activities;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.Toast;

import com.ink.va.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import ink.va.adapters.BadgeAdapter;
import ink.va.interfaces.BadgeClickListener;
import ink.va.models.BadgeModel;
import ink.va.models.BadgeResponseModel;
import ink.va.utils.Constants;
import ink.va.utils.Retrofit;
import ink.va.utils.SharedHelper;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by PC-Comp on 1/31/2017.
 */

public class BadgeShop extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener, BadgeClickListener {

    @Bind(R.id.badgeRecycler)
    RecyclerView badgeRecycler;
    @Bind(R.id.badgeRefresh)
    SwipeRefreshLayout badgeRefresh;
    private BadgeAdapter badgeAdapter;
    private SharedHelper sharedHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.badge_shop_view);
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        badgeAdapter = new BadgeAdapter(this);
        sharedHelper = new SharedHelper(this);
        badgeRefresh.setOnRefreshListener(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        badgeRecycler.setLayoutManager(linearLayoutManager);
        badgeRecycler.setAdapter(badgeAdapter);
        badgeAdapter.setOnClickListener(this);
        badgeRefresh.post(new Runnable() {
            @Override
            public void run() {
                badgeRefresh.setRefreshing(true);
            }
        });
        getBadges();
    }

    @Override
    public void onRefresh() {
        badgeAdapter.clear();
        getBadges();
    }


    @Override
    public void onBuyClicked(BadgeModel badgeModel) {

    }

    public void getBadges() {
        Call<BadgeResponseModel> modelCall = Retrofit.getInstance().getInkService().getBagdes(Constants.BADGE_TYPE_VIEW);
        modelCall.enqueue(new Callback<BadgeResponseModel>() {
            @Override
            public void onResponse(Call<BadgeResponseModel> call, Response<BadgeResponseModel> response) {
                if (badgeRefresh.isRefreshing()) {
                    badgeRefresh.setRefreshing(false);
                }
                badgeAdapter.setBadgeModels(response.body().getBadgeModels());
            }

            @Override
            public void onFailure(Call<BadgeResponseModel> call, Throwable t) {
                if (badgeRefresh.isRefreshing()) {
                    badgeRefresh.setRefreshing(false);
                }
                Toast.makeText(BadgeShop.this, getString(R.string.serverErrorText), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    public void buyBadge(final String badgeId) {
        Call<ResponseBody> modelCall = Retrofit.getInstance().getInkService().buyBadge(Constants.BADGE_TYPE_BUY, sharedHelper.getUserId(), badgeId);
        modelCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    buyBadge(badgeId);
                    return;
                }
                if (response.body() == null) {
                    buyBadge(badgeId);
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                } catch (IOException e) {
                    Toast.makeText(BadgeShop.this, getString(R.string.serverErrorText), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                } catch (JSONException e) {
                    Toast.makeText(BadgeShop.this, getString(R.string.serverErrorText), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(BadgeShop.this, getString(R.string.serverErrorText), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
