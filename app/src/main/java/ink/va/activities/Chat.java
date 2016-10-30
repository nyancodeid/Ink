package ink.va.activities;

import android.app.DownloadManager;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.danikula.videocache.HttpProxyCacheServer;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.ink.va.R;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.wang.avi.AVLoadingIndicatorView;

import org.apache.commons.lang3.StringEscapeUtils;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.StartupApplication;
import ink.va.adapters.ChatAdapter;
import ink.va.callbacks.GeneralCallback;
import ink.va.interfaces.ItemClickListener;
import ink.va.models.ChatModel;
import ink.va.models.MessageModel;
import ink.va.models.UserStatus;
import ink.va.utils.CircleTransform;
import ink.va.utils.Constants;
import ink.va.utils.DimDialog;
import ink.va.utils.FileUtils;
import ink.va.utils.Keyboard;
import ink.va.utils.Notification;
import ink.va.utils.PingHelper;
import ink.va.utils.ProgressRequestBody;
import ink.va.utils.QueHelper;
import ink.va.utils.RealmHelper;
import ink.va.utils.RecyclerTouchListener;
import ink.va.utils.Regex;
import ink.va.utils.Retrofit;
import ink.va.utils.SharedHelper;
import ink.va.utils.Time;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static ink.va.utils.Constants.REQUEST_CODE_CHOSE_STICKER;

public class Chat extends BaseActivity implements ProgressRequestBody.UploadCallbacks {

