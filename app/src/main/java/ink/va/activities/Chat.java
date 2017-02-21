package ink.va.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.ink.va.R;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.koushikdutta.ion.Ion;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.va.adapters.ChatAdapter;
import ink.va.callbacks.GeneralCallback;
import ink.va.interfaces.RecyclerItemClickListener;
import ink.va.interfaces.SocketListener;
import ink.va.models.ChatModel;
import ink.va.service.MessageService;
import ink.va.utils.CircleTransform;
import ink.va.utils.Constants;
import ink.va.utils.Keyboard;
import ink.va.utils.Notification;
import ink.va.utils.RealmHelper;
import ink.va.utils.Retrofit;
import ink.va.utils.SharedHelper;
import ink.va.utils.Time;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Observer;
import rx.functions.Func1;

import static ink.va.service.MessageService.sendGeneralNotification;
import static ink.va.utils.Constants.EVENT_SEND_MESSAGE;
import static ink.va.utils.Constants.EVENT_STOPPED_TYPING;
import static ink.va.utils.Constants.EVENT_TYPING;
import static ink.va.utils.Constants.NOTIFICATION_MESSAGE_BUNDLE_KEY;
import static ink.va.utils.Constants.REQUEST_CODE_CHOSE_STICKER;
import static ink.va.utils.Constants.STARTING_FOR_RESULT_BUNDLE_KEY;


public class Chat extends BaseActivity implements RecyclerItemClickListener, SocketListener {

    public static final String TAG = Chat.class.getSimpleName();
    public static final int UPDATE_USER_MESSAGES = 2;

    @BindView(R.id.sendChatMessage)
    fab.FloatingActionButton mSendChatMessage;
    @BindView(R.id.messageBody)
    EditText mWriteEditText;
    @BindView(R.id.noMessageLayout)
    View mNoMessageLayout;
    @BindView(R.id.chatRecyclerView)
    RecyclerView mRecyclerView;
    @BindView(R.id.chatTitle)
    TextView chatTitle;
    @BindView(R.id.opponentImage)
    ImageView opponentImage;
    @BindView(R.id.opponentStatus)
    TextView opponentStatus;
    @BindView(R.id.statusColor)
    ImageView statusColor;
    @BindView(R.id.scrollDownChat)
    ImageView scrollDownChat;
    @BindView(R.id.stickerIcon)
    ImageView attachmentIcon;
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

    private ChatAdapter chatAdapter;
    private RealmHelper realmHelper;


