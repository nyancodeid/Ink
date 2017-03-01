package ink.va.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.ink.va.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.va.adapters.MafiaRoomAdapter;
import ink.va.interfaces.MafiaItemClickListener;
import ink.va.models.MafiaRoomsModel;
import ink.va.utils.Retrofit;
import ink.va.utils.SharedHelper;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MafiaRoomActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener, MafiaItemClickListener {

    public static final int ADD_ROOM_REQUEST_CODE = 8;
    @BindView(R.id.roomRecycler)
    RecyclerView roomRecycler;
    private SharedHelper sharedHelper;
    @BindView(R.id.addRoomButton)
    FloatingActionButton addRoomButton;
    @BindView(R.id.mafiaRoomsSwipe)
    SwipeRefreshLayout mafiaRoomsSwipe;
    @BindView(R.id.noMafiaRoomView)
    View noMafiaRoomView;
    private MafiaRoomAdapter mafiaRoomAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mafia_room);
        ButterKnife.bind(this);

        getSupportActionBar().setTitle(getString(R.string.rooms));

        sharedHelper = new SharedHelper(this);

        mafiaRoomAdapter = new MafiaRoomAdapter();
        mafiaRoomAdapter.setOnMafiaItemClickListener(this);

        if (sharedHelper.getMenuButtonColor() != null) {
            addRoomButton.setBackgroundTintList((ColorStateList.valueOf(Color.parseColor(sharedHelper.getMenuButtonColor()))));
        }

        mafiaRoomsSwipe.setOnRefreshListener(this);

        getRooms();
    }

    @OnClick(R.id.addRoomButton)
    public void addRoomClicked() {
        startActivityForResult(new Intent(this, MafiaAddRoomActivity.class), ADD_ROOM_REQUEST_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ADD_ROOM_REQUEST_CODE:
                getRooms();
                break;
        }
    }

    private void getRooms() {
        showSwipe();
        Call<List<MafiaRoomsModel>> mafiaRooms = Retrofit.getInstance().getInkService().getMafiaRooms();
        mafiaRooms.enqueue(new Callback<List<MafiaRoomsModel>>() {
            @Override
            public void onResponse(Call<List<MafiaRoomsModel>> call, Response<List<MafiaRoomsModel>> response) {
                dismissSwipe();
                List<MafiaRoomsModel> mafiaRoomsModels = response.body();
                if (mafiaRoomsModels.isEmpty()) {
                    mafiaRoomAdapter.clear();
                    showNoRooms();
                } else {
                    hideNoRooms();
                    mafiaRoomAdapter.setMafiaRoomsModels(mafiaRoomsModels);
                }
            }

            @Override
            public void onFailure(Call<List<MafiaRoomsModel>> call, Throwable t) {
                dismissSwipe();
                Toast.makeText(MafiaRoomActivity.this, getString(R.string.serverErrorText), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void hideNoRooms() {
        if (noMafiaRoomView.getVisibility() == View.VISIBLE) {
            noMafiaRoomView.setVisibility(View.GONE);
        }
    }

    private void showNoRooms() {
        if (noMafiaRoomView.getVisibility() == View.GONE) {
            noMafiaRoomView.setVisibility(View.VISIBLE);
        }
    }

    private void dismissSwipe() {
        if (mafiaRoomsSwipe.isRefreshing()) {
            mafiaRoomsSwipe.post(new Runnable() {
                @Override
                public void run() {
                    mafiaRoomsSwipe.setRefreshing(false);
                }
            });
        }
    }

    private void showSwipe() {
        if (!mafiaRoomsSwipe.isRefreshing()) {
            mafiaRoomsSwipe.post(new Runnable() {
                @Override
                public void run() {
                    mafiaRoomsSwipe.setRefreshing(true);
                }
            });
        }
    }

    @Override
    public void onRefresh() {
        getRooms();
    }

    @Override
    public void onJoinClicked(MafiaRoomsModel mafiaRoomsModel) {

    }

    @Override
    public void onDeleteClicked(MafiaRoomsModel mafiaRoomsModel) {
        int roomId = mafiaRoomsModel.getId();
        deleteRoom(roomId);
    }


    @Override
    public void onLeaveClicked(MafiaRoomsModel mafiaRoomsModel) {

    }

    @Override
    public void onItemClicked(MafiaRoomsModel mafiaRoomsModel) {
        Intent intent = new Intent(this, MafiaGameView.class);
        startActivity(intent);
    }

    private void deleteRoom(final int roomId) {
        showSwipe();
        Call<ResponseBody> deleteCall = Retrofit.getInstance().getInkService().deleteMafiRoom(roomId);
        deleteCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    deleteRoom(roomId);
                    return;
                }
                if (response.body() == null) {
                    deleteRoom(roomId);
                    return;
                }
                dismissSwipe();
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    if (success) {
                        mafiaRoomAdapter.clear();
                        getRooms();
                    } else {
                        Toast.makeText(MafiaRoomActivity.this, getString(R.string.failedToDelete), Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                dismissSwipe();
                Toast.makeText(MafiaRoomActivity.this, getString(R.string.serverErrorText), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
