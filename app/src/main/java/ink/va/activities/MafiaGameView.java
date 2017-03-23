package ink.va.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
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
import com.google.gson.reflect.TypeToken;
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
import ink.va.interfaces.RecyclerItemClickListener;
import ink.va.models.MafiaMessageModel;
import ink.va.models.MafiaRoomsModel;
import ink.va.models.ParticipantModel;
import ink.va.models.ParticipantModelWithoutUser;
import ink.va.models.UserModel;
import ink.va.service.MafiaGameService;
import ink.va.utils.Constants;
import ink.va.utils.DialogUtils;
import ink.va.utils.ErrorCause;
import ink.va.utils.Keyboard;
import ink.va.utils.MafiaConstants;
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
import static com.github.nkzawa.socketio.client.Socket.EVENT_DISCONNECT;
import static ink.va.utils.Constants.EVENT_MAFIA_GLOBAL_MESSAGE;
import static ink.va.utils.Constants.EVENT_ON_MAFIA_GAME_STARTED;
import static ink.va.utils.Constants.EVENT_ON_SOCKET_MESSAGE_RECEIVED;
import static ink.va.utils.Constants.EVENT_ON_USER_JOINED_MAFIA_ROOM;
import static ink.va.utils.Constants.EVENT_ON_USER_LEFT_MAFIA_ROOM;
import static ink.va.utils.ErrorCause.ALREADY_IN_ROOM;
import static ink.va.utils.ErrorCause.ALREADY_SHOT_PLAYER;
import static ink.va.utils.ErrorCause.GAME_ALREADY_IN_PROGRESS;
import static ink.va.utils.ErrorCause.GAME_IN_PROGRESS;
import static ink.va.utils.ErrorCause.MAXIMUM_PLAYERS_REACHED;
import static ink.va.utils.ErrorCause.PLAYER_ELIMINATED;
import static ink.va.utils.ErrorCause.ROOM_DELETED;
import static ink.va.utils.MafiaConstants.DAY_TYPE_DAYLIGHT;
import static ink.va.utils.MafiaConstants.DAY_TYPE_NIGHT;
import static ink.va.utils.Time.UNIT_DAY;
import static ink.va.utils.Time.UNIT_HOUR;
import static ink.va.utils.Time.UNIT_MINUTE;
import static ink.va.utils.Time.convertToMillis;