    private boolean socketConnected;
    private String lastChosenStickerUrl;
    private boolean isStickerChosen;
    private Animation slideIn;
    private Animation slideOut;
    private boolean showSuccess;
    private SharedHelper sharedHelper;
    private String currentUserId;
    private Gson chatGSON;
    private String opponentId;
    private String opponentFirstName;
    private String opponentLastName;
    private JSONObject messageJson;
    private JSONObject typingJson;
    private boolean isSocialAccount;
    private String opponentImageUrl;
    private MessageService messageService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);
        sharedHelper = new SharedHelper(this);

        chatGSON = new Gson();

        chatAdapter = new ChatAdapter();
        chatAdapter.setOnItemClickListener(this);

        initRecyclerView();

        realmHelper = RealmHelper.getInstance();

        Bundle extras = getIntent().getExtras();

        initVariables(extras, false);

        checkNotification(extras, false, null);


        initUser();

        mSendChatMessage.setEnabled(false);

        currentUserId = sharedHelper.getUserId();


        Notification.get().setSendingRemote(false);

        slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_and_rotate_in);
        slideOut = AnimationUtils.loadAnimation(this, R.anim.slide_and_rotate_out);

        getWindow().setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.chat_vector_background));
        initWriteField();
        getMessages();
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
            initUser();
        }

    }

    private void getMessages() {
        realmHelper.getMessagesAsChatModel(opponentId, currentUserId, new GeneralCallback<List<ChatModel>>() {
            @Override
            public void onSuccess(final List<ChatModel> messageModels) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (messageModels.isEmpty()) {
                            showNoMessages();
                        } else {
                            hideNoMessages();
                            chatAdapter.setChatModelList(messageModels);
                            mRecyclerView.post(new Runnable() {
                                @Override
                                public void run() {
                                    scrollToBottom();
                                }
                            });
                        }
                    }
                });

            }

            @Override
            public void onFailure(List<ChatModel> messageModels) {

            }
        });
    }


    /**
     * Click Handlers
     */
    @OnClick(R.id.sendChatMessage)
    public void sendMessageClicked() {
        if (!socketConnected) {
            Snackbar.make(mRecyclerView, getString(R.string.notConnectedToServer), Snackbar.LENGTH_LONG).setAction(getString(R.string.vk_retry), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showSuccess = true;
                    if (messageService != null) {
                        messageService.connectSocket();
                    } else {
                        Snackbar.make(mRecyclerView, getString(R.string.couldNotConnectToServer), Snackbar.LENGTH_SHORT).show();
                    }
                }
            }).show();
        } else {
            if (messageJson != null) {
                messageJson = null;
            }
            messageJson = new JSONObject();
            String message = mWriteEditText.getText().toString().trim();
            try {
                messageJson.put("messageId", System.currentTimeMillis());
                messageJson.put("userId", currentUserId);
                messageJson.put("opponentId", opponentId);
                messageJson.put("firstName", opponentFirstName);
                messageJson.put("opponentImage", opponentImageUrl);
                messageJson.put("lastName", opponentLastName);
                messageJson.put("isSocialAccount", isSocialAccount);
                messageJson.put("message", message);
                messageJson.put("date", Time.getCurrentTime());
                messageJson.put("stickerChosen", isStickerChosen);
                messageJson.put("stickerUrl", lastChosenStickerUrl);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            mWriteEditText.setText("");
            hideNoMessages();
            final ChatModel chatModel = chatGSON.fromJson(messageJson.toString(), ChatModel.class);
            chatAdapter.insertChatModel(chatModel);
            mRecyclerView.post(new Runnable() {
                @Override
                public void run() {
                    scrollToBottom();
                }
            });
            handleStickerRemoved();
            localMessageInsert(chatModel, true);
        }
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
    private void localMessageInsert(ChatModel chatModel, final boolean sendMessage) {
        realmHelper.insertMessage(chatModel, new GeneralCallback() {
            @Override
            public void onSuccess(Object o) {
                if (sendMessage) {
                    messageService.emit(EVENT_SEND_MESSAGE, messageJson);
                }

            }

            @Override
            public void onFailure(Object o) {
                Snackbar.make(mRecyclerView, getString(R.string.failedToSent), Snackbar.LENGTH_LONG).setAction(getString(R.string.vk_retry), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendMessageClicked();
                    }
                }).show();
            }
        });
    }

    private void hideNoMessages() {
        if (mNoMessageLayout.getVisibility() == View.VISIBLE) {
            mNoMessageLayout.setVisibility(View.GONE);
        }
    }

    private void showNoMessages() {
        if (mNoMessageLayout.getVisibility() == View.GONE) {
            mNoMessageLayout.setVisibility(View.VISIBLE);
        }

    }

    private void initVariables(Object object, boolean treatAsModel) {

        if (treatAsModel) {
            ChatModel chatModel = (ChatModel) object;
            opponentId = chatModel.getUserId();
            opponentFirstName = chatModel.getFirstName();
            opponentLastName = chatModel.getLastName();
            opponentImageUrl = chatModel.getOpponentImage();
            isSocialAccount = chatModel.isSocialAccount();
        } else {
            Bundle extras = (Bundle) object;
            opponentId = extras != null ? extras.containsKey("opponentId") ? extras.getString("opponentId") : "" : "";
            opponentFirstName = extras != null ? extras.containsKey("firstName") ? extras.getString("firstName") : "" : "";
            opponentLastName = extras != null ? extras.containsKey("lastName") ? extras.getString("lastName") : "" : "";
            opponentImageUrl = extras != null ? extras.containsKey("opponentImage") ? extras.getString("opponentImage") : "" : "";
            isSocialAccount = extras != null ? extras.containsKey("isSocialAccount") ? extras.getBoolean("isSocialAccount") : false : false;
        }

        checkOpponentNames();
    }

    private void checkOpponentNames() {
        if (opponentFirstName == null || opponentFirstName.equals("null") || opponentFirstName.isEmpty()) {
            Retrofit.getInstance().getInkService().getSingleUserDetails(opponentId, sharedHelper.getUserId()).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response == null) {
                        checkOpponentNames();
                        return;
                    }
                    if (response.body() == null) {
                        checkOpponentNames();
                        return;
                    }
                    try {
                        String responseBody = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseBody);
                        String firstName = jsonObject.optString("first_name");
                        String lastName = jsonObject.optString("last_name");

                        opponentFirstName = firstName;
                        opponentLastName = lastName;
                        initUser();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {

                }
            });
        }
    }

    private void checkNotification(Bundle extras, boolean insertMessage, @Nullable final GeneralCallback insertCallback) {
        if (extras != null) {
            if (extras.containsKey(NOTIFICATION_MESSAGE_BUNDLE_KEY)) {
                String receivedMessageJson = extras.getString(NOTIFICATION_MESSAGE_BUNDLE_KEY);
                ChatModel chatModel = chatGSON.fromJson(receivedMessageJson, ChatModel.class);
                initVariables(chatModel, true);
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
                Ion.with(this).load(opponentImageUrl).withBitmap().placeholder(R.drawable.no_background_image).transform(new CircleTransform())
                        .intoImageView(opponentImage);
            } else {
                String encodedImage = Uri.encode(opponentImageUrl);
                Ion.with(this).load(Constants.MAIN_URL + Constants.USER_IMAGES_FOLDER + encodedImage)
                        .withBitmap().placeholder(R.drawable.no_background_image).transform(new CircleTransform()).intoImageView(opponentImage);
            }
        } else {
            Ion.with(this).load(Constants.ANDROID_DRAWABLE_DIR + "no_image")
                    .withBitmap().transform(new CircleTransform())
                    .intoImageView(opponentImage);
        }
    }

    private void initWriteField() {
        RxTextView.textChanges(mWriteEditText)
                .filter(new Func1<CharSequence, Boolean>() {
                    @Override
                    public Boolean call(CharSequence charSequence) {
                        String textToCheck = charSequence.toString().trim();
                        if (textToCheck.length() <= 0 && !isStickerChosen) {
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

                            messageService.emit(EVENT_TYPING, typingJson);
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
                        messageService.emit(EVENT_STOPPED_TYPING, typingJson);
                    }
                });

    }

    private void initRecyclerView() {
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(500);
        itemAnimator.setRemoveDuration(500);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setItemAnimator(itemAnimator);
        mRecyclerView.setAdapter(chatAdapter);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    Keyboard.hideKeyboard(Chat.this);
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
        scrollDownChat.startAnimation(slideOut);
        slideOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                scrollDownChat.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void showScroller() {
        scrollDownChat.setEnabled(true);
        scrollDownChat.setTag(getString(R.string.visible));
        scrollDownChat.startAnimation(slideIn);
        scrollDownChat.setVisibility(View.VISIBLE);
    }


    private void openStickerChooser() {
        Intent intent = new Intent(getApplicationContext(), MyCollection.class);
        intent.putExtra(STARTING_FOR_RESULT_BUNDLE_KEY, true);
        startActivityForResult(intent, Constants.REQUEST_CODE_CHOSE_STICKER);
    }


    private void handleStickerChosenView() {
        mSendChatMessage.setEnabled(true);
        Ion.with(this).load(Constants.MAIN_URL + lastChosenStickerUrl).withBitmap().placeholder(R.drawable.time_loading_vector).intoImageView(stickerPreviewImageView);
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
        builder.setItems(new String[]{getString(R.string.delete), getString(R.string.copy)}, new DialogInterface.OnClickListener() {
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
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText(getString(R.string.messageText), chatModel.getMessage());
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(messageService, getString(R.string.copied), Toast.LENGTH_SHORT).show();
                        break;
                }

            }
        });
        builder.show();
    }

    @Override
    public void onAdditionItemClick(int position, View view) {

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
            default:
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onServiceConnected(MessageService messageService) {
        super.onServiceConnected(messageService);
        this.messageService = messageService;
        socketConnected = messageService.isSocketConnected();
        messageService.setOnSocketListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Notification.get().setSendingRemote(true);
        unbindService();
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
        final ChatModel chatModel = chatGSON.fromJson(messageJson.toString(), ChatModel.class);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (chatModel.getUserId().equals(opponentId)) {
                    chatAdapter.insertChatModel(chatModel);
                    localMessageInsert(chatModel, false);
                    mRecyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            scrollToBottom();
                        }
                    });
                } else {
                    sendGeneralNotification(getApplicationContext(), messageJson);
                }

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
}
