package kashmirr.social.fragments;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.Toast;

import com.kashmirr.social.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import kashmirr.social.activities.CreateTrend;
import kashmirr.social.activities.NewsAndTrendsActivity;
import kashmirr.social.adapters.HintAdapter;
import kashmirr.social.adapters.TrendAdapter;
import kashmirr.social.interfaces.RecyclerItemClickListener;
import kashmirr.social.interfaces.RequestCallback;
import kashmirr.social.utils.Constants;
import kashmirr.social.utils.DialogUtils;
import kashmirr.social.utils.Retrofit;
import kashmirr.social.utils.SharedHelper;
import okhttp3.ResponseBody;

/**
 * Created by PC-Comp on 9/12/2016.
 */
public class WhatsTrending extends Fragment implements SwipeRefreshLayout.OnRefreshListener, RecyclerItemClickListener {

    private SwipeRefreshLayout trendSwipe;
    private RecyclerView trendRecycler;
    private AppCompatSpinner categoriesSpinner;
    private HintAdapter hintAdapter;
    private ArrayList<String> categoriesList;
    private RelativeLayout spinnerWrapper;
    private ArrayList<TrendModel> trendModelArrayList;
    private String lastKnownCategory;
    private TrendModel trendModel;
    private TrendAdapter trendAdapter;
    private String advertisementPrice;
    private String topTrendPrice;
    public static final int UPDATE_TRENDS = 5;
    private boolean attemptedToCreate;
    private boolean isDataLoaded;
    private SharedHelper sharedHelper;

    @BindView(R.id.createTrend)
    FloatingActionButton createTrend;

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
        ButterKnife.bind(this, view);
        trendModelArrayList = new ArrayList<>();
        sharedHelper = new SharedHelper(getActivity());

        if (sharedHelper.getMenuButtonColor() != null) {
            createTrend.setBackgroundTintList((ColorStateList.valueOf(Color.parseColor(sharedHelper.getMenuButtonColor()))));
        }

