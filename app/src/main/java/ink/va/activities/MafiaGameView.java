package ink.va.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Parcelable;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
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
import com.google.gson.Gson;
import com.ink.va.R;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.va.adapters.MafiaChatAdapter;
import ink.va.adapters.MafiaPlayersAdapter;
import ink.va.models.MafiaMessageModel;
import ink.va.models.MafiaRoomsModel;
import ink.va.models.ParticipantModel;
import ink.va.service.MafiaGameService;
import ink.va.utils.Constants;
import ink.va.utils.DialogUtils;
import ink.va.utils.Keyboard;
import ink.va.utils.ProgressDialog;
import ink.va.utils.Retrofit;
import ink.va.utils.SharedHelper;
import ink.va.utils.Time;
import ink.va.utils.TransparentPanel;
import ink.va.utils.User;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.github.nkzawa.socketio.client.Socket.EVENT_CONNECT;
import static com.github.nkzawa.socketio.client.Socket.EVENT_CONNECT_ERROR;
import static com.github.nkzawa.socketio.client.Socket.EVENT_CONNECT_TIMEOUT;
import static ink.va.utils.Constants.EVENT_MAFIA_GLOBAL_MESSAGE;
import static ink.va.utils.Constants.EVENT_ON_MAFIA_GAME_STARTED;
import static ink.va.utils.Constants.EVENT_ON_ROLE_RECEIVED;
import static ink.va.utils.Constants.EVENT_ON_USER_JOINED_MAFIA_ROOM;
import static ink.va.utils.Constants.EVENT_ON_USER_LEFT_MAFIA_ROOM;
import static ink.va.utils.ErrorCause.ALREADY_IN_ROOM;
import static ink.va.utils.ErrorCause.GAME_ALREADY_IN_PROGRESS;
import static ink.va.utils.ErrorCause.GAME_IN_PROGRESS;
import static ink.va.utils.ErrorCause.MAXIMUM_PLAYERS_REACHED;
import static ink.va.utils.ErrorCause.ROOM_DELETED;
import static ink.va.utils.Time.UNIT_DAY;
import static ink.va.utils.Time.UNIT_HOUR;
import static ink.va.utils.Time.UNIT_MINUTE;
import static ink.va.utils.Time.convertToMillis;

public class MafiaGameView extends BaseActivity {
    private static final int ITEM_LEAVE_ID = 1;
    private static final int ITEM_JOIN_ID = 2;
    private static final int ITEM_DELETE_ID = 3;
    private static final int ITEM_START_GAME = 4;
    private static final String NIGHT_COME_SYSTEM_MESSAGE = "The night has come to the city wrapping the danger around the civilians as the Mafia has gone hunting. Be careful!";
    private static final String DAY_COME_SYSTEM_MESSAGE = "The Day has come to the city with the significant pleasure of secure and warm feeling.";
    public static final String GAME_STARTED_SYSTEM_MESSAGE = "The game has started. Drawing the roles to the players.";


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
    @BindView(R.id.timeLeftTV)
    TextView timeLeftTV;
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
    private int minimumClassicPlayers = 9;
    private int minimumYakudzaPlayers = 13;
    private CountDownTimer countDownTimer;
    private Menu menu;
    private JSONObject socketJson;
    private Gson gson;


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

