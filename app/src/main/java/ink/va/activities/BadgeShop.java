package ink.va.activities;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.ink.va.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import ink.va.adapters.BadgeAdapter;
import ink.va.interfaces.BadgeClickListener;
import ink.va.interfaces.RequestCallback;
import ink.va.models.BadgeModel;
import ink.va.models.BadgeResponseModel;
import ink.va.utils.Constants;
import ink.va.utils.ErrorCause;
import ink.va.utils.Retrofit;
import ink.va.utils.SharedHelper;
import okhttp3.ResponseBody;

/**
 * Created by PC-Comp on 1/31/2017.
 */

public class BadgeShop extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener, BadgeClickListener, RequestCallback {

    @BindView(R.id.badgeRecycler)
    RecyclerView badgeRecycler;
    @BindView(R.id.badgeRefresh)
    SwipeRefreshLayout badgeRefresh;
    @BindView(R.id.badgeShopBG)
    RelativeLayout badgeShopBG;
    private BadgeAdapter badgeAdapter;
    private SharedHelper sharedHelper;
    private android.app.ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.badge_shop_view);
        ButterKnife.bind(this);
        progressDialog = new android.app.ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.pleaseWait));
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage(getString(R.string.purchasing));
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

        if (sharedHelper.getFeedColor() != null) {
            badgeShopBG.setBackgroundColor(Color.parseColor(sharedHelper.getFeedColor()));
        }


        getBadges();
    }

    @Override
    public void onRefresh() {
        badgeAdapter.clear();
        getBadges();
    }


    @Override
    public void onBuyClicked(BadgeModel badgeModel) {
        progressDialog.show();
        buyBadge(badgeModel.getBadgeId());
    }

    public void getBadges() {
        makeRequest(Retrofit.getInstance().getInkService().getBagdes(Constants.BADGE_TYPE_VIEW), badgeRefresh, true, this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    public void buyBadge(final String badgeId) {
        makeRequest(Retrofit.getInstance().getInkService().buyBadge(Constants.BADGE_TYPE_BUY, sharedHelper.getUserId(), badgeId), null, false, new RequestCallback() {
            @Override
            public void onRequestSuccess(Object result) {
                progressDialog.dismiss();
                try {
                    String responseBody = ((ResponseBody) result).string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    String cause = jsonObject.optString("cause");
                    if (success) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(BadgeShop.this);
                        builder.setTitle(getString(R.string.congratulation));
                        builder.setMessage(getString(R.string.badgeBought));
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        builder.show();
                    } else if (cause.equals(ErrorCause.NOT_ENOUGH_COINS)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(BadgeShop.this);
                        builder.setMessage(getString(R.string.not_enough_coins));
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        builder.show();
                    } else if (cause.equals(ErrorCause.ALREADY_BOUGHT)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(BadgeShop.this);
                        builder.setTitle(getString(R.string.error));
                        builder.setMessage(getString(R.string.badge_already_bought));
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        builder.show();
                    } else {
                        Toast.makeText(BadgeShop.this, getString(R.string.serverErrorText), Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    progressDialog.dismiss();
                    Toast.makeText(BadgeShop.this, getString(R.string.serverErrorText), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                } catch (JSONException e) {
                    progressDialog.dismiss();
                    Toast.makeText(BadgeShop.this, getString(R.string.serverErrorText), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }

            @Override
            public void onRequestFailed(Object[] result) {
                progressDialog.dismiss();
            }
        });

    }

    @Override
    public void onRequestSuccess(Object result) {
        badgeAdapter.setBadgeModels(((BadgeResponseModel) result).getBadgeModels());
    }

    @Override
    public void onRequestFailed(Object[] result) {

    }
}
