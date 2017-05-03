package ink.va.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.ink.va.R;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.va.adapters.MafiaRoomAdapter;
import ink.va.interfaces.MafiaItemClickListener;
import ink.va.interfaces.RequestCallback;
import ink.va.models.MafiaRoomsModel;
import ink.va.service.MafiaGameService;
import ink.va.utils.DialogUtils;
import ink.va.utils.Retrofit;
import ink.va.utils.SharedHelper;
import okhttp3.ResponseBody;

import static ink.va.utils.ErrorCause.ALREADY_IN_ROOM;
import static ink.va.utils.ErrorCause.GAME_ALREADY_IN_PROGRESS;
import static ink.va.utils.ErrorCause.GAME_IN_PROGRESS;
import static ink.va.utils.ErrorCause.MAXIMUM_PLAYERS_REACHED;
import static ink.va.utils.ErrorCause.ROOM_DELETED;

public class MafiaRoomActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener, MafiaItemClickListener {

    public static final int ADD_ROOM_REQUEST_CODE = 8;
    @BindView(R.id.roomRecycler)
    RecyclerView roomRecycler;
    @BindView(R.id.addRoomButton)
    FloatingActionButton addRoomButton;
    @BindView(R.id.mafiaRoomsSwipe)
    SwipeRefreshLayout mafiaRoomsSwipe;
    @BindView(R.id.noMafiaRoomView)
    View noMafiaRoomView;
    private MafiaRoomAdapter mafiaRoomAdapter;
    private SharedHelper sharedHelper;
    private boolean myRoomSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mafia_room);
        ButterKnife.bind(this);

        getSupportActionBar().setTitle(getString(R.string.rooms));

        sharedHelper = new SharedHelper(this);

        mafiaRoomAdapter = new MafiaRoomAdapter();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        roomRecycler.setLayoutManager(linearLayoutManager);
        roomRecycler.setAdapter(mafiaRoomAdapter);
        mafiaRoomAdapter.setOnMafiaItemClickListener(this);

        if (sharedHelper.getMenuButtonColor() != null) {
            addRoomButton.setBackgroundTintList((ColorStateList.valueOf(Color.parseColor(sharedHelper.getMenuButtonColor()))));
        }

        mafiaRoomsSwipe.setOnRefreshListener(this);
        LocalBroadcastManager.getInstance(this).registerReceiver(updateReceiver, new IntentFilter(getPackageName() + "update"));
        getRooms();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mafia_rooms_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.roomsSwitch:
                if (myRoomSelected) {
                    myRoomSelected = false;
                    item.setTitle(getString(R.string.myRooms));
                    mafiaRoomAdapter.clear();
                    getRooms();
                } else {
                    item.setTitle(getString(R.string.globalRooms));
                    myRoomSelected = true;
                    mafiaRoomAdapter.clear();
                    getMyRooms();
                }
                break;
            case R.id.mafiaInfo:
                openMafiaInfo();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openMafiaInfo() {
        startActivity(new Intent(this, MafiaInfoActivity.class));
    }

    private void getMyRooms() {
        showSwipe();
        makeRequest(Retrofit.getInstance().getInkService().getMyMafiaRooms(sharedHelper.getUserId()), null, new RequestCallback() {
            @Override
            public void onRequestSuccess(Object result) {
                dismissSwipe();
                List<MafiaRoomsModel> mafiaRoomsModels = (List<MafiaRoomsModel>) result;
                if (mafiaRoomsModels.isEmpty()) {
                    mafiaRoomAdapter.clear();
                    showNoRooms();
                } else {
                    hideNoRooms();
                    mafiaRoomAdapter.setMafiaRoomsModels(mafiaRoomsModels);
                }
            }

            @Override
            public void onRequestFailed(Object[] result) {
                dismissSwipe();
            }
        });
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
                boolean hasRoomAdded = false;
                if (data != null && data.getExtras() != null) {
                    hasRoomAdded = data.getExtras().getBoolean("hasAdded");
                }
                if (hasRoomAdded) {
                    getRoomsAccordingly();
                }
                break;
        }
    }

    private void getRoomsAccordingly() {
        if (myRoomSelected) {
            getMyRooms();
        } else {
            getRooms();
        }
    }

    private void getRooms() {
        showSwipe();
        makeRequest(Retrofit.getInstance().getInkService().getMafiaRooms(), null, new RequestCallback() {
            @Override
            public void onRequestSuccess(Object result) {
                dismissSwipe();
                List<MafiaRoomsModel> mafiaRoomsModels = (List<MafiaRoomsModel>) result;
                if (mafiaRoomsModels.isEmpty()) {
                    mafiaRoomAdapter.clear();
                    showNoRooms();
                } else {
                    hideNoRooms();
                    mafiaRoomAdapter.setMafiaRoomsModels(mafiaRoomsModels);
                }
            }

            @Override
            public void onRequestFailed(Object[] result) {
                dismissSwipe();
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
        if (myRoomSelected) {
            getMyRooms();
        } else {
            getRooms();
        }
    }

    @Override
    public void onJoinClicked(MafiaRoomsModel mafiaRoomsModel) {
        joinRoom(mafiaRoomsModel.getId());
    }


    @Override
    public void onDeleteClicked(MafiaRoomsModel mafiaRoomsModel) {
        final int roomId = mafiaRoomsModel.getId();
        DialogUtils.showDialog(this, getString(R.string.delete), getString(R.string.actionCannotUndone), true, new DialogUtils.DialogListener() {
            @Override
            public void onNegativeClicked() {

            }

            @Override
            public void onDialogDismissed() {

            }

            @Override
            public void onPositiveClicked() {
                deleteRoom(roomId);
            }
        }, true, getString(R.string.cancel));
    }


    @Override
    public void onLeaveClicked(final MafiaRoomsModel mafiaRoomsModel) {
        DialogUtils.showDialog(this, getString(R.string.leaveTitle), getString(R.string.leaveContent), true, new DialogUtils.DialogListener() {
            @Override
            public void onNegativeClicked() {

            }

            @Override
            public void onDialogDismissed() {

            }

            @Override
            public void onPositiveClicked() {
                leaveRoom(mafiaRoomsModel.getId());
            }
        }, true, getString(R.string.cancel));
    }

    @Override
    public void onItemClicked(MafiaRoomsModel mafiaRoomsModel) {
        Parcelable parcelable = Parcels.wrap(mafiaRoomsModel);

        Intent intent = new Intent(this, MafiaGameView.class);
        intent.putExtra("mafiaRoomsModel", parcelable);
        startActivity(intent);
    }

    private BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            showSwipe();
            onRefresh();
        }
    };

    private void deleteRoom(final int roomId) {
        showSwipe();
        makeRequest(Retrofit.getInstance().getInkService().deleteMafiaRoom(roomId, sharedHelper.getUserId()), null, new RequestCallback() {
            @Override
            public void onRequestSuccess(Object result) {
                dismissSwipe();
                try {
                    String responseBody = ((ResponseBody) result).string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    if (success) {
                        sharedHelper.putRoleSeen(false);
                        stopService(new Intent(MafiaRoomActivity.this, MafiaGameService.class));
                        sharedHelper.putMafiaParticipation(false);
                        mafiaRoomAdapter.clear();
                        getRoomsAccordingly();
                    } else {
                        String cause = jsonObject.optString("cause");
                        if (cause.equals(GAME_IN_PROGRESS)) {
                            Toast.makeText(MafiaRoomActivity.this, getString(R.string.gameInProgressWarning), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(MafiaRoomActivity.this, getString(R.string.failedToDelete), Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onRequestFailed(Object[] result) {
                dismissSwipe();
            }
        });
    }


    private void joinRoom(final int id) {
        showSwipe();
        makeRequest(Retrofit.getInstance().getInkService().joinRoom(id, sharedHelper.getUserId()), null, new RequestCallback() {
            @Override
            public void onRequestSuccess(Object result) {
                dismissSwipe();
                try {
                    String responseBody = ((ResponseBody) result).string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    if (success) {
                        sharedHelper.putRoleSeen(false);
                        sharedHelper.putMafiaParticipation(true);
                        sharedHelper.putMafiaLastRoomId(id);
                        try {
                            stopService(new Intent(getApplicationContext(), MafiaGameService.class));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        startService(new Intent(MafiaRoomActivity.this, MafiaGameService.class));
                        Toast.makeText(MafiaRoomActivity.this, getString(R.string.joined), Toast.LENGTH_SHORT).show();
                        getRoomsAccordingly();
                    } else {
                        String cause = jsonObject.optString("cause");
                        if (cause.equals(GAME_ALREADY_IN_PROGRESS)) {
                            Toast.makeText(MafiaRoomActivity.this, getString(R.string.cantJoinGameInProgress), Toast.LENGTH_LONG).show();
                            getRoomsAccordingly();
                        } else if (cause.equals(MAXIMUM_PLAYERS_REACHED)) {
                            Toast.makeText(MafiaRoomActivity.this, getString(R.string.cantJoinMaximumPlayers), Toast.LENGTH_LONG).show();
                            getRoomsAccordingly();
                        } else if (cause.equals(ROOM_DELETED)) {
                            Toast.makeText(MafiaRoomActivity.this, getString(R.string.cantJoinRoomDeleted), Toast.LENGTH_LONG).show();
                            getRoomsAccordingly();
                        } else if (cause.equals(ALREADY_IN_ROOM)) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MafiaRoomActivity.this);
                            builder.setTitle(getString(R.string.error));
                            builder.setMessage(getString(R.string.alreadyInRoom));
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            builder.show();
                        } else {
                            Toast.makeText(MafiaRoomActivity.this, getString(R.string.serverErrorText), Toast.LENGTH_LONG).show();
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onRequestFailed(Object[] result) {
                dismissSwipe();
            }
        });
    }


    private void leaveRoom(final int id) {
        showSwipe();
        makeRequest(Retrofit.getInstance().getInkService().leaveRoom(id, sharedHelper.getUserId()), null, new RequestCallback() {
            @Override
            public void onRequestSuccess(Object result) {
                dismissSwipe();
                try {
                    String responseBody = ((ResponseBody) result).string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    if (success) {
                        sharedHelper.putRoleSeen(false);
                        stopService(new Intent(MafiaRoomActivity.this, MafiaGameService.class));
                        sharedHelper.putMafiaParticipation(false);
                        getRoomsAccordingly();
                    } else {
                        Toast.makeText(MafiaRoomActivity.this, getString(R.string.serverErrorText), Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                dismissSwipe();
            }

            @Override
            public void onRequestFailed(Object[] result) {
                dismissSwipe();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(updateReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