        trendAdapter = new TrendAdapter(getActivity(), trendModelArrayList);
        trendAdapter.setOnItemClickListener(this);
        trendSwipe = (SwipeRefreshLayout) view.findViewById(R.id.trendSwipe);
        trendRecycler = (RecyclerView) view.findViewById(R.id.trendRecycler);
        spinnerWrapper = (RelativeLayout) view.findViewById(R.id.spinnerWrapper);
        trendRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        trendRecycler.setAdapter(trendAdapter);
        trendSwipe.setOnRefreshListener(this);
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
        getCategories();
    }


    @OnClick(R.id.createTrend)
    public void createTrendClicked() {
        attemptedToCreate = true;
        if (isDataLoaded) {
            Intent intent = new Intent(getActivity(), CreateTrend.class);
            intent.putStringArrayListExtra("trendCategories", categoriesList);
            intent.putExtra("advertisementPrice", advertisementPrice);
            intent.putExtra("topTrendPrice", topTrendPrice);
            startActivityForResult(intent, UPDATE_TRENDS);
            attemptedToCreate = false;
        } else {
            Snackbar.make(trendRecycler, getString(R.string.waitTillLoad), Snackbar.LENGTH_SHORT).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            }).show();
        }
    }

    private void getTrendByCategory(final String category) {
        startRefreshing();
        trendModelArrayList.clear();
        isDataLoaded = false;
        if (category == null) {
            ((NewsAndTrendsActivity) getActivity()).makeRequest(Retrofit.getInstance().getInkService().getTrends(Constants.TREND_TYPE_ALL),
                    null, new RequestCallback() {
                        @Override
                        public void onRequestSuccess(Object result) {
                            try {
                                String responseBody = ((ResponseBody) result).string();
                                JSONObject jsonObject = new JSONObject(responseBody);
                                boolean success = jsonObject.optBoolean("success");
                                advertisementPrice = jsonObject.optString("advertisementPrice");
                                topTrendPrice = jsonObject.optString("topTrendPrice");
                                isDataLoaded = true;
                                if (attemptedToCreate) {
                                    attemptedToCreate = false;
                                    Snackbar.make(trendRecycler, getString(R.string.canProceedText), Snackbar.LENGTH_SHORT).setAction("OK", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                        }
                                    }).show();
                                }
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
                                        trendModel = new TrendModel(eachObject.optString("creatorId"), eachObject.optString("title"), eachObject.optString("content"), eachObject.optString("image_url"),
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
                        public void onRequestFailed(Object[] result) {
                            trendSwipe.setRefreshing(false);
                        }
                    });
        } else {
            ((NewsAndTrendsActivity) getActivity()).makeRequest(Retrofit.getInstance().getInkService().getTrends(category),
                    null, new RequestCallback() {
                        @Override
                        public void onRequestSuccess(Object result) {
                            try {
                                String responseBody = ((ResponseBody) result).string();
                                JSONObject jsonObject = new JSONObject(responseBody);
                                boolean success = jsonObject.optBoolean("success");
                                isDataLoaded = true;
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
                                        trendModel = new TrendModel(eachObject.optString("creatorId"), eachObject.optString("title"), eachObject.optString("content"), eachObject.optString("image_url"),
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
                        public void onRequestFailed(Object[] result) {
                            trendSwipe.setRefreshing(false);
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
        categoriesList.clear();
        hintAdapter.notifyDataSetChanged();
        ((NewsAndTrendsActivity) getActivity()).makeRequest(Retrofit.getInstance().getInkService().getTrendCategories(Constants.TREND_CATEGORIES_TOKEN), null,
                new RequestCallback() {
                    @Override
                    public void onRequestSuccess(Object result) {
                        try {
                            String responseBody = ((ResponseBody) result).string();
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
                    public void onRequestFailed(Object[] result) {
                        trendSwipe.setRefreshing(false);
                    }
                });
    }

    @Override
    public void onRefresh() {
        getCategories();
        getTrendByCategory(lastKnownCategory);
    }

    @Override
    public void onItemClicked(int position, View view) {

    }

    @Override
    public void onItemLongClick(Object object) {

    }

    @Override
    public void onAdditionalItemClick(int position, View view) {

    }

    @Override
    public void onAdditionalItemClicked(Object object) {
        final TrendModel trendModel = (TrendModel) object;

        DialogUtils.showDialog(getActivity(), getString(R.string.caution), getString(R.string.trendDeleteWarning), true, new DialogUtils.DialogListener() {
            @Override
            public void onNegativeClicked() {

            }

            @Override
            public void onDialogDismissed() {

            }

            @Override
            public void onPositiveClicked() {
                trendSwipe.post(new Runnable() {
                    @Override
                    public void run() {
                        trendSwipe.setRefreshing(true);
                        callToDeleteServer(trendModel);
                    }
                });
            }
        }, true, getString(R.string.cancel));
    }

    private void callToDeleteServer(final TrendModel trendModel) {
        ((NewsAndTrendsActivity) getActivity()).makeRequest(Retrofit.getInstance().getInkService().deleteTrend(trendModel.getId()),
                trendSwipe, new RequestCallback() {
                    @Override
                    public void onRequestSuccess(Object result) {
                        try {
                            String responseBody = ((ResponseBody) result).string();
                            JSONObject jsonObject = new JSONObject(responseBody);
                            boolean success = jsonObject.optBoolean("success");
                            if (success) {
                                getTrendByCategory(lastKnownCategory);
                                Toast.makeText(getActivity(), getString(R.string.deleted), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getActivity(), getString(R.string.notDeleted), Toast.LENGTH_SHORT).show();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onRequestFailed(Object[] result) {

                    }
                });
    }

    @Override
    public void onItemClicked(Object object) {
        TrendModel trendModel = (TrendModel) object;

        String urlToOpen = trendModel.getExternalUrl();
        if (!urlToOpen.startsWith("http://")) {
            if (!urlToOpen.startsWith("https://")) {
                urlToOpen = "http://" + urlToOpen;
            }
        }

        if (!urlToOpen.startsWith("https://")) {
            if (!urlToOpen.startsWith("http://")) {
                urlToOpen = "https://" + urlToOpen;
            }
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse(urlToOpen));
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case UPDATE_TRENDS:
                onRefresh();
                break;
        }
    }
}