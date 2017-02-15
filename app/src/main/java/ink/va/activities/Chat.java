package ink.va.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
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

import com.google.gson.Gson;
import com.ink.va.R;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.koushikdutta.ion.Ion;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.va.adapters.ChatAdapter;
import ink.va.interfaces.RecyclerItemClickListener;
import ink.va.interfaces.SocketListener;
import ink.va.models.ChatModel;
import ink.va.service.MessageService;
import ink.va.utils.CircleTransform;
import ink.va.utils.Constants;
import ink.va.utils.Keyboard;
import ink.va.utils.Notification;
import ink.va.utils.SharedHelper;
import ink.va.utils.Time;
import rx.Observer;
import rx.functions.Func1;

import static ink.va.utils.Constants.EVENT_SEND_MESSAGE;
import static ink.va.utils.Constants.EVENT_STOPPED_TYPING;
import static ink.va.utils.Constants.EVENT_TYPING;
import static ink.va.utils.Constants.NOTIFICATION_MESSAGE_BUNDLE_KEY;
import static ink.va.utils.Constants.REQUEST_CODE_CHOSE_STICKER;
import static ink.va.utils.Constants.STARTING_FOR_RESULT_BUNDLE_KEY;


public class Chat extends BaseActivity implements RecyclerItemClickListener, SocketListener {

    public static final String TAG = Chat.class.getSimpleName();

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

    private List<ChatModel> chatModels;

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
    private Bundle extras;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);
        chatGSON = new Gson();
        Bundle extras = getIntent().getExtras();

        opponentId = extras != null ? extras.containsKey("opponentId") ? extras.getString("opponentId") : "" : "";
        opponentFirstName = extras != null ? extras.containsKey("firstName") ? extras.getString("firstName") : "" : "";
        opponentLastName = extras != null ? extras.containsKey("lastName") ? extras.getString("lastName") : "" : "";
        opponentImageUrl = extras != null ? extras.containsKey("opponentImage") ? extras.getString("opponentImage") : "" : "";
        isSocialAccount = extras != null ? extras.containsKey("isSocialAccount") ? extras.getBoolean("isSocialAccount") : false : false;
        extras = getIntent().getExtras() != null ? getIntent().getExtras() : null;

        initUser();

        mSendChatMessage.setEnabled(false);

        sharedHelper = new SharedHelper(this);
        currentUserId = sharedHelper.getUserId();

        chatModels = new LinkedList<>();
        chatAdapter = new ChatAdapter();

        Notification.get().setSendingRemote(false);

        slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_and_rotate_in);
        slideOut = AnimationUtils.loadAnimation(this, R.anim.slide_and_rotate_out);
        getWindow().setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.chat_vector_background));
        initRecyclerView();
        initWriteField();
        checkExtra();
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
                messageJson.put("firstName", sharedHelper.getFirstName());
                messageJson.put("lastName", sharedHelper.getLastName());
                messageJson.put("message", message);
                messageJson.put("date", Time.getCurrentTime());
                messageJson.put("stickerChosen", isStickerChosen);
                messageJson.put("stickerUrl", lastChosenStickerUrl);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            mWriteEditText.setText("");

            ChatModel chatModel = chatGSON.fromJson(messageJson.toString(), ChatModel.class);
            chatAdapter.insertChatModel(chatModel);
            mRecyclerView.post(new Runnable() {
                @Override
                public void run() {
                    scrollToBottom();
                }
            });
            handleStickerRemoved();
            messageService.emit(EVENT_SEND_MESSAGE, messageJson);
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

    private void checkExtra() {
        if (extras != null) {
            String receivedMessageJson = extras.getString(NOTIFICATION_MESSAGE_BUNDLE_KEY);
            ChatModel chatModel = chatGSON.fromJson(receivedMessageJson, ChatModel.class);
            chatAdapter.insertChatModel(chatModel);
            mRecyclerView.post(new Runnable() {
                @Override
                public void run() {
                    scrollToBottom();
                }
            });
        }
    }

    private void initUser() {
        chatTitle.setText(opponentFirstName + " " + opponentLastName);
        if (opponentImageUrl != null && !opponentImageUrl.isEmpty()) {
            if (isSocialAccount) {
                Ion.with(this).load(opponentImageUrl).withBitmap().placeholder(R.drawable.no_background_image)
                        .intoImageView(opponentImage);
            } else {
                String encodedImage = Uri.encode(opponentImageUrl);
                Ion.with(this).load(Constants.MAIN_URL + Constants.USER_IMAGES_FOLDER + encodedImage)
                        .withBitmap().placeholder(R.drawable.no_background_image).intoImageView(opponentImage);
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
                                typingJson.put("opponentFirstName", opponentFirstName);
                                typingJson.put("opponentLastName", opponentLastName);
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
                            typingJson.put("opponentFirstName", opponentFirstName);
                            typingJson.put("opponentLastName", opponentLastName);
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
    public void onItemLongClick(int position) {

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
    public void onUserStoppedTyping() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                opponentTypingLayout.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onUserTyping(final JSONObject jsonObject) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String opponentFirstName = jsonObject.optString("opponentFirstName");
                opponentTypingTV.setText(getString(R.string.opponentTyping, opponentFirstName));
                opponentTypingLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onNewMessageReceived(JSONObject messageJson) {
        final ChatModel chatModel = chatGSON.fromJson(messageJson.toString(), ChatModel.class);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                chatAdapter.insertChatModel(chatModel);
                mRecyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollToBottom();
                    }
                });

            }
        });
    }
}
