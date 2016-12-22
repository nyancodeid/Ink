package ink.va.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.ink.va.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.va.adapters.MyCollectionHorizontalAdapter;
import ink.va.adapters.StickerAdapter;
import ink.va.interfaces.ItemClickListener;
import ink.va.interfaces.RecyclerItemClickListener;
import ink.va.models.GifResponse;
import ink.va.models.GifResponseModel;
import ink.va.models.MyCollectionModel;
import ink.va.models.MyCollectionResponseModel;
import ink.va.models.StickerModel;
import ink.va.utils.Constants;
import ink.va.utils.ErrorCause;
import ink.va.utils.PopupMenu;
import ink.va.utils.Retrofit;
import ink.va.utils.SharedHelper;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static ink.va.utils.Constants.REQUEST_CODE_CHOSE_STICKER;
import static ink.va.utils.Constants.STARTING_FOR_RESULT_BUNDLE_KEY;

public class MyCollection extends BaseActivity implements MyCollectionHorizontalAdapter.OnCollectionClickListener, RecyclerItemClickListener {

    private MyCollectionHorizontalAdapter myCollectionHorizontalAdapter;

    @Bind(R.id.collectionHorizontalRecycler)
    RecyclerView collectionHorizontalRecycler;

    @Bind(R.id.horizontalProgress)
    View horizontalProgress;

    @Bind(R.id.gifsRecycler)
    RecyclerView gifsRecycler;
    @Bind(R.id.gifLoadingProgress)
    ProgressBar verticalProgress;

    @Bind(R.id.noGifsText)
    TextView noGifsText;

    @Bind(R.id.goToStore)
    Button goToStore;

    @Bind(R.id.animationHintLayout)
    RelativeLayout editorHintLayout;

    private StickerModel stickerModel;

    private StickerAdapter stickerAdapter;

    private List<StickerModel> stickerModelList;
    private Gson gson;

    private SharedHelper sharedHelper;
    private boolean startingForActivityResult;
    private Animation fadeInAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_collection);
        ButterKnife.bind(this);
        sharedHelper = new SharedHelper(this);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey(STARTING_FOR_RESULT_BUNDLE_KEY)) {
                startingForActivityResult = extras.getBoolean(STARTING_FOR_RESULT_BUNDLE_KEY);
            }
        }

        fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.my_collection_text));
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


        initHorizontalRecycler();
        initVerticalRecycler();

        if (!sharedHelper.isEditorHintShown()) {
            editorHintLayout.startAnimation(fadeInAnimation);
            editorHintLayout.setVisibility(View.VISIBLE);
        }
    }

    private void initHorizontalRecycler() {
        myCollectionHorizontalAdapter = new MyCollectionHorizontalAdapter(this);
        myCollectionHorizontalAdapter.setOnCollectionClickListener(this);

        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        collectionHorizontalRecycler.setLayoutManager(horizontalLayoutManager);
        collectionHorizontalRecycler.setAdapter(myCollectionHorizontalAdapter);

    }

    private void disableHint() {
        sharedHelper.putEditorHintShow(true);
        editorHintLayout.setVisibility(View.GONE);
    }

    @OnClick(R.id.neverShowAnimationHint)
    public void neverShowEditorHint() {
        disableHint();
    }

    private void initVerticalRecycler() {
        LinearLayoutManager gridLayoutManager = new LinearLayoutManager(this);
        gifsRecycler.setLayoutManager(gridLayoutManager);
        stickerModelList = new ArrayList<>();
        stickerAdapter = new StickerAdapter(stickerModelList, this);
        gson = new Gson();
        stickerAdapter.setOnItemClickListener(this);
        gifsRecycler.setAdapter(stickerAdapter);
    }

    @OnClick(R.id.goToStore)
    public void setGoToStore() {
        startActivity(new Intent(getApplicationContext(), Shop.class));
    }


    private void getSinglePack(final String packId) {
        stickerModelList.clear();
        stickerAdapter.notifyDataSetChanged();

        if (verticalProgress.getVisibility() == View.GONE) {
            verticalProgress.setVisibility(View.VISIBLE);
        }
        Call<ResponseBody> gifCall = Retrofit.getInstance().getInkService().getsSinglePack(packId,
                Constants.SERVER_AUTH_KEY);
        gifCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    getSinglePack(packId);
                    return;
                }
                if (response.body() == null) {
                    getSinglePack(packId);
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    GifResponse gifResponse = gson.fromJson(responseBody, GifResponse.class);
                    verticalProgress.setVisibility(View.GONE);
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
                getSinglePack(packId);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getUserCollections();
    }

    private void getUserCollections() {
        if (horizontalProgress.getVisibility() == View.GONE) {
            horizontalProgress.setVisibility(View.VISIBLE);
        }
        Call<MyCollectionResponseModel> listCall = Retrofit.getInstance().getInkService().getUserCollection(sharedHelper.getUserId());
        listCall.enqueue(new Callback<MyCollectionResponseModel>() {
            @Override
            public void onResponse(Call<MyCollectionResponseModel> call, Response<MyCollectionResponseModel> response) {
                myCollectionHorizontalAdapter.setMyCollectionModels(response.body().getMyCollectionModels());
                horizontalProgress.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<MyCollectionResponseModel> call, Throwable t) {
                buildErrorDialog();
                horizontalProgress.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    private void buildErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.error));
        builder.setMessage(getString(R.string.errorFetchingData));
        builder.show();
    }

    @Override
    public void onMoreClicked(View view, MyCollectionModel myCollectionModel) {

        PopupMenu.showPopUp(this, view, new ItemClickListener<MenuItem>() {
            @Override
            public void onItemClick(MenuItem clickedItem) {

            }
        }, getString(R.string.delete));
    }

    @Override
    public void onCollectionClicked(MyCollectionModel myCollectionModel) {
        String packId = myCollectionModel.getId();
        getSinglePack(packId);
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
        StickerModel singleModel = (StickerModel) object;

        String stickerUrl = singleModel.getStickerUrl();
        Intent intent = new Intent();
        intent.putExtra(Constants.STICKER_URL_EXTRA_KEY, stickerUrl);
        intent.putExtra(Constants.STICKER_IS_ANIMATED_EXTRA_KEY, singleModel.isAnimated());
        setResult(REQUEST_CODE_CHOSE_STICKER, intent);
        finish();
    }
}