    private static final int PICK_FILE_REQUEST_CODE = 400;
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
    @Bind(R.id.single_video_view)
    VideoView singleVideoView;
    @Bind(R.id.singleGifViewLoading)
    AVLoadingIndicatorView singleGifViewLoading;
    @Bind(R.id.scrollDownChat)
    ImageView scrollDownChat;
    @Bind(R.id.locationSessionIcon)
    ImageView locationSessionIcon;
    @Bind(R.id.attachmentIcon)
    ImageView attachmentIcon;
    @Bind(R.id.messageFiledDivider)
    View messageFiledDivider;

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
    private Gson userStatusGson;
    private Animation fadeAnimation;
    private String firstName;
    private String lastName;
    private boolean isGifChosen = false;
    private String lasChosenGifName;
    private Gson gson;
    private Animation slideIn;
    private Animation slideOut;
    private boolean hasFriendCheckLoaded;
    private boolean isFriend;
    private ProgressDialog progressDialog;
    private boolean scrolledToBottom;
    private Menu chatMenuItem;
    private Toolbar chatToolbar;
    private Handler handler;
    private boolean isAnimated;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSharedHelper = new SharedHelper(this);
        handler = new Handler();
        if (mSharedHelper.getChatColor() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor(mSharedHelper.getChatColor())));
        } else {
            getWindow().setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.chat_vector_background));
        }
        setContentView(R.layout.activity_chat);
        chatToolbar = (Toolbar) findViewById(R.id.toolbarChat);
        setSupportActionBar(chatToolbar);

        fadeAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in_scale);

        ButterKnife.bind(this);
        userStatusGson = new Gson();

        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressDrawable(ContextCompat.getDrawable(this, R.drawable.progress_dialog_circle));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.setIndeterminateDrawable(ContextCompat.getDrawable(this, R.drawable.progress_dialog_circle));
        progressDialog.setTitle(getString(R.string.updatingMessages));
        progressDialog.setMessage(getString(R.string.updatingYourMessages));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);

        slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_and_rotate_in);
        slideOut = AnimationUtils.loadAnimation(this, R.anim.slide_and_rotate_out);


        Notification.get().setSendingRemote(false);


        mChatAdapter = new ChatAdapter(mChatModelArrayList, this);
        mRealHelper = RealmHelper.getInstance();

        configureChat(getIntent());


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


        scheduleTask();
        checkForActionBar();
        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(this, mRecyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                final ChatModel chatModel = mChatModelArrayList.get(position);

                if (chatModel.isClickable()) {
                    if (chatModel.hasSticker()) {
                        if (!chatModel.isAnimated()) {
                            Intent intent = new Intent(getApplicationContext(), FullscreenActivity.class);
                            intent.putExtra("link", Constants.MAIN_URL + chatModel.getStickerUrl());
                            startActivity(intent);
                        }

                    } else if (chatModel.isAttachment()) {
                        System.gc();
                        String finalFileName;
                        if (chatModel.getMessage().contains(":")) {
                            int index = chatModel.getMessage().indexOf(":");
                            finalFileName = chatModel.getMessage().substring(index + 1, chatModel.getMessage().length());
                        } else {
                            int index = chatModel.getMessage().indexOf(":");
                            finalFileName = chatModel.getMessage().substring(index + 1, chatModel.getMessage().length());
                        }

                        if (FileUtils.isImageType(chatModel.getMessage())) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(Chat.this);
                            builder.setTitle(getString(R.string.downloadQuestion));
                            builder.setMessage(getString(R.string.downloadTheFile) + " " + finalFileName);
                            builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });
                            builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    String downloadFileName = chatModel.getMessage();

                                    queDownload(downloadFileName, chatModel);
                                }
                            });
                            builder.setNeutralButton(getString(R.string.viewImage), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (chatModel == null) {
                                        Snackbar.make(sendMessageGifView, getString(R.string.pleaseWait), Snackbar.LENGTH_LONG).setAction("OK", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {

                                            }
                                        }).show();
                                    } else {
                                        Intent intent = new Intent(getApplicationContext(), FullscreenActivity.class);
                                        String encodedFileName = Uri.encode(chatModel.getMessage());
                                        intent.putExtra("link", Constants.MAIN_URL + Constants.UPLOADED_FILES_DIR + encodedFileName);
                                        startActivity(intent);
                                    }

                                }
                            });
                            builder.show();
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(Chat.this);
                            if (chatModel.getMessage().contains(":")) {
                                int index = chatModel.getMessage().indexOf(":");
                                finalFileName = chatModel.getMessage().substring(index + 1, chatModel.getMessage().length());
                            } else {
                                int index = chatModel.getMessage().indexOf(":");
                                finalFileName = chatModel.getMessage().substring(index + 1, chatModel.getMessage().length());
                            }
                            builder.setTitle(getString(R.string.downloadQuestion));
                            builder.setMessage(getString(R.string.downloadTheFile) + " " + finalFileName);
                            builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });
                            builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    String downloadFileName = chatModel.getMessage();

                                    queDownload(downloadFileName, chatModel);
                                }
                            });
                            builder.show();
                        }

                    }
                } else {
                    Snackbar.make(chatTitle, getString(R.string.waitTillSent), Snackbar.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onLongClick(View view, final int position) {
                System.gc();
                final ChatModel chatModel = mChatModelArrayList.get(position);
                String date = chatModel.getDate();
                AlertDialog.Builder builder = new AlertDialog.Builder(Chat.this);
                builder.setTitle("Message Details");
                builder.setPositiveButton(getString(R.string.close), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                if (mCurrentUserId.equals(chatModel.getUserId())) {
                    try {
                        calendar.setTime(sourceFormat.parse(date));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    String monthName = new SimpleDateFormat("MMMM").format(calendar.getTime());

                    String weekName = new SimpleDateFormat("EEEE").format(calendar.getTime());

                    builder.setMessage(getString(R.string.dateOfMessage) + calendar.get(Calendar.YEAR) + "," + weekName + "," + monthName
                            + "-" + (calendar.get(Calendar.DAY_OF_MONTH) < 10 ? "0" + calendar.get(Calendar.DAY_OF_MONTH) : calendar.get(Calendar.DAY_OF_MONTH))
                            + " (" + (calendar.get(Calendar.HOUR_OF_DAY) < 10 ? "0" + calendar.get(Calendar.HOUR_OF_DAY) : calendar.get(Calendar.HOUR_OF_DAY)) + ":"
                            + (calendar.get(Calendar.MINUTE) < 10 ? "0" + calendar.get(Calendar.MINUTE) : calendar.get(Calendar.MINUTE)) +
                            ":" + (calendar.get(Calendar.SECOND) < 10 ? "0" + calendar.get(Calendar.SECOND) : calendar.get(Calendar.SECOND)) + ")");

                    builder.setNegativeButton(getString(R.string.deleteMessage), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            showDeleteWarning(chatModel.getMessageId(), position);
                        }
                    });
                } else {

                    try {
                        calendar.setTime(sourceFormat.parse(Time.convertToLocalTime(date)));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    String monthName = new SimpleDateFormat("MMMM").format(calendar.getTime());

                    String weekName = new SimpleDateFormat("EEEE").format(calendar.getTime());

                    builder.setMessage(getString(R.string.dateOfMessage) + calendar.get(Calendar.YEAR) + "," + weekName + "," + monthName
                            + "-" + (calendar.get(Calendar.DAY_OF_MONTH) < 10 ? "0" + calendar.get(Calendar.DAY_OF_MONTH) : calendar.get(Calendar.DAY_OF_MONTH))
                            + " (" + (calendar.get(Calendar.HOUR_OF_DAY) < 10 ? "0" + calendar.get(Calendar.HOUR_OF_DAY) : calendar.get(Calendar.HOUR_OF_DAY)) + ":"
                            + (calendar.get(Calendar.MINUTE) < 10 ? "0" + calendar.get(Calendar.MINUTE) : calendar.get(Calendar.MINUTE)) +
                            ":" + (calendar.get(Calendar.SECOND) < 10 ? "0" + calendar.get(Calendar.SECOND) : calendar.get(Calendar.SECOND)) + ")");
                }


                builder.show();
            }
        }));

        mSendChatMessage.setEnabled(false);
        mWriteEditText.addTextChangedListener(chatTextWatcher);
        if (mSharedHelper.getChatFieldTextColor() != null) {
            mWriteEditText.setHintTextColor(Color.parseColor(mSharedHelper.getChatFieldTextColor()));
            mWriteEditText.setTextColor(Color.parseColor(mSharedHelper.getChatFieldTextColor()));
            messageFiledDivider.setBackgroundColor(Color.parseColor(mSharedHelper.getChatFieldTextColor()));
            attachmentIcon.setColorFilter(Color.parseColor(mSharedHelper.getChatFieldTextColor()), PorterDuff.Mode.SRC_ATOP);
        }

    }

    private void checkForActionBar() {
        if (mSharedHelper.getActionBarColor() != null) {
            chatToolbar.setBackgroundColor(Color.parseColor(mSharedHelper.getActionBarColor()));
        }
    }

    private void scheduleTask() {
        if (handler != null && statusRunnable != null) {
            handler.postDelayed(statusRunnable, 2000);
        }
    }

    private Runnable statusRunnable = new Runnable() {
        @Override
        public void run() {
            getStatus();
        }
    };

    private void queDownload(String fileName, ChatModel chatModel) {
        String finalFileName;
        if (chatModel.getMessage().contains(":")) {
            int index = chatModel.getMessage().indexOf(":");
            finalFileName = chatModel.getMessage().substring(index + 1, chatModel.getMessage().length());
        } else {
            int index = chatModel.getMessage().indexOf(":");
            finalFileName = chatModel.getMessage().substring(index + 1, chatModel.getMessage().length());
        }


        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(
                Uri.parse(Constants.MAIN_URL + Constants.UPLOADED_FILES_DIR + fileName));
        request.setTitle(finalFileName);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setVisibleInDownloadsUi(true);
        downloadManager.enqueue(request);

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

    @Override
    protected void onStart() {
        LocalBroadcastManager.getInstance(this).registerReceiver(generalReceiver, new IntentFilter(getPackageName() + ".Chat"));
        super.onStart();
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


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.chat_menu, menu);
//        chatMenuItem = menu;
//        if (mSharedHelper.isRainbowMessageActivated()) {
//            menu.getItem(0).setTitle(getString(R.string.removeRainbowEffect));
//        } else {
//            menu.getItem(0).setTitle(getString(R.string.showAsRainbow));
//        }
//        return super.onCreateOptionsMenu(menu);
//    }


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
                        final UserStatus userStatus = userStatusGson.fromJson(responseBody, UserStatus.class);
                        if (userStatus.success) {
                            SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            Date date = sourceFormat.parse(Time.convertToLocalTime(userStatus.lastSeenTime));
                            Period period = new Period(new DateTime(new Date()), new DateTime(date), PeriodType.dayTime());
                            int minutes = period.getMinutes();
                            int hours = period.getHours();
                            int days = period.getDays();
                            int month = period.getMonths();
                            int year = period.getYears();

                            if (userStatus.isOnline) {
                                statusColor.setVisibility(View.VISIBLE);
                                statusColor.startAnimation(fadeAnimation);
                                opponentStatus.setText(getString(R.string.onlineStatus));
                            } else {
                                statusColor.setVisibility(View.GONE);
                                statusColor.clearAnimation();


                                String appendableString = getString(R.string.minutes);
                                int lastTime = minutes;

                                if (year != 0) {
                                    appendableString = getString(R.string.years);
                                    lastTime = year;
                                } else if (month != 0) {
                                    appendableString = getString(R.string.month);
                                    lastTime = month;
                                } else if (days != 0) {
                                    appendableString = getString(R.string.days);
                                    lastTime = days;
                                } else if (hours != 0) {
                                    appendableString = getString(R.string.hours);
                                    lastTime = hours;
                                }

                                opponentStatus.setText(getString(R.string.lastSeen, Math.abs(lastTime) + " " + appendableString));
                            }
                            scheduleTask();
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

        ink.va.utils.PopupMenu.showPopUp(Chat.this, attachmentIcon, new ItemClickListener<MenuItem>() {
            @Override
            public void onItemClick(MenuItem clickedItem) {
                switch (clickedItem.getItemId()) {
                    case 0:
                        openStickerChooser();
                        break;
                    case 1:
                        openIntentPicker();
                        break;
                }
            }
        }, getString(R.string.sendSticker), getString(R.string.sendFile));

    }

    private void openIntentPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");      //all files
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), PICK_FILE_REQUEST_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a AlertDialogView
            Toast.makeText(this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
        }
    }

    private void openStickerChooser() {
        startActivityForResult(new Intent(getApplicationContext(), StickerChooserActivity.class), Constants.REQUEST_CODE_CHOSE_STICKER);
    }


    @OnClick(R.id.sendChatMessage)
    public void sendChatMessage() {
        if (mNoMessageLayout.getVisibility() == View.VISIBLE) {
            mNoMessageLayout.setVisibility(View.GONE);
        }
        String message = StringEscapeUtils.escapeJava(mWriteEditText.getText().toString().trim());
        dismissStickerChooser();
        ChatModel tempChat = new ChatModel(false, isGifChosen, lasChosenGifName, null, mCurrentUserId, mOpponentId, StringEscapeUtils.unescapeJava(message.trim()),
                false, Constants.STATUS_NOT_DELIVERED,
                mUserImage, mOpponentImage, "", isAnimated);
        mChatModelArrayList.add(tempChat);
        tempChat.setClickable(false);

        int itemLocation = mChatModelArrayList.indexOf(tempChat);

        attemptToQue(message.trim(), itemLocation, mDeleteOpponentId, mDeleteUserId, isGifChosen, lasChosenGifName, isAnimated);
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
            singleVideoView.setVisibility(View.GONE);
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
                              String deleteUserId, final boolean hasGif, final String gifUrl, boolean isAnimated) {
        RealmHelper.getInstance().insertMessage(mCurrentUserId, mOpponentId,
                message, "0", "",
                String.valueOf(itemLocation),
                Constants.STATUS_NOT_DELIVERED, mUserImage,
                mOpponentImage, deleteOpponentId, deleteUserId, hasGif, gifUrl, isAnimated);

        QueHelper queHelper = new QueHelper();
        queHelper.attachToQue(mOpponentId, message, itemLocation, isGifChosen, gifUrl, Chat.this, isAnimated);


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
                if (mNoMessageLayout.getVisibility() == View.VISIBLE) {
                    mNoMessageLayout.setVisibility(View.GONE);
                }

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

        mRealHelper.getMessages(mOpponentId, mCurrentUserId, new GeneralCallback<List<MessageModel>>() {
            @Override
            public void onSuccess(List<MessageModel> messageModels) {
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
                        boolean isAnimated = eachModel.isAnimated();
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

                        mChatModel = new ChatModel(Regex.isAttachment(message), isGifChosen, gifUrl, messageId, userId, opponentId, message, true,
                                eachModel.getDeliveryStatus(), userImage, opponentImage, date, isAnimated);
                        mChatModelArrayList.add(mChatModel);
                        if (eachModel.getDeliveryStatus().equals(Constants.STATUS_NOT_DELIVERED) && !Regex.isAttachment(message)) {
//                            int itemLocation = mChatModelArrayList.indexOf(mChatModel);
//                            attemptToQue(message, itemLocation,
//                                    deleteOpponentId, deleteUserId, isGifChosen, lasChosenGifName, isAnimated);
                            Snackbar.make(mRecyclerView, getString(R.string.unsent_message) + message, Snackbar.LENGTH_SHORT).show();
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
            public void onFailure(List<MessageModel> messageModels) {

            }
        });

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.showMessageAsRainbow:
                if (item.getTitle().equals(getString(R.string.showAsRainbow))) {
                    updateToRainbow();
                } else if (item.getTitle().equals(getString(R.string.removeRainbowEffect))) {
                    removeRainbow();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void removeRainbow() {
        mChatAdapter.setShowAsRainbow(false);
        mChatAdapter.notifyDataSetChanged();
        chatMenuItem.getItem(0).setTitle(getString(R.string.showAsRainbow));
    }

    private void updateToRainbow() {
        mChatAdapter.setShowAsRainbow(true);
        mChatAdapter.notifyDataSetChanged();
        if (DimDialog.isDialogAlive()) {
            DimDialog.hideDialog();
        }
        chatMenuItem.getItem(0).setTitle(getString(R.string.removeRainbowEffect));
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
        handler.removeCallbacks(statusRunnable);
        statusRunnable = null;
        handler = null;
        Notification.get().setSendingRemote(true);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(generalReceiver);
        super.onDestroy();
    }

    private BroadcastReceiver generalReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();

            if (extras != null) {
                String type = extras.getString("type");
                boolean canProceedToShow = true;

                switch (type) {
                    case "showMessage":
                        RemoteMessage remoteMessage = extras.getParcelable("data");
                        Map<String, String> response = remoteMessage.getData();
                        if (mOpponentId.equals(response.get("user_id"))) {
                            String messageIdToCompare = response.get("message_id");

                            for (ChatModel chatModel : mChatModelArrayList) {
                                String currentMessageId = chatModel.getMessageId();
                                if (messageIdToCompare.equals(currentMessageId)) {
                                    canProceedToShow = false;
                                    break;
                                }
                            }

                            if (canProceedToShow) {
                                mChatModel = new ChatModel(Regex.isAttachment(response.get("message")), Boolean.valueOf(response.get("hasGif")), response.get("gifUrl"), response.get("message_id"), response.get("user_id"),
                                        response.get("opponent_id"), StringEscapeUtils.unescapeJava(response.get("message")), true, Constants.STATUS_DELIVERED,
                                        response.get("user_image"), response.get("opponent_image"), response.get("date"), Boolean.valueOf(response.get("isAnimated")));
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
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(Integer.valueOf(mOpponentId));
        mSharedHelper.removeLastNotificationId(mOpponentId);
        Notification.get().setSendingRemote(false);
        Notification.get().setActiveOpponentId(mOpponentId);
        getStatus();
        getIsFriend();
        PingHelper.get().startPinging(mSharedHelper.getUserId());
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
    protected void onNewIntent(Intent intent) {
        configureChat(intent);
        super.onNewIntent(intent);
    }

    private void configureChat(Intent intent) {
        ActionBar actionBar = getSupportActionBar();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            firstName = bundle.getString("firstName");
            lastName = bundle.getString("lastName");
            mOpponentId = bundle.getString("opponentId");
            String opponentImage = bundle.getString("opponentImage");
            boolean isSocialAccount = bundle.getBoolean("isSocialAccount");
            if (bundle.containsKey("notificationId")) {
                mSharedHelper.removeLastNotificationId(bundle.getString("notificationId"));
            }

            if (opponentImage != null && !opponentImage.isEmpty()) {
                if (!isImageLoaded) {
                    isImageLoaded = true;
                    if (isSocialAccount) {
                        Ion.with(this).load(opponentImage).withBitmap().placeholder(R.drawable.no_background_image).transform(new CircleTransform()).intoImageView(this.opponentImage);
                    } else {
                        String encodedImage = Uri.encode(opponentImage);
                        Ion.with(this).load(Constants.MAIN_URL + Constants.USER_IMAGES_FOLDER +
                                encodedImage).withBitmap().placeholder(R.drawable.no_background_image).transform(new CircleTransform()).intoImageView(this.opponentImage);
                    }
                }
            } else {
                Ion.with(this).load(Constants.ANDROID_DRAWABLE_DIR + "no_image").withBitmap().transform(new CircleTransform()).intoImageView(this.opponentImage);
            }
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
        Notification.get().setSendingRemote(true);
        super.onPause();
    }


    private void scrollToBottom() {
        mRecyclerView.smoothScrollToPosition(mChatAdapter.getItemCount());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case REQUEST_CODE_CHOSE_STICKER:
                if (data != null) {
                    handleStickerChoose(data.getExtras().getBoolean(Constants.STICKER_IS_ANIMATED_EXTRA_KEY),
                            data.getExtras().getString(Constants.STICKER_URL_EXTRA_KEY));
                }

                break;
            default:
                handlePickedResult(data);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void handleStickerChoose(boolean isIntentStickerAnimated, String stickerUrl) {
        sendMessageGifViewWrapper.setVisibility(View.VISIBLE);
        singleGifViewLoading.setVisibility(View.VISIBLE);
        mSendChatMessage.setEnabled(true);
        if (isIntentStickerAnimated) {
            singleVideoView.setVisibility(View.VISIBLE);

            singleGifViewLoading.setVisibility(View.VISIBLE);
            sendMessageGifView.setVisibility(View.GONE);
            singleVideoView.setVisibility(View.VISIBLE);

            singleVideoView.setVisibility(View.VISIBLE);

            HttpProxyCacheServer proxy = StartupApplication.getProxy(this);
            String proxyUrl = proxy.getProxyUrl(Constants.MAIN_URL + stickerUrl);
            singleVideoView.setVideoPath(proxyUrl);


            singleVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.seekTo(1000);
                    singleVideoView.seekTo(1000);
                    singleGifViewLoading.setVisibility(View.GONE);
                }
            });

            singleVideoView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (!singleVideoView.isPlaying()) {
                        singleVideoView.setBackground(null);
                        singleVideoView.start();
                    }
                    return false;
                }
            });
        } else {
            singleVideoView.setZOrderOnTop(false);
            sendMessageGifView.setVisibility(View.VISIBLE);
            singleVideoView.setVisibility(View.GONE);
            Ion.with(getApplicationContext()).load(Constants.MAIN_URL + stickerUrl).intoImageView(sendMessageGifView).setCallback(new FutureCallback<ImageView>() {
                @Override
                public void onCompleted(Exception e, ImageView result) {
                    singleGifViewLoading.setVisibility(View.GONE);
                }
            });
        }
        lasChosenGifName = stickerUrl;
        isGifChosen = true;
        isAnimated = isIntentStickerAnimated;
    }


    private void handlePickedResult(Intent data) {
        if (data != null) {
            Uri uri = data.getData();
            // Get the path
            String path = null;
            AlertDialog.Builder fileErrorDialog = new AlertDialog.Builder(Chat.this);
            fileErrorDialog.setTitle(getString(R.string.fileError));
            fileErrorDialog.setMessage(getString(R.string.couldNotOpenFile));
            fileErrorDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            try {
                path = FileUtils.getPath(this, uri);
            } catch (URISyntaxException e) {
                e.printStackTrace();
                fileErrorDialog.show();
                return;
            }
            // Get the file instance
            // File file = new File(path);
            // Initiate the upload

            if (path != null) {
                File file = new File(path);
                if (!file.exists()) {
                    fileErrorDialog.show();
                    return;
                }
                if (file.length() > MakePost.MAX_FILE_SIZE) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Chat.this);
                    builder.setTitle(getString(R.string.sizeExceeded)).show();
                    builder.setMessage(getString(R.string.sizeExceededMessage));
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    builder.show();
                    return;
                }

                ChatModel tempChat = new ChatModel(true, false, "", null, mCurrentUserId, mOpponentId, "userid=" + mSharedHelper.getUserId() + ":" +
                        Constants.TYPE_MESSAGE_ATTACHMENT +
                        file.getName(),
                        false, Constants.STATUS_NOT_DELIVERED,
                        mUserImage, mOpponentImage, "", false);
                mChatModelArrayList.add(tempChat);
                tempChat.setClickable(false);

                int itemLocation = mChatModelArrayList.indexOf(tempChat);

                RealmHelper.getInstance().insertMessage(mCurrentUserId, mOpponentId,
                        "userid=" + mSharedHelper.getUserId() + ":" +
                                Constants.TYPE_MESSAGE_ATTACHMENT +
                                file.getName(), "0", "",
                        String.valueOf(itemLocation),
                        Constants.STATUS_NOT_DELIVERED, mUserImage,
                        mOpponentImage, mDeleteOpponentId, mDeleteUserId, false, "", false);

                sendMessageWithAttachment(file, itemLocation);

            } else {
                fileErrorDialog.show();
            }
        }
    }

    private void sendMessageWithAttachment(final File file, final int sentItemLocation) {
        System.gc();
        if (mNoMessageLayout.getVisibility() == View.VISIBLE) {
            mNoMessageLayout.setVisibility(View.GONE);
        }
        Map<String, ProgressRequestBody> map = new HashMap<>();
        ProgressRequestBody requestBody = new ProgressRequestBody(file, this);
        map.put("file\"; filename=\"" + file.getName() + "\"", requestBody);


        Call<ResponseBody> responseBodyCall = Retrofit.getInstance().getInkService().sendMessageWithAttachment(map, mSharedHelper.getUserId(), mOpponentId,
                "userid=" + mSharedHelper.getUserId() + ":" + Constants.TYPE_MESSAGE_ATTACHMENT + file.getName(), Time.getTimeZone(), false, "");
        responseBodyCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    sendMessageWithAttachment(file, sentItemLocation);
                    return;
                }
                if (response.body() == null) {
                    sendMessageWithAttachment(file, sentItemLocation);
                    return;
                }

                try {
                    String responseString = response.body().string();
                    handleMessageSent(responseString, Integer.valueOf(sentItemLocation));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    @Override
    public void onProgressUpdate(int percentage) {
        if (mChatAdapter != null) {
            if (percentage >= 99) {
                mChatAdapter.stopUpdate();
                mChatAdapter.notifyDataSetChanged();
                scrolledToBottom = false;
            }
            mChatAdapter.setUpdate(percentage);
            if (!scrolledToBottom) {
                scrollToBottom();
                scrolledToBottom = true;
            }
            mChatAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onError() {
        if (mChatAdapter != null) {
            mChatAdapter.stopUpdate();
            mChatAdapter.notifyDataSetChanged();
        }
        scrolledToBottom = false;
    }

    @Override
    public void onFinish() {
    }
}
