package ink.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.ink.R;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.wang.avi.AVLoadingIndicatorView;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.adapters.ChatAdapter;
import ink.adapters.GifAdapter;
import ink.callbacks.GeneralCallback;
import ink.callbacks.QueCallback;
import ink.interfaces.RecyclerItemClickListener;
import ink.models.ChatModel;
import ink.models.GifModel;
import ink.models.GifResponse;
import ink.models.GifResponseModel;
import ink.models.MessageModel;
import ink.models.UserStatus;
import ink.service.LocationRequestSessionDestroyer;
import ink.utils.CircleTransform;
import ink.utils.Constants;
import ink.utils.ErrorCause;
import ink.utils.Keyboard;
import ink.utils.Notification;
import ink.utils.PermissionsChecker;
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

public class Chat extends BaseActivity implements RecyclerItemClickListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

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
    @Bind(R.id.locationRequestLayout)
    RelativeLayout locationRequestLayout;
    @Bind(R.id.requestStatus)
    TextView requestStatus;
    @Bind(R.id.closeSession)
    RelativeLayout closeSession;
    @Bind(R.id.minimize)
    RelativeLayout minimize;

    private String mOpponentId;
    String mCurrentUserId;
    private SharedHelper mSharedHelper;
    private RealmHelper mRealHelper;
    private List<ChatModel> mChatModelArrayList = new ArrayList<>();
    private ChatAdapter mChatAdapter;
    private ChatModel mChatModel;
    private String mUserImage = "";
    private String mOpponentImage = "";
    private AlertDialog.Builder mBuilder;
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
    private boolean isSessionOpened;
    private Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mGoogleMap;
    private Thread mWorkerThread;
    private Menu menuItem;
    private Animation slideUp;
    private Animation slideDown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.chat_background));
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarik);
        setSupportActionBar(toolbar);
        mBuilder = new AlertDialog.Builder(this);
        fadeAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in_scale);
        ButterKnife.bind(this);
        mSharedHelper = new SharedHelper(this);
        gson = new Gson();
        gifModelList = new ArrayList<>();
        gifAdapter = new GifAdapter(gifModelList, this);
        gifAdapter.setOnItemClickListener(this);
        gifGson = new Gson();
        slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_and_rotate_in);
        slideOut = AnimationUtils.loadAnimation(this, R.anim.slide_and_rotate_out);
        slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up_slow);
        slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down_slow);
        Notification.getInstance().setSendingRemote(false);
        LocalBroadcastManager.getInstance(this).registerReceiver(generalReceiver, new IntentFilter(getPackageName() + ".Chat"));
        mChatAdapter = new ChatAdapter(mChatModelArrayList, this);
        mRealHelper = RealmHelper.getInstance();
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

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
            public void onLongClick(View view, int position) {
                ChatModel chatModel = mChatModelArrayList.get(position);
                String date = chatModel.getDate();
                if (!mCurrentUserId.equals(chatModel.getUserId())) {
                    date = Time.convertToLocalTime(date);
                }
                mBuilder.setTitle("Message Details");
                mBuilder.setMessage("Date of message:" + date);
                mBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                mBuilder.show();
            }
        }));

        mSendChatMessage.setEnabled(false);
        mWriteEditText.addTextChangedListener(chatTextWatcher);

    }

    @OnClick(R.id.locationSessionIcon)
    public void locationSessionIcon() {
        locationRequestLayout.setEnabled(false);
        locationRequestLayout.setVisibility(View.VISIBLE);
        locationRequestLayout.startAnimation(slideUp);
        slideUp.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                locationRequestLayout.setEnabled(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

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
        getMenuInflater().inflate(R.menu.location_menu, menu);
        menuItem = menu;
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
                            noGifsText.setVisibility(View.GONE);
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
        queHelper.attachToQue(mCurrentUserId, mOpponentId, message, itemLocation, isGifChosen, gifUrl,
                new QueCallback() {
                    @Override
                    public void onMessageSent(String response, int sentItemLocation) {
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

                    @Override
                    public void onMessageSentFail(QueHelper failedHelperInstance, String failedMessage, int failedItemLocation) {
                        failedHelperInstance.attachToQue(mCurrentUserId, mOpponentId, failedMessage, failedItemLocation, hasGif, gifUrl, this);
                    }
                });
    }

    private void getMessages() {
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

        if (item.getItemId() == android.R.id.home) {
            if (isSessionOpened) {
                showWarning();
            } else {
                finish();
            }
            return super.onOptionsItemSelected(item);
        } else if (item.getItemId() == R.id.requestLocation) {
            startLocationSession();
            return false;

        } else {
            return super.onOptionsItemSelected(item);
        }
    }


    @OnClick(R.id.minimize)
    public void minimize() {
        locationSessionIcon.setVisibility(View.VISIBLE);

        locationRequestLayout.setEnabled(false);
        slideDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                locationRequestLayout.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        locationRequestLayout.startAnimation(slideDown);
    }

    @OnClick(R.id.closeSession)
    public void closeSession() {
        showMenuItem();
        destroySession(false);
        locationRequestLayout.setEnabled(false);
        slideDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                locationRequestLayout.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        locationRequestLayout.startAnimation(slideDown);
        locationSessionIcon.setVisibility(View.GONE);
    }

    private void showMenuItem() {
        if (menuItem != null) {
            for (int i = 0; i < menuItem.size(); i++) {
                menuItem.getItem(i).setVisible(true);
            }

        }
    }

    private void startLocationSession() {
        System.gc();
        hideMenuItem();
        locationRequestLayout.setVisibility(View.VISIBLE);
        slideUp.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                locationRequestLayout.setEnabled(false);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                locationRequestLayout.setEnabled(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        locationRequestLayout.startAnimation(slideUp);
        locationSessionIcon.setVisibility(View.VISIBLE);
        requestLocation();

    }

    private void hideMenuItem() {
        if (menuItem != null) {
            for (int i = 0; i < menuItem.size(); i++) {
                menuItem.getItem(i).setVisible(false);
            }

        }
    }


    private void requestLocation() {
        Call<ResponseBody> friendLocationCall = Retrofit.getInstance().getInkService().requestFriendLocation(mSharedHelper.getUserId(), mOpponentId,
                mSharedHelper.getFirstName() + " " + mSharedHelper.getLastName(), firstName + " " + lastName, Constants.LOCATION_REQUEST_TYPE_INSERT);
        friendLocationCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    requestLocation();
                    return;
                }
                if (response.body() == null) {
                    requestLocation();
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    if (success) {
                        isSessionOpened = true;
                        requestStatus.setText(getString(R.string.requestSentWaiting));
                    } else {
                        Snackbar.make(requestStatus, getString(R.string.failedRequestLocation), Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                finish();
                            }
                        }).show();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                requestLocation();
            }
        });
    }


    private void destroySession(boolean finish) {
        isSessionOpened = false;
        Intent intent = new Intent(getApplicationContext(), LocationRequestSessionDestroyer.class);
        intent.putExtra("opponentId", mOpponentId);
        startService(intent);
        if (finish) {
            finish();
        }

    }

    private void showWarning() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.warning));
        builder.setMessage(getString(R.string.leavingSession));
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                destroySession(true);
            }
        });
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
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
                if (type.equals("showMessage")) {
                    RemoteMessage remoteMessage = extras.getParcelable("data");
                    Map<String, String> response = remoteMessage.getData();
                    if (mOpponentId.equals(response.get("user_id"))) {
                        mChatModel = new ChatModel(Boolean.valueOf(response.get("hasGif")), response.get("gifUrl"), response.get("message_id"), response.get("user_id"),
                                response.get("opponent_id"), StringEscapeUtils.unescapeJava(response.get("message")), true, Constants.STATUS_DELIVERED,
                                response.get("user_image"), response.get("opponent_image"), response.get("date"));
                        mChatModelArrayList.add(mChatModel);
                        mChatAdapter.notifyDataSetChanged();
                        mRecyclerView.post(new Runnable() {
                            @Override
                            public void run() {
                                scrollToBottom();
                            }
                        });
                    }
                } else if (type.equals("finish")) {
                    finish();
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

    @Override
    public void onBackPressed() {
        if (isSessionOpened) {
            showWarning();
        } else {
            super.onBackPressed();
            finish();
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

    private void scrollToBottom() {
        mRecyclerView.smoothScrollToPosition(mChatAdapter.getItemCount());
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mGoogleMap = googleMap;
        boolean isGranted = PermissionsChecker.isLocationPermissionGranted(this);
        if (isGranted) {
            getLastKnownLocation(googleMap);
        } else {
            requestPermission();
        }
        googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);
            }
        });
    }


    private void getAddress(final double latitude, final double longitude, final GeneralCallback generalCallback) {
        if (mWorkerThread != null) {
            mWorkerThread = null;
        }
        mWorkerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Geocoder geocoder;
                List<Address> addresses;
                geocoder = new Geocoder(Chat.this, Locale.getDefault());
                Looper mainLooper = Looper.getMainLooper();
                String strAdd = "";
                try {
                    addresses = geocoder.getFromLocation(latitude, longitude, 1);
                    if (addresses != null) {
                        Address returnedAddress = addresses.get(0);
                        StringBuilder strReturnedAddress = new StringBuilder("");

                        for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
                            strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                        }
                        strAdd = strReturnedAddress.toString();

                        Handler handler = new Handler(mainLooper);
                        final String finalStrAdd = strAdd;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                generalCallback.onSuccess(finalStrAdd);
                            }
                        });
                        mWorkerThread = null;
                    } else {
                        Handler handler = new Handler(mainLooper);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                generalCallback.onFailure(null);
                                mWorkerThread = null;
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Handler handler = new Handler(mainLooper);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            generalCallback.onFailure(null);
                            mWorkerThread = null;
                        }
                    });
                }


            }
        });
        mWorkerThread.start();
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_CODE);
    }

    public void getLastKnownLocation(GoogleMap googleMap) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            requestPermission();
            return;
        }
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastLocation.getLatitude(),
                    mLastLocation.getLongitude()), 20));
        }
        googleMap.setMyLocationEnabled(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_CODE: {
                if (PermissionsChecker.isLocationPermissionGranted(this)) {
                    getLastKnownLocation(mGoogleMap);
                } else {
                    Snackbar.make(sendMessageGifView, getString(R.string.permissionsRequired), Snackbar.LENGTH_LONG).show();
                }
            }

        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
