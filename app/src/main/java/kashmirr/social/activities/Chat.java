package kashmirr.social.activities;

import android.Manifest;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.kashmirr.social.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import in.gauriinfotech.commons.Commons;
import kashmirr.social.adapters.ChatAdapter;
import kashmirr.social.callbacks.GeneralCallback;
import kashmirr.social.interfaces.RecyclerItemClickListener;
import kashmirr.social.interfaces.RequestCallback;
import kashmirr.social.interfaces.SocketListener;
import kashmirr.social.models.ChatModel;
import kashmirr.social.service.SocketService;
import kashmirr.social.utils.ClipManager;
import kashmirr.social.utils.Constants;
import kashmirr.social.utils.DialogUtils;
import kashmirr.social.utils.ImageLoader;
import kashmirr.social.utils.Keyboard;
import kashmirr.social.utils.Notification;
import kashmirr.social.utils.PermissionsChecker;
import kashmirr.social.utils.RealmHelper;
import kashmirr.social.utils.Retrofit;
import kashmirr.social.utils.SharedHelper;
import kashmirr.social.utils.Time;
import lombok.Setter;
import okhttp3.ResponseBody;
import rx.Observer;
import rx.functions.Func1;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static kashmirr.social.activities.SplashScreen.LOCK_SCREEN_REQUEST_CODE;
import static kashmirr.social.service.SocketService.sendMessageNotification;
import static kashmirr.social.utils.Constants.EVENT_ONLINE_STATUS;
import static kashmirr.social.utils.Constants.EVENT_REQUEST_FILE_TRANSFER;
import static kashmirr.social.utils.Constants.EVENT_SEND_MESSAGE;
import static kashmirr.social.utils.Constants.EVENT_STOPPED_TYPING;
import static kashmirr.social.utils.Constants.EVENT_TYPING;
import static kashmirr.social.utils.Constants.NOTIFICATION_MESSAGE_BUNDLE_KEY;
import static kashmirr.social.utils.Constants.REQUEST_CODE_CHOSE_STICKER;
import static kashmirr.social.utils.Constants.STARTING_FOR_RESULT_BUNDLE_KEY;
import static kashmirr.social.utils.NotificationUtils.sendNotification;


public class Chat extends BaseActivity implements RecyclerItemClickListener, SocketListener {

    public static final String TAG = Chat.class.getSimpleName();
    public static final int UPDATE_USER_MESSAGES = 2;
    private static final int USE_SIP_REQUEST_PERMISSION = 5;
    private static final int ATTACHMENT_STORAGE_PERMISSION = 105;
    private static final int FILE_CHOOSER_REQUEST = 100;
    private static final int FILE_DOWNLOAD_STORAGE_REQUEST = 48;
    private static final long TIM_OUT_LENGTH = 15000;

    @BindView(R.id.sendChatMessage)
    fab.FloatingActionButton mSendChatMessage;
    @BindView(R.id.messageBody)
    EditText mWriteEditText;
    @BindView(R.id.toolbarChat)
    Toolbar chatToolbar;
    @BindView(R.id.noMessageLayout)
    View mNoMessageLayout;
    @BindView(R.id.chatRecyclerView)
    RecyclerView mRecyclerView;
    @BindView(R.id.chatTitle)
    TextView chatTitle;
    @BindView(R.id.opponentImage)
    ImageView opponentImage;
    @BindView(R.id.scrollDownChat)
    ImageView scrollDownChat;
    @BindView(R.id.stickerIcon)
    ImageView stickerIcon;
    @BindView(R.id.messageFiledDivider)
    View messageFiledDivider;
    @BindView(R.id.callIcon)
    ImageView callIcon;
    @BindView(R.id.opponentTypingLayout)
    View opponentTypingLayout;
    @BindView(R.id.opponentTypingTV)
    TextView opponentTypingTV;
    @BindView(R.id.stickerPreviewLayout)
    View stickerPreviewLayout;
    @BindView(R.id.stickerPreviewImageView)
    ImageView stickerPreviewImageView;
    @BindView(R.id.loadingMessages)
    public View loadingMessages;
    @BindView(R.id.opponentStatus)
    TextView opponentStatus;
    @BindView(R.id.statusColor)
    ImageView statusColor;
    @BindView(R.id.moreMessagesHint)
    View moreMessagesHint;
    @BindView(R.id.attachmentWrapper)
    View attachmentWrapper;
    @BindView(R.id.attachmentNameTV)
    TextView attachmentNameTV;

    private ChatAdapter chatAdapter;
    private RealmHelper realmHelper;


