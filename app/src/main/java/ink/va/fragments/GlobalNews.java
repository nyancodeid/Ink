package ink.va.fragments;

import android.content.Intent;
import android.net.Uri;
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

import ink.va.activities.NewsAndTrendsActivity;
import ink.va.adapters.NewsAdapter;
import ink.va.interfaces.NewsItemClickListener;
import ink.va.interfaces.RequestCallback;
import ink.va.models.NewsModel;
import ink.va.models.NewsResponse;
import ink.va.utils.Constants;
import ink.va.utils.Retrofit;
import okhttp3.ResponseBody;

/**
 * Created by PC-Comp on 9/12/2016.
 */
public class GlobalNews extends Fragment implements NewsItemClickListener, SwipeRefreshLayout.OnRefreshListener {

    private RecyclerView newsRecycler;
    private Gson gson;
    private NewsAdapter newsAdapter;
    private ArrayList<NewsModel> newsModels;
    private SwipeRefreshLayout globalNewsSwipe;
    private String lastKnownUrl;

    public static GlobalNews create() {
        GlobalNews globalNews = new GlobalNews();
        return globalNews;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.global_news_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        newsRecycler = (RecyclerView) view.findViewById(R.id.newsRecycler);
        globalNewsSwipe = (SwipeRefreshLayout) view.findViewById(R.id.globalNewsSwipe);
        globalNewsSwipe.setOnRefreshListener(this);
        showRefreshing();
        gson = new Gson();
        newsModels = new ArrayList<>();
        newsRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        newsAdapter = new NewsAdapter(getActivity());
        newsAdapter.setOnItemClickListener(this);
        newsRecycler.setAdapter(newsAdapter);
        getNews(Constants.NEWS_PRIMARY_URL, false);
    }


    private void showRefreshing() {
        globalNewsSwipe.post(new Runnable() {
            @Override
            public void run() {
                globalNewsSwipe.setRefreshing(true);
            }
        });
    }

    private void getNews(final String nextUrl, final boolean shouldDelete) {
        showRefreshing();
        ((NewsAndTrendsActivity) getActivity()).makeRequest(Retrofit.getInstance().getNewsInterface().getNews(Constants.NEWS_BASE_URL + nextUrl), globalNewsSwipe, new RequestCallback() {
            @Override
            public void onRequestSuccess(Object result) {
                try {
                    String responseBody = ((ResponseBody) result).string();
                    NewsResponse newsResponse = gson.fromJson(responseBody, NewsResponse.class);
                    if (newsResponse != null && newsResponse.newsModels != null && !newsResponse.newsModels.isEmpty()) {
                        lastKnownUrl = newsResponse.newsMeta.nextNewsUrl;
                        if (shouldDelete) {
                            newsModels.clear();
                            newsAdapter.notifyDataSetChanged();
                        }
                        for (int i = 0; i < newsResponse.newsModels.size(); i++) {
                            newsModels.add(newsResponse.newsModels.get(i));
                        }
                        newsAdapter.setNewsModels(newsModels);
                    } else {
                        Snackbar.make(newsRecycler, getString(R.string.noMoreNews), Snackbar.LENGTH_SHORT).setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                            }
                        }).show();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onRequestFailed(Object[] result) {

            }
        });
    }

    @Override
    public void onViewMoreClicked(View clickedView, int position) {
        String urlToOpen = newsModels.get(position).urlExternalLink;
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(urlToOpen));
        startActivity(i);
    }

    @Override
    public void onLoadMoreClicked(View clickedView) {
        getNews(lastKnownUrl, false);
    }

    @Override
    public void onRefresh() {
        getNews(Constants.NEWS_PRIMARY_URL, true);
    }
}
