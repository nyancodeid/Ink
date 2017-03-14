package ink.va.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.ink.va.R;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.va.adapters.MafiaChatAdapter;
import ink.va.adapters.MafiaPlayersAdapter;
import ink.va.models.MafiaMessageModel;
import ink.va.models.MafiaRoomsModel;
import ink.va.models.UserModel;
import ink.va.utils.Constants;
import ink.va.utils.DialogUtils;
import ink.va.utils.Keyboard;
import ink.va.utils.ProgressDialog;
import ink.va.utils.Retrofit;
import ink.va.utils.SharedHelper;
import ink.va.utils.TransparentPanel;
import ink.va.utils.User;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static ink.va.utils.Constants.EVENT_MAFIA_GLOBAL_MESSAGE;
import static ink.va.utils.ErrorCause.ALREADY_IN_ROOM;
import static ink.va.utils.ErrorCause.GAME_ALREADY_IN_PROGRESS;
import static ink.va.utils.ErrorCause.GAME_IN_PROGRESS;
import static ink.va.utils.ErrorCause.MAXIMUM_PLAYERS_REACHED;
import static ink.va.utils.ErrorCause.ROOM_DELETED;

public class MafiaGameView extends BaseActivity {
    private static final int ITEM_LEAVE_ID = 1;
    private static final int ITEM_JOIN_ID = 2;
    private static final int ITEM_DELETE_ID = 3;
    private MafiaRoomsModel mafiaRoomsModel;
    @BindView(R.id.playersLoading)
    ProgressBar playersLoading;
    @BindView(R.id.noPlayersTV)
    TextView noPlayersTV;
    @BindView(R.id.playersRecycler)
    RecyclerView playersRecycler;
    @BindView(R.id.mafiaChatRecycler)
    RecyclerView mafiaChatRecycler;
    @BindView(R.id.replyToRoomED)
    EditText replyToRoomED;
    @BindView(R.id.activity_mafia_game_view)
    TransparentPanel transparentPanel;
    @BindView(R.id.replyToRoomIV)
    ImageView replyToRoomIV;
    @BindView(R.id.chatLoading)
    ProgressBar chatLoading;
    @BindView(R.id.noMessagesTV)
    TextView noMessagesTV;
    @BindView(R.id.nightDayIV)
    ImageView nightDayIV;
    @BindView(R.id.mafiaRoleView)
    View mafiaRoleView;
    @BindView(R.id.mafiaRoleExplanationTV)
    TextView mafiaRoleExplanationTV;
    @BindView(R.id.mafiaRoleHolder)
    ImageView mafiaRoleHolder;
    @BindView(R.id.closeRoleView)
    Button closeRoleView;
    @BindView(R.id.gameStartedTV)
    TextView gameStartedTV;
    @BindView(R.id.gameTypeTV)
    TextView gameTypeTV;
    @BindView(R.id.singleMorningDurationTV)
    TextView singleMorningDurationTV;
    @BindView(R.id.singleNightDurationTV)
    TextView singleNightDurationTV;
    @BindView(R.id.roomLanguageTV)
    TextView roomLanguageTV;

