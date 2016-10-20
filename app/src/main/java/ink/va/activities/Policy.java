package ink.va.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ink.va.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import ink.va.utils.Retrofit;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Policy extends AppCompatActivity {

    @Bind(R.id.policyLoading)
    ProgressBar policyLoading;
    @Bind(R.id.policyMessage)
    TextView policyMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_policy);
        ButterKnife.bind(this);
        getPolicy();
    }

    private void getPolicy() {
        Call<okhttp3.ResponseBody> policyCall = Retrofit.getInstance().getInkService().getPolicy();
        policyCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    getPolicy();
                    return;
                }
                if (response.body() == null) {
                    getPolicy();
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    policyLoading.setVisibility(View.GONE);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                policyLoading.setVisibility(View.GONE);
            }
        });
    }
}
