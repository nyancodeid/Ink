package ink.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.RelativeLayout;

import com.ink.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ink.adapters.HintAdapter;
import ink.utils.Constants;
import ink.utils.Retrofit;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by PC-Comp on 9/12/2016.
 */
public class WhatsTrending extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private SwipeRefreshLayout trendSwipe;
    private RecyclerView trendRecycler;
    private AppCompatSpinner categoriesSpinner;
    private HintAdapter hintAdapter;
    private List<String> categoriesList;
    private RelativeLayout spinnerWrapper;

    public static WhatsTrending create() {
        WhatsTrending whatsTrending = new WhatsTrending();
        return whatsTrending;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.whats_trending_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        trendSwipe = (SwipeRefreshLayout) view.findViewById(R.id.trendSwipe);
        trendRecycler = (RecyclerView) view.findViewById(R.id.trendRecycler);
        spinnerWrapper = (RelativeLayout) view.findViewById(R.id.spinnerWrapper);
        trendSwipe.setOnRefreshListener(this);
        getCategories();

        categoriesList = new ArrayList<>();
        hintAdapter = new HintAdapter(getActivity(), android.R.layout.simple_spinner_item, categoriesList);
        hintAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        categoriesSpinner = (AppCompatSpinner) view.findViewById(R.id.categoriesSpinner);
        categoriesSpinner.setAdapter(hintAdapter);
        categoriesSpinner.setSelection(hintAdapter.getCount());
        categoriesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("Fsafasfasfas", "onItemSelected: "+categoriesList.get(i));
                getTrendByCategory(categoriesList.get(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void getTrendByCategory(String category) {

    }


    private void startRefreshing() {
        trendSwipe.post(new Runnable() {
            @Override
            public void run() {
                trendSwipe.setRefreshing(true);
            }
        });
    }

    private void getCategories() {
        startRefreshing();
        Call<ResponseBody> categoriesCall = Retrofit.getInstance().getInkService().getTrendCategories(Constants.TREND_CATEGORIES_TOKEN);
        categoriesCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    return;
                }
                if (response.body() == null) {
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    if (success) {
                        spinnerWrapper.setVisibility(View.VISIBLE);
                        JSONArray categories = jsonObject.optJSONArray("categories");
                        for (int i = 0; i < categories.length(); i++) {
                            categoriesList.add(categories.optString(i));
                        }
                        categoriesList.add(getString(R.string.selectCategory));
                        hintAdapter.notifyDataSetChanged();
                    } else {
                        Snackbar.make(trendRecycler, getString(R.string.notAuthorized), Snackbar.LENGTH_SHORT).show();
                    }
                    trendSwipe.setRefreshing(false);
                } catch (IOException e) {
                    e.printStackTrace();
                    trendSwipe.setRefreshing(false);
                } catch (JSONException e) {
                    e.printStackTrace();
                    trendSwipe.setRefreshing(false);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                getCategories();
            }
        });
    }

    @Override
    public void onRefresh() {

    }
}