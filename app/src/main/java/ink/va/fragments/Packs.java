package ink.va.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.google.gson.Gson;
import com.ink.va.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import ink.va.activities.Shop;
import ink.va.adapters.PacksAdapter;
import ink.va.models.PacksModel;
import ink.va.models.PacksResponse;
import ink.va.utils.ErrorCause;
import ink.va.utils.Retrofit;
import ink.va.utils.SharedHelper;
import ink.va.utils.User;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tyrantgit.explosionfield.ExplosionField;

/**
 * Created by USER on 2016-07-20.
 */
public class Packs extends Fragment implements PacksAdapter.PackClickListener, SwipeRefreshLayout.OnRefreshListener {

    @Bind(R.id.packs_recycler)
    RecyclerView packsRecycler;
    @Bind(R.id.packsSwipe)
    SwipeRefreshLayout swipeRefreshLayout;
    private Dialog mProgressDialog;

    private PacksAdapter packsAdapter;
    private SharedHelper sharedHelper;
    private ExplosionField mExplosionField;

    public static Packs create() {
        Packs packs = new Packs();
        return packs;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.packs_layout, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeDialog();
        sharedHelper = new SharedHelper(getActivity());
        swipeRefreshLayout.setOnRefreshListener(this);
        packsAdapter = new PacksAdapter(this, getActivity());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        packsRecycler.setLayoutManager(linearLayoutManager);
        packsRecycler.setAdapter(packsAdapter);
        getPacks();
        mExplosionField = ExplosionField.attach2Window(getActivity());

    }

    private void initializeDialog() {
        mProgressDialog = new Dialog(getActivity());
        mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mProgressDialog.setContentView(R.layout.dialog_progress);
        mProgressDialog.setCancelable(false);
        mProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
    }

    private void getPacks() {
        if (!swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(true);
                }
            });
        }
        Call<ResponseBody> packsCall = Retrofit.getInstance().getInkService().getPacks();
        packsCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    getPacks();
                    return;
                }
                if (response.body() == null) {
                    getPacks();
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    Gson gson = new Gson();
                    PacksResponse packsResponse = gson.fromJson(responseBody, PacksResponse.class);
                    if (packsResponse.success) {
                        ArrayList<PacksModel> packsModels = packsResponse.packsModels;
                        packsAdapter.setData(packsModels);
                        swipeRefreshLayout.post(new Runnable() {
                            @Override
                            public void run() {
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        });
                    } else {
                        getPacks();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                getPacks();
            }
        });
    }

    @Override
    public void onBuyClicked(final int packPrice, final String packId, final View clickedView) {
        mExplosionField.explode(clickedView, new ExplosionField.ExplosionAnimationListener() {
            @Override
            public void onAnimationEnd() {
                if (User.get().getCoins() != null || !User.get().getCoins().isEmpty()) {
                    int userCoins = Integer.valueOf(User.get().getCoins());
                    if (userCoins < packPrice) {
                        Snackbar.make(packsRecycler, getString(R.string.not_enough_coins), Snackbar.LENGTH_SHORT).show();
                    } else {
                        showProgress();
                        openPack(packId);
                    }
                } else {
                    Snackbar.make(packsRecycler, getString(R.string.pleaseWait), Snackbar.LENGTH_SHORT).setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    }).show();
                }
            }
        });


    }

    public void showProgress() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressDialog.show();
            }
        });
    }

    public void hideProgress() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressDialog.dismiss();
            }
        });
    }

    private void openPack(final String packId) {
        Call<ResponseBody> packCall = Retrofit.getInstance().getInkService().openPack(sharedHelper.getUserId(), packId);
        packCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    openPack(packId);
                    return;
                }
                if (response.body() == null) {
                    openPack(packId);
                    return;
                }
                try {
                    hideProgress();
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    if (success) {
                        String userCoinsLeft = jsonObject.optString("userCoinsLeft");
                        User.get().setCoins(userCoinsLeft);
                        ((Shop) getActivity()).updateCoins();
                        builder.setTitle(getString(R.string.pack_bought));
                        builder.setMessage(getString(R.string.gift_bought_message));
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        builder.show();
                    } else {
                        String cause = jsonObject.optString("cause");
                        if (cause.equals(ErrorCause.PACK_ALREADY_BOUGHT)) {
                            builder.setTitle(getString(R.string.error));
                            builder.setMessage(getString(R.string.gift_already_bought));
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });
                            builder.show();
                        } else {
                            builder.setTitle(getString(R.string.error));
                            builder.setMessage(getString(R.string.serverErrorText));
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });
                            builder.show();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    hideProgress();
                } catch (JSONException e) {
                    e.printStackTrace();
                    hideProgress();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    @Override
    public void onRefresh() {
        getPacks();
    }
}
