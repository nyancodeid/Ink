package ink.va.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.RelativeLayout;

import com.ink.va.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ink.va.adapters.HintAdapter;
import ink.va.adapters.TrendAdapter;
import ink.va.utils.Constants;
import ink.va.utils.Retrofit;
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
    private ArrayList<TrendModel> trendModelArrayList;
    private String lastKnownCategory;
    private TrendModel trendModel;
    private TrendAdapter trendAdapter;

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
        trendModelArrayList = new ArrayList<>();
        trendAdapter = new TrendAdapter(getActivity(), trendModelArrayList);
        trendSwipe = (SwipeRefreshLayout) view.findViewById(R.id.trendSwipe);
        trendRecycler = (RecyclerView) view.findViewById(R.id.trendRecycler);
        spinnerWrapper = (RelativeLayout) view.findViewById(R.id.spinnerWrapper);
        trendRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        trendRecycler.setAdapter(trendAdapter);
        trendSwipe.setOnRefreshListener(this);
        getCategories();

        categoriesList = new ArrayList<>();
        hintAdapter = new HintAdapter(getActivity(), android.R.layout.simple_spinner_item, categoriesList);
        hintAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        categoriesSpinner = (AppCompatSpinner) view.findViewById(R.id.categoriesSpinner);
        categoriesSpinner.setAdapter(hintAdapter);
        categoriesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (!categoriesList.get(i).equals(getString(R.string.all))) {
                    lastKnownCategory = categoriesList.get(i);
                    getTrendByCategory(categoriesList.get(i));
                } else if (categoriesList.get(i).equals(getString(R.string.all))) {
                    lastKnownCategory = null;
                    getTrendByCategory(null);

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void getTrendByCategory(final String category) {
        startRefreshing();
        trendModelArrayList.clear();
        if (category == null) {
            Call<ResponseBody> trendsCall = Retrofit.getInstance().getInkService().getTrends(Constants.TREND_TYPE_ALL);
            trendsCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response == null) {
                        getTrendByCategory(category);
                        return;
                    }
                    if (response.body() == null) {
                        getTrendByCategory(category);
                    }
                    try {
                        String responseBody = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseBody);
                        boolean success = jsonObject.optBoolean("success");
                        if (success) {
                            JSONArray trendsArray = jsonObject.optJSONArray("trends");
                            if (trendsArray.length() == 0) {
                                Snackbar.make(trendRecycler, getString(R.string.noTrends), Snackbar.LENGTH_LONG).setAction("OK", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                    }
                                }).show();
                                trendSwipe.setRefreshing(false);
                                return;
                            }
                            for (int i = 0; i < trendsArray.length(); i++) {
                                JSONObject eachObject = trendsArray.optJSONObject(i);
                                trendModel = new TrendModel(eachObject.optString("title"), eachObject.optString("content"), eachObject.optString("image_url"),
                                        eachObject.optString("external_url"), eachObject.optString("category"), eachObject.optString("id"), eachObject.optBoolean("isTop"));
                                trendModelArrayList.add(trendModel);
                                trendAdapter.notifyDataSetChanged();
                            }
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
                    getTrendByCategory(category);
                }
            });
        } else {
            Call<ResponseBody> trendsCall = Retrofit.getInstance().getInkService().getTrends(category);
            trendsCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response == null) {
                        getTrendByCategory(category);
                        return;
                    }
                    if (response.body() == null) {
                        getTrendByCategory(category);
                    }
                    try {
                        String responseBody = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseBody);
                        boolean success = jsonObject.optBoolean("success");
                        if (success) {
                            JSONArray trendsArray = jsonObject.optJSONArray("trends");
                            if (trendsArray.length() == 0) {
                                Snackbar.make(trendRecycler, getString(R.string.noTrends), Snackbar.LENGTH_LONG).setAction("OK", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                    }
                                }).show();
                                trendSwipe.setRefreshing(false);
                                return;
                            }
                            for (int i = 0; i < trendsArray.length(); i++) {
                                JSONObject eachObject = trendsArray.optJSONObject(i);
                                trendModel = new TrendModel(eachObject.optString("title"), eachObject.optString("content"), eachObject.optString("image_url"),
                                        eachObject.optString("external_url"), eachObject.optString("category"), eachObject.optString("id"), eachObject.optBoolean("isTop"));
                                trendModelArrayList.add(trendModel);
                                trendAdapter.notifyDataSetChanged();
                            }
                        }
                        trendSwipe.setRefreshing(false);
                    } catch (IOException e) {
                        trendSwipe.setRefreshing(false);
                        e.printStackTrace();
                    } catch (JSONException e) {
                        trendSwipe.setRefreshing(false);
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    getTrendByCategory(category);
                }
            });
        }
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
                        categoriesList.add(getString(R.string.all));
                        categoriesSpinner.setSelection(hintAdapter.getCount());
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
        getTrendByCategory(lastKnownCategory);
    }
}