        gson = new Gson();

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
        initDayTypeAndTime(null, false);
        if (!mafiaRoomsModel.isGameStarted()) {
            nightDayIV.setVisibility(View.INVISIBLE);
            timeLeftTV.setVisibility(View.INVISIBLE);
        }
    }

    private void initDayTypeAndTime(final String messageToInsert, final boolean isSystemMessage) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = null;

        if (messageToInsert != null) {
            silentMessageServerInsert(messageToInsert, isSystemMessage);
            replyToRoomIV.setEnabled(false);
            replyToRoomIV.setEnabled(false);
            replyToRoomIV.setClickable(false);
        }

        stopService(new Intent(MafiaGameView.this, MafiaGameService.class));

        Retrofit.getInstance().getInkService().getSingleMafiaRoom(mafiaRoomsModel.getId()).enqueue(new Callback<MafiaRoomsModel>() {
            @Override
            public void onResponse(Call<MafiaRoomsModel> call, Response<MafiaRoomsModel> response) {
                mafiaRoomsModel = response.body();

                if (mafiaRoomsModel.isGameStarted()) {
                    nightDayIV.setVisibility(View.VISIBLE);
                    timeLeftTV.setVisibility(View.VISIBLE);

                    boolean isMorning = true;

                    nightDayIV.setVisibility(View.VISIBLE);

                    switch (mafiaRoomsModel.getCurrentDayType()) {
                        case Constants.DAY_TYPE_DAYLIGHT:
                            isMorning = true;
                            nightDayIV.setImageResource(R.drawable.sun_icon);
                            transparentPanel.setDay();
                            break;
                        case Constants.DAY_TYPE_NIGHT:
                            isMorning = false;
                            transparentPanel.setNight();
                            nightDayIV.setImageResource(R.drawable.moon_icon);
                            break;
                    }

                    String currentServerDate = mafiaRoomsModel.getCurrentServerDate();
                    String gameStartDate = mafiaRoomsModel.getGameStartDate();

                    Date firstDate = Time.parseDate(currentServerDate);
                    Date secondDate = Time.parseDate(gameStartDate);

                    DateTime start = new DateTime(firstDate);
                    DateTime end = new DateTime(secondDate);
                    Period period = new Period(start, end, PeriodType.millis());

                    final long serverMillis = Math.abs(period.getMillis());
                    long gameDurationMillis = 0;

                    if (isMorning) {
                        if (mafiaRoomsModel.getMorningDurationUnit().equals(getString(R.string.minutesUnit))) {
                            gameDurationMillis = convertToMillis(UNIT_MINUTE, Long.valueOf(mafiaRoomsModel.getMorningDuration()));
                        } else if (mafiaRoomsModel.getMorningDurationUnit().equals(getString(R.string.hoursUnit))) {
                            gameDurationMillis = convertToMillis(UNIT_HOUR, Long.valueOf(mafiaRoomsModel.getMorningDuration()));
                        } else if (mafiaRoomsModel.getMorningDurationUnit().equals(getString(R.string.daysUnit))) {
                            gameDurationMillis = convertToMillis(UNIT_DAY, Long.valueOf(mafiaRoomsModel.getMorningDuration()));
                        }
                    } else {
                        if (mafiaRoomsModel.getNightDurationUnit().equals(getString(R.string.minutesUnit))) {
                            gameDurationMillis = convertToMillis(UNIT_MINUTE, Long.valueOf(mafiaRoomsModel.getNightDuration()));
                        } else if (mafiaRoomsModel.getNightDurationUnit().equals(getString(R.string.hoursUnit))) {
                            gameDurationMillis = convertToMillis(UNIT_HOUR, Long.valueOf(mafiaRoomsModel.getNightDuration()));
                        } else if (mafiaRoomsModel.getNightDurationUnit().equals(getString(R.string.daysUnit))) {
                            gameDurationMillis = convertToMillis(UNIT_DAY, Long.valueOf(mafiaRoomsModel.getNightDuration()));
                        }
                    }


                    final Date date = new Date();

                    long finalCountDownMillis = Math.abs(gameDurationMillis - serverMillis);

                    countDownTimer = new CountDownTimer(finalCountDownMillis, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            Calendar calendar = Calendar.getInstance();
                            date.setTime(millisUntilFinished);
                            calendar.setTime(date);

                            long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished);

                            String text = String.format("%02d:%02d:%02d", seconds / 3600,
                                    (seconds % 3600) / 60, (seconds % 60));


                            timeLeftTV.setText(mafiaRoomsModel.getCurrentDayType().equals(Constants.DAY_TYPE_DAYLIGHT) ?
                                    getString(R.string.timeLeftForNight, text) :
                                    getString(R.string.timeLeftForMorning, text));
                        }

                        @Override
                        public void onFinish() {
                            nightDayIV.setVisibility(View.INVISIBLE);
                            timeLeftTV.setText(getString(R.string.loadingText));

                            switch (mafiaRoomsModel.getCurrentDayType()) {
                                case Constants.DAY_TYPE_DAYLIGHT:
                                    //insert as night has come
                                    initDayTypeAndTime(NIGHT_COME_SYSTEM_MESSAGE, true);
                                    break;
                                case Constants.DAY_TYPE_NIGHT:
                                    //insert as day has come
                                    initDayTypeAndTime(DAY_COME_SYSTEM_MESSAGE, true);
                                    break;
                            }
                        }
                    };
                    countDownTimer.start();
                }


            }

            @Override
            public void onFailure(Call<MafiaRoomsModel> call, Throwable t) {
                Snackbar.make(mafiaRoleView, getString(R.string.serverErrorText), BaseTransientBottomBar.LENGTH_LONG).setAction(getString(R.string.vk_retry), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        initDayTypeAndTime(messageToInsert, isSystemMessage);
                    }
                }).show();
                Toast.makeText(MafiaGameView.this, getString(R.string.serverErrorText), Toast.LENGTH_SHORT).show();
            }
        });
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
        if (noMessagesTV.getVisibility() == View.VISIBLE) {
            noMessagesTV.setVisibility(View.GONE);
        }
        if (chatLoading.getVisibility() == View.VISIBLE) {
            chatLoading.setVisibility(View.GONE);
        }


    }

    private void showNoMessages() {
        if (noMessagesTV.getVisibility() == View.GONE) {
            noMessagesTV.setVisibility(View.VISIBLE);
        }

        if (chatLoading.getVisibility() == View.VISIBLE) {
            chatLoading.setVisibility(View.GONE);
        }
    }


    /**
     * Emitters
     */
    private Emitter.Listener onGlobalMessageReceived = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject jsonObject = (JSONObject) args[0];
            MafiaMessageModel mafiaMessageModel = gson.fromJson(jsonObject.toString(), MafiaMessageModel.class);
            mafiaChatAdapter.insertMessage(mafiaMessageModel);
            scrollToBottom();
        }
    };

    private Emitter.Listener onConnectionError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
        }
    };

    private Emitter.Listener onConnectionTimeOut = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
        }
    };

    private Emitter.Listener onSocketConnected = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
        }
    };

    private Emitter.Listener onUserLeftRoom = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject jsonObject = (JSONObject) args[0];
            int roomId = jsonObject.optInt("roomId");
            if (roomId == mafiaRoomsModel.getId()) {
                ParticipantModel participantModel = gson.fromJson(jsonObject.optString("participantModel"), ParticipantModel.class);
                mafiaPlayersAdapter.removeUser(participantModel);
            }
        }
    };

    private Emitter.Listener onUserJoinedRoom = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject jsonObject = (JSONObject) args[0];
            int roomId = jsonObject.optInt("roomId");
            if (roomId == mafiaRoomsModel.getId()) {
                ParticipantModel participantModel = gson.fromJson(jsonObject.optString("participantModel"), ParticipantModel.class);
                mafiaPlayersAdapter.addUser(participantModel);
            }
        }
    };

    private Emitter.Listener onGameStarted = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

        }
    };

    private Emitter.Listener onRoleReceived = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

        }
    };


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        this.menu = menu;
        if (!isMenuAdded) {
            if (isOwner() && !mafiaRoomsModel.isGameStarted()) {
                menu.add(0, ITEM_START_GAME, 0, getString(R.string.startGame));
                menu.add(0, ITEM_DELETE_ID, 1, getString(R.string.delete));
            }

            if (isParticipant()) {
                menu.add(0, ITEM_LEAVE_ID, 0, getString(R.string.leave));
            } else {
                menu.add(0, ITEM_JOIN_ID, 0, getString(R.string.join));
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
            case ITEM_START_GAME:
                startGame();
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

    private void startGame() {
        if (mafiaRoomsModel.getGameType().equals(getString(R.string.yakudza))) {
            int currentPlayers = mafiaRoomsModel.getJoinedUsers().size();
            if (currentPlayers < minimumYakudzaPlayers) {
                int playersNeeded = minimumYakudzaPlayers - currentPlayers;
                DialogUtils.showDialog(this, getString(R.string.cantStart), getString(R.string.needMorePlayersText, playersNeeded), true, null, false, null);
            } else {
                callStartGame();
            }
        } else if (mafiaRoomsModel.getGameType().equals(getString(R.string.classic))) {
            int currentPlayers = mafiaRoomsModel.getJoinedUsers().size();
            if (currentPlayers < minimumClassicPlayers) {
                int playersNeeded = minimumClassicPlayers - currentPlayers;
                DialogUtils.showDialog(this, getString(R.string.cantStart), getString(R.string.needMorePlayersText, playersNeeded), true, null, false, null);
            } else {
                callStartGame();
            }
        }
    }

    private void callStartGame() {
        showDialog(getString(R.string.connecting), getString(R.string.startingGame));
        Retrofit.getInstance().getInkService().startMafiaGame(mafiaRoomsModel.getId()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    callStartGame();
                    return;
                }
                if (response.body() == null) {
                    callStartGame();
                    return;
                }
                hideDialog();
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    if (success) {
                        menu.removeItem(ITEM_START_GAME);
                        menu.removeItem(ITEM_DELETE_ID);
                        if (socketJson != null) {
                            socketJson = null;
                        }
                        socketJson = new JSONObject();
                        String roomJson = gson.toJson(mafiaRoomsModel);
                        socketJson.put("roomModel", roomJson);
                        socket.emit(EVENT_ON_MAFIA_GAME_STARTED, socketJson);

                        initDayTypeAndTime(GAME_STARTED_SYSTEM_MESSAGE, true);
                    } else {
                        Toast.makeText(MafiaGameView.this, getString(R.string.couldNotStartGame), Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(MafiaGameView.this, getString(R.string.couldNotStartGame), Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(MafiaGameView.this, getString(R.string.couldNotStartGame), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                hideDialog();
                Toast.makeText(MafiaGameView.this, getString(R.string.couldNotStartGame), Toast.LENGTH_SHORT).show();
            }
        });
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
                        stopService(new Intent(MafiaGameView.this, MafiaGameService.class));
                        sharedHelper.putMafiaParticipation(false);
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
        socket.on(EVENT_CONNECT_ERROR, onConnectionError);
        socket.on(EVENT_CONNECT_TIMEOUT, onConnectionTimeOut);
        socket.on(EVENT_CONNECT, onSocketConnected);
        socket.on(EVENT_ON_USER_LEFT_MAFIA_ROOM, onUserLeftRoom);
        socket.on(EVENT_ON_USER_JOINED_MAFIA_ROOM, onUserJoinedRoom);
        socket.on(EVENT_ON_MAFIA_GAME_STARTED, onGameStarted);
        socket.on(EVENT_ON_ROLE_RECEIVED, onRoleReceived);
        socket.connect();
    }

    private void getMafiaRoomParticipants() {
        Retrofit.getInstance().getInkService().getMafiaRoomParticipants(mafiaRoomsModel.getId()).enqueue(new Callback<List<ParticipantModel>>() {
            @Override
            public void onResponse(Call<List<ParticipantModel>> call, Response<List<ParticipantModel>> response) {
                List<ParticipantModel> participants = response.body();
                if (participants.isEmpty()) {
                    showNoParticipants();
                } else {
                    hideNoParticipants();
                    mafiaPlayersAdapter.setUsers(participants);
                }
            }

            @Override
            public void onFailure(Call<List<ParticipantModel>> call, Throwable t) {
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

                        if (socketJson != null) {
                            socketJson = null;
                        }
                        sharedHelper.putMafiaParticipation(true);
                        sharedHelper.putMafiaLastRoomId(mafiaRoomsModel.getId());
                        startService(new Intent(MafiaGameView.this, MafiaGameService.class));
                        Toast.makeText(MafiaGameView.this, getString(R.string.joined), Toast.LENGTH_SHORT).show();
                        Parcelable parcelable = Parcels.wrap(mafiaRoomsModel);
                        List<ParticipantModel> joinedUsers = mafiaRoomsModel.getJoinedUsers();

                        ParticipantModel participantModel = new ParticipantModel();
                        participantModel.setEliminated(false);
                        participantModel.setRole("");
                        participantModel.setUser(User.get().buildUser(sharedHelper));
                        joinedUsers.add(participantModel);

                        socketJson = new JSONObject();
                        String participantJson = gson.toJson(participantModel);

                        socketJson.put("roomId", mafiaRoomsModel.getId());
                        socketJson.put("participantModel", participantJson);

                        socket.emit(EVENT_ON_USER_JOINED_MAFIA_ROOM, socketJson);

                        mafiaRoomsModel.setJoinedUsers(joinedUsers);
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
                        if (socketJson != null) {
                            socketJson = null;
                        }
                        ParticipantModel participantModel = new ParticipantModel();
                        participantModel.setUser(User.get().buildUser(sharedHelper));
                        participantModel.setEliminated(true);
                        participantModel.setRole("none");

                        String userJson = gson.toJson(participantModel);

                        socketJson = new JSONObject();
                        socketJson.put("roomId", mafiaRoomsModel.getId());
                        socketJson.put("participantModel", userJson);
                        socket.emit(EVENT_ON_USER_LEFT_MAFIA_ROOM, socketJson);


                        sharedHelper.putMafiaParticipation(false);
                        stopService(new Intent(MafiaGameView.this, MafiaGameService.class));
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
        silentMessageServerInsert(unescapedMessage, false);
        if (socketJson != null) {
            socketJson = null;

        }
        try {
            socketJson = new JSONObject();
            socketJson.put("id", String.valueOf(System.currentTimeMillis()));
            socketJson.put("room_id", mafiaRoomsModel.getId());
            socketJson.put("message", replyToRoomED.getText().toString());
            socketJson.put("sender_id", sharedHelper.getUserId());
            socketJson.put("isSystemMessage", false);
            String userJson = gson.toJson(User.get().buildUser(sharedHelper));

            socketJson.put("user", userJson);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.emit(EVENT_MAFIA_GLOBAL_MESSAGE, socketJson);
        replyToRoomED.setText("");
        replyToRoomIV.setEnabled(false);
        Keyboard.hideKeyboard(this);
    }

    private void silentMessageServerInsert(final String message, final boolean isSystemMessage) {
        Retrofit.getInstance().getInkService().silentMafiaMessageInsert(mafiaRoomsModel.getId(), message, sharedHelper.getUserId(), isSystemMessage).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    silentMessageServerInsert(message, isSystemMessage);
                    return;
                }
                if (response.body() == null) {
                    silentMessageServerInsert(message, isSystemMessage);
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
        mafiaMessageModel.setSystemMessage(isSystemMessage);
        mafiaMessageModel.setUser(User.get().buildUser(sharedHelper));
        mafiaChatAdapter.insertMessage(mafiaMessageModel);
        hideNoMessages();
        scrollToBottom();

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
        for (ParticipantModel eachId : mafiaRoomsModel.getJoinedUsers()) {
            if (eachId.getUser().getUserId().equals(currentUserId)) {
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
        if (mafiaRoomsModel.isGameStarted()) {
            gameStartedTV.setTextColor(ContextCompat.getColor(this, R.color.darkGreen));
            gameStartedTV.setText(getString(R.string.gameStarted));
        } else {
            gameStartedTV.setTextColor(ContextCompat.getColor(this, R.color.red));
            gameStartedTV.setText(getString(R.string.gameNotStarted));
        }
        String gameType = getString(R.string.classic);
        if (mafiaRoomsModel.getGameType().equals(getString(R.string.yakudza))) {
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
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        destroySocket();
    }

    private void destroySocket() {
        socket.disconnect();
        socket.off(EVENT_MAFIA_GLOBAL_MESSAGE, onGlobalMessageReceived);
        socket.off(EVENT_CONNECT_ERROR, onConnectionError);
        socket.off(EVENT_CONNECT_TIMEOUT, onConnectionTimeOut);
        socket.off(EVENT_ON_USER_LEFT_MAFIA_ROOM, onUserLeftRoom);
        socket.off(EVENT_CONNECT, onSocketConnected);
        socket.off(EVENT_ON_USER_JOINED_MAFIA_ROOM, onUserJoinedRoom);
        socket.off(EVENT_ON_MAFIA_GAME_STARTED, onGameStarted);
        socket.off(EVENT_ON_ROLE_RECEIVED, onRoleReceived);
    }

}
