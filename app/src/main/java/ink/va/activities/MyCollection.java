package ink.va.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
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
import android.widget.Toast;

import com.google.gson.Gson;
import com.ink.va.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.va.adapters.MyCollectionHorizontalAdapter;
import ink.va.adapters.StickerAdapter;
import ink.va.interfaces.ItemClickListener;
import ink.va.interfaces.RecyclerItemClickListener;
import ink.va.interfaces.RequestCallback;
import ink.va.models.GifResponse;
import ink.va.models.GifResponseModel;
import ink.va.models.MyCollectionModel;
import ink.va.models.MyCollectionResponseModel;
import ink.va.models.StickerModel;
import ink.va.utils.Constants;
import ink.va.utils.DialogUtils;
import ink.va.utils.Retrofit;
import ink.va.utils.SharedHelper;
import okhttp3.ResponseBody;

import static ink.va.utils.Constants.REQUEST_CODE_CHOSE_STICKER;
import static ink.va.utils.Constants.STARTING_FOR_RESULT_BUNDLE_KEY;

public class MyCollection extends BaseActivity implements MyCollectionHorizontalAdapter.OnCollectionClickListener, RecyclerItemClickListener {

    private MyCollectionHorizontalAdapter myCollectionHorizontalAdapter;

    @BindView(R.id.collectionHorizontalRecycler)
    RecyclerView collectionHorizontalRecycler;

    @BindView(R.id.horizontalProgress)
    View horizontalProgress;

    @BindView(R.id.gifsRecycler)
    RecyclerView gifsRecycler;
    @BindView(R.id.gifLoadingProgress)
    ProgressBar verticalProgress;

    @BindView(R.id.noGifsText)
    TextView noGifsText;

    @BindView(R.id.goToStore)
    Button goToStore;

    @BindView(R.id.animationHintLayout)
    RelativeLayout editorHintLayout;

    private StickerModel stickerModel;

    private StickerAdapter stickerAdapter;

    private List<StickerModel> stickerModelList;
    private Gson gson;

    private SharedHelper sharedHelper;
    private boolean startingForActivityResult;
    private Animation fadeInAnimation;
    private Snackbar snackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_collection);
        ButterKnife.bind(this);
        sharedHelper = new SharedHelper(this);
        Bundle extras = getIntent().getExtras();
        snackbar = Snackbar.make(collectionHorizontalRecycler, getString(R.string.deleting), Snackbar.LENGTH_INDEFINITE);
        if (extras != null) {
            if (extras.containsKey(STARTING_FOR_RESULT_BUNDLE_KEY)) {
                startingForActivityResult = extras.getBoolean(STARTING_FOR_RESULT_BUNDLE_KEY);
            }
        }
        initHorizontalRecycler();
        initVerticalRecycler();
        stickerAdapter.setHideChooser(startingForActivityResult ? false : true);
        fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.my_collection_text));
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


        if (!sharedHelper.isAnimationHintShown()) {
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
        sharedHelper.putAnimationHintShow(true);
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
        makeRequest(Retrofit.getInstance().getInkService().getsSinglePack(packId,
                Constants.SERVER_AUTH_KEY), null, new RequestCallback() {
            @Override
            public void onRequestSuccess(Object result) {
                try {
                    String responseBody = ((ResponseBody) result).string();
                    GifResponse gifResponse = gson.fromJson(responseBody, GifResponse.class);
                    verticalProgress.setVisibility(View.GONE);
                    if (gifResponse.success) {
                        ArrayList<GifResponseModel> gifResponseModels = gifResponse.gifResponseModels;
                        for (int i = 0; i < gifResponseModels.size(); i++) {
                            GifResponseModel eachModel = gifResponseModels.get(i);
                            stickerModel = new StickerModel(eachModel.id, eachModel.userId, eachModel.gifName, eachModel.isAnimated, eachModel.hasSound);
                            stickerModelList.add(stickerModel);
                            stickerAdapter.notifyDataSetChanged();
                        }
                    } else {
                        Toast.makeText(MyCollection.this, getString(R.string.serverErrorText), Toast.LENGTH_SHORT).show();
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
    protected void onResume() {
        super.onResume();
        getUserCollections();
    }

    private void showNoCollection() {
        noGifsText.setVisibility(View.VISIBLE);
        goToStore.setVisibility(View.VISIBLE);
        collectionHorizontalRecycler.setVisibility(View.GONE);
        myCollectionHorizontalAdapter.clearItems();
        stickerAdapter.clearItems();
    }

    private void getUserCollections() {
        if (horizontalProgress.getVisibility() == View.GONE) {
            horizontalProgress.setVisibility(View.VISIBLE);
        }
        myCollectionHorizontalAdapter.clearItems();
        stickerAdapter.clearItems();


        makeRequest(Retrofit.getInstance().getInkService().getUserCollection(sharedHelper.getUserId()), null, new RequestCallback() {
            @Override
            public void onRequestSuccess(Object result) {
                if (((MyCollectionResponseModel) result).getMyCollectionModels().isEmpty()) {
                    showNoCollection();
                } else {
                    hideNoCollection();
                    myCollectionHorizontalAdapter.setMyCollectionModels(((MyCollectionResponseModel) result).getMyCollectionModels());
                }

                horizontalProgress.setVisibility(View.GONE);
            }

            @Override
            public void onRequestFailed(Object[] result) {
                buildErrorDialog();
                horizontalProgress.setVisibility(View.GONE);
            }
        });
    }

    private void hideNoCollection() {
        noGifsText.setVisibility(View.GONE);
        goToStore.setVisibility(View.GONE);
        collectionHorizontalRecycler.setVisibility(View.VISIBLE);
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
    public void onMoreClicked(View view, final MyCollectionModel myCollectionModel) {

        DialogUtils.showPopUp(this, view, new ItemClickListener<MenuItem>() {
            @Override
            public void onItemClick(MenuItem clickedItem) {
                switch (clickedItem.getItemId()) {
                    case 0:
                        showDeleteWarning(myCollectionModel.getId());
                        break;
                }
            }
        }, getString(R.string.delete));
    }

    private void showDeleteWarning(final String packId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.delete_collection));
        builder.setMessage(getString(R.string.delete_collection_warning_message));
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        final AlertDialog alertDialog = builder.show();
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteCollection(packId);
                alertDialog.dismiss();
                snackbar.show();
            }
        });
        alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });


    }

    private void deleteCollection(final String packId) {
        makeRequest(Retrofit.getInstance().getInkService().deleteCollection(sharedHelper.getUserId(), packId), null, new RequestCallback() {
            @Override
            public void onRequestSuccess(Object result) {
                snackbar.dismiss();
                try {
                    String responseBody = ((ResponseBody) result).string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    if (success) {
                        getUserCollections();
                    } else {
                        Toast.makeText(MyCollection.this, getString(R.string.serverErrorText), Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(MyCollection.this, getString(R.string.serverErrorText), Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    Toast.makeText(MyCollection.this, getString(R.string.serverErrorText), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }

            @Override
            public void onRequestFailed(Object[] result) {
                snackbar.dismiss();
            }
        });
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
    public void onItemLongClick(Object object) {

    }

    @Override
    public void onAdditionalItemClick(int position, View view) {

    }

    @Override
    public void onAdditionalItemClicked(Object object) {

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
