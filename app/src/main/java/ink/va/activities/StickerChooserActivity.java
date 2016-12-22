package ink.va.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.ink.va.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.va.adapters.StickerAdapter;
import ink.va.interfaces.RecyclerItemClickListener;
import ink.va.models.GifResponse;
import ink.va.models.GifResponseModel;
import ink.va.models.StickerModel;
import ink.va.utils.Constants;
import ink.va.utils.ErrorCause;
import ink.va.utils.Retrofit;
import ink.va.utils.SharedHelper;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static ink.va.utils.Constants.REQUEST_CODE_CHOSE_STICKER;

public class StickerChooserActivity extends AppCompatActivity implements RecyclerItemClickListener {


    @Bind(R.id.gifsRecycler)
    RecyclerView gifsRecycler;
    @Bind(R.id.gifLoadingProgress)
    ProgressBar gifLoadingProgress;

    @Bind(R.id.noGifsText)
    TextView noGifsText;

    @Bind(R.id.goToStore)
    Button goToStore;

    private StickerModel stickerModel;

    private StickerAdapter stickerAdapter;

    private List<StickerModel> stickerModelList;
    private SharedHelper mSharedHelper;
    private Gson gson;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sticker_choser);
        ButterKnife.bind(this);
        mSharedHelper = new SharedHelper(this);
        LinearLayoutManager gridLayoutManager = new LinearLayoutManager(this);
        gifsRecycler.setLayoutManager(gridLayoutManager);
        stickerModelList = new ArrayList<>();
        stickerAdapter = new StickerAdapter(stickerModelList, this);
        gson = new Gson();
        stickerAdapter.setOnItemClickListener(this);
        gifsRecycler.setAdapter(stickerAdapter);
        getUserGifs();
        getSupportActionBar().setTitle(getString(R.string.sentSticker));
    }

    @OnClick(R.id.goToStore)
    public void setGoToStore() {
        startActivity(new Intent(getApplicationContext(), Shop.class));
    }



    private void getUserGifs() {
        Call<ResponseBody> gifCall = Retrofit.getInstance().getInkService().getUserStickers(mSharedHelper.getUserId(),
                Constants.SERVER_AUTH_KEY);
        gifCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    getUserGifs();
                    return;
                }
                if (response.body() == null) {
                    getUserGifs();
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    GifResponse gifResponse = gson.fromJson(responseBody, GifResponse.class);
                    gifLoadingProgress.setVisibility(View.INVISIBLE);
                    if (gifResponse.success) {
                        if (!gifResponse.cause.equals(ErrorCause.NO_GIFS)) {
                            ArrayList<GifResponseModel> gifResponseModels = gifResponse.gifResponseModels;
                            for (int i = 0; i < gifResponseModels.size(); i++) {
                                GifResponseModel eachModel = gifResponseModels.get(i);
                                stickerModel = new StickerModel(eachModel.id, eachModel.userId, eachModel.gifName, eachModel.isAnimated, eachModel.hasSound);
                                stickerModelList.add(stickerModel);
                                stickerAdapter.notifyDataSetChanged();
                            }
                            if (stickerModelList.size() <= 0) {
                                noGifsText.setVisibility(View.VISIBLE);
                                goToStore.setVisibility(View.VISIBLE);
                            } else {
                                noGifsText.setVisibility(View.GONE);
                                goToStore.setVisibility(View.GONE);
                            }

                        } else {
                            noGifsText.setVisibility(View.VISIBLE);
                            goToStore.setVisibility(View.VISIBLE);
                        }
                    } else {
                        noGifsText.setVisibility(View.VISIBLE);
                        goToStore.setVisibility(View.VISIBLE);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                getUserGifs();
            }
        });
    }

    @Override
    public void onItemClicked(int position, View view) {

    }

    @Override
    public void onItemLongClick(int position) {

    }

    @Override
    public void onAdditionItemClick(int position, View view) {

    }

    @Override
    public void onItemClicked(Object object) {
        System.gc();
        StickerModel singleModel = (StickerModel) object;

        String stickerUrl = singleModel.getStickerUrl();
        Intent intent = new Intent();
        intent.putExtra(Constants.STICKER_URL_EXTRA_KEY, stickerUrl);
        intent.putExtra(Constants.STICKER_IS_ANIMATED_EXTRA_KEY, singleModel.isAnimated());
        setResult(REQUEST_CODE_CHOSE_STICKER, intent);
        finish();
    }
}
