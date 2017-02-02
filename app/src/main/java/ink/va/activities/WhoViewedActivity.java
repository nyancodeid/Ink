package ink.va.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ink.va.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ink.va.adapters.WhoViewedAdapter;
import ink.va.models.WhoViewedModel;
import ink.va.utils.DimDialog;
import ink.va.utils.RealmHelper;
import ink.va.utils.Retrofit;
import ink.va.utils.SharedHelper;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WhoViewedActivity extends AppCompatActivity implements
        SwipeRefreshLayout.OnRefreshListener, WhoViewedAdapter.OnItemClickListener {


    @BindView(R.id.recyclerWhoViewed)
    RecyclerView recyclerView;
    @BindView(R.id.swipeWhoViewed)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.noProfileViewWrapper)
    RelativeLayout noProfileViewWrapper;
    @BindView(R.id.totalAccountViews)
    TextView totalAccountViews;

    private SharedHelper sharedHelper;

    private List<WhoViewedModel> whoViewedModels;
    private WhoViewedModel whoViewedModel;
    private WhoViewedAdapter whoViewedAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_who_viewed);
        ButterKnife.bind(this);
        whoViewedModels = new ArrayList<>();
        whoViewedAdapter = new WhoViewedAdapter(this, whoViewedModels);
        whoViewedAdapter.setOnItemClickListener(this);
        sharedHelper = new SharedHelper(this);
        swipeRefreshLayout.setOnRefreshListener(this);

        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(500);
        itemAnimator.setRemoveDuration(500);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(itemAnimator);

        recyclerView.setAdapter(whoViewedAdapter);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setTitle(getString(R.string.whoViewedText));
        getWhoViewedList();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        getWhoViewedList();
    }

    public void getWhoViewedList() {
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
            }
        });
        final Call<okhttp3.ResponseBody> getWhoViewed = Retrofit.getInstance().getInkService().getWhoViewed(sharedHelper.getUserId());
        getWhoViewed.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    getWhoViewedList();
                    return;
                }
                if (response.body() == null) {
                    getWhoViewedList();
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    int totalViews = jsonObject.optInt("totalViews");
                    totalAccountViews.setText(getString(R.string.totalAccountViews, totalViews));
                    if (success) {
                        whoViewedModels.clear();
                        whoViewedAdapter.notifyDataSetChanged();
                        JSONArray result = jsonObject.optJSONArray("result");
                        if (result.length() <= 0) {
                            noProfileViewWrapper.setVisibility(View.VISIBLE);
                        } else {
                            noProfileViewWrapper.setVisibility(View.GONE);
                            for (int i = 0; i < result.length(); i++) {
                                JSONObject eachObject = result.optJSONObject(i);
                                String firstName = eachObject.optString("firstName");
                                String lastName = eachObject.optString("lastName");
                                String imageLink = eachObject.optString("imageLink");
                                boolean isSocialAccount = eachObject.optBoolean("isSocialAccount");
                                boolean isFriend = eachObject.optBoolean("isFriend");
                                String userId = eachObject.optString("userId");
                                String timeViewed = eachObject.optString("timeViewed");
                                whoViewedModel = new WhoViewedModel(firstName,
                                        lastName, imageLink, isSocialAccount, isFriend, userId, timeViewed);
                                whoViewedModels.add(whoViewedModel);
                                int index = whoViewedModels.indexOf(whoViewedModel);
                                whoViewedAdapter.notifyItemInserted(index);
                            }
                        }

                        swipeRefreshLayout.setRefreshing(false);
                    } else {
                        Toast.makeText(WhoViewedActivity.this, getString(R.string.serverErrorText), Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
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

    @Override
    public void onMoreItemClicked(final WhoViewedModel whoViewedModel, View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenu().add(getString(R.string.removeFromFriends));
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getTitle().toString().equals(getString(R.string.removeFromFriends))) {
                    removeFriend(whoViewedModel.getUserId());
                }
                return false;
            }
        });
        popupMenu.show();
    }

    @Override
    public void onCardItemClicked(WhoViewedModel whoViewedModel) {
        String opponentId = whoViewedModel.getUserId();
        Intent intent = new Intent(this, OpponentProfile.class);
        intent.putExtra("firstName", whoViewedModel.getFirstName());
        intent.putExtra("lastName", whoViewedModel.getLastName());
        intent.putExtra("isFriend", whoViewedModel.isFriend());
        intent.putExtra("id", opponentId);
        startActivity(intent);
    }


    private void removeFriend(final String friendId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(WhoViewedActivity.this);
        builder.setTitle(getString(R.string.removeFriend));
        builder.setMessage(getString(R.string.removefriendHint));
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                DimDialog.showDimDialog(WhoViewedActivity.this, getString(R.string.removingFriend));
                RealmHelper.getInstance().removeMessage(friendId, sharedHelper.getUserId());
                Call<ResponseBody> removeFriendCall = Retrofit.getInstance().getInkService().removeFriend(sharedHelper.getUserId(),
                        friendId);
                removeFriendCall.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response == null) {
                            removeFriend(friendId);
                            return;
                        }
                        if (response.body() == null) {
                            removeFriend(friendId);
                            return;
                        }
                        try {
                            String responseBody = response.body().string();
                            JSONObject jsonObject = new JSONObject(responseBody);
                            boolean success = jsonObject.optBoolean("success");
                            DimDialog.hideDialog();
                            if (success) {
                                Snackbar.make(recyclerView, getString(R.string.friendRemoved), Snackbar.LENGTH_SHORT).setAction("OK", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                    }
                                }).show();
                                getWhoViewedList();
                            }
                        } catch (IOException e) {
                            DimDialog.hideDialog();
                            e.printStackTrace();
                        } catch (JSONException e) {
                            DimDialog.hideDialog();
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        removeFriend(friendId);
                    }
                });
            }
        });
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();


    }
}
