package ink.va.activities;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionMenu;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

import com.ink.va.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import fab.FloatingActionButton;
import ink.StartupApplication;
import ink.va.interfaces.AccountDeleteListener;
import ink.va.models.ServerInformationModel;
import ink.va.service.MessageService;
import ink.va.utils.Constants;
import ink.va.utils.DimDialog;
import ink.va.utils.Notification;
import ink.va.utils.ProcessManager;
import ink.va.utils.Retrofit;
import ink.va.utils.SharedHelper;
import ink.va.utils.Version;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static ink.va.utils.Constants.EVENT_PING;
import static ink.va.utils.Constants.KILL_APP_BUNDLE_KEY;
import static ink.va.utils.Constants.SERVER_NOTIFICATION_SHARED_KEY;
import static ink.va.utils.Constants.WARNING_TEXT_BUNDLE_KEY;


/**
 * Created by USER on 2016-07-24.
 */
public abstract class BaseActivity extends AppCompatActivity {


    private static final long SERVER_INFO_TIME = 300000;//5 minutes each server call
    private static final String COMPONENT_NAME_FULL_SCREEN_ACTIVITY = "ink.va.activities.FullscreenActivity";

    @Nullable
    @BindView(R.id.sendChatMessage)
    FloatingActionButton sendChatMessage;

    @Nullable
    @BindView(R.id.makePostToolbar)
    Toolbar makePostToolbar;


    @Nullable
    @BindView(R.id.chatRouletteSendMessage)
    FloatingActionButton chatRouletteSendMessage;

    @Nullable
    @BindView(R.id.replyMessage)
    FloatingActionButton replyMessage;

    @Nullable
    @BindView(R.id.sendGroupMessage)
    FloatingActionButton sendGroupMessage;

    @Nullable
    @BindView(R.id.profileFab)
    FloatingActionMenu profileFab;

    @Nullable
    @BindView(R.id.editImageNameFab)
    FloatingActionButton editImageNameFab;

    @Nullable
    @BindView(R.id.saveProfileEdits)
    FloatingActionButton saveProfileEdits;

    @Nullable
    @BindView(R.id.createGroup)
    FloatingActionButton createGroup;


    @Nullable
    @BindView(R.id.pickImageButton)
    FloatingActionButton pickImageButton;

    @Nullable
    @BindView(R.id.connectDisconnectButton)
    Button connectDisconnectButton;

    @Nullable
    @BindView(R.id.joinWaitRoom)
    Button joinWaitRoom;

