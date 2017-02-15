package ink.va.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
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

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.ink.va.R;

import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.StartupApplication;
import ink.va.adapters.ChatAdapter;
import ink.va.interfaces.RecyclerItemClickListener;
import ink.va.models.ChatModel;
import ink.va.utils.Constants;
import ink.va.utils.Keyboard;
import ink.va.utils.Notification;

import static com.github.nkzawa.socketio.client.Socket.EVENT_CONNECT;
import static com.github.nkzawa.socketio.client.Socket.EVENT_CONNECT_ERROR;
import static com.github.nkzawa.socketio.client.Socket.EVENT_DISCONNECT;
import static ink.va.utils.Constants.EVENT_NEW_MESSAGE;
import static ink.va.utils.Constants.REQUEST_CODE_CHOSE_STICKER;
import static ink.va.utils.Constants.STARTING_FOR_RESULT_BUNDLE_KEY;


public class Chat extends BaseActivity implements RecyclerItemClickListener {

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

    private ChatAdapter chatAdapter;

    private List<ChatModel> chatModels;

    private Socket mSocket;
    private boolean socketConnected;
    private boolean isAnimated;
    private String lastChosenStickerUrl;
    private boolean isStickerChosen;
    private Animation slideIn;
    private Animation slideOut;
    private boolean showSuccess;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);

        chatModels = new LinkedList<>();
        chatAdapter = new ChatAdapter();

        Notification.get().setSendingRemote(false);

        slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_and_rotate_in);
        slideOut = AnimationUtils.loadAnimation(this, R.anim.slide_and_rotate_out);

        initRecyclerView();
        initSocket();
    }


    @OnClick(R.id.sendChatMessage)
    public void sendMessageClicked() {
        if (!socketConnected) {
            Snackbar.make(mRecyclerView, getString(R.string.notConnectedToServer), Snackbar.LENGTH_LONG).setAction(getString(R.string.vk_retry), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showSuccess = true;
                    mSocket.connect();
                }
            });
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
        slideIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }


    private void openStickerChooser() {
        Intent intent = new Intent(getApplicationContext(), MyCollection.class);
        intent.putExtra(STARTING_FOR_RESULT_BUNDLE_KEY, true);
        startActivityForResult(intent, Constants.REQUEST_CODE_CHOSE_STICKER);
    }


    private void handleStickerChosenView() {

    }

    private void destroySocket() {
        mSocket.disconnect();
        mSocket.off(EVENT_CONNECT, onSocketConnected);
        mSocket.off(EVENT_DISCONNECT, onSocketDisconnected);
        mSocket.off(EVENT_NEW_MESSAGE, onNewMessageReceived);
        mSocket.off(EVENT_CONNECT_ERROR, onSocketConnectionError);
    }

    private void initSocket() {
        mSocket = ((StartupApplication) getApplication()).getSocket();
        mSocket.on(EVENT_CONNECT, onSocketConnected);
        mSocket.on(EVENT_CONNECT_ERROR, onSocketConnectionError);
        mSocket.on(EVENT_DISCONNECT, onSocketDisconnected);
        mSocket.on(EVENT_NEW_MESSAGE, onNewMessageReceived);
        mSocket.connect();
    }

    private void scrollToBottom() {
        mRecyclerView.scrollToPosition(chatAdapter.getItemCount());
    }

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


    private Emitter.Listener onNewMessageReceived = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

        }
    };

    private Emitter.Listener onSocketConnected = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
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
    };

    private Emitter.Listener onSocketDisconnected = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            socketConnected = false;
        }
    };

    private Emitter.Listener onSocketConnectionError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            socketConnected = false;
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case REQUEST_CODE_CHOSE_STICKER:
                if (data != null) {
                    isAnimated = data.getExtras().getBoolean(Constants.STICKER_IS_ANIMATED_EXTRA_KEY);
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
    protected void onDestroy() {
        super.onDestroy();
        Notification.get().setSendingRemote(true);
        destroySocket();
    }
}