    private boolean socketConnected;
    private String lastChosenStickerUrl;
    private boolean isStickerChosen;
    private boolean showSuccess;
    private SharedHelper sharedHelper;
    private String currentUserId;
    private Gson chatGSON;
    private String opponentId;
    private Animation fadeAnimation;
    private String opponentFirstName;
    private String opponentLastName;
    private JSONObject messageJson;
    private JSONObject typingJson;
    private boolean isSocialAccount;
    private String opponentImageUrl;
    private SocketService socketService;
    private MediaPlayer sendMessagePlayer;
    private MediaPlayer receiveMessagePlayer;
    private boolean isDataLoaded;
    private ChatModel lastSentChatModel;
    private ScheduledExecutorService scheduler;
    private List<ChatModel> messages;
    private int pagingStart;
    private int pagingEnd = 50;
    private LinearLayoutManager linearLayoutManager;
    private int lastVisiblePosition;
    private MessagePagingTask messagePagingTask;
    private boolean furtherLoad = true;
    private String lastDestination;
    private String filePath;
    private boolean hasPendingDownload;
    private ChatModel downloadChatModel;
    private CountDownTimer timeOutTimer;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);
        sharedHelper = new SharedHelper(this);
        messages = new LinkedList<>();

        fadeAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in_scale);

        chatGSON = new Gson();

        chatAdapter = new ChatAdapter();
        chatAdapter.setOnItemClickListener(this);

        initRecyclerView();

        realmHelper = RealmHelper.getInstance();

        Bundle extras = getIntent().getExtras();

        sendMessagePlayer = MediaPlayer.create(this, R.raw.send_message_pop);
        receiveMessagePlayer = MediaPlayer.create(this, R.raw.receive_message_pop);

        initVariables(extras, false);

        checkNotification(extras, false, null);
        LocalBroadcastManager.getInstance(this).registerReceiver(downloadFinishedReceiver, new IntentFilter(getPackageName() + ".Chat"));
        initUser();

        mSendChatMessage.setEnabled(false);

        currentUserId = sharedHelper.getUserId();


        Notification.get().setSendingRemote(false);
        initWriteField();
        getMessages();
        removeNotificationIfNeeded();
        initColors();

        setSupportActionBar(chatToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        checkLock();

    }

    private void checkLock() {
        if (Notification.get().isCheckLock()) {
            Notification.get().setCheckLock(false);
            if (sharedHelper.hasFingerprintAttached() || sharedHelper.hasPinAttached()) {
                startActivityForResult(new Intent(this, SecurityScreen.class), LOCK_SCREEN_REQUEST_CODE);
            }
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mWriteEditText.getText().toString().length() > 0 || isStickerChosen) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.discardChanges));
            builder.setMessage(getString(R.string.discardChangesQuestion));
            builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            final AlertDialog alertDialog = builder.show();
            alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                }
            });
            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Chat.super.onBackPressed();
                }
            });
        } else {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null && intent.getExtras() != null) {
            checkNotification(intent.getExtras(), true, new GeneralCallback() {
                @Override
                public void onSuccess(Object o) {
                    getMessages();
                }

                @Override
                public void onFailure(Object o) {

                }
            });
        }

    }


    /**
     * Click Handlers
     */

    @OnClick(R.id.moreMessagesHint)
    public void moreMessagesHintClicked() {
        moreMessagesHint.setVisibility(View.GONE);
    }

    @OnClick(R.id.removeAttachmentIV)
    public void removeAttachmentIVClicked() {
        removeAttachment();
    }

    @OnClick(R.id.attachmentVector)
    public void attachmentVectorClicked() {
        if (PermissionsChecker.isStoragePermissionGranted(this)) {
            openFilePicker();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE}, ATTACHMENT_STORAGE_PERMISSION);
        }
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, FILE_CHOOSER_REQUEST);
    }


    @OnClick(R.id.callIcon)
    public void callIconClicked() {
        String destination = opponentFirstName + opponentLastName + Constants.SIP_USERNAME_EXTENSION;
        lastDestination = destination;
        if (!PermissionsChecker.isSipPermissionGranted(this)) {
            DialogUtils.showCustomDialog(this, getString(R.string.sipPermissionNeeded),
                    getString(R.string.proceed), getString(R.string.actionRequired), new DialogUtils.DialogListener() {
                        @Override
                        public void onNegativeClicked() {

                        }

                        @Override
                        public void onDialogDismissed() {

                        }

                        @Override
                        public void onPositiveClicked() {
                            ActivityCompat.requestPermissions(Chat.this, new String[]{Manifest.permission.USE_SIP}, USE_SIP_REQUEST_PERMISSION);
                        }
                    });
        } else {
            callUser(destination);
        }
    }

    private BroadcastReceiver downloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            hasPendingDownload = false;
            String action = intent.getExtras().getString("action");
            if (action.equals("downloadDone")) {
                boolean success = intent.getExtras().getBoolean("success");
                if (success) {
                    showFileDownloadedDialog();
                } else {
                    String cause = intent.getExtras().getString("cause");
                }
            } else if (action.equals("transferStarted")) {
                if (timeOutTimer != null) {
                    timeOutTimer.cancel();
                }
            }
        }
    };

    private void showTimeOutDownloadError() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.downloadFailed));
        builder.setMessage(getString(R.string.downloadFailedReason));
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        try {
            builder.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showFileDownloadedDialog() {
        if (downloadChatModel != null) {
            String filePath = downloadChatModel.getFilePath();
            File file = new File(filePath);
            String fileName = file.getName();


            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.fileDownloaded));
            builder.setMessage(getString(R.string.yourFileDownloadedTo, fileName, "Internal Storage of Device"));
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builder.show();
        }
    }

    private void callUser(String destination) {
        Intent intent = new Intent(this, OutgoingCallActivity.class);
        intent.putExtra("destination", destination);
        intent.putExtra("opponentName", opponentFirstName + " " + opponentLastName);
        intent.putExtra("isSocial", isSocialAccount);
        intent.putExtra("imageUrl", opponentImageUrl);
        startActivity(intent);

    }

    @OnClick(R.id.opponentImage)
    public void opponentImageClicked() {
        openOpponentProfile();
    }

    @OnClick(R.id.sendChatMessage)
    public void sendMessageClicked() {
        if (!socketConnected) {
            Snackbar.make(mRecyclerView, getString(R.string.notConnectedToServer), Snackbar.LENGTH_LONG).setAction(getString(R.string.vk_retry), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showSuccess = true;
                    if (socketService != null) {
                        socketService.connectSocket();
                    } else {
                        Snackbar.make(mRecyclerView, getString(R.string.couldNotConnectToServer), Snackbar.LENGTH_SHORT).show();
                    }
                }
            }).show();
        } else {
            String message = mWriteEditText.getText().toString().replaceAll(":\\)", "\u263A")
                    .replaceAll(":\\(", "\u2639").replaceAll(":D", "\uD83D\uDE00").trim();
            sendMessage(message);
        }
    }


    private void removeAttachment() {
        filePath = null;
        attachmentWrapper.setVisibility(View.GONE);
        String charSequence = mWriteEditText.getText().toString().trim();

        if (charSequence.length() <= 0) {
            mSendChatMessage.setEnabled(false);
        } else {
            mSendChatMessage.setEnabled(true);
        }
    }

    private void addAttachment() {
        mSendChatMessage.setEnabled(true);
        attachmentWrapper.setVisibility(View.VISIBLE);
    }

    private void sendMessage(String message) {
        if (messageJson != null) {
            messageJson = null;
        }
        playSend();
        messageJson = new JSONObject();
        try {
            messageJson.put("messageId", System.currentTimeMillis());
            messageJson.put("userId", currentUserId);
            messageJson.put("opponentId", opponentId);
            messageJson.put("firstName", sharedHelper.getFirstName());
            messageJson.put("lastName", sharedHelper.getLastName());
            messageJson.put("opponentFirstName", opponentFirstName);
            messageJson.put("opponentLastName", opponentLastName);
            messageJson.put("opponentImage", opponentImageUrl);
            messageJson.put("currentUserImage", sharedHelper.getImageLink());
            messageJson.put("isSocialAccount", isSocialAccount);
            messageJson.put("isCurrentUserSocial", sharedHelper.isSocialAccount());
            messageJson.put("message", message);
            messageJson.put("filePath", filePath != null ? filePath : "");
            messageJson.put("date", Time.getCurrentTime());
            messageJson.put("stickerChosen", isStickerChosen);
            messageJson.put("stickerUrl", lastChosenStickerUrl);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mWriteEditText.setText("");
        hideNoMessages();
        final ChatModel chatModel = chatGSON.fromJson(messageJson.toString(), ChatModel.class);
        lastSentChatModel = chatModel;
        chatAdapter.insertChatModel(chatModel);
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                scrollToBottom();
            }
        });
        handleStickerRemoved();
        localMessageInsert(chatModel, true);
        removeAttachment();

    }


    @OnClick(R.id.stickerIcon)
    public void stickerClicked() {
        openStickerChooser();
    }

    @OnClick(R.id.scrollDownChat)
    public void scrollDownChat() {
        mRecyclerView.stopScroll();
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                scrollToBottom();
            }
        });
        hideScroller();
    }

    @OnClick(R.id.stickerPreviewLayout)
    public void stickerLayoutClicked() {
        handleStickerRemoved();
    }

    private void handleStickerRemoved() {
        isStickerChosen = false;
        stickerPreviewLayout.setVisibility(View.GONE);
        String charSequence = mWriteEditText.getText().toString().trim();

        if (charSequence.length() <= 0) {
            mSendChatMessage.setEnabled(false);
        } else {
            mSendChatMessage.setEnabled(true);
        }

    }

    /**
     * Methods
     */

    public void destroyScheduler() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }

    private void scheduleTask() {
        if (scheduler == null || !scheduler.isTerminated() & !scheduler.isShutdown()) {
            scheduler =
                    Executors.newSingleThreadScheduledExecutor();

            scheduler.scheduleAtFixedRate
                    (new Runnable() {
                        public void run() {
                            getOpponentStatus();
                        }
                    }, 0, 15, TimeUnit.SECONDS);
        }
    }

    private void getOpponentStatus() {
        JSONObject onlineStatusJson = new JSONObject();
        try {
            onlineStatusJson.put("opponentId", opponentId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socketService.emit(EVENT_ONLINE_STATUS, onlineStatusJson);
    }

    private void getMessages() {
        realmHelper.getMessagesAsChatModel(opponentId, currentUserId, new GeneralCallback<List<ChatModel>>() {
            @Override
            public void onSuccess(final List<ChatModel> messageModels) {
                messages.clear();
                messages.addAll(messageModels);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (messageModels.isEmpty()) {
                            showNoMessages();
                        } else {
                            hideNoMessages();
                            pagingStart = messages.size() - 1;
                            pagingEnd = pagingStart - 50;
                            doMessagesPaging(pagingStart, pagingEnd, true);
                        }
                        loadingMessages.setVisibility(View.GONE);
                    }
                });

            }

            @Override
            public void onFailure(List<ChatModel> messageModels) {

            }
        });
    }

    private void doMessagesPaging(final int start, int end, boolean firstPaging) {
        if (furtherLoad) {
            if (messagePagingTask != null) {
                messagePagingTask = null;
            }
            messagePagingTask = new MessagePagingTask();

            lastVisiblePosition = chatAdapter.getItemCount();
            messagePagingTask.setFirstPaging(firstPaging);
            messagePagingTask.execute(start, end);
        }
    }


    private void initColors() {
        if (sharedHelper.getChatColor() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor(sharedHelper.getChatColor())));
        } else {
            getWindow().setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.chat_vector_background));
        }

        if (sharedHelper.getChatFieldTextColor() != null) {
            mWriteEditText.setHintTextColor(Color.parseColor(sharedHelper.getChatFieldTextColor()));
            mWriteEditText.setTextColor(Color.parseColor(sharedHelper.getChatFieldTextColor()));
            messageFiledDivider.setBackgroundColor(Color.parseColor(sharedHelper.getChatFieldTextColor()));
        }
        checkForActionBar();
    }

    private void removeNotificationIfNeeded() {
        final NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                notificationManager.cancel(Integer.valueOf(opponentId));
                RealmHelper.getInstance().removeNotificationCount(getApplicationContext(), Integer.valueOf(opponentId));
            }
        });

    }

    private void checkForActionBar() {
        if (sharedHelper.getActionBarColor() != null) {
            chatToolbar.setBackgroundColor(Color.parseColor(sharedHelper.getActionBarColor()));
        }
    }

    private void playSend() {
        if (receiveMessagePlayer.isPlaying()) {
            receiveMessagePlayer.stop();
        }
        sendMessagePlayer.start();
    }

    private void playReceive() {
        if (sendMessagePlayer.isPlaying()) {
            sendMessagePlayer.stop();
        }
        receiveMessagePlayer.start();
    }

    private void localMessageInsert(ChatModel chatModel, final boolean sendMessage) {
        chatModel.setFirstName(opponentFirstName);
        chatModel.setLastName(opponentLastName);
        chatModel.setOpponentFirstName(sharedHelper.getFirstName());
        chatModel.setOpponentLastName(sharedHelper.getLastName());
        realmHelper.insertMessage(chatModel, new GeneralCallback() {
            @Override
            public void onSuccess(Object o) {
                if (sendMessage) {
                    socketService.emit(EVENT_SEND_MESSAGE, messageJson);
                }

            }

            @Override
            public void onFailure(Object o) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Snackbar.make(mRecyclerView, getString(R.string.failedToSent), Snackbar.LENGTH_LONG).setAction(getString(R.string.vk_retry), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                sendMessageClicked();
                            }
                        }).show();
                    }
                });

            }
        });
    }

    private void hideNoMessages() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mNoMessageLayout.getVisibility() == View.VISIBLE) {
                    mNoMessageLayout.setVisibility(View.GONE);
                }
            }
        });

    }

    private void showNoMessages() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mNoMessageLayout.getVisibility() == View.GONE) {
                    mNoMessageLayout.setVisibility(View.VISIBLE);
                }
            }
        });


    }

    private void initVariables(Object object, boolean treatAsModel) {

        if (treatAsModel) {
            ChatModel chatModel = (ChatModel) object;
            opponentId = chatModel.getUserId();
            opponentFirstName = chatModel.getFirstName();
            opponentLastName = chatModel.getLastName();
            opponentImageUrl = chatModel.getCurrentUserImage();
            isSocialAccount = chatModel.isSocialAccount();
        } else {
            Bundle extras = (Bundle) object;
            opponentId = extras != null ? extras.containsKey("opponentId") ? extras.getString("opponentId") : "" : "";
            opponentFirstName = extras != null ? extras.containsKey("firstName") ? extras.getString("firstName") : "" : "";
            opponentLastName = extras != null ? extras.containsKey("lastName") ? extras.getString("lastName") : "" : "";
            opponentImageUrl = extras != null ? extras.containsKey("opponentImage") ? extras.getString("opponentImage") : "" : "";
            isSocialAccount = extras != null ? extras.containsKey("isSocialAccount") ? extras.getBoolean("isSocialAccount") : false : false;
        }

        getOpponentData();
    }

    private void getOpponentData() {
        if (opponentId.isEmpty()) {
            return;
        }
        makeRequest(Retrofit.getInstance().getInkService().getSingleUserDetails(opponentId, ""), null, new RequestCallback() {
            @Override
            public void onRequestSuccess(Object result) {
                try {
                    String responseBody = ((ResponseBody) result).string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    String firstName = jsonObject.optString("first_name");
                    String lastName = jsonObject.optString("last_name");
                    opponentFirstName = firstName;
                    opponentLastName = lastName;
                    opponentImageUrl = jsonObject.optString("image_link");
                    isSocialAccount = jsonObject.optBoolean("isSocialAccount");
                    loadUserImage();
                    initUser();
                    isDataLoaded = true;
                } catch (IOException e) {
                    isDataLoaded = true;
                    e.printStackTrace();
                } catch (JSONException e) {
                    isDataLoaded = true;
                    e.printStackTrace();
                }
                isDataLoaded = true;
            }

            @Override
            public void onRequestFailed(Object[] result) {
                opponentFirstName = "N/A";
                opponentLastName = "N/A";
            }
        });
    }

    private void loadUserImage() {
        if (opponentImageUrl != null && !opponentImageUrl.isEmpty()) {
            if (isSocialAccount) {
                ImageLoader.loadImage(this, true, false, opponentImageUrl, 0, R.drawable.user_image_placeholder, opponentImage, null);

            } else {
                String encodedImage = Uri.encode(opponentImageUrl);
                ImageLoader.loadImage(this, true, false, Constants.MAIN_URL + Constants.USER_IMAGES_FOLDER + encodedImage, 0, R.drawable.user_image_placeholder, opponentImage, null);
            }
        } else {
            ImageLoader.loadImage(this, true, true, null, R.drawable.no_image, R.drawable.user_image_placeholder, opponentImage, null);
        }
    }

    private void checkNotification(Bundle extras, boolean insertMessage, @Nullable final GeneralCallback insertCallback) {
        if (extras != null) {
            if (extras.containsKey(NOTIFICATION_MESSAGE_BUNDLE_KEY)) {
                String receivedMessageJson = extras.getString(NOTIFICATION_MESSAGE_BUNDLE_KEY);
                ChatModel chatModel = chatGSON.fromJson(receivedMessageJson, ChatModel.class);
                initVariables(chatModel, true);
                initUser();
                if (insertMessage) {
                    RealmHelper.getInstance().insertMessage(chatModel, new GeneralCallback() {
                        @Override
                        public void onSuccess(Object o) {
                            if (insertCallback != null) {
                                insertCallback.onSuccess(null);
                            }
                        }

                        @Override
                        public void onFailure(Object o) {
                            if (insertCallback != null) {
                                insertCallback.onFailure(null);
                            }
                        }
                    });
                }
            }

        }
    }

    private void initUser() {
        chatTitle.setText(opponentFirstName + " " + opponentLastName);
        if (opponentImageUrl != null && !opponentImageUrl.isEmpty()) {
            if (isSocialAccount) {
                ImageLoader.loadImage(this, true, false, opponentImageUrl, 0, R.drawable.user_image_placeholder, opponentImage, null);
            } else {
                String encodedImage = Uri.encode(opponentImageUrl);
                ImageLoader.loadImage(this, true, false, Constants.MAIN_URL + Constants.USER_IMAGES_FOLDER + encodedImage, 0, R.drawable.user_image_placeholder, opponentImage, null);
            }
        } else {
            ImageLoader.loadImage(this, true, true, null, R.drawable.no_image, R.drawable.user_image_placeholder, opponentImage, null);
        }
    }

    private void initWriteField() {
        RxTextView.textChanges(mWriteEditText)
                .filter(new Func1<CharSequence, Boolean>() {
                    @Override
                    public Boolean call(CharSequence charSequence) {
                        String textToCheck = charSequence.toString().trim();
                        if (textToCheck.length() <= 0 && !isStickerChosen && filePath == null) {
                            mSendChatMessage.setEnabled(false);
                        } else {
                            mSendChatMessage.setEnabled(true);

                            if (typingJson != null) {
                                typingJson = null;
                            }
                            typingJson = new JSONObject();
                            try {
                                typingJson.put("opponentId", opponentId);
                                typingJson.put("userId", sharedHelper.getUserId());
                                typingJson.put("opponentFirstName", sharedHelper.getFirstName());
                                typingJson.put("opponentLastName", sharedHelper.getLastName());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            socketService.emit(EVENT_TYPING, typingJson);
                        }
                        return true;
                    }
                })
                .debounce(1, TimeUnit.SECONDS)
                .subscribe(new Observer<CharSequence>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(CharSequence charSequence) {
                        if (typingJson != null) {
                            typingJson = null;
                        }
                        typingJson = new JSONObject();
                        try {
                            typingJson.put("opponentId", opponentId);
                            typingJson.put("userId", sharedHelper.getUserId());
                            typingJson.put("opponentFirstName", sharedHelper.getFirstName());
                            typingJson.put("opponentLastName", sharedHelper.getLastName());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        socketService.emit(EVENT_STOPPED_TYPING, typingJson);
                    }
                });

    }

    private void initRecyclerView() {
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(500);
        itemAnimator.setRemoveDuration(500);
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setItemAnimator(itemAnimator);
        mRecyclerView.setAdapter(chatAdapter);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (chatAdapter.getItemCount() >= 50) {
                    if (((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition() == 0) {
                        recyclerView.post(new Runnable() {
                            @Override
                            public void run() {
                                pagingStart = (pagingEnd - 1);
                                pagingEnd = pagingStart - 50;
                                doMessagesPaging(pagingStart, pagingEnd, false);
                            }
                        });
                    }
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    Keyboard.hideKeyboard(Chat.this);
                    if (moreMessagesHint.getVisibility() == View.VISIBLE) {
                        moreMessagesHint.setVisibility(View.GONE);
                    }
                }

                LinearLayoutManager layoutManager = ((LinearLayoutManager) recyclerView.getLayoutManager());
                int firstVisiblePosition = layoutManager.findLastVisibleItemPosition();
                if (chatAdapter.getItemCount() > 5) {
                    if (firstVisiblePosition < chatAdapter.getItemCount() - 4) {
                        if (scrollDownChat.getTag().equals(getString(R.string.notVisible))) {
                            showScroller();
                        }
                    } else {
                        if (scrollDownChat.getTag().equals(getString(R.string.visible))) {
                            hideScroller();
                        }
                    }
                }
            }
        });
    }

    private void hideScroller() {
        scrollDownChat.setTag(getString(R.string.notVisible));
        scrollDownChat.setEnabled(false);
        scrollDownChat.setVisibility(View.GONE);
    }

    private void showScroller() {
        scrollDownChat.setEnabled(true);
        scrollDownChat.setTag(getString(R.string.visible));
        scrollDownChat.setVisibility(View.VISIBLE);
    }


    private void openStickerChooser() {
        Intent intent = new Intent(getApplicationContext(), MyCollection.class);
        intent.putExtra(STARTING_FOR_RESULT_BUNDLE_KEY, true);
        startActivityForResult(intent, Constants.REQUEST_CODE_CHOSE_STICKER);
    }


    private void handleStickerChosenView() {
        mSendChatMessage.setEnabled(true);
        ImageLoader.loadImage(this, false, false, Constants.MAIN_URL + lastChosenStickerUrl, 0, R.drawable.time_loading_vector, stickerPreviewImageView, null);
        stickerPreviewLayout.setVisibility(View.VISIBLE);
    }


    private void scrollToBottom() {
        mRecyclerView.stopScroll();
        mRecyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
    }


    /**
     * Overridden Methods
     */
    @Override
    public void onItemClicked(int position, View view) {

    }

    @Override
    public void onItemLongClick(Object object) {
        final ChatModel chatModel = (ChatModel) object;
        AlertDialog.Builder builder = new AlertDialog.Builder(Chat.this);
        builder.setItems(new String[]{getString(R.string.delete), getString(R.string.copy), getString(R.string.resend)}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        String messageId = chatModel.getMessageId();
                        RealmHelper.getInstance().deleteSingleMessage(messageId, new GeneralCallback() {
                            @Override
                            public void onSuccess(Object o) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        chatAdapter.removeItem(chatModel);
                                        Snackbar.make(mRecyclerView, getString(R.string.messageDeleted), Snackbar.LENGTH_LONG).setAction("OK", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                            }
                                        }).show();
                                    }
                                });
                            }

                            @Override
                            public void onFailure(Object o) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Snackbar.make(mRecyclerView, getString(R.string.messagedeleteError), Snackbar.LENGTH_LONG).setAction("OK", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                            }
                                        }).show();
                                    }
                                });
                            }
                        });
                        break;
                    case 1:
                        ClipManager.copy(Chat.this, chatModel.getMessage());
                        break;
                    case 2:
                        sendMessage(chatModel.getMessage());
                        break;
                }

            }
        });
        builder.show();
    }

    @Override
    public void onAdditionalItemClick(int position, View view) {

    }

    @Override
    public void onAdditionalItemClicked(Object object) {
        if (hasPendingDownload) {
            Toast.makeText(this, getString(R.string.wait_till_download), Toast.LENGTH_SHORT).show();
            return;
        }
        if (PermissionsChecker.isStoragePermissionGranted(this)) {
            ChatModel chatModel = (ChatModel) object;
            downloadChatModel = chatModel;
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("destinationId", opponentId);
                jsonObject.put("requesterId", sharedHelper.getUserId());
                jsonObject.put("filePath", chatModel.getFilePath());
                jsonObject.put("requesterFirstName", sharedHelper.getFirstName());
                jsonObject.put("requesterLastName", sharedHelper.getLastName());
                if (socketService != null) {
                    initTimeOutTimer();
                    Toast.makeText(Chat.this, "See Notification", Toast.LENGTH_SHORT).show();
                    buildWaitNotification();
                    hasPendingDownload = true;
                    socketService.emit(EVENT_REQUEST_FILE_TRANSFER, jsonObject);
                } else {
                    Snackbar.make(mRecyclerView, getString(R.string.notConnectedToServer), Snackbar.LENGTH_LONG).setAction(getString(R.string.vk_retry), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showSuccess = true;
                            if (socketService != null) {
                                socketService.connectSocket();
                            } else {
                                Snackbar.make(mRecyclerView, getString(R.string.couldNotConnectToServer), Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    }).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE}, FILE_DOWNLOAD_STORAGE_REQUEST);
        }

    }

    private void buildWaitNotification() {
        String text = "Waiting for response from the other side to start download";
        mNotifyManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle("Waiting...")
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setSmallIcon(R.mipmap.ic_launcher);
        mBuilder.setProgress(0, 0, true);
        mNotifyManager.notify(0, mBuilder.build());
    }

    private void initTimeOutTimer() {
        timeOutTimer = new CountDownTimer(TIM_OUT_LENGTH, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                sendNotification(0, getApplicationContext(), "Unable to download", "The other side respond timed out");
                showTimeOutDownloadError();
            }
        };
        timeOutTimer.start();
    }

    @Override
    public void onItemClicked(Object object) {

    }


    /**
     * Callbacks
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case REQUEST_CODE_CHOSE_STICKER:
                if (data != null) {
                    lastChosenStickerUrl = data.getExtras().getString(Constants.STICKER_URL_EXTRA_KEY);
                    isStickerChosen = true;
                    handleStickerChosenView();
                }
                break;
            case LOCK_SCREEN_REQUEST_CODE:
                boolean hasUnlocked = data.getExtras() != null ? data.getExtras().getBoolean("hasUnlocked") : false;
                if (hasUnlocked) {

                }
                break;
            default:
        }
        switch (requestCode) {
            case FILE_CHOOSER_REQUEST:
                if (data != null) {
                    Uri uri = data.getData();
                    filePath = Commons.getPath(uri, this);
                    File file = new File(filePath);
                    if (file.exists()) {
                        attachmentNameTV.setText(file.getName());
                        addAttachment();
                    } else {
                        removeAttachment();
                        Toast.makeText(socketService, "Error while picking the file...", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onServiceConnected(SocketService socketService) {
        super.onServiceConnected(socketService);
        this.socketService = socketService;
        if (socketService != null) {
            socketConnected = socketService.isSocketConnected();
        }
        socketService.setOnSocketListener(this, Integer.valueOf(sharedHelper.getUserId()));
        getOpponentStatus();
        scheduleTask();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Notification.get().setSendingRemote(true);
//        if (timeOutTimer != null) {
//            try {
//                timeOutTimer.cancel();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
        if (socketService != null) {
            socketService.destroyListener();
        }
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(downloadFinishedReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        unbindService();
        destroyScheduler();
    }


    @Override
    public void onSocketConnected() {
        socketConnected = true;
        if (showSuccess) {
            showSuccess = false;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Snackbar.make(mRecyclerView, getString(R.string.connected), Snackbar.LENGTH_SHORT).show();
                }
            });

        }
    }

    @Override
    public void onSocketDisconnected() {
        socketConnected = false;
    }

    @Override
    public void onSocketConnectionError() {
        socketConnected = false;
    }

    @Override
    public void onUserStoppedTyping(final JSONObject jsonObject) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String opponentId = jsonObject.optString("userId");
                if (opponentId.equals(Chat.this.opponentId)) {
                    opponentTypingLayout.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onUserTyping(final JSONObject jsonObject) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String opponentFirstName = jsonObject.optString("opponentFirstName");
                String opponentId = jsonObject.optString("userId");
                if (opponentId.equals(Chat.this.opponentId)) {
                    opponentTypingTV.setText(getString(R.string.opponentTyping, opponentFirstName));
                    opponentTypingLayout.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onNewMessageReceived(final JSONObject messageJson) {
        hideNoMessages();
        final ChatModel chatModel = chatGSON.fromJson(messageJson.toString(), ChatModel.class);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (chatModel.getUserId().equals(opponentId)) {
                    chatModel.setHasRead(true);
                    chatAdapter.insertChatModel(chatModel);
                    localMessageInsert(chatModel, false);
                    playReceive();
                    mRecyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            scrollToBottom();
                        }
                    });
                } else {
                    sendMessageNotification(getApplicationContext(), messageJson);
                }

            }
        });
    }

    @Override
    public void onMessageSent(final JSONObject chatJson) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (chatAdapter != null) {
                    chatAdapter.setAllDelivered();
                }
            }
        });
    }

    @Override
    public void onOnlineStatusReceived(final boolean isOnline, final String friendlyMessage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isOnline) {
                    statusColor.setVisibility(View.VISIBLE);
                    statusColor.startAnimation(fadeAnimation);
                    opponentStatus.setText(getString(R.string.onlineStatus));
                } else {
                    statusColor.setVisibility(View.GONE);
                    statusColor.clearAnimation();
                    opponentStatus.setText(friendlyMessage);
                }
            }
        });

    }

    @Override
    public void onFileNotExists() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                DialogUtils.showDialog(Chat.this, "File doesn't exist", "The requested file doesn't exist on the other side", true, null, false, null);
            }
        });
    }


    @Override
    public void onBackPressed() {
        setResult(UPDATE_USER_MESSAGES);
        if (mWriteEditText.getText().toString().length() > 0 || isStickerChosen) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.discardChanges));
            builder.setMessage(getString(R.string.discardChangesQuestion));
            builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            final AlertDialog alertDialog = builder.show();
            alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                }
            });
            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Chat.super.onBackPressed();
                }
            });
        } else {
            super.onBackPressed();
        }
    }


    private void openOpponentProfile() {
        if (!isDataLoaded) {
            Snackbar.make(mRecyclerView, getString(R.string.waitTillLoad), Snackbar.LENGTH_LONG).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            }).show();
        } else {
            Intent intent = new Intent(getApplicationContext(), OpponentProfile.class);
            intent.putExtra("id", opponentId);
            intent.putExtra("firstName", opponentFirstName);
            intent.putExtra("lastName", opponentLastName);
            intent.putExtra("isFriend", true);
            intent.putExtra("disableButton", true);
            startActivity(intent);
        }
    }

    private class MessagePagingTask extends AsyncTask<Integer, String, List<ChatModel>> {
        @Setter
        private boolean firstPaging;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!firstPaging && chatAdapter.getHeaderView() != null && chatAdapter.getHeaderView().getVisibility() != View.VISIBLE) {
                chatAdapter.getHeaderView().setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected List<ChatModel> doInBackground(Integer... params) {
            List<ChatModel> chatModels = new LinkedList<>();
            for (int i = params[0]; i >= params[1]; i--) {
                try {
                    ChatModel chatModel = messages.get(i);
                    chatModels.add(chatModel);
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                    break;
                }
            }
            return chatModels;
        }

        @Override
        protected void onPostExecute(List<ChatModel> chatModels) {
            super.onPostExecute(chatModels);

            if (!chatModels.isEmpty()) {
                furtherLoad = true;
                Collections.reverse(chatModels);
                chatAdapter.insertChatModelWithItemNotify(chatModels, firstPaging);
                if (!firstPaging) {
                    moreMessagesHint.setVisibility(View.VISIBLE);
                }
            } else {
                furtherLoad = false;
            }
            chatModels = null;
            if (chatAdapter.getHeaderView() != null && chatAdapter.getHeaderView().getVisibility() == View.VISIBLE) {
                chatAdapter.getHeaderView().setVisibility(View.GONE);
            }
            if (firstPaging) {
                scrollToBottom();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case USE_SIP_REQUEST_PERMISSION:
                if (PermissionsChecker.isSipPermissionGranted(this)) {
                    callUser(lastDestination);
                }
                break;
            case ATTACHMENT_STORAGE_PERMISSION:
                if (PermissionsChecker.isStoragePermissionGranted(this)) {
                    openFilePicker();
                }
                break;
            case FILE_DOWNLOAD_STORAGE_REQUEST:
                if (PermissionsChecker.isStoragePermissionGranted(this)) {
                    Toast.makeText(socketService, "Now you can click again to download", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
