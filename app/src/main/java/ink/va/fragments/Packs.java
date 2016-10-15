package ink.va.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.ink.va.R;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import ink.va.adapters.PacksAdapter;
import ink.va.models.PacksModel;
import ink.va.models.PacksResponse;
import ink.va.utils.Retrofit;
import ink.va.utils.User;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by USER on 2016-07-20.
 */
public class Packs extends Fragment implements PacksAdapter.PackClickListener, SwipeRefreshLayout.OnRefreshListener {

    @Bind(R.id.packs_recycler)
    RecyclerView packsRecycler;
    @Bind(R.id.packsSwipe)
    SwipeRefreshLayout swipeRefreshLayout;

    private PacksAdapter packsAdapter;

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
        swipeRefreshLayout.setOnRefreshListener(this);
        packsAdapter = new PacksAdapter(this, getActivity());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        packsRecycler.setLayoutManager(linearLayoutManager);
        packsRecycler.setAdapter(packsAdapter);
        getPacks();

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
                        // TODO: 2016-10-15 set adapter
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
    public void onBuyClicked(int packPrice) {
        int userCoins = Integer.valueOf(User.get().getCoins());
        if (userCoins < packPrice) {
            Snackbar.make(packsRecycler, getString(R.string.not_enough_coins), Snackbar.LENGTH_SHORT).show();
        } else {

        }
    }

    @Override
    public void onRefresh() {
        getPacks();
    }
}
