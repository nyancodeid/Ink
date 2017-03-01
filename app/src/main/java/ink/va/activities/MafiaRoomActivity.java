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

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.va.adapters.MafiaRoomAdapter;
import ink.va.interfaces.MafiaItemClickListener;
import ink.va.models.MafiaRoomsModel;
import ink.va.utils.Retrofit;
import ink.va.utils.SharedHelper;
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
        mafiaRoomsSwipe.post(new Runnable() {
            @Override
            public void run() {
                mafiaRoomsSwipe.setRefreshing(true);
            }
        });

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

    @Override
    public void onRefresh() {
        getRooms();
    }

    @Override
    public void onJoinClicked(MafiaRoomsModel mafiaRoomsModel) {

    }

    @Override
    public void onDeleteClicked(MafiaRoomsModel mafiaRoomsModel) {

    }

    @Override
    public void onLeaveClicked(MafiaRoomsModel mafiaRoomsModel) {

    }

    @Override
    public void onItemClicked(MafiaRoomsModel mafiaRoomsModel) {

    }
}
