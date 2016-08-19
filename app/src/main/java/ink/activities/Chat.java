package ink.activities;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.ink.R;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.wang.avi.AVLoadingIndicatorView;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.adapters.ChatAdapter;
import ink.adapters.GifAdapter;
import ink.interfaces.RecyclerItemClickListener;
import ink.models.ChatModel;
import ink.models.GifModel;
import ink.models.GifResponse;
import ink.models.GifResponseModel;
import ink.models.MessageModel;
import ink.models.UserStatus;
import ink.utils.CircleTransform;
import ink.utils.Constants;
import ink.utils.ErrorCause;
import ink.utils.Keyboard;
import ink.utils.Notification;
import ink.utils.QueHelper;
import ink.utils.RealmHelper;
import ink.utils.RecyclerTouchListener;
import ink.utils.Retrofit;
import ink.utils.SharedHelper;
import ink.utils.Time;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Chat extends BaseActivity implements RecyclerItemClickListener {

    private static final int REQUEST_LOCATION_CODE = 454;
    @Bind(R.id.sendChatMessage)
    fab.FloatingActionButton mSendChatMessage;
    @Bind(R.id.messageBody)
    EditText mWriteEditText;
    @Bind(R.id.noMessageLayout)
    NestedScrollView mNoMessageLayout;
    @Bind(R.id.chatRecyclerView)
    RecyclerView mRecyclerView;
    @Bind(R.id.chatTitle)
    TextView chatTitle;
    private boolean isImageLoaded = false;
    @Bind(R.id.opponentImage)
    ImageView opponentImage;
    @Bind(R.id.opponentStatus)
    TextView opponentStatus;
    @Bind(R.id.statusColor)
    ImageView statusColor;
    @Bind(R.id.sendMessageGifView)
    ImageView sendMessageGifView;
    @Bind(R.id.sendMessageGifViewWrapper)
    RelativeLayout sendMessageGifViewWrapper;
    @Bind(R.id.singleGifViewLoading)
    AVLoadingIndicatorView singleGifViewLoading;
    @Bind(R.id.scrollDownChat)
    ImageView scrollDownChat;
    @Bind(R.id.locationSessionIcon)
    ImageView locationSessionIcon;

    private String mOpponentId;
    String mCurrentUserId;
    private SharedHelper mSharedHelper;
    private RealmHelper mRealHelper;
    private List<ChatModel> mChatModelArrayList = new ArrayList<>();
    private ChatAdapter mChatAdapter;
    private ChatModel mChatModel;
    private String mUserImage = "";
    private String mOpponentImage = "";
    private String mDeleteUserId;
    private String mDeleteOpponentId;
    private Gson gifGson;
    private Animation fadeAnimation;
    private String firstName;
    private String lastName;
    private GifAdapter gifAdapter;
    private BottomSheetDialog gifChooserDialog;
    private List<GifModel> gifModelList;
    private GifModel gifModel;
    private boolean isGifChosen = false;
    private String lasChosenGifName;
    private Gson gson;
    private Animation slideIn;
    private Animation slideOut;
    private boolean hasFriendCheckLoaded;
    private boolean isFriend;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.chat_background));
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarChat);
        setSupportActionBar(toolbar);

        fadeAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in_scale);

        ButterKnife.bind(this);
        mSharedHelper = new SharedHelper(this);
        gson = new Gson();
        gifModelList = new ArrayList<>();
        gifAdapter = new GifAdapter(gifModelList, this);
        gifAdapter.setOnItemClickListener(this);
        gifGson = new Gson();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.updatingMessages));
        progressDialog.setMessage(getString(R.string.updatingYourMessages));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);

        slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_and_rotate_in);
        slideOut = AnimationUtils.loadAnimation(this, R.anim.slide_and_rotate_out);


        Notification.getInstance().setSendingRemote(false);

        LocalBroadcastManager.getInstance(this).registerReceiver(generalReceiver, new IntentFilter(getPackageName() + ".Chat"));
        mChatAdapter = new ChatAdapter(mChatModelArrayList, this);
        mRealHelper = RealmHelper.getInstance();

        configureChat();


        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(500);
        itemAnimator.setRemoveDuration(500);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setItemAnimator(itemAnimator);
        mRecyclerView.setAdapter(mChatAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                System.gc();
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    Keyboard.hideKeyboard(getApplicationContext(), mRecyclerView);
                }

                LinearLayoutManager layoutManager = ((LinearLayoutManager) recyclerView.getLayoutManager());
                int firstVisiblePosition = layoutManager.findLastVisibleItemPosition();
                if (mChatAdapter.getItemCount() > 5) {
                    if (firstVisiblePosition < mChatAdapter.getItemCount() - 4) {
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


        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(this, mRecyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                ChatModel chatModel = mChatModelArrayList.get(position);
                if (chatModel.hasGif()) {
                    Intent intent = new Intent(getApplicationContext(), FullscreenActivity.class);
                    intent.putExtra("link", Constants.MAIN_URL + Constants.ANIMATED_STICKERS_FOLDER + chatModel.getGifUrl());
                    startActivity(intent);
                }
            }

            @Override
            public void onLongClick(View view, final int position) {
                System.gc();
                final ChatModel chatModel = mChatModelArrayList.get(position);
                String date = chatModel.getDate();
                AlertDialog.Builder builder = new AlertDialog.Builder(Chat.this);
                builder.setTitle("Message Details");
                builder.setMessage("Date of message : " + date);
                builder.setPositiveButton(getString(R.string.close), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.setNegativeButton(getString(R.string.deleteMessage), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        showDeleteWarning(chatModel.getMessageId(), position);
                    }
                });

                builder.show();
            }
        }));

        mSendChatMessage.setEnabled(false);
        mWriteEditText.addTextChangedListener(chatTextWatcher);

    }

    private void showDeleteWarning(final String messageId, final int position) {
        System.gc();
        AlertDialog.Builder builder = new AlertDialog.Builder(Chat.this);
        builder.setTitle(getString(R.string.warning));
        builder.setMessage(getString(R.string.deleteMessageWarning));
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteMessage(messageId, position);
            }
        });
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.show();
    }

    private void deleteMessage(final String messageId, final int positionOfItem) {
        progressDialog.setTitle(getString(R.string.deleting));
        progressDialog.setMessage(getString(R.string.deletingMessage));
        progressDialog.show();
        Call<ResponseBody> deleteMessageCall = Retrofit.getInstance().getInkService().deleteMessage(messageId, mSharedHelper.getUserId(), mOpponentId);
        deleteMessageCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    deleteMessage(messageId, positionOfItem);
                    return;
                }
                if (response.body() == null) {
                    deleteMessage(messageId, positionOfItem);
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");

                    if (success) {
                        mChatModelArrayList.remove(positionOfItem);
                        RealmHelper.getInstance().removeMessage(messageId);
                        mChatAdapter.notifyDataSetChanged();
                        getMessages();
                        progressDialog.dismiss();
                        Snackbar.make(chatTitle, getString(R.string.messageDeleted), Snackbar.LENGTH_SHORT).show();
                    } else {
                        progressDialog.dismiss();
                        Snackbar.make(chatTitle, getString(R.string.messagedeleteError), Snackbar.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    deleteMessage(messageId, positionOfItem);
                    e.printStackTrace();
                } catch (JSONException e) {
                    deleteMessage(messageId, positionOfItem);
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                deleteMessage(messageId, positionOfItem);
            }
        });
    }

    @OnClick(R.id.scrollDownChat)
    public void scrollDownChat() {
        mRecyclerView.stopScroll();
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                mRecyclerView.scrollToPosition(mChatAdapter.getItemCount() - 1);
            }
        });
        hideScroller();
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }


    private void getStatus() {
        if (mOpponentId != null && !mOpponentId.isEmpty()) {
            final Call<ResponseBody> statusCall = Retrofit.getInstance().getInkService().getUserStatus(mOpponentId);
            statusCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response == null) {
                        getStatus();
                        return;
                    }
                    if (response.body() == null) {
                        getStatus();
                        return;
                    }
                    try {
                        String responseBody = response.body().string();
                        final UserStatus userStatus = gifGson.fromJson(responseBody, UserStatus.class);
                        if (userStatus.success) {
                            if (userStatus.isOnline) {
                                statusColor.setVisibility(View.VISIBLE);
                                statusColor.startAnimation(fadeAnimation);
                                opponentStatus.setText(getString(R.string.onlineStatus));
                            } else {
                                opponentStatus.setText(getString(R.string.lastSeen, Time.convertToLocalTime(userStatus.lastSeenTime)));
                            }
                        } else {
                            getStatus();
                        }
                    } catch (Exception e) {
                        getStatus();
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    getStatus();
                }
            });
        }
    }


    @OnClick(R.id.trashIcon)
    public void trashIcon() {
        isGifChosen = false;
        System.gc();
        dismissStickerChooser();
        if (mWriteEditText.getText().toString().trim().isEmpty()) {
            mSendChatMessage.setEnabled(false);
        }
    }

    @OnClick(R.id.attachmentIcon)
    public void attachmentIcon() {
        System.gc();
        gifModelList.clear();
        gifChooserDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.user_gifs_view, null);
        gifChooserDialog.setContentView(view);
        final RecyclerView gifsRecycler = (RecyclerView) view.findViewById(R.id.gifsRecycler);
        ImageView closeGifChoser = (ImageView) view.findViewById(R.id.closeGifChoser);
        ProgressBar gifLoadingProgress = (ProgressBar) view.findViewById(R.id.gifLoadingProgress);
        TextView noGifsText = (TextView) view.findViewById(R.id.noGifsText);
        closeGifChoser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gifChooserDialog.dismiss();
                gifsRecycler.setAdapter(null);
            }
        });

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        gifsRecycler.setLayoutManager(gridLayoutManager);

        gifsRecycler.setAdapter(gifAdapter);
        getUserGifs(noGifsText, gifLoadingProgress);
        gifChooserDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                System.gc();
                gifsRecycler.setAdapter(null);
            }
        });
        gifChooserDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                System.gc();
                gifsRecycler.setAdapter(null);
            }
        });
        gifChooserDialog.show();
    }

    private void getUserGifs(final TextView noGifsText, final ProgressBar gifLoadingProgress) {
        Call<ResponseBody> gifCall = Retrofit.getInstance().getInkService().getUserGifs(mSharedHelper.getUserId(),
                Constants.SERVER_AUTH_KEY);
        gifCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    getUserGifs(noGifsText, gifLoadingProgress);
                    return;
                }
                if (response.body() == null) {
                    getUserGifs(noGifsText, gifLoadingProgress);
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    GifResponse gifResponse = gson.fromJson(responseBody, GifResponse.class);
                    gifLoadingProgress.setVisibility(View.INVISIBLE);
                    if (gifResponse.success) {
                        if (!gifResponse.cause.equals(ErrorCause.NO_GIFS)) {
                            ArrayList<GifResponseModel> gifResponseModels = gifResponse.gifResponseModels;
                            for (int i = 0; i < gifResponseModels.size(); i++) {
                                GifResponseModel eachModel = gifResponseModels.get(i);
                                gifModel = new GifModel(eachModel.id, eachModel.userId, eachModel.gifName, eachModel.isAnimated, eachModel.hasSound);
                                gifModelList.add(gifModel);
                                gifAdapter.notifyDataSetChanged();
                            }
                            if (gifModelList.size() <= 0) {
                                noGifsText.setVisibility(View.VISIBLE);
                            } else {
                                noGifsText.setVisibility(View.GONE);
                            }

                        } else {
                            noGifsText.setVisibility(View.VISIBLE);
                        }
                    } else {

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                getUserGifs(noGifsText, gifLoadingProgress);
            }
        });
    }


    @OnClick(R.id.sendChatMessage)
    public void sendChatMessage() {
        if (mNoMessageLayout.getVisibility() == View.VISIBLE) {
            mNoMessageLayout.setVisibility(View.GONE);
        }
        String message = StringEscapeUtils.escapeJava(mWriteEditText.getText().toString().trim());
        dismissStickerChooser();
        ChatModel tempChat = new ChatModel(isGifChosen, lasChosenGifName, null, mCurrentUserId, mOpponentId, StringEscapeUtils.unescapeJava(message.trim()),
                false, Constants.STATUS_NOT_DELIVERED,
                mUserImage, mOpponentImage, "");
        mChatModelArrayList.add(tempChat);
        int itemLocation = mChatModelArrayList.indexOf(tempChat);

        attemptToQue(message.trim(), itemLocation, mDeleteOpponentId, mDeleteUserId, isGifChosen, lasChosenGifName);
        mChatAdapter.notifyDataSetChanged();


        mWriteEditText.setText("");
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                // Call smooth scroll
                scrollToBottom();
            }
        });
        isGifChosen = false;

    }

    private void dismissStickerChooser() {
        System.gc();
        sendMessageGifView.setBackground(null);
        sendMessageGifView.setImageResource(0);
        if (sendMessageGifViewWrapper.getVisibility() == View.VISIBLE) {
            sendMessageGifViewWrapper.setVisibility(View.GONE);
        }
    }


    @OnClick(R.id.opponentImage)
    public void opponentImage() {
        if (!hasFriendCheckLoaded) {
            getIsFriend();
            Snackbar.make(sendMessageGifView, getString(R.string.waitTillLoad), Snackbar.LENGTH_SHORT).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            }).show();
        } else {
            Intent intent = new Intent(getApplicationContext(), OpponentProfile.class);
            intent.putExtra("id", mOpponentId);
            intent.putExtra("firstName", firstName);
            intent.putExtra("lastName", lastName);
            intent.putExtra("disableButton", true);
            intent.putExtra("isFriend", isFriend);
            startActivity(intent);
        }

    }

    private void attemptToQue(String message, int itemLocation, String deleteOpponentId,
                              String deleteUserId, final boolean hasGif, final String gifUrl) {
        RealmHelper.getInstance().insertMessage(mCurrentUserId, mOpponentId,
                message, "0", "",
                String.valueOf(itemLocation),
                Constants.STATUS_NOT_DELIVERED, mUserImage,
                mOpponentImage, deleteOpponentId, deleteUserId, hasGif, gifUrl);

        QueHelper queHelper = new QueHelper();
        queHelper.attachToQue(mOpponentId, message, itemLocation, isGifChosen, gifUrl, Chat.this);


    }

    private void handleMessageSent(String response, int sentItemLocation) {
        System.gc();
        try {
            JSONObject jsonObject = new JSONObject(response);
            boolean success = jsonObject.optBoolean("success");
            if (success) {
                String messageId = jsonObject.optString("message_id");
                mChatModelArrayList.get(sentItemLocation).setMessageId(messageId);
                mChatModelArrayList.get(sentItemLocation).setClickable(true);
                mChatModelArrayList.get(sentItemLocation).setDeliveryStatus(Constants.STATUS_DELIVERED);
                mChatModelArrayList.get(sentItemLocation).setDate(Time.convertToLocalTime(jsonObject.optString("date")));
                mChatAdapter.notifyItemChanged(sentItemLocation);
                RealmHelper.getInstance().updateMessages(messageId,
                        Constants.STATUS_DELIVERED, String.valueOf(sentItemLocation),
                        mOpponentId);

            } else {

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getMessages() {
        if (mChatModelArrayList != null) {
            mChatModelArrayList.clear();
        }
        mChatAdapter.notifyDataSetChanged();

        List<MessageModel> messageModels = mRealHelper.getMessages(mOpponentId, mCurrentUserId);
        if (messageModels.isEmpty()) {
            mNoMessageLayout.setVisibility(View.VISIBLE);
        } else {

            for (int i = 0; i < messageModels.size(); i++) {
                MessageModel eachModel = messageModels.get(i);
                String messageId = eachModel.getMessageId();
                String opponentId = eachModel.getOpponentId();
                String message = StringEscapeUtils.unescapeJava(eachModel.getMessage());
                String userId = eachModel.getUserId();
                String userImage = eachModel.getUserImage();
                String opponentImage = eachModel.getOpponentImage();
                String date = eachModel.getDate();
                boolean isGifChosen = eachModel.hasGif();
                String gifUrl = eachModel.getGifUrl();

                String deleteUserId = eachModel.getDeleteUserId();
                String deleteOpponentId = eachModel.getDeleteOpponentId();
                mDeleteOpponentId = eachModel.getDeleteOpponentId();
                mDeleteUserId = eachModel.getDeleteUserId();

                if (deleteOpponentId != null && deleteUserId != null) {
                    if (deleteOpponentId.equals(mSharedHelper.getUserId()) || deleteUserId.equals(mSharedHelper.getUserId())) {
                        continue;
                    }
                }

                mChatModel = new ChatModel(isGifChosen, gifUrl, messageId, userId, opponentId, message, true,
                        eachModel.getDeliveryStatus(), userImage, opponentImage, date);
                mChatModelArrayList.add(mChatModel);
                if (eachModel.getDeliveryStatus().equals(Constants.STATUS_NOT_DELIVERED)) {
                    int itemLocation = mChatModelArrayList.indexOf(mChatModel);
                    attemptToQue(message, itemLocation,
                            deleteOpponentId, deleteUserId, isGifChosen, lasChosenGifName);
                }
                mChatAdapter.notifyDataSetChanged();
            }

            if (mChatModelArrayList.size() <= 0) {
                mNoMessageLayout.setVisibility(View.VISIBLE);
            }
            mRecyclerView.post(new Runnable() {
                @Override
                public void run() {
                    scrollToBottom();
                }
            });

        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        finish();
        return super.onOptionsItemSelected(item);
    }


    private TextWatcher chatTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

            if (s.toString().trim().length() <= 0) {
                if (!isGifChosen) {
                    mSendChatMessage.setEnabled(false);
                } else {
                    mSendChatMessage.setEnabled(true);
                }
            } else {
                mSendChatMessage.setEnabled(true);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    };

    @Override
    protected void onDestroy() {
        System.gc();
        Notification.getInstance().setSendingRemote(true);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(generalReceiver);
        super.onDestroy();
    }

    private BroadcastReceiver generalReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                String type = extras.getString("type");

                switch (type) {
                    case "showMessage":
                        RemoteMessage remoteMessage = extras.getParcelable("data");
                        Map<String, String> response = remoteMessage.getData();
                        if (mOpponentId.equals(response.get("user_id"))) {
                            mChatModel = new ChatModel(Boolean.valueOf(response.get("hasGif")), response.get("gifUrl"), response.get("message_id"), response.get("user_id"),
                                    response.get("opponent_id"), StringEscapeUtils.unescapeJava(response.get("message")), true, Constants.STATUS_DELIVERED,
                                    response.get("user_image"), response.get("opponent_image"), Time.convertToLocalTime(response.get("date")));
                            mChatModelArrayList.add(mChatModel);
                            mChatAdapter.notifyDataSetChanged();
                            mRecyclerView.post(new Runnable() {
                                @Override
                                public void run() {
                                    scrollToBottom();
                                }
                            });
                        }
                        break;
                    case "finish":
                        finish();
                        break;
                    case Constants.TYPE_MESSAGE_SENT:
                        String responseBody = extras.getString("response");
                        String sentItemLocation = extras.getString("sentItemLocation");
                        handleMessageSent(responseBody, Integer.valueOf(sentItemLocation));
                        break;

                    case Constants.DELETE_MESSAGE_REQUESTED:
                        String messageId = extras.getString("messageId");
                        RealmHelper.getInstance().removeMessage(messageId);
                        getMessages();
                        break;

                }
            }
        }
    };


    @Override
    protected void onResume() {
        System.gc();
        Notification.getInstance().setSendingRemote(false);
        getStatus();
        getIsFriend();
        super.onResume();
    }

    private void getIsFriend() {
        hasFriendCheckLoaded = false;
        if (mOpponentId != null && !mOpponentId.isEmpty()) {
            Call<ResponseBody> isFriendCheckCall = Retrofit.getInstance().getInkService().isFriendCheck(mSharedHelper.getUserId(), mOpponentId);
            isFriendCheckCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response == null) {
                        getIsFriend();
                        return;
                    }
                    if (response.body() == null) {
                        getIsFriend();
                        return;
                    }
                    try {
                        String responseBody = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseBody);
                        isFriend = jsonObject.optBoolean("isFriend");
                        hasFriendCheckLoaded = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    getIsFriend();
                }
            });
        }

    }


    private void configureChat() {
        ActionBar actionBar = getSupportActionBar();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            firstName = bundle.getString("firstName");
            lastName = bundle.getString("lastName");
            mOpponentId = bundle.getString("opponentId");
            String opponentImage = bundle.getString("opponentImage");
            boolean isSocialAccount = bundle.getBoolean("isSocialAccount");

            if (bundle.containsKey("messageId")) {
                String messageId = bundle.getString("messageId");
                boolean isMessageExist = RealmHelper.getInstance().isMessageExist(messageId);
                if (!isMessageExist) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(getString(R.string.messageMissing));
                    builder.setMessage(getString(R.string.messageMissingText));
                    builder.setPositiveButton(getString(R.string.updateMessages), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            progressDialog.show();
                            getMyMessages(mSharedHelper.getUserId());
                        }
                    });
                    builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    builder.show();
                }
            }

            if (opponentImage != null && !opponentImage.isEmpty()) {
                if (!isImageLoaded) {
                    isImageLoaded = true;
                    if (isSocialAccount) {
                        Ion.with(this).load(opponentImage).withBitmap().placeholder(R.drawable.no_background_image).transform(new CircleTransform()).intoImageView(this.opponentImage);
                    } else {
                        Ion.with(this).load(Constants.MAIN_URL + Constants.USER_IMAGES_FOLDER +
                                opponentImage).withBitmap().placeholder(R.drawable.no_background_image).transform(new CircleTransform()).intoImageView(this.opponentImage);
                    }
                }
            } else {
                Ion.with(this).load(Constants.ANDROID_DRAWABLE_DIR + "no_image").withBitmap().transform(new CircleTransform()).intoImageView(this.opponentImage);
            }
            getStatus();
            mCurrentUserId = mSharedHelper.getUserId();
            if (mChatModelArrayList != null) {
                mChatModelArrayList.clear();
            }
            getMessages();
            //action bar set ups.
            chatTitle.setText(firstName);
        }

        //action bar set ups
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void sendLocationUpdate(final String mOpponentId, final String latitude, final String longitude, final boolean showSnack) {
        Call<ResponseBody> locationUpdateCall = Retrofit.getInstance().getInkService().sendLocationUpdate(mOpponentId,
                longitude, latitude);
        locationUpdateCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    sendLocationUpdate(mOpponentId, latitude, longitude, showSnack);
                    return;
                }
                if (response.body() == null) {
                    sendLocationUpdate(mOpponentId, latitude, longitude, showSnack);
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    if (success) {
                        if (showSnack) {
                            Snackbar.make(chatTitle, getString(R.string.locationSent), Snackbar.LENGTH_SHORT).setAction("OK", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                }
                            }).show();
                        }

                    } else {
                        if (showSnack) {
                            Snackbar.make(chatTitle, getString(R.string.locationSendError), Snackbar.LENGTH_SHORT).setAction("OK", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                }
                            }).show();
                        }

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    sendLocationUpdate(mOpponentId, latitude, longitude, showSnack);
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                sendLocationUpdate(mOpponentId, latitude, longitude, showSnack);
            }
        });
    }


    @Override
    protected void onPause() {
        System.gc();
        Notification.getInstance().setSendingRemote(true);
        super.onPause();
    }

    @Override
    public void onItemClicked(int position, View view) {
        System.gc();
        GifModel singleModel = gifModelList.get(position);
        String gifName = gifModelList.get(position).getGifName();
        sendMessageGifViewWrapper.setVisibility(View.VISIBLE);
        gifChooserDialog.dismiss();
        singleGifViewLoading.setVisibility(View.VISIBLE);
        mSendChatMessage.setEnabled(true);
        if (singleModel.isAnimated()) {
            if (singleModel.hasSound()) {

            }
            Ion.with(getApplicationContext()).load(Constants.MAIN_URL + Constants.ANIMATED_STICKERS_FOLDER + gifName).intoImageView(sendMessageGifView).setCallback(new FutureCallback<ImageView>() {
                @Override
                public void onCompleted(Exception e, ImageView result) {
                    singleGifViewLoading.setVisibility(View.GONE);
                }
            });
        } else {

        }
        lasChosenGifName = gifName;
        isGifChosen = true;
    }


    /**
     * UNUSABLE
     *
     * @param position
     */
    @Override
    public void onItemLongClick(int position) {

    }

    /**
     * UNUSABLE
     *
     * @param position
     * @param view
     */
    @Override
    public void onAdditionItemClick(int position, View view) {

    }

    private void getMyMessages(final String userId) {
        Call<ResponseBody> myMessagesResponse = Retrofit.getInstance().getInkService().getChatMessages(userId);
        myMessagesResponse.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    getMyMessages(userId);
                    return;
                }
                if (response.body() == null) {
                    getMyMessages(userId);
                    return;
                }
                try {
                    String responseString = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseString);
                    JSONArray messagesArray = jsonObject.optJSONArray("messages");
                    RealmHelper realmHelper = RealmHelper.getInstance();
                    if (messagesArray.length() > 0) {
                        for (int i = 0; i < messagesArray.length(); i++) {
                            JSONObject eachObject = messagesArray.optJSONObject(i);
                            String userId = eachObject.optString("user_id");
                            String opponentId = eachObject.optString("opponent_id");
                            String message = eachObject.optString("message");
                            String messageId = eachObject.optString("message_id");
                            String date = Time.convertToLocalTime(eachObject.optString("date"));
                            String deliveryStatus = Constants.STATUS_DELIVERED;
                            String userIdImage = eachObject.optString("user_id_image");
                            String opponentImage = eachObject.optString("opponent_id_image");
                            String deleteUserId = eachObject.optString("delete_user_id");
                            String deleteOpponentId = eachObject.optString("delete_opponent_id");

                            boolean hasGif = eachObject.optBoolean("hasGif");
                            String gifUrl = eachObject.optString("gifUrl");
                            String isAnimated = eachObject.optString("isAnimated");
                            String hasSound = eachObject.optString("hasSound");

                            realmHelper.insertMessage(userId,
                                    opponentId, message, messageId, date, messageId,
                                    deliveryStatus,
                                    userIdImage, opponentImage, deleteOpponentId, deleteUserId, hasGif, gifUrl);

                        }

                        getMessages();

                        Snackbar.make(chatTitle, getString(R.string.messagesUpdated), Snackbar.LENGTH_LONG).setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                            }
                        }).show();
                    } else {
                        Snackbar.make(chatTitle, getString(R.string.noMessages), Snackbar.LENGTH_LONG).show();
                    }
                    progressDialog.dismiss();
                } catch (IOException e) {
                    progressDialog.dismiss();
                    e.printStackTrace();
                } catch (JSONException e) {
                    progressDialog.dismiss();
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                getMyMessages(userId);
            }
        });
    }

    private void scrollToBottom() {
        mRecyclerView.smoothScrollToPosition(mChatAdapter.getItemCount());
    }

}