    private Animation slideOutWithFade;
    private Animation slideInWithFade;
    private SharedHelper sharedHelper;
    private boolean isMenuAdded;
    private ProgressDialog progressDialog;
    private Socket socket;
    private MafiaPlayersAdapter mafiaPlayersAdapter;
    private MafiaChatAdapter mafiaChatAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mafia_game_view);
        ButterKnife.bind(this);
        progressDialog = ProgressDialog.get().buildProgressDialog(this, false);
        progressDialog.setCancelable(false);
        sharedHelper = new SharedHelper(this);
        slideOutWithFade = AnimationUtils.loadAnimation(this, R.anim.slide_out_with_fade);
        slideInWithFade = AnimationUtils.loadAnimation(this, R.anim.slide_in_with_fade);
        replyToRoomIV.setEnabled(false);

        initSocket();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mafiaRoomsModel = Parcels.unwrap(extras.getParcelable("mafiaRoomsModel"));
            getSupportActionBar().setTitle(mafiaRoomsModel.getRoomName());
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initEditText(isParticipant());
        initGameInfo();
        initRecyclers();
    }

    private void initRecyclers() {
        mafiaPlayersAdapter = new MafiaPlayersAdapter();
        mafiaChatAdapter = new MafiaChatAdapter();
        mafiaPlayersAdapter.setOwnerId(mafiaRoomsModel.getCreatorId());
        LinearLayoutManager playersLayout = new LinearLayoutManager(this);
        LinearLayoutManager chatLayout = new LinearLayoutManager(this);
        chatLayout.setStackFromEnd(true);
        playersRecycler.setLayoutManager(playersLayout);
        mafiaChatRecycler.setLayoutManager(chatLayout);
        mafiaChatRecycler.setAdapter(mafiaChatAdapter);
        playersRecycler.setAdapter(mafiaPlayersAdapter);
        getMafiaRoomParticipants();
        getMafiaRoomMessages();
    }

    private void getMafiaRoomMessages() {
        Retrofit.getInstance().getInkService().getMafiaChat(mafiaRoomsModel.getId()).enqueue(new Callback<List<MafiaMessageModel>>() {
            @Override
            public void onResponse(Call<List<MafiaMessageModel>> call, Response<List<MafiaMessageModel>> response) {
                List<MafiaMessageModel> mafiaMessageModels = response.body();
                if (mafiaMessageModels.isEmpty()) {
                    showNoMessages();
                } else {
                    hideNoMessages();
                    mafiaChatAdapter.setMafiaMessageModels(mafiaMessageModels);
                    scrollToBottom();
                }
            }

            @Override
            public void onFailure(Call<List<MafiaMessageModel>> call, Throwable t) {
                hideNoMessages();
                Toast.makeText(MafiaGameView.this, getString(R.string.serverErrorText), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void scrollToBottom() {
        mafiaChatRecycler.post(new Runnable() {
            @Override
            public void run() {
                mafiaChatRecycler.stopScroll();
                mafiaChatRecycler.scrollToPosition(mafiaChatAdapter.getItemCount() - 1);
            }
        });
    }

    private void hideNoMessages() {
        chatLoading.setVisibility(View.GONE);
        noMessagesTV.setVisibility(View.GONE);
    }

    private void showNoMessages() {
        noMessagesTV.setVisibility(View.VISIBLE);
        chatLoading.setVisibility(View.GONE);
    }


    /**
     * Emitters
     */
    private Emitter.Listener onGlobalMessageReceived = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

        }
    };


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (!isMenuAdded) {
            if (isParticipant()) {
                menu.add(0, ITEM_LEAVE_ID, 0, getString(R.string.leave));
            } else {
                menu.add(0, ITEM_JOIN_ID, 0, getString(R.string.join));
            }
            if (isOwner()) {
                menu.add(0, ITEM_DELETE_ID, 1, getString(R.string.delete));
            }
            isMenuAdded = true;
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mafia_game_view_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case ITEM_LEAVE_ID:
                DialogUtils.showDialog(this, getString(R.string.leaveTitle), getString(R.string.leaveContent), true, new DialogUtils.DialogListener() {
                    @Override
                    public void onNegativeClicked() {

                    }

                    @Override
                    public void onDialogDismissed() {

                    }

                    @Override
                    public void onPositiveClicked() {
                        leaveRoom();
                    }
                }, true, getString(R.string.cancel));
                break;
            case ITEM_JOIN_ID:
                joinRoom();
                break;
            case ITEM_DELETE_ID:
                DialogUtils.showDialog(this, getString(R.string.delete), getString(R.string.actionCannotUndone), true, new DialogUtils.DialogListener() {
                    @Override
                    public void onNegativeClicked() {

                    }

                    @Override
                    public void onDialogDismissed() {

                    }

                    @Override
                    public void onPositiveClicked() {
                        deleteRoom();
                    }
                }, true, getString(R.string.cancel));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteRoom() {
        showDialog(getString(R.string.pleaseWait), getString(R.string.deleting));
        Call<ResponseBody> deleteCall = Retrofit.getInstance().getInkService().deleteMafiaRoom(mafiaRoomsModel.getId(), sharedHelper.getUserId());
        deleteCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    deleteRoom();
                    return;
                }
                if (response.body() == null) {
                    deleteRoom();
                    return;
                }
                hideDialog();
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    if (success) {
                        finish();
                        LocalBroadcastManager.getInstance(MafiaGameView.this).sendBroadcast(new Intent(getPackageName() + "update"));
                    } else {
                        String cause = jsonObject.optString("cause");
                        if (cause.equals(GAME_IN_PROGRESS)) {
                            Toast.makeText(MafiaGameView.this, getString(R.string.gameInProgressWarning), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(MafiaGameView.this, getString(R.string.failedToDelete), Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                hideDialog();
                Toast.makeText(MafiaGameView.this, getString(R.string.serverErrorText), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initSocket() {
        try {
            socket = IO.socket(Constants.MAFIA_SOCKET_URL);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        socket.on(EVENT_MAFIA_GLOBAL_MESSAGE, onGlobalMessageReceived);
        socket.connect();
    }

    private void getMafiaRoomParticipants() {
        Retrofit.getInstance().getInkService().getMafiaRoomParticipants(mafiaRoomsModel.getId()).enqueue(new Callback<List<UserModel>>() {
            @Override
            public void onResponse(Call<List<UserModel>> call, Response<List<UserModel>> response) {
                List<UserModel> participants = response.body();
                if (participants.isEmpty()) {
                    showNoParticipants();
                } else {
                    hideNoParticipants();
                    mafiaPlayersAdapter.setUsers(participants);
                }
            }

            @Override
            public void onFailure(Call<List<UserModel>> call, Throwable t) {
                Toast.makeText(MafiaGameView.this, getString(R.string.serverErrorText), Toast.LENGTH_SHORT).show();
                hideNoParticipants();
            }
        });
    }

    private void hideNoParticipants() {
        playersLoading.setVisibility(View.GONE);
        noPlayersTV.setVisibility(View.GONE);
    }

    private void showNoParticipants() {
        playersLoading.setVisibility(View.GONE);
        noPlayersTV.setVisibility(View.VISIBLE);
    }

    private void joinRoom() {
        showDialog(getString(R.string.pleaseWait), getString(R.string.joining));
        Call<ResponseBody> joinRoomCall = Retrofit.getInstance().getInkService().joinRoom(mafiaRoomsModel.getId(), sharedHelper.getUserId());
        joinRoomCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    joinRoom();
                    return;
                }
                if (response.body() == null) {
                    joinRoom();
                    return;
                }
                hideDialog();
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    if (success) {
                        Toast.makeText(MafiaGameView.this, getString(R.string.joined), Toast.LENGTH_SHORT).show();

                        Parcelable parcelable = Parcels.wrap(mafiaRoomsModel);
                        List<String> joinedUsers = mafiaRoomsModel.getJoinedUserIds();
                        joinedUsers.add(sharedHelper.getUserId());
                        mafiaRoomsModel.setJoinedUserIds(joinedUsers);

                        relaunchActivity(parcelable);

                    } else {
                        String cause = jsonObject.optString("cause");
                        if (cause.equals(GAME_ALREADY_IN_PROGRESS)) {
                            Toast.makeText(MafiaGameView.this, getString(R.string.cantJoinGameInProgress), Toast.LENGTH_LONG).show();

                            Parcelable parcelable = Parcels.wrap(mafiaRoomsModel);
                            mafiaRoomsModel.setGameStarted(true);

                            relaunchActivity(parcelable);
                        } else if (cause.equals(MAXIMUM_PLAYERS_REACHED)) {
                            Toast.makeText(MafiaGameView.this, getString(R.string.cantJoinMaximumPlayers), Toast.LENGTH_LONG).show();
                        } else if (cause.equals(ROOM_DELETED)) {
                            Toast.makeText(MafiaGameView.this, getString(R.string.cantJoinRoomDeleted), Toast.LENGTH_LONG).show();
                            finish();
                            LocalBroadcastManager.getInstance(MafiaGameView.this).sendBroadcast(new Intent(getPackageName() + "update"));
                        } else if (cause.equals(ALREADY_IN_ROOM)) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MafiaGameView.this);
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
                            Toast.makeText(MafiaGameView.this, getString(R.string.serverErrorText), Toast.LENGTH_LONG).show();
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                hideDialog();
                Toast.makeText(MafiaGameView.this, getString(R.string.serverErrorText), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void relaunchActivity(Parcelable parcelable) {
        Intent intent = new Intent(this, MafiaGameView.class);
        intent.putExtra("mafiaRoomsModel", parcelable);
        finish();
        startActivity(intent);
    }

    private void leaveRoom() {
        showDialog(getString(R.string.pleaseWait), getString(R.string.leavingText));
        Call<ResponseBody> responseBodyCall = Retrofit.getInstance().getInkService().leaveRoom(mafiaRoomsModel.getId(), sharedHelper.getUserId());
        responseBodyCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    leaveRoom();
                    return;
                }
                if (response.body() == null) {
                    leaveRoom();
                    return;
                }
                hideDialog();
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    if (success) {
                        finish();
                        LocalBroadcastManager.getInstance(MafiaGameView.this).sendBroadcast(new Intent(getPackageName() + "update"));
                    } else {
                        Toast.makeText(MafiaGameView.this, getString(R.string.serverErrorText), Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                hideDialog();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                hideDialog();
                Toast.makeText(MafiaGameView.this, getString(R.string.serverErrorText), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @OnClick(R.id.replyToRoomIV)
    public void replyToRoomIVClicked() {
        String unescapedMessage = replyToRoomED.getText().toString().trim();
        silentMessageServerInsert(StringEscapeUtils.escapeJava(unescapedMessage));
        replyToRoomED.setText("");
        replyToRoomIV.setEnabled(false);
    }

    private void silentMessageServerInsert(final String message) {
        Retrofit.getInstance().getInkService().silentMafiaMessageInsert(mafiaRoomsModel.getId(), message, sharedHelper.getUserId()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    silentMessageServerInsert(message);
                    return;
                }
                if (response.body() == null) {
                    silentMessageServerInsert(message);
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    if (!success) {
                        Toast.makeText(MafiaGameView.this, getString(R.string.messageNotSent), Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(MafiaGameView.this, getString(R.string.messageNotSent), Toast.LENGTH_SHORT).show();
            }
        });
        MafiaMessageModel mafiaMessageModel = new MafiaMessageModel();
        mafiaMessageModel.setId(String.valueOf(System.currentTimeMillis()));
        mafiaMessageModel.setMessage(message);
        mafiaMessageModel.setRoomId(mafiaRoomsModel.getId());
        mafiaMessageModel.setSenderId(sharedHelper.getUserId());
        mafiaMessageModel.setUser(User.get().buildUser(sharedHelper));
        mafiaChatAdapter.insertMessage(mafiaMessageModel);

    }

    @OnClick(R.id.closeRoleView)
    public void closeRoleClicked() {
        closeRoleView();
    }

    private void clearImage() {
        mafiaRoleHolder.setImageDrawable(null);
    }

    private void setButtonState(boolean enabled) {
        closeRoleView.setEnabled(enabled);
    }

    private void openRoleView(final int roleResourceId) {
        Keyboard.hideKeyboard(this);
        mafiaRoleView.setVisibility(View.VISIBLE);
        slideInWithFade.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                setButtonState(true);
                mafiaRoleHolder.setImageDrawable(ContextCompat.getDrawable(MafiaGameView.this, roleResourceId));
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mafiaRoleView.startAnimation(slideInWithFade);
    }

    private void closeRoleView() {
        setButtonState(false);
        slideOutWithFade.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mafiaRoleView.setVisibility(View.GONE);
                clearImage();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mafiaRoleView.startAnimation(slideOutWithFade);
    }

    private void initEditText(boolean enabled) {
        if (enabled) {
            replyToRoomED.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String text = s.toString().toString();
                    if (text.isEmpty()) {
                        replyToRoomIV.setEnabled(false);
                    } else {
                        replyToRoomIV.setEnabled(true);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        } else {
            replyToRoomED.setEnabled(false);
            replyToRoomED.setClickable(false);
            replyToRoomED.setFocusable(false);
            replyToRoomED.setFocusableInTouchMode(false);
            replyToRoomED.setHint(getString(R.string.cantReply));
            replyToRoomIV.setEnabled(false);
        }

    }

    private boolean isParticipant() {
        boolean isParticipant = false;
        String currentUserId = sharedHelper.getUserId();
        for (String eachId : mafiaRoomsModel.getJoinedUserIds()) {
            if (eachId.equals(currentUserId)) {
                isParticipant = true;
                break;
            }
        }
        return isParticipant;
    }

    private boolean isOwner() {
        return mafiaRoomsModel.getCreatorId().equals(sharedHelper.getUserId());
    }

    private void hideDialog() {
        ProgressDialog.get().hide();
    }

    private void showDialog(String title, String message) {
        progressDialog.setTitle(title);
        progressDialog.setMessage(message);
        progressDialog.show();
    }


    private void initGameInfo() {
        if (mafiaRoomsModel.gameStarted) {
            gameStartedTV.setTextColor(ContextCompat.getColor(this, R.color.darkGreen));
            gameStartedTV.setText(getString(R.string.gameStarted));
        } else {
            gameStartedTV.setTextColor(ContextCompat.getColor(this, R.color.red));
            gameStartedTV.setText(getString(R.string.gameNotStarted));
        }
        String gameType = getString(R.string.classic);
        if (mafiaRoomsModel.gameType.equals(getString(R.string.yakudza))) {
            gameType = getString(R.string.yakudza);
        }
        gameTypeTV.setText(getString(R.string.gameTypeText, gameType));

        String unit = getString(R.string.minutesUnit);
        if (mafiaRoomsModel.getMorningDurationUnit().equals(getString(R.string.hoursUnit))) {
            unit = getString(R.string.hoursUnit);
        } else if (mafiaRoomsModel.getMorningDurationUnit().equals(getString(R.string.daysUnit))) {
            unit = getString(R.string.daysUnit);
        }

        singleMorningDurationTV.setText(getString(R.string.oneMorningDuration, mafiaRoomsModel.getMorningDuration(), unit));

        String nightDuration = getString(R.string.minutesUnit);
        if (mafiaRoomsModel.getNightDuration().equals(getString(R.string.hoursUnit))) {
            nightDuration = getString(R.string.hoursUnit);
        } else if (mafiaRoomsModel.getNightDuration().equals(getString(R.string.daysUnit))) {
            nightDuration = getString(R.string.daysUnit);
        }

        singleNightDurationTV.setText(getString(R.string.oneNightDuration, mafiaRoomsModel.getNightDuration(), nightDuration));
        String language = getString(R.string.english);
        if (mafiaRoomsModel.getRoomLanguage().equals(getString(R.string.russian))) {
            language = getString(R.string.russian);
        }
        roomLanguageTV.setText(getString(R.string.roomLanguageText, language));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroySocket();
    }

    private void destroySocket() {
        socket.disconnect();
        socket.off(EVENT_MAFIA_GLOBAL_MESSAGE, onGlobalMessageReceived);
    }
}