    private AccountDeleteListener accountDeleteListener;
    private SharedHelper sharedHelper;
    private CountDownTimer countDownTimer;
    private Dialog vipLoadingDialog;
    private int appVersionCode = 0;
    private MessageService messageService;
    private Intent messageIntent;
    private boolean unbindCalled;
    private ScheduledExecutorService scheduler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (sharedHelper == null) {
            sharedHelper = new SharedHelper(this);
        }
        Notification.get().setAppAlive(true);
        initCountDownTimer();
        checkBan();
        checkHacks();
        if (vipLoadingDialog == null) {
            vipLoadingDialog = DimDialog.createVipLoadingDialog(this);
        }
        if (sharedHelper.getActionBarColor() != null) {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(sharedHelper.getActionBarColor())));
            }
        }

        if (sharedHelper.getStatusBarColor() != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.parseColor(sharedHelper.getStatusBarColor()));
            }
        }
        messageIntent = new Intent(this, MessageService.class);
        if (messageService == null) {
            bindService(messageIntent, mConnection, BIND_AUTO_CREATE);
        }
        scheduleTask();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService();
        destroyScheduler();
    }

    private void checkHacks() {
        if (ProcessManager.hasHacks(this)) {
            AlertDialog alertDialog;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder.setTitle(getString(R.string.error));
            builder.setMessage(getString(R.string.hack_engine_detected));
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            alertDialog = builder.show();
            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                    System.exit(0);
                }
            });

        }
    }


    protected void setStatusBarColor(@ColorRes int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, color));
        }
    }

    protected void setActionBarColor(@ColorRes int color) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, color)));
        }

    }

    private void initCountDownTimer() {
        countDownTimer = new CountDownTimer(SERVER_INFO_TIME, 1000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                checkBan();
            }
        };
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            MessageService.LocalBinder binder = (MessageService.LocalBinder) service;
            messageService = binder.getService();
            BaseActivity.this.onServiceConnected(messageService);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            if (!unbindCalled) {
                bindService(messageIntent, mConnection, BIND_AUTO_CREATE);
            }

        }
    };

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.bind(this);
        if (sharedHelper.getActionBarColor() != null) {
            if (makePostToolbar != null) {
                makePostToolbar.setBackgroundColor(Color.parseColor(sharedHelper.getActionBarColor()));
            }
        }
        if (sharedHelper.getSendButtonColor() != null) {
            if (sendChatMessage != null) {
                sendChatMessage.setColorNormal(Color.parseColor(sharedHelper.getSendButtonColor()));
                sendChatMessage.setColorPressed(Color.parseColor(sharedHelper.getSendButtonColor()));
            }
            if (chatRouletteSendMessage != null) {
                chatRouletteSendMessage.setColorNormal(Color.parseColor(sharedHelper.getSendButtonColor()));
                chatRouletteSendMessage.setColorPressed(Color.parseColor(sharedHelper.getSendButtonColor()));
            }
            if (sendGroupMessage != null) {
                sendGroupMessage.setColorNormal(Color.parseColor(sharedHelper.getSendButtonColor()));
                sendGroupMessage.setColorPressed(Color.parseColor(sharedHelper.getSendButtonColor()));
            }
            if (replyMessage != null) {
                replyMessage.setColorNormal(Color.parseColor(sharedHelper.getSendButtonColor()));
            }
            if (pickImageButton != null) {
                pickImageButton.setColorNormal(Color.parseColor(sharedHelper.getSendButtonColor()));
                pickImageButton.setColorPressed(Color.parseColor(sharedHelper.getSendButtonColor()));
            }
        }
        if (sharedHelper.getMenuButtonColor() != null) {
            if (profileFab != null) {
                profileFab.setMenuButtonColorNormal(Color.parseColor(sharedHelper.getMenuButtonColor()));
                profileFab.setMenuButtonColorPressed(Color.parseColor(sharedHelper.getMenuButtonColor()));
            }
            if (editImageNameFab != null) {
                editImageNameFab.setColorNormal(Color.parseColor(sharedHelper.getMenuButtonColor()));
                editImageNameFab.setColorPressed(Color.parseColor(sharedHelper.getMenuButtonColor()));
                saveProfileEdits.setColorNormal(Color.parseColor(sharedHelper.getMenuButtonColor()));
                saveProfileEdits.setColorPressed(Color.parseColor(sharedHelper.getMenuButtonColor()));
            }
            if (createGroup != null) {
                createGroup.setColorNormal(Color.parseColor(sharedHelper.getMenuButtonColor()));
                createGroup.setColorPressed(Color.parseColor(sharedHelper.getMenuButtonColor()));
            }
            if (connectDisconnectButton != null) {
                connectDisconnectButton.setTextColor(Color.parseColor(sharedHelper.getMenuButtonColor()));
            }
            if (joinWaitRoom != null) {
                joinWaitRoom.getBackground().setColorFilter(Color.parseColor(sharedHelper.getMenuButtonColor()),
                        PorterDuff.Mode.SRC_ATOP);
            }
        }

    }

    protected void setOnAccountDeleteListener(AccountDeleteListener accountDeleteListener) {
        this.accountDeleteListener = accountDeleteListener;
    }

    protected void fireAccountDeleteListener() {
        if (accountDeleteListener != null) {
            accountDeleteListener.onAccountDeleted();
        }
    }

    private void checkBan() {
        countDownTimer.cancel();
        countDownTimer.start();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                callToBanServer();
            }
        });
        thread.start();
    }

    private void callToBanServer() {
        appVersionCode = Version.getVersionCode(this);
        Call<ServerInformationModel> banCall = Retrofit.getInstance().getInkService().checkBan(sharedHelper.getUserId(), appVersionCode);
        banCall.enqueue(new Callback<ServerInformationModel>() {
            @Override
            public void onResponse(Call<ServerInformationModel> call, Response<ServerInformationModel> response) {
                ServerInformationModel serverInformationModel = response.body();
                boolean banned;
                if (serverInformationModel != null) {
                    banned = serverInformationModel.isBanned();
                } else {
                    callToBanServer();
                    return;
                }


                if (banned) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(((StartupApplication) getApplicationContext()).getCurrentActivity());
                    builder.setTitle(getString(R.string.ban_title));
                    builder.setMessage(getString(R.string.ban_message));
                    builder.setCancelable(false);
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                            System.exit(0);

                        }
                    });
                    builder.show();
                } else {
                    if (serverInformationModel.isOdlAppSupport()) {
                        proceedToShow(serverInformationModel, false);
                    } else {
                        int serverAppVersion = serverInformationModel.getServerAppVersion();
                        if (appVersionCode < serverAppVersion) {
                            proceedToShow(serverInformationModel, true);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ServerInformationModel> call, Throwable t) {
                callToBanServer();
            }
        });
    }

    private void proceedToShow(ServerInformationModel serverInformationModel, boolean killApp) {
        if (serverInformationModel.HasContent()) {
            String content = serverInformationModel.getContent();
            boolean singleLoad = serverInformationModel.isSingleLoad();
            String newsId = serverInformationModel.getNewsId();

            if (singleLoad) {
                if (!sharedHelper.hasShownServerNews(newsId)) {
                    Map<String, ?> keys = sharedHelper.getAllSharedPrefs();

                    for (Map.Entry<String, ?> entry : keys.entrySet()) {
                        String singleKey = entry.getKey();
                        if (singleKey.startsWith(SERVER_NOTIFICATION_SHARED_KEY)) {
                            sharedHelper.removeObject(singleKey);
                        }
                    }

                    sharedHelper.putShownServerNews(newsId);
                    Intent intent = new Intent(getApplicationContext(), ServerNotification.class);
                    intent.putExtra(Constants.SERVER_NOTIFICATION_CONTENT_BUNDLE_KEY, content);
                    intent.putExtra(KILL_APP_BUNDLE_KEY, killApp);
                    intent.putExtra(WARNING_TEXT_BUNDLE_KEY, serverInformationModel.getWarningText());
                    startActivity(intent);
                    overridePendingTransition(R.anim.activity_scale_up, R.anim.activity_scale_down);
                }
            } else {
                if (sharedHelper.showServerNewsOnStartup()) {
                    Intent intent = new Intent(getApplicationContext(), ServerNotification.class);
                    intent.putExtra(Constants.SERVER_NOTIFICATION_CONTENT_BUNDLE_KEY, content);
                    intent.putExtra(KILL_APP_BUNDLE_KEY, killApp);
                    intent.putExtra(WARNING_TEXT_BUNDLE_KEY, serverInformationModel.getWarningText());
                    startActivity(intent);
                    overridePendingTransition(R.anim.activity_scale_up, R.anim.activity_scale_down);
                    sharedHelper.putServerNewsOnStartup(false);
                }

            }
        }
    }

    protected void hideActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    public void overrideActivityAnimation() {
        overridePendingTransition(R.anim.activity_scale_up, R.anim.activity_scale_down);
    }

    protected boolean isSocialAccountRegistered() {
        return sharedHelper.isRegistered();
    }

    protected boolean isSocialAccount() {
        return sharedHelper.isSocialAccount();
    }

    protected boolean isAccountRecoverable() {
        return sharedHelper.isAccountRecoverable();
    }

    protected void showVipLoading() {
        final ImageView imageView = (ImageView) vipLoadingDialog.findViewById(R.id.vip_place_holder_image);
        Animation pulseAnimation = AnimationUtils.loadAnimation(this, R.anim.pulse_animation);
        imageView.startAnimation(pulseAnimation);
        vipLoadingDialog.show();
    }

    protected void hideVipLoading() {
        vipLoadingDialog.dismiss();
    }

    public void onServiceConnected(MessageService messageService) {

    }

    protected void unbindService() {
        unbindCalled = true;
        try {
            unbindService(mConnection);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void ping() {
        JSONObject pingJson = new JSONObject();
        try {
            pingJson.put("currentUserId", sharedHelper.getUserId());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (messageService != null) {
            messageService.emit(EVENT_PING, pingJson);
        }
    }

    private void scheduleTask() {
        if (scheduler == null || !scheduler.isTerminated() & !scheduler.isShutdown()) {
            scheduler =
                    Executors.newSingleThreadScheduledExecutor();

            scheduler.scheduleAtFixedRate
                    (new Runnable() {
                        public void run() {
                            ping();
                        }
                    }, 0, 30, TimeUnit.SECONDS);
        }
    }

    public void destroyScheduler() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }


    private void cacheUserData() {
        final Call<ResponseBody> myDataResponse = Retrofit.getInstance()
                .getInkService().getSingleUserDetails(sharedHelper.getUserId(), "");
        myDataResponse.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    cacheUserData();
                    return;
                }
                if (response.body() == null) {
                    cacheUserData();
                    return;
                }
                fetchData(response);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    @Override
    public void startActivity(Intent intent) {
        if (intent.getComponent() != null) {
            if (intent.getComponent().getClassName().equals(COMPONENT_NAME_FULL_SCREEN_ACTIVITY)) {
                super.startActivity(intent);
                overridePendingTransition(R.anim.slide_up_calm, R.anim.slide_down_slow);
            } else {
                super.startActivity(intent);
            }
        } else {
            super.startActivity(intent);
        }
    }

    private void fetchData(Response<ResponseBody> response) {
        try {
            String responseString = response.body().string();
            try {
                JSONObject jsonObject = new JSONObject(responseString);
                boolean success = jsonObject.optBoolean("success");
                if (success) {
                    String userId = jsonObject.optString("userId");
                    String mFirstNameToSend = jsonObject.optString("first_name");
                    String mLastNameToSend = jsonObject.optString("last_name");
                    String mGenderToSend = jsonObject.optString("gender");
                    String mBadgeName = jsonObject.optString("badge_name");
                    String mPhoneNumberToSend = jsonObject.optString("phone_number");
                    String mFacebookProfileToSend = jsonObject.optString("facebook_profile");
                    String mFacebookName = jsonObject.optString("facebook_name");
                    String mImageLinkToSend = jsonObject.optString("image_link");
                    String mSkypeToSend = jsonObject.optString("skype");
                    String mAddressToSend = jsonObject.optString("address");
                    String mRelationshipToSend = jsonObject.optString("relationship");
                    String mStatusToSend = jsonObject.optString("status");
                    boolean isIncognito = jsonObject.optBoolean("isIncognito");
                    boolean isIncognitoBought = jsonObject.optBoolean("isIncognitoBought");
                    boolean isHidden = jsonObject.optBoolean("isHidden");
                    boolean isHiddenBought = jsonObject.optBoolean("isHiddenBought");
                    int incognitoCost = jsonObject.optInt("incognitoCost");
                    int hiddenProfileCost = jsonObject.optInt("hiddenProfileCost");
                    int userCoins = jsonObject.optInt("userCoins");

                    sharedHelper.putFirstName(mFirstNameToSend);
                    sharedHelper.putLastName(mLastNameToSend);
                    sharedHelper.putUserGender(mGenderToSend);
                    sharedHelper.putUserPhoneNumber(mPhoneNumberToSend);
                    sharedHelper.putUserFacebookLink(mFacebookProfileToSend);
                    sharedHelper.putUserFacebookName(mFacebookName);
                    sharedHelper.putUserSkype(mSkypeToSend);
                    sharedHelper.putUserAddress(mAddressToSend);
                    sharedHelper.putUserRelationship(mRelationshipToSend);
                    sharedHelper.putUserStatus(mStatusToSend);
                    sharedHelper.putShouldLoadImage(true);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
