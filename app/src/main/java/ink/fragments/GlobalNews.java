package ink.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.ink.R;

import java.io.IOException;
import java.util.ArrayList;

import ink.adapters.NewsAdapter;
import ink.interfaces.NewsItemClickListener;
import ink.models.NewsModel;
import ink.models.NewsResponse;
import ink.utils.Constants;
import ink.utils.Retrofit;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by PC-Comp on 9/12/2016.
 */
public class GlobalNews extends Fragment implements NewsItemClickListener {

    private RecyclerView newsRecycler;
    private Gson gson;
    private NewsAdapter newsAdapter;

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
        gson = new Gson();
        newsRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        newsAdapter = new NewsAdapter(getActivity());
        newsAdapter.setOnItemClickListener(this);
        newsRecycler.setAdapter(newsAdapter);
        getNews(Constants.NEWS_PRIMARY_URL);
    }


    private void getNews(final String nextUrl) {
        final Call<ResponseBody> newsCall = Retrofit.getInstance().getNewsInterface().getNews(Constants.NEWS_BASE_URL + nextUrl);
        newsCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    getNews(nextUrl);
                    return;
                }
                if (response.body() == null) {
                    getNews(nextUrl);
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    NewsResponse newsResponse = gson.fromJson(responseBody, NewsResponse.class);
                    String nextUrl = newsResponse.newsMeta.nextNewsUrl;
                    ArrayList<NewsModel> newsModels = newsResponse.newsModels;
                    newsAdapter.setNewsModels(newsModels);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Snackbar.make(newsRecycler, getString(R.string.newsError), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onViewMoreClicked(View clickedView, int position) {

    }

}
