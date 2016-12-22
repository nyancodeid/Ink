package ink.va.activities;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.ink.va.R;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import ink.va.adapters.MyCollectionHorizontalAdapter;
import ink.va.models.MyCollectionModel;
import ink.va.utils.Retrofit;
import ink.va.utils.SharedHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyCollection extends BaseActivity {

    private MyCollectionHorizontalAdapter myCollectionHorizontalAdapter;

    @Bind(R.id.collectionHorizontalRecycler)
    RecyclerView collectionHorizontalRecycler;

    @Bind(R.id.horizontalProgress)
    View horizontalProgress;
    private SharedHelper sharedHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_collection);
        ButterKnife.bind(this);
        sharedHelper = new SharedHelper(this);
        myCollectionHorizontalAdapter = new MyCollectionHorizontalAdapter(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        collectionHorizontalRecycler.setLayoutManager(layoutManager);
        getUserCollections();
    }

    private void getUserCollections() {
        Call<List<MyCollectionModel>> listCall = Retrofit.getInstance().getInkService().getUserCollection(sharedHelper.getUserId());
        listCall.enqueue(new Callback<List<MyCollectionModel>>() {
            @Override
            public void onResponse(Call<List<MyCollectionModel>> call, Response<List<MyCollectionModel>> response) {
                myCollectionHorizontalAdapter.setMyCollectionModels(response.body());
            }

            @Override
            public void onFailure(Call<List<MyCollectionModel>> call, Throwable t) {
                buildErrorDialog();
                horizontalProgress.setVisibility(View.GONE);
            }
        });
    }

    private void buildErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.error));
        builder.setMessage(getString(R.string.errorFetchingData));
        builder.show();
    }
}
