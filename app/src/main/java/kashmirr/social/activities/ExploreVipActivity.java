package kashmirr.social.activities;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kashmirr.social.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import kashmirr.social.adapters.VipMemberAdapter;
import kashmirr.social.interfaces.RequestCallback;
import kashmirr.social.interfaces.VipMemberItemClickListener;
import kashmirr.social.models.UserModel;
import kashmirr.social.utils.ProgressDialog;
import kashmirr.social.utils.Retrofit;
import kashmirr.social.utils.SharedHelper;
import kashmirr.social.utils.User;
import okhttp3.ResponseBody;

public class ExploreVipActivity extends BaseActivity implements VipMemberItemClickListener {

    @BindView(R.id.exploreVipRootTitle)
    TextView exploreVipRootTitle;
    @BindView(R.id.noVipUsers)
    TextView noVipUsers;
    @BindView(R.id.refreshVipMembers)
    ImageView refreshVipMembers;
    @BindView(R.id.vipMemberRecycler)
    RecyclerView recyclerView;

    private String chosenMembership;
    private Typeface typeface;
    private SharedHelper sharedHelper;
    private VipMemberAdapter vipMemberAdapter;
    private ProgressDialog transferDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore_vip);
        ButterKnife.bind(this);
        sharedHelper = new SharedHelper(this);
        transferDialog = new ProgressDialog();
        transferDialog.setTitle(getString(R.string.trasnferring));
        transferDialog.setMessage(getString(R.string.trasnferring_coins_text));
        vipMemberAdapter = new VipMemberAdapter(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
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
        makeRequest(Retrofit.getInstance().getInkService().getVipMembers(sharedHelper.getUserId()), null, new RequestCallback() {
            @Override
            public void onRequestSuccess(Object result) {
                List<UserModel> userModels = (List<UserModel>) result;
                hideVipLoading();
                if (userModels.isEmpty()) {
                    changeNoUserTVVisibility(true);
                } else {
                    for (int i = 0; i < userModels.size(); i++) {
                        UserModel singleModel = userModels.get(i);
                        vipMemberAdapter.addSingleUser(singleModel);
                    }
                    changeNoUserTVVisibility(false);
                }
            }

            @Override
            public void onRequestFailed(Object[] result) {
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
        showVipCoinsDialog(userModel);
    }

    private void showVipCoinsDialog(final UserModel userModel) {
        final Dialog dialog = new Dialog(ExploreVipActivity.this);
        dialog.setContentView(R.layout.coins_chooser_dialog);
        final Button acceptCoins = (Button) dialog.findViewById(R.id.acceptCoins);
        final EditText coinsFiled = (EditText) dialog.findViewById(R.id.coinsFiled);
        acceptCoins.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (coinsFiled.getText().toString().trim().isEmpty()) {
                    coinsFiled.setError(getString(R.string.emptyField));
                } else {
                    coinsFiled.setError(null);
                    int inputtedCoins;
                    try {
                        inputtedCoins = Integer.valueOf(coinsFiled.getText().toString().trim());
                    } catch (NumberFormatException e) {
                        inputtedCoins = 0;
                        e.printStackTrace();
                    }
                    if (inputtedCoins == 0) {
                        dialog.dismiss();
                        Snackbar.make(recyclerView, getString(R.string.input_number), Snackbar.LENGTH_LONG).show();
                    } else if (Integer.valueOf(User.get().getCoins()) < inputtedCoins) {
                        dialog.dismiss();
                        Snackbar.make(recyclerView, getString(R.string.not_enough_coins), Snackbar.LENGTH_LONG).show();
                    } else {
                        dialog.dismiss();
                        transferCoins(inputtedCoins, sharedHelper.getUserId(), userModel.getUserId());
                    }
                }

            }
        });
        dialog.show();
    }

    private void transferCoins(final int coinsAmount, final String transferrerId, final String receiverId) {
        transferDialog.show();
        makeRequest(Retrofit.getInstance().getInkService().transferCoins(transferrerId, receiverId, coinsAmount), null, new RequestCallback() {
            @Override
            public void onRequestSuccess(Object result) {
                try {
                    String responseBoy = ((ResponseBody) result).string();
                    JSONObject jsonObject = new JSONObject(responseBoy);
                    boolean success = jsonObject.optBoolean("success");
                    if (success) {
                        transferDialog.hide();
                        int userCoinsLeft = jsonObject.optInt("userCoinsLeft");
                        User.get().setCoins(userCoinsLeft);
                        Toast.makeText(ExploreVipActivity.this, getString(R.string.coins_transferred), Toast.LENGTH_SHORT).show();
                    } else {
                        Snackbar.make(recyclerView, getString(R.string.serverErrorText), Snackbar.LENGTH_LONG).show();
                    }
                } catch (IOException e) {
                    Snackbar.make(recyclerView, getString(R.string.serverErrorText), Snackbar.LENGTH_LONG).show();
                    transferDialog.hide();
                    e.printStackTrace();
                } catch (JSONException e) {
                    Snackbar.make(recyclerView, getString(R.string.serverErrorText), Snackbar.LENGTH_LONG).show();
                    transferDialog.hide();
                    e.printStackTrace();
                }
            }

            @Override
            public void onRequestFailed(Object[] result) {
                transferDialog.hide();
            }
        });
    }

    @Override
    public void onSendMessageClicked(@Nullable UserModel userModel) {
        Intent intent = new Intent(getApplicationContext(), Chat.class);
        intent.putExtra("firstName", userModel.getFirstName());
        intent.putExtra("lastName", userModel.getLastName());
        intent.putExtra("opponentId", userModel.getUserId());
        intent.putExtra("isSocialAccount", userModel.isSocialAccount());
        intent.putExtra("opponentImage", userModel.getImageUrl());
        startActivity(intent);
        overrideActivityAnimation();
    }
}
