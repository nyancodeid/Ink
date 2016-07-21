package ink.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.ink.R;

import java.io.IOException;
import java.util.ArrayList;

import ink.models.PacksModel;
import ink.models.PacksResponse;
import ink.utils.Retrofit;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by USER on 2016-07-20.
 */
public class Packs extends Fragment {

    public static Packs create() {
        Packs packs = new Packs();
        return packs;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.packs_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getPacks();

    }

    private void getPacks() {
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
                        for (int i = 0; i < packsModels.size(); i++) {
                            PacksModel eachModel = packsModels.get(i);
                        }
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
}
