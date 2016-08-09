package ink.activities;

import android.app.Activity;
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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.ink.R;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import org.apache.commons.lang3.StringEscapeUtils;
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
import ink.callbacks.QueCallback;
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
    @Bind(R.id.makeCall)
    RelativeLayout makeCall;
    @Bind(R.id.sendMessageGifView)
    ImageView sendMessageGifView;
    @Bind(R.id.sendMessageGifViewWrapper)
    RelativeLayout sendMessageGifViewWrapper;
    @Bind(R.id.singleGifViewLoading)
    AVLoadingIndicatorView singleGifViewLoading;

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
        Notification.getInstance().setSendingRemote(false);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(getPackageName() + ".Chat"));
        mChatAdapter = new ChatAdapter(mChatModelArrayList, this);
        mRealHelper = RealmHelper.getInstance();
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
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(mRecyclerView.getWindowToken(), 0);
                }

                LinearLayoutManager layoutManager = ((LinearLayoutManager) mRecyclerView.getLayoutManager());
                int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
                Log.d("fasfsafsafasfsa", "onScrollStateChanged: " + firstVisiblePosition);
            }
        });


        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(this, mRecyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
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

    private void getStatus() {
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
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                getStatus();
            }
        });
    }

    @OnClick(R.id.makeCall)
    public void makeCall() {


    }

    @OnClick(R.id.trashIcon)
    public void trashIcon() {
        isGifChosen = false;
        System.gc();
        sendMessageGifView.setBackground(null);
        sendMessageGifView.setImageResource(0);
        sendMessageGifViewWrapper.setVisibility(View.GONE);
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
        getUserGifs();
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

    private void getUserGifs() {
        Call<ResponseBody> gifCall = Retrofit.getInstance().getInkService().getUserGifs(mSharedHelper.getUserId(),
                Constants.SERVER_AUTH_KEY);
        gifCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    getUserGifs();
                    return;
                }
                if (response.body() == null) {
                    getUserGifs();
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    GifResponse gifResponse = gson.fromJson(responseBody, GifResponse.class);
                    if (gifResponse.success) {
                        if (!gifResponse.cause.equals(ErrorCause.NO_GIFS)) {
                            ArrayList<GifResponseModel> gifResponseModels = gifResponse.gifResponseModels;
                            for (int i = 0; i < gifResponseModels.size(); i++) {
                                GifResponseModel eachModel = gifResponseModels.get(i);
                                gifModel = new GifModel(eachModel.id, eachModel.userId, eachModel.gifName, eachModel.isAnimated, eachModel.hasSound);
                                gifModelList.add(gifModel);
                                gifAdapter.notifyDataSetChanged();
                            }
                        } else {
                            showNoGifSnackbar();
                        }
                    } else {

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                getUserGifs();
            }
        });
    }

    private void showNoGifSnackbar() {
        Snackbar.make(opponentImage, getString(R.string.noGif), Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (gifChooserDialog != null) {
                    System.gc();
                    gifChooserDialog.dismiss();
                }
            }
        }).show();
    }

    @OnClick(R.id.sendChatMessage)
    public void sendChatMessage() {
        if (mNoMessageLayout.getVisibility() == View.VISIBLE) {
            mNoMessageLayout.setVisibility(View.GONE);
        }
        String message = StringEscapeUtils.escapeJava(mWriteEditText.getText().toString().trim());

        ChatModel tempChat = new ChatModel(isGifChosen, lasChosenGifName, null, mCurrentUserId, mOpponentId, StringEscapeUtils.unescapeJava(message.trim()),
                false, Constants.STATUS_NOT_DELIVERED,
                mUserImage, mOpponentImage, "");
        mChatModelArrayList.add(tempChat);
        int itemLocation = mChatModelArrayList.indexOf(tempChat);

        attemptToQue(message.trim(), itemLocation, mDeleteOpponentId, mDeleteUserId);
        mChatAdapter.notifyDataSetChanged();


        mWriteEditText.setText("");
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                // Call smooth scroll
                scrollToBottom();
            }
        });


    }


    @OnClick(R.id.opponentImage)
    public void opponentImage() {
        Intent intent = new Intent(getApplicationContext(), OpponentProfile.class);
        intent.putExtra("id", mOpponentId);
        intent.putExtra("firstName", firstName);
        intent.putExtra("lastName", lastName);
        intent.putExtra("disableButton", true);
        startActivity(intent);
    }

    private void attemptToQue(String message, int itemLocation, String deleteOpponentId,
                              String deleteUserId) {
        RealmHelper.getInstance().insertMessage(mCurrentUserId, mOpponentId,
                message, "0", "",
                String.valueOf(itemLocation),
                Constants.STATUS_NOT_DELIVERED, mUserImage,
                mOpponentImage, deleteOpponentId, deleteUserId);

        QueHelper queHelper = new QueHelper();
        queHelper.attachToQue(mCurrentUserId, mOpponentId, message, itemLocation,
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
                        failedHelperInstance.attachToQue(mCurrentUserId, mOpponentId, failedMessage, failedItemLocation, this);
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

                String deleteUserId = eachModel.getDeleteUserId();
                String deleteOpponentId = eachModel.getDeleteOpponentId();
                mDeleteOpponentId = eachModel.getDeleteOpponentId();
                mDeleteUserId = eachModel.getDeleteUserId();

                if (deleteOpponentId != null && deleteUserId != null) {
                    if (deleteOpponentId.equals(mSharedHelper.getUserId()) || deleteUserId.equals(mSharedHelper.getUserId())) {
                        continue;
                    }
                }

                mChatModel = new ChatModel(isGifChosen, lasChosenGifName, messageId, userId, opponentId, message, true,
                        eachModel.getDeliveryStatus(), userImage, opponentImage, date);
                mChatModelArrayList.add(mChatModel);
                if (eachModel.getDeliveryStatus().equals(Constants.STATUS_NOT_DELIVERED)) {
                    int itemLocation = mChatModelArrayList.indexOf(mChatModel);
                    attemptToQue(message, itemLocation,
                            deleteOpponentId, deleteUserId);
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
        Notification.getInstance().setSendingRemote(true);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        Ion.getDefault(getApplicationContext()).getCache().clear();
        Ion.getDefault(getApplicationContext()).configure().getResponseCache().clear();
        super.onDestroy();
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                RemoteMessage remoteMessage = extras.getParcelable("data");
                Map<String, String> response = remoteMessage.getData();

                if (mOpponentId.equals(response.get("user_id"))) {
                    // TODO: 8/9/2016  change the variables to real ones.
                    mChatModel = new ChatModel(false, null, response.get("message_id"), response.get("user_id"),
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

            }
        }
    };

    @Override
    protected void onResume() {
        Notification.getInstance().setSendingRemote(false);
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
                        Picasso.with(this).load(opponentImage).error(R.drawable.image_laoding_error)
                                .placeholder(R.drawable.no_image_yet_state).transform(new CircleTransform()).fit()
                                .centerCrop().into(this.opponentImage);
                    } else {
                        Picasso.with(this).load(Constants.MAIN_URL + Constants.USER_IMAGES_FOLDER +
                                opponentImage).error(R.drawable.image_laoding_error)
                                .placeholder(R.drawable.no_image_yet_state).transform(new CircleTransform()).fit()
                                .centerCrop().into(this.opponentImage);
                    }
                }
            } else {
                Picasso.with(this).load(R.drawable.no_image).transform(new CircleTransform()).fit()
                        .centerCrop().into(this.opponentImage);
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

        super.onResume();
    }


    @Override
    protected void onPause() {
        Notification.getInstance().setSendingRemote(true);
        super.onPause();
    }

    @Override
    public void OnItemClicked(int position) {
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

    private void scrollToBottom() {
        mRecyclerView.smoothScrollToPosition(mChatAdapter.getItemCount());
    }
}