public class MafiaGameView extends BaseActivity implements RecyclerItemClickListener {
    private static final int ITEM_LEAVE_ID = 1;
    private static final int ITEM_JOIN_ID = 2;
    private static final int ITEM_DELETE_ID = 3;
    private static final int ITEM_START_GAME = 4;
    private static final String NIGHT_COME_SYSTEM_MESSAGE = "The night has come to the city wrapping the danger around the civilians as the Mafia has gone hunting. Be careful!";
    private static final String DAY_COME_SYSTEM_MESSAGE = "The Day has come to the city with the significant pleasure of secure and warm feeling";
    public static final String GAME_STARTED_SYSTEM_MESSAGE = "The game has started. Drawing the roles to the players";
    public static final String MAFIA_PICKING_MESSAGE = "The Mafia is now Picking the victims ";


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
    @BindView(R.id.toggleMafiaChatMode)
    ImageView toggleMafiaChatMode;
    @BindView(R.id.choseVictimButton)
    View choseVictimButton;

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
    private Runnable lastMethodToRun;
    private boolean isMafiaToggled;
    private String lastVictimId = "";
    private int serverReconnectTry;
    private String lastVotedUserId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mafia_game_view);
        ButterKnife.bind(this);
        progressDialog = ProgressDialog.get().buildProgressDialog(this, false);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
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

        initGameInfo();
        initRecyclersAndService();
        initToggleIcon();
        if (mafiaRoomsModel.isGameEnded()) {
            buildEliminatedView();
            buildGameEnding();
        } else {
            initEditText(isParticipant(), true);
            initDayTypeAndTime(null, false, true);
        }
        if (!mafiaRoomsModel.isGameStarted()) {
            nightDayIV.setVisibility(View.INVISIBLE);
            timeLeftTV.setVisibility(View.INVISIBLE);
        } else {
            if (!sharedHelper.hasSeenRole() && isParticipant()) {
                openRoleView(sharedHelper.getRoleResourceId(), sharedHelper.getRoleName());
            }
        }

    }

    private void initToggleIcon() {
        if (mafiaRoomsModel.isGameStarted() && mafiaRoomsModel.getCurrentDayType().equals(DAY_TYPE_NIGHT) && isParticipant() && isMafia()) {
            toggleMafiaChatMode.setVisibility(View.VISIBLE);
            toggleMafiaChatMode.setImageResource(R.drawable.citizen_icon);
            Snackbar.make(mafiaChatRecycler, getString(R.string.talkingGlobal), Snackbar.LENGTH_LONG).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            }).show();
        }
    }

    private void toggleTalkSteam() {
        if (isMafiaToggled) {
            isMafiaToggled = false;
            toggleMafiaChatMode.setImageResource(R.drawable.citizen_icon);
            Snackbar.make(mafiaChatRecycler, getString(R.string.talkingGlobal), Snackbar.LENGTH_LONG).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            }).show();
        } else {
            isMafiaToggled = true;
            toggleMafiaChatMode.setImageResource(R.drawable.mafia_icon);
            Snackbar.make(mafiaChatRecycler, getString(R.string.talkingOnlyMafia), Snackbar.LENGTH_LONG).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            }).show();
        }
    }

    private void initDayTypeAndTime(final String messageToInsert, final boolean isSystemMessage, final boolean checkGameEnding) {
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
                if (checkGameEnding) {
                    if (mafiaRoomsModel.isGameEnded()) {
                        buildGameEnding();
                        return;
                    }
                }
                initToggleIcon();
                if (mafiaRoomsModel.isGameStarted()) {
                    boolean isMorning = true;
                    boolean proceed = true;

                    nightDayIV.setVisibility(View.VISIBLE);
                    switch (mafiaRoomsModel.getCurrentDayType()) {
                        case DAY_TYPE_DAYLIGHT:
                            isMorning = true;
                            lastVotedUserId = null;
                            nightDayIV.setImageResource(R.drawable.sun_icon);

                            if (mafiaRoomsModel.isGameEnded()) {
                                buildGameEnding();
                                proceed = false;
                            }

                            transparentPanel.setDay();
                            mafiaRoomsModel.setCurrentDayType(DAY_TYPE_DAYLIGHT);
                            getMafiaRoomParticipants();
                            break;
                        case DAY_TYPE_NIGHT:
                            isMorning = false;
                            transparentPanel.setNight();
                            nightDayIV.setImageResource(R.drawable.moon_icon);
                            mafiaRoomsModel.setCurrentDayType(DAY_TYPE_NIGHT);
                            checkIfSheriff();
                            getMafiaRoomParticipants();
                            break;
                    }
                    if (proceed) {
                        nightDayIV.setVisibility(View.VISIBLE);
                        timeLeftTV.setVisibility(View.VISIBLE);
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
                            if (mafiaRoomsModel.getMorningDurationUnit().equals(MafiaConstants.UNIT_MINUTES)) {
                                gameDurationMillis = convertToMillis(UNIT_MINUTE, Long.valueOf(mafiaRoomsModel.getMorningDuration()));
                            } else if (mafiaRoomsModel.getMorningDurationUnit().equals(MafiaConstants.UNIT_HOURS)) {
                                gameDurationMillis = convertToMillis(UNIT_HOUR, Long.valueOf(mafiaRoomsModel.getMorningDuration()));
                            } else if (mafiaRoomsModel.getMorningDurationUnit().equals(MafiaConstants.UNIT_DAYS)) {
                                gameDurationMillis = convertToMillis(UNIT_DAY, Long.valueOf(mafiaRoomsModel.getMorningDuration()));
                            }
                        } else {
                            if (mafiaRoomsModel.getNightDurationUnit().equals(MafiaConstants.UNIT_MINUTES)) {
                                gameDurationMillis = convertToMillis(UNIT_MINUTE, Long.valueOf(mafiaRoomsModel.getNightDuration()));
                            } else if (mafiaRoomsModel.getNightDurationUnit().equals(MafiaConstants.UNIT_HOURS)) {
                                gameDurationMillis = convertToMillis(UNIT_HOUR, Long.valueOf(mafiaRoomsModel.getNightDuration()));
                            } else if (mafiaRoomsModel.getNightDurationUnit().equals(MafiaConstants.UNIT_DAYS)) {
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


                                timeLeftTV.setText(mafiaRoomsModel.getCurrentDayType().equals(DAY_TYPE_DAYLIGHT) ?
                                        getString(R.string.timeLeftForNight, text) :
                                        getString(R.string.timeLeftForMorning, text));
                            }

                            @Override
                            public void onFinish() {
                                nightDayIV.setVisibility(View.INVISIBLE);
                                timeLeftTV.setText(getString(R.string.loadingText));

                                switch (mafiaRoomsModel.getCurrentDayType()) {
                                    case DAY_TYPE_DAYLIGHT:
                                        //insert as night has come
                                        initDayTypeAndTime(NIGHT_COME_SYSTEM_MESSAGE, true, false);
                                        break;
                                    case DAY_TYPE_NIGHT:
                                        //insert as day has come
                                        initDayTypeAndTime(DAY_COME_SYSTEM_MESSAGE, true, false);
                                        break;
                                }
                            }
                        };
                        countDownTimer.start();
                    }
                }


            }

            @Override
            public void onFailure(Call<MafiaRoomsModel> call, Throwable t) {
                if (serverReconnectTry == 0) {
                    initDayTypeAndTime(null, false, false);
                    serverReconnectTry++;
                }
                Snackbar.make(mafiaRoleView, getString(R.string.serverErrorText), BaseTransientBottomBar.LENGTH_LONG).setAction(getString(R.string.vk_retry), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        serverReconnectTry = 0;
                        reorderItems();
                    }
                }).show();
            }
        });
    }

    private void buildGameEnding() {
        nightDayIV.setImageResource(R.drawable.sun_icon);
        toggleMafiaChatMode.setVisibility(View.GONE);
        replyToRoomIV.setEnabled(false);
        replyToRoomIV.setClickable(false);
        String winnerName = getString(R.string.citizenText);
        nightDayIV.setVisibility(View.VISIBLE);
        if (mafiaRoomsModel.getWhoWon().equals(MafiaConstants.ROLE_MAFIA)) {
            winnerName = getString(R.string.mafiaText);
        }
        replyToRoomED.setHint(getString(R.string.gameEndedText, winnerName));
        timeLeftTV.setText(getString(R.string.selfDestructHint));
        initEditText(false, false);
    }

    private void checkIfSheriff() {
        if (isSheriff()) {
            Snackbar.make(mafiaChatRecycler, getString(R.string.sheriffCheckHint), Snackbar.LENGTH_SHORT).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            }).show();
        }
    }

    private void initRecyclersAndService() {
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
        mafiaPlayersAdapter.setOnItemClickListener(this);
        getMafiaRoomParticipants();

        if (isOwner()) {
            sharedHelper.putMafiaParticipation(true);
            startService(new Intent(this, MafiaGameService.class));
        }
    }


    private void getMafiaRoomMessages() {
        Retrofit.getInstance().getInkService().getMafiaChat(mafiaRoomsModel.getId()).enqueue(new Callback<List<MafiaMessageModel>>() {
            @Override
            public void onResponse(Call<List<MafiaMessageModel>> call, Response<List<MafiaMessageModel>> response) {
                List<MafiaMessageModel> mafiaMessageModels = response.body();
                if (mafiaMessageModels.isEmpty()) {
                    showNoMessages();
                } else {
                    mafiaChatAdapter.setMafiaMessageModels(mafiaMessageModels, isMafia());
                    hideNoMessages();
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

    private boolean isMafia() {
        ParticipantModel currentUserParticipant = null;
        for (ParticipantModel participantModel : mafiaRoomsModel.getJoinedUsers()) {
            if (participantModel.getUser().getUserId().equals(sharedHelper.getUserId())) {
                currentUserParticipant = participantModel;
                break;
            }
        }
        if (currentUserParticipant != null && currentUserParticipant.getRole().equals(MafiaConstants.ROLE_MAFIA) ||
                currentUserParticipant != null && currentUserParticipant.getRole().equals(MafiaConstants.ROLE_MAFIA_DON)) {
            return true;
        }
        return false;
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
            long id = jsonObject.optLong("id");
            int roomId = jsonObject.optInt("room_id");
            String message = jsonObject.optString("message");
            String senderId = jsonObject.optString("sender_id");
            String user = jsonObject.optString("user");
            boolean isSystemMessage = jsonObject.optBoolean("isSystemMessage");
            boolean isMafiaMessage = jsonObject.optBoolean("isMafiaMessage");

            UserModel userModel = gson.fromJson(user, UserModel.class);

            final MafiaMessageModel mafiaMessageModel = new MafiaMessageModel();
            mafiaMessageModel.setId(String.valueOf(id));
            mafiaMessageModel.setRoomId(roomId);
            mafiaMessageModel.setMessage(message);
            mafiaMessageModel.setSenderId(senderId);
            mafiaMessageModel.setSystemMessage(isSystemMessage);
            mafiaMessageModel.setMafiaMessage(isMafiaMessage);
            mafiaMessageModel.setUser(userModel);

            if (mafiaMessageModel.getRoomId() == mafiaMessageModel.getRoomId()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mafiaChatAdapter.insertMessage(mafiaMessageModel, isMafia());
                        hideNoMessages();
                        scrollToBottom();
                    }
                });

            }
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

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

        }
    };

    private Emitter.Listener onSocketMessageReceived = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            proceedRunnable();
        }
    };

    private void proceedRunnable() {
        if (lastMethodToRun != null) {
            lastMethodToRun.run();
            lastMethodToRun = null;
        }
    }

    private Emitter.Listener onUserLeftRoom = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject jsonObject = (JSONObject) args[0];
            int roomId = jsonObject.optInt("roomId");
            if (roomId == mafiaRoomsModel.getId()) {

                String systemMessage = jsonObject.optString("systemMessage");
                long newOwnerId = jsonObject.optLong("newOwnerId");
                final MafiaMessageModel mafiaMessageModel = new MafiaMessageModel();
                boolean isNewOwnerChosen = jsonObject.optBoolean("isNewOwnerChosen");

                if (isNewOwnerChosen) {
                    mafiaRoomsModel.setCreatorId(String.valueOf(newOwnerId));
                    if (isOwner()) {
                        sharedHelper.putMafiaParticipation(true);
                        startService(new Intent(MafiaGameView.this, MafiaGameService.class));
                    }
                }

                mafiaPlayersAdapter.setOwnerId(mafiaRoomsModel.getCreatorId());
                mafiaMessageModel.setUser(User.get().buildUser(sharedHelper));
                mafiaMessageModel.setSystemMessage(true);
                mafiaMessageModel.setSenderId("0");
                mafiaMessageModel.setId(String.valueOf(System.currentTimeMillis()));
                mafiaMessageModel.setRoomId(MafiaGameView.this.mafiaRoomsModel.getId());
                mafiaMessageModel.setMessage(systemMessage);

                final ParticipantModel participantModel = gson.fromJson(jsonObject.optString("participantModel"), ParticipantModel.class);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        LocalBroadcastManager.getInstance(MafiaGameView.this).sendBroadcast(new Intent(getPackageName() + "update"));
                        mafiaPlayersAdapter.removeUser(participantModel);
                        mafiaChatAdapter.insertMessage(mafiaMessageModel, isMafia());
                        hideNoMessages();
                        scrollToBottom();
                        getMafiaRoomParticipants();
                    }
                });
            }
        }
    };

    private Emitter.Listener onUserJoinedRoom = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject jsonObject = (JSONObject) args[0];
            int roomId = jsonObject.optInt("roomId");
            final String systemMessage = jsonObject.optString("systemMessage");
            if (roomId == mafiaRoomsModel.getId()) {
                final ParticipantModel participantModel = gson.fromJson(jsonObject.optString("participantModel"), ParticipantModel.class);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MafiaMessageModel mafiaMessageModel = new MafiaMessageModel();
                        mafiaMessageModel.setMessage(systemMessage);
                        mafiaMessageModel.setSystemMessage(true);
                        mafiaMessageModel.setId(String.valueOf(System.currentTimeMillis()));
                        mafiaMessageModel.setRoomId(mafiaRoomsModel.getId());
                        mafiaMessageModel.setSenderId("0");
                        mafiaMessageModel.setUser(User.get().buildUser(sharedHelper));
                        mafiaPlayersAdapter.addUser(participantModel);
                        mafiaChatAdapter.insertMessage(mafiaMessageModel, isMafia());
                        hideNoMessages();
                        scrollToBottom();
                    }
                });
            }
        }
    };

    private Emitter.Listener onGameStarted = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject jsonObject = (JSONObject) args[0];
                    long id = jsonObject.optLong("roomId");
                    if (id == mafiaRoomsModel.getId()) {
                        String participantWithoutUser = jsonObject.optString("participants");
                        List<ParticipantModelWithoutUser> participantModelWithoutUsers = gson.fromJson(participantWithoutUser, new TypeToken<List<ParticipantModelWithoutUser>>() {
                        }.getType());

                        String role = "none";

                        for (ParticipantModelWithoutUser participantModelWithoutUser : participantModelWithoutUsers) {
                            String userId = participantModelWithoutUser.getParticipantId();
                            if (userId.equals(sharedHelper.getUserId())) {
                                role = participantModelWithoutUser.getRole();
                                break;
                            }
                        }

                        buildRole(role);

                        mafiaChatAdapter.clear();
                        mafiaRoomsModel.setGameStarted(true);
                        getMafiaRoomParticipants();
                        initGameInfo();
                        initDayTypeAndTime(null, false, false);
                        LocalBroadcastManager.getInstance(MafiaGameView.this).sendBroadcast(new Intent(getPackageName() + "update"));
                    }

                }
            });
        }
    };


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        this.menu = menu;

        if (!isMenuAdded) {
            menu.clear();
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
        if (mafiaRoomsModel.getGameType().equals(MafiaConstants.GAME_TYPE_YAKUDZA)) {
            int currentPlayers = mafiaRoomsModel.getJoinedUsers().size();
            if (currentPlayers < minimumYakudzaPlayers) {
                int playersNeeded = minimumYakudzaPlayers - currentPlayers;
                DialogUtils.showDialog(this, getString(R.string.cantStart), getString(R.string.needMorePlayersText, playersNeeded), true, null, false, null);
                getMafiaRoomParticipants();
            } else {
                callStartGame();
            }
        } else if (mafiaRoomsModel.getGameType().equals(MafiaConstants.GAME_TYPE_CLASSIC)) {
            int currentPlayers = mafiaRoomsModel.getJoinedUsers().size();
            if (currentPlayers < minimumClassicPlayers) {
                int playersNeeded = minimumClassicPlayers - currentPlayers;
                DialogUtils.showDialog(this, getString(R.string.cantStart), getString(R.string.needMorePlayersText, playersNeeded), true, null, false, null);
                getMafiaRoomParticipants();
            } else {
                callStartGame();
            }
        }
    }

    private void callStartGame() {
        sharedHelper.putRoleSeen(false);
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
                    String participantWithoutUser = jsonObject.optString("participants");
                    if (success) {
                        LocalBroadcastManager.getInstance(MafiaGameView.this).sendBroadcast(new Intent(getPackageName() + "update"));
                        List<ParticipantModelWithoutUser> participantModelWithoutUsers = gson.fromJson(participantWithoutUser, new TypeToken<List<ParticipantModelWithoutUser>>() {
                        }.getType());
                        String role = "none";

                        for (ParticipantModelWithoutUser participantModelWithoutUser : participantModelWithoutUsers) {
                            String userId = participantModelWithoutUser.getParticipantId();
                            if (userId.equals(sharedHelper.getUserId())) {
                                role = participantModelWithoutUser.getRole();
                                break;
                            }
                        }

                        buildRole(role);

                        menu.removeItem(ITEM_START_GAME);
                        menu.removeItem(ITEM_DELETE_ID);
                        initDayTypeAndTime(null, false, false);
                        silentMessageServerInsert(GAME_STARTED_SYSTEM_MESSAGE, true);
                        silentMessageServerInsert(MAFIA_PICKING_MESSAGE, true);

                        if (socketJson != null) {
                            socketJson = null;
                        }
                        socketJson = new JSONObject();
                        String participantsJson = gson.toJson(participantModelWithoutUsers);

                        socketJson.put("roomId", mafiaRoomsModel.getId());
                        socketJson.put("message", GAME_STARTED_SYSTEM_MESSAGE);
                        socketJson.put("participants", participantsJson);
                        socket.emit(EVENT_ON_MAFIA_GAME_STARTED, socketJson);
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

    private void buildRole(String role) {
        if (!sharedHelper.hasSeenRole()) {
            switch (role) {
                case MafiaConstants.ROLE_CITIZEN:
                    openRoleView(R.drawable.mafia_role_citizen, getString(R.string.citizenText));
                    sharedHelper.putLastRole(R.drawable.mafia_role_citizen, getString(R.string.citizenText));
                    break;
                case MafiaConstants.ROLE_DOCTOR:
                    openRoleView(R.drawable.mafia_doctor_role, getString(R.string.doctorText));
                    sharedHelper.putLastRole(R.drawable.mafia_doctor_role, getString(R.string.doctorText));
                    break;
                case MafiaConstants.ROLE_MAFIA:
                    openRoleView(R.drawable.mafia_mafia_role, getString(R.string.mafiaText));
                    sharedHelper.putLastRole(R.drawable.mafia_mafia_role, getString(R.string.mafiaText));
                    break;
                case MafiaConstants.ROLE_MAFIA_DON:
                    openRoleView(R.drawable.mafia_done_role, getString(R.string.donText));
                    sharedHelper.putLastRole(R.drawable.mafia_done_role, getString(R.string.donText));
                    break;
                case MafiaConstants.ROLE_MANIAC:
                    openRoleView(R.drawable.mafia_maniac_role, getString(R.string.maniacText));
                    sharedHelper.putLastRole(R.drawable.mafia_maniac_role, getString(R.string.maniacText));
                    break;
                case MafiaConstants.ROLE_SHERIFF:
                    openRoleView(R.drawable.mafia_sheriff_role, getString(R.string.sheriffText));
                    sharedHelper.putLastRole(R.drawable.mafia_sheriff_role, getString(R.string.sheriffText));
                    break;
                case MafiaConstants.ROLE_SNIPER:
                    openRoleView(R.drawable.mafia_sniper_role, getString(R.string.sniperText));
                    sharedHelper.putLastRole(R.drawable.mafia_sniper_role, getString(R.string.sniperText));
                    break;
            }
        }
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
                        sharedHelper.putRoleSeen(false);
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
        socket.on(EVENT_DISCONNECT, onDisconnect);
        socket.on(EVENT_CONNECT_ERROR, onConnectionError);
        socket.on(EVENT_CONNECT_TIMEOUT, onConnectionTimeOut);
        socket.on(EVENT_CONNECT, onSocketConnected);
        socket.on(EVENT_ON_USER_LEFT_MAFIA_ROOM, onUserLeftRoom);
        socket.on(EVENT_ON_USER_JOINED_MAFIA_ROOM, onUserJoinedRoom);
        socket.on(EVENT_ON_MAFIA_GAME_STARTED, onGameStarted);
        socket.on(EVENT_ON_SOCKET_MESSAGE_RECEIVED, onSocketMessageReceived);
        socket.connect();
    }

    private void getMafiaRoomParticipants() {
        Retrofit.getInstance().getInkService().getMafiaRoomParticipants(mafiaRoomsModel.getId()).enqueue(new Callback<List<ParticipantModel>>() {
            @Override
            public void onResponse(Call<List<ParticipantModel>> call, Response<List<ParticipantModel>> response) {
                List<ParticipantModel> participants = response.body();
                mafiaRoomsModel.setJoinedUsers(participants);
                if (participants.isEmpty()) {
                    showNoParticipants();
                } else {
                    hideNoParticipants();
                    mafiaPlayersAdapter.setUsers(participants);
                    if (mafiaRoomsModel.isGameEnded()) {
                        getMafiaRoomMessages();
                        return;
                    }
                    if (getCurrentParticipantModel().isEliminated()) {
                        initEditText(!getCurrentParticipantModel().isEliminated(), true);
                    } else {
                        initEditText(isParticipant(), true);
                    }
                    if (getCurrentParticipantModel().isEliminated()) {
                        buildEliminatedView();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<ParticipantModel>> call, Throwable t) {
                Toast.makeText(MafiaGameView.this, getString(R.string.serverErrorText), Toast.LENGTH_SHORT).show();
                hideNoParticipants();
                getMafiaRoomMessages();
            }
        });
    }

    private void buildEliminatedView() {
        replyToRoomED.setHint(getString(R.string.eliminatedHint));
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
                    String systemMessage = jsonObject.optString("systemMessage");
                    if (success) {
                        sharedHelper.putRoleSeen(false);
                        if (socketJson != null) {
                            socketJson = null;
                        }
                        sharedHelper.putMafiaLastRoomId(mafiaRoomsModel.getId());
                        startService(new Intent(MafiaGameView.this, MafiaGameService.class));
                        Toast.makeText(MafiaGameView.this, getString(R.string.joined), Toast.LENGTH_SHORT).show();
                        List<ParticipantModel> joinedUsers = mafiaRoomsModel.getJoinedUsers();

                        ParticipantModel participantModel = new ParticipantModel();
                        participantModel.setEliminated(false);
                        participantModel.setRole("");
                        participantModel.setUser(User.get().buildUser(sharedHelper));
                        joinedUsers.add(participantModel);

                        socketJson = new JSONObject();
                        String participantJson = gson.toJson(participantModel);

                        socketJson.put("roomId", mafiaRoomsModel.getId());
                        socketJson.put("systemMessage", systemMessage);
                        socketJson.put("participantModel", participantJson);

                        socket.emit(EVENT_ON_USER_JOINED_MAFIA_ROOM, socketJson);

                        mafiaRoomsModel.setJoinedUsers(joinedUsers);
                        reorderItems();

                    } else {
                        String cause = jsonObject.optString("cause");
                        if (cause.equals(GAME_ALREADY_IN_PROGRESS)) {
                            Toast.makeText(MafiaGameView.this, getString(R.string.cantJoinGameInProgress), Toast.LENGTH_LONG).show();
                            mafiaRoomsModel.setGameStarted(true);
                            reorderItems();

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

    private void reorderItems() {
        LocalBroadcastManager.getInstance(MafiaGameView.this).sendBroadcast(new Intent(getPackageName() + "update"));
        isMenuAdded = false;
        initEditText(isParticipant(), true);
        initGameInfo();
        initRecyclersAndService();
        initDayTypeAndTime(null, false, false);
        if (!mafiaRoomsModel.isGameStarted()) {
            nightDayIV.setVisibility(View.INVISIBLE);
            timeLeftTV.setVisibility(View.INVISIBLE);
        } else {
            if (!sharedHelper.hasSeenRole() && isParticipant()) {
                openRoleView(sharedHelper.getRoleResourceId(), sharedHelper.getRoleName());
            }
        }
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

                    String systemMessage = jsonObject.optString("systemMessage");
                    long newOwnerId = jsonObject.optLong("newOwnerId");
                    boolean isNewOwnerChosen = jsonObject.optBoolean("isNewOwnerChosen");

                    if (success) {
                        if (socketJson != null) {
                            socketJson = null;
                        }
                        sharedHelper.putRoleSeen(false);
                        ParticipantModel participantModel = new ParticipantModel();
                        participantModel.setUser(User.get().buildUser(sharedHelper));
                        participantModel.setEliminated(true);
                        participantModel.setRole("none");

                        String userJson = gson.toJson(participantModel);

                        socketJson = new JSONObject();
                        socketJson.put("roomId", mafiaRoomsModel.getId());
                        socketJson.put("systemMessage", systemMessage);
                        socketJson.put("newOwnerId", newOwnerId);
                        socketJson.put("isNewOwnerChosen", isNewOwnerChosen);
                        socketJson.put("participantModel", userJson);


                        sharedHelper.putMafiaParticipation(false);
                        stopService(new Intent(MafiaGameView.this, MafiaGameService.class));

                        lastMethodToRun = new Runnable() {
                            @Override
                            public void run() {
                                finish();
                                LocalBroadcastManager.getInstance(MafiaGameView.this).sendBroadcast(new Intent(getPackageName() + "update"));
                            }
                        };

                        socket.emit(EVENT_ON_USER_LEFT_MAFIA_ROOM, socketJson);
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

    @OnClick(R.id.choseVictimButton)
    public void choseVictimButtonClicked() {
        choseVictimButton.setVisibility(View.GONE);
        chooseVictim();
    }

    private void chooseVictim() {

    }

    @OnClick(R.id.toggleMafiaChatMode)
    public void toggleMafiaChatModeClicked() {
        toggleTalkSteam();
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
            socketJson.put("isMafiaMessage", isMafiaToggled);
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
        if (isSystemMessage) {
            if (isOwner()) {
                Retrofit.getInstance().getInkService().silentMafiaMessageInsert(mafiaRoomsModel.getId(), message, sharedHelper.getUserId(), isSystemMessage, false).enqueue(new Callback<ResponseBody>() {
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
                            String cause = jsonObject.optString("cause");
                            if (!success) {
                                if (cause.equals(ROOM_DELETED)) {
                                    finish();
                                    LocalBroadcastManager.getInstance(MafiaGameView.this).sendBroadcast(new Intent(getPackageName() + "update"));
                                    Toast.makeText(MafiaGameView.this, getString(R.string.roomDeleted), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MafiaGameView.this, getString(R.string.messageNotSent), Toast.LENGTH_SHORT).show();
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
                mafiaChatAdapter.insertMessage(mafiaMessageModel, isMafia());
                hideNoMessages();
                scrollToBottom();
            }
        } else {
            Retrofit.getInstance().getInkService().silentMafiaMessageInsert(mafiaRoomsModel.getId(), message, sharedHelper.getUserId(), isSystemMessage, isMafiaToggled).enqueue(new Callback<ResponseBody>() {
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
                        String cause = jsonObject.optString("cause");
                        if (!success) {
                            if (cause.equals(ROOM_DELETED)) {
                                finish();
                                LocalBroadcastManager.getInstance(MafiaGameView.this).sendBroadcast(new Intent(getPackageName() + "update"));
                                Toast.makeText(MafiaGameView.this, getString(R.string.roomDeleted), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MafiaGameView.this, getString(R.string.messageNotSent), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(MafiaGameView.this, getString(R.string.messageNotSent), Toast.LENGTH_SHORT).show();
                }
            });
            MafiaMessageModel mafiaMessageModel = new MafiaMessageModel();
            mafiaMessageModel.setId(String.valueOf(System.currentTimeMillis()));
            mafiaMessageModel.setMessage(message);
            mafiaMessageModel.setRoomId(mafiaRoomsModel.getId());
            mafiaMessageModel.setMafiaMessage(isMafiaToggled);
            mafiaMessageModel.setSenderId(sharedHelper.getUserId());
            mafiaMessageModel.setSystemMessage(isSystemMessage);
            mafiaMessageModel.setUser(User.get().buildUser(sharedHelper));
            mafiaChatAdapter.insertMessage(mafiaMessageModel, isMafia());
            hideNoMessages();
            scrollToBottom();
        }
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

    private void openRoleView(final int roleResourceId, String roleName) {
        Keyboard.hideKeyboard(this);
        mafiaRoleView.setVisibility(View.VISIBLE);
        mafiaRoleExplanationTV.setText(getString(R.string.youAre, roleName));
        slideInWithFade.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                setButtonState(true);
                mafiaRoleHolder.setImageDrawable(ContextCompat.getDrawable(MafiaGameView.this, roleResourceId));
                sharedHelper.putRoleSeen(true);
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

    private void initEditText(boolean enabled, boolean useHint) {
        if (enabled) {
            if (useHint) {
                replyToRoomED.setHint(getString(R.string.replyToRoom));
            }
            replyToRoomED.setEnabled(true);
            replyToRoomED.setClickable(true);
            replyToRoomED.setFocusable(true);
            replyToRoomED.setFocusableInTouchMode(true);
            replyToRoomIV.setEnabled(false);
            replyToRoomIV.setFocusable(false);
            replyToRoomIV.setFocusableInTouchMode(false);
            replyToRoomED.removeTextChangedListener(editTextWatcher);
            replyToRoomED.addTextChangedListener(editTextWatcher);
        } else {
            replyToRoomED.setEnabled(false);
            replyToRoomED.setClickable(false);
            replyToRoomED.setFocusable(false);
            replyToRoomED.setFocusableInTouchMode(false);
            if (useHint) {
                replyToRoomED.setHint(getString(R.string.cantReply));
            }
            replyToRoomIV.setFocusable(true);
            replyToRoomIV.setFocusableInTouchMode(true);
            replyToRoomIV.setEnabled(false);
            replyToRoomED.removeTextChangedListener(editTextWatcher);
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

    private ParticipantModel getCurrentParticipantModel() {
        ParticipantModel participantModel = new ParticipantModel();
        participantModel.setRoomCreatorId(mafiaRoomsModel.getCreatorId());
        participantModel.setRole("none");
        participantModel.setEliminated(false);
        participantModel.setUser(User.get().buildUser(sharedHelper));
        String currentUserId = sharedHelper.getUserId();
        for (ParticipantModel eachParticipant : mafiaRoomsModel.getJoinedUsers()) {
            if (eachParticipant.getUser().getUserId().equals(currentUserId)) {
                participantModel = eachParticipant;
                break;
            }
        }
        return participantModel;
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

    private TextWatcher editTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String text = s.toString().toString();
            if (text.isEmpty() || mafiaRoomsModel.isGameEnded() || !isParticipant()) {
                replyToRoomIV.setEnabled(false);
            } else {
                replyToRoomIV.setEnabled(true);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private void initGameInfo() {
        if (mafiaRoomsModel.isGameStarted()) {
            gameStartedTV.setTextColor(ContextCompat.getColor(this, R.color.darkGreen));
            gameStartedTV.setText(getString(R.string.gameStarted));
        } else {
            gameStartedTV.setTextColor(ContextCompat.getColor(this, R.color.red));
            gameStartedTV.setText(getString(R.string.gameNotStarted));
        }
        String gameType = getString(R.string.classic);
        if (mafiaRoomsModel.getGameType().equals(MafiaConstants.GAME_TYPE_YAKUDZA)) {
            gameType = getString(R.string.yakudza);
        }
        gameTypeTV.setText(getString(R.string.gameTypeText, gameType));

        String unit = getString(R.string.minutesUnit);
        if (mafiaRoomsModel.getMorningDurationUnit().equals(MafiaConstants.UNIT_HOURS)) {
            unit = getString(R.string.hoursUnit);
        } else if (mafiaRoomsModel.getMorningDurationUnit().equals(MafiaConstants.UNIT_DAYS)) {
            unit = getString(R.string.daysUnit);
        }

        singleMorningDurationTV.setText(getString(R.string.oneMorningDuration, mafiaRoomsModel.getMorningDuration(), unit));

        String nightDuration = getString(R.string.minutesUnit);
        if (mafiaRoomsModel.getNightDuration().equals(MafiaConstants.UNIT_HOURS)) {
            nightDuration = getString(R.string.hoursUnit);
        } else if (mafiaRoomsModel.getNightDuration().equals(MafiaConstants.UNIT_DAYS)) {
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
        socket.off(EVENT_DISCONNECT, onDisconnect);
        socket.off(EVENT_CONNECT_ERROR, onConnectionError);
        socket.off(EVENT_ON_SOCKET_MESSAGE_RECEIVED, onSocketMessageReceived);
        socket.off(EVENT_CONNECT_TIMEOUT, onConnectionTimeOut);
        socket.off(EVENT_ON_USER_LEFT_MAFIA_ROOM, onUserLeftRoom);
        socket.off(EVENT_CONNECT, onSocketConnected);
        socket.off(EVENT_ON_USER_JOINED_MAFIA_ROOM, onUserJoinedRoom);
        socket.off(EVENT_ON_MAFIA_GAME_STARTED, onGameStarted);
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
        ParticipantModel currentModel = getCurrentParticipantModel();
        if (mafiaRoomsModel.isGameEnded()) {
            return;
        }
        if (!currentModel.isEliminated()) {
            ParticipantModel participantModel = (ParticipantModel) object;
            if (mafiaRoomsModel.getCurrentDayType().equals(DAY_TYPE_NIGHT)) {
                showNightOptions(participantModel);
            } else {
                if (!mafiaRoomsModel.isFirstNight()) {
                    showDayOptions(participantModel);
                } else {
                    DialogUtils.showDialog(MafiaGameView.this, getString(R.string.information), getString(R.string.firstNightGeneralHint), true, null, false, null);
                }
            }
        }
    }

    private void showDayOptions(final ParticipantModel participantModel) {
        if (sharedHelper.getUserId().equals(participantModel.getUser().getUserId())) {
            DialogUtils.showDialog(MafiaGameView.this, getString(R.string.error), getString(R.string.cantVoteSelf), true, null, false, null);
        } else {
            if (lastVotedUserId != null) {
                if (lastVotedUserId.equals(participantModel.getUser().getUserId())) {
                    DialogUtils.showDialog(this, getString(R.string.caution), getString(R.string.removingHint), true, new DialogUtils.DialogListener() {
                        @Override
                        public void onNegativeClicked() {

                        }

                        @Override
                        public void onDialogDismissed() {

                        }

                        @Override
                        public void onPositiveClicked() {
                            lastVotedUserId = "noId";
                            removeVote(participantModel);
                        }
                    }, true, getString(R.string.cancel));
                } else {
                    DialogUtils.showDialog(this, getString(R.string.error), getString(R.string.cantVote), true, null, false, null);
                }
            } else {
                DialogUtils.showDialog(this, getString(R.string.caution), getString(R.string.voteHint), true, new DialogUtils.DialogListener() {
                    @Override
                    public void onNegativeClicked() {

                    }

                    @Override
                    public void onDialogDismissed() {

                    }

                    @Override
                    public void onPositiveClicked() {
                        lastVotedUserId = participantModel.getUser().getUserId();
                        vote(participantModel);
                    }
                }, true, getString(R.string.cancel));
            }
        }
    }

    private void vote(final ParticipantModel participantModel) {
        showDialog(getString(R.string.loadingText), getString(R.string.voting));
        Retrofit.getInstance().getInkService().voteMafiaPlayer(sharedHelper.getUserId(),
                mafiaRoomsModel.getId(), participantModel.getUser().getUserId()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    removeVote(participantModel);
                    return;
                }
                if (response.body() == null) {
                    removeVote(participantModel);
                    return;
                }
                hideDialog();
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    if (success) {
                        Toast.makeText(MafiaGameView.this, getString(R.string.voted), Toast.LENGTH_SHORT).show();
                        getMafiaRoomParticipants();
                    } else {
                        String cause = jsonObject.optString("cause");
                        if (cause.equals(ErrorCause.ALREADY_VOTED)) {
                            DialogUtils.showDialog(MafiaGameView.this, getString(R.string.error), getString(R.string.cantVote), true, null, false, null);
                        } else {
                            DialogUtils.showDialog(MafiaGameView.this, getString(R.string.information), getString(R.string.serverErrorText), true, null, false, null);
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
                DialogUtils.showDialog(MafiaGameView.this, getString(R.string.error), getString(R.string.serverErrorText), true, null, false, null);
            }
        });
    }

    private void removeVote(final ParticipantModel participantModel) {
        showDialog(getString(R.string.loadingText), getString(R.string.removingVote));
        Retrofit.getInstance().getInkService().removeMafiaPlayerVote(sharedHelper.getUserId(),
                mafiaRoomsModel.getId(), participantModel.getUser().getUserId()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    removeVote(participantModel);
                    return;
                }
                if (response.body() == null) {
                    removeVote(participantModel);
                    return;
                }
                hideDialog();
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    if (success) {
                        Toast.makeText(MafiaGameView.this, getString(R.string.voteRemoved), Toast.LENGTH_SHORT).show();
                        getMafiaRoomParticipants();
                    } else {
                        String cause = jsonObject.optString("cause");
                        if (cause.equals(ErrorCause.NOT_VOTED_TO_REMOVE)) {
                            DialogUtils.showDialog(MafiaGameView.this, getString(R.string.error), getString(R.string.notVotedToDelet), true, null, false, null);
                        } else {
                            DialogUtils.showDialog(MafiaGameView.this, getString(R.string.information), getString(R.string.serverErrorText), true, null, false, null);
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
                DialogUtils.showDialog(MafiaGameView.this, getString(R.string.error), getString(R.string.serverErrorText), true, null, false, null);
            }
        });
    }

    private void showNightOptions(ParticipantModel participantModel) {
        if (isMafia() && mafiaRoomsModel.getCurrentDayType().equals(DAY_TYPE_NIGHT) && !mafiaRoomsModel.isFirstNight()) {
            shoot(participantModel);
        } else {
            if (isMafia()) {
                if (mafiaRoomsModel.isFirstNight()) {
                    DialogUtils.showDialog(MafiaGameView.this, getString(R.string.cantShoot), getString(R.string.firstNightHint), true, null, false, null);
                }
            } else if (isSheriff()) {
                if (mafiaRoomsModel.getCurrentDayType().equals(DAY_TYPE_NIGHT)) {
                    if (sharedHelper.getUserId().equals(participantModel.getUser().getUserId())) {
                        DialogUtils.showDialog(MafiaGameView.this, getString(R.string.error), getString(R.string.checkingSelf), true, null, false, null);
                    } else {
                        checkPlayer(participantModel);
                    }
                } else {
                    DialogUtils.showDialog(MafiaGameView.this, getString(R.string.error), getString(R.string.waitForNightToCheck), true, null, false, null);
                }
            }
        }
    }

    private void checkPlayer(final ParticipantModel participantModel) {
        showDialog(getString(R.string.loadingText), getString(R.string.loadingText));

        Retrofit.getInstance().getInkService().checkMafiaPlayer(mafiaRoomsModel.getId(), sharedHelper.getUserId(), participantModel.getUser().getUserId()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    checkPlayer(participantModel);
                    return;
                }
                if (response.body() == null) {
                    checkPlayer(participantModel);
                    return;
                }
                hideDialog();
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    if (success) {
                        String role = jsonObject.optString("role");
                        switch (role) {
                            case MafiaConstants.ROLE_CITIZEN:
                                DialogUtils.showDialog(MafiaGameView.this, getString(R.string.information), getString(R.string.playerIsText, getString(R.string.citizenText)), true, null, false, null);
                                break;
                            case MafiaConstants.ROLE_MAFIA:
                                DialogUtils.showDialog(MafiaGameView.this, getString(R.string.information), getString(R.string.playerIsText, getString(R.string.mafiaText)), true, null, false, null);
                                break;
                            case MafiaConstants.ROLE_MAFIA_DON:
                                DialogUtils.showDialog(MafiaGameView.this, getString(R.string.information), getString(R.string.playerIsText, getString(R.string.donText)), true, null, false, null);
                                break;
                            default:
                                DialogUtils.showDialog(MafiaGameView.this, getString(R.string.information), getString(R.string.serverErrorText), true, null, false, null);
                        }
                    } else {
                        String cause = jsonObject.optString("cause");
                        switch (cause) {
                            case ErrorCause.NOT_SHERIFF:
                                DialogUtils.showDialog(MafiaGameView.this, getString(R.string.error), getString(R.string.notSheriffError), true, null, false, null);
                                break;
                            case ErrorCause.PLAYER_ALREADY_CHECKED:
                                DialogUtils.showDialog(MafiaGameView.this, getString(R.string.error), getString(R.string.alreadyCheckedError), true, null, false, null);
                                break;
                            default:
                                DialogUtils.showDialog(MafiaGameView.this, getString(R.string.error), getString(R.string.serverErrorText), true, null, false, null);
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
                Snackbar.make(mafiaRoleView, getString(R.string.serverErrorText), BaseTransientBottomBar.LENGTH_LONG).setAction(getString(R.string.vk_retry), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        checkPlayer(participantModel);
                    }
                }).show();
            }
        });
    }

    private boolean isSheriff() {
        ParticipantModel participantModel = getCurrentParticipantModel();
        return participantModel.getRole().equals(MafiaConstants.ROLE_SHERIFF) && !participantModel.isEliminated();
    }


    private void shoot(final ParticipantModel participantModel) {
        showDialog(getString(R.string.shooting), getString(R.string.shootingPlayer));
        Retrofit.getInstance().getInkService().shoot(mafiaRoomsModel.getId(), sharedHelper.getUserId(), participantModel.getUser().getUserId()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    shoot(participantModel);
                    return;
                }
                if (response.body() == null) {
                    shoot(participantModel);
                    return;
                }
                hideDialog();
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    if (success) {
                        DialogUtils.showDialog(MafiaGameView.this, getString(R.string.boomShakkaLakkaText), getString(R.string.shotPlayer), true, null, false, null);
                    } else {
                        String cause = jsonObject.optString("cause");
                        if (cause.equals(ALREADY_SHOT_PLAYER)) {
                            DialogUtils.showDialog(MafiaGameView.this, getString(R.string.error), getString(R.string.alreadyShot), true, null, false, null);
                        } else if (cause.equals(PLAYER_ELIMINATED)) {
                            DialogUtils.showDialog(MafiaGameView.this, getString(R.string.error), getString(R.string.playerEliminatedText), true, null, false, null);
                        } else {
                            DialogUtils.showDialog(MafiaGameView.this, getString(R.string.error), getString(R.string.serverErrorText), true, null, false, null);
                        }
                    }
                } catch (IOException e) {
                    DialogUtils.showDialog(MafiaGameView.this, getString(R.string.error), getString(R.string.serverErrorText), true, null, false, null);
                    e.printStackTrace();
                } catch (JSONException e) {
                    DialogUtils.showDialog(MafiaGameView.this, getString(R.string.error), getString(R.string.serverErrorText), true, null, false, null);
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                DialogUtils.showDialog(MafiaGameView.this, getString(R.string.error), getString(R.string.serverErrorText), true, null, false, null);
                hideDialog();
            }
        });
    }
}
