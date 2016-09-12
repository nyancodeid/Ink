package ink.activities;

import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.ink.R;

import java.io.IOException;

import ink.utils.Retrofit;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewsActivity extends BaseActivity {

    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        gson = new Gson();
    }

    private void getNews(final String nextUrl) {
        Call<ResponseBody> newsCall = Retrofit.getInstance().getInkService().getNews(nextUrl);
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
                    Log.d("fasfasfsafa", "onResponse: "+responseBody);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                getNews(nextUrl);
            }
        });
    }
}
