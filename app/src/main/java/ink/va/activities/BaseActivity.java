package ink.va.activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionMenu;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

import com.ink.va.R;

import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import fab.FloatingActionButton;
import ink.StartupApplication;
import ink.va.interfaces.AccountDeleteListener;
import ink.va.models.ServerInformationModel;
import ink.va.utils.Constants;
import ink.va.utils.DimDialog;
import ink.va.utils.Retrofit;
import ink.va.utils.SharedHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static ink.va.utils.Constants.SERVER_NOTIFICATION_SHARED_KEY;


/**
 * Created by USER on 2016-07-24.
 */
public abstract class BaseActivity extends AppCompatActivity {


    private static final long SERVER_INFO_TIME = 300000;//5 minutes each server call
    @Nullable
    @Bind(R.id.sendChatMessage)
    FloatingActionButton sendChatMessage;

    @Nullable
    @Bind(R.id.makePostToolbar)
    Toolbar makePostToolbar;


    @Nullable
    @Bind(R.id.chatRouletteSendMessage)
    FloatingActionButton chatRouletteSendMessage;

    @Nullable
    @Bind(R.id.replyMessage)
    FloatingActionButton replyMessage;

    @Nullable
    @Bind(R.id.sendGroupMessage)
    FloatingActionButton sendGroupMessage;

    @Nullable
    @Bind(R.id.profileFab)
    FloatingActionMenu profileFab;

    @Nullable
    @Bind(R.id.editImageNameFab)
    FloatingActionButton editImageNameFab;

    @Nullable
    @Bind(R.id.createGroup)
    FloatingActionButton createGroup;

    @Nullable
    @Bind(R.id.sendFeedbackButton)
    FloatingActionButton sendFeedbackButton;

    @Nullable
    @Bind(R.id.sendIssueButton)
    FloatingActionButton sendIssueButton;

    @Nullable
    @Bind(R.id.pickImageButton)
    FloatingActionButton pickImageButton;

    @Nullable
    @Bind(R.id.connectDisconnectButton)
    Button connectDisconnectButton;

    @Nullable
    @Bind(R.id.joinWaitRoom)
    Button joinWaitRoom;

    private AccountDeleteListener accountDeleteListener;
    private SharedHelper sharedHelper;
    private CountDownTimer countDownTimer;
    private Dialog vipLoadingDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.gc();
        sharedHelper = new SharedHelper(this);
        initCountDownTimer();
        checkBan();
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
            }
            if (chatRouletteSendMessage != null) {
                chatRouletteSendMessage.setColorNormal(Color.parseColor(sharedHelper.getSendButtonColor()));
            }
            if (sendGroupMessage != null) {
                sendChatMessage.setColorNormal(Color.parseColor(sharedHelper.getSendButtonColor()));
            }
            if (replyMessage != null) {
                replyMessage.setColorNormal(Color.parseColor(sharedHelper.getSendButtonColor()));
            }
            if (sendFeedbackButton != null) {
                sendFeedbackButton.setColorNormal(Color.parseColor(sharedHelper.getSendButtonColor()));
            }
            if (sendIssueButton != null) {
                sendIssueButton.setColorNormal(Color.parseColor(sharedHelper.getSendButtonColor()));
            }
            if (pickImageButton != null) {
                pickImageButton.setColorNormal(Color.parseColor(sharedHelper.getSendButtonColor()));
            }
        }
        if (sharedHelper.getMenuButtonColor() != null) {
            if (profileFab != null) {
                profileFab.setMenuButtonColorNormal(Color.parseColor(sharedHelper.getMenuButtonColor()));
            }
            if (editImageNameFab != null) {
                editImageNameFab.setColorNormal(Color.parseColor(sharedHelper.getMenuButtonColor()));
            }
            if (createGroup != null) {
                createGroup.setColorNormal(Color.parseColor(sharedHelper.getMenuButtonColor()));
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
        Call<ServerInformationModel> banCall = Retrofit.getInstance().getInkService().checkBan(sharedHelper.getUserId());
        banCall.enqueue(new Callback<ServerInformationModel>() {
            @Override
            public void onResponse(Call<ServerInformationModel> call, Response<ServerInformationModel> response) {
                ServerInformationModel serverInformationModel = response.body();

                boolean banned = serverInformationModel.isBanned();

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
                                startActivity(intent);
                                overridePendingTransition(R.anim.activity_scale_up, R.anim.activity_scale_down);
                            }
                        } else {
                            if (sharedHelper.showServerNewsOnStartup()) {
                                Intent intent = new Intent(getApplicationContext(), ServerNotification.class);
                                intent.putExtra(Constants.SERVER_NOTIFICATION_CONTENT_BUNDLE_KEY, content);
                                startActivity(intent);
                                overridePendingTransition(R.anim.activity_scale_up, R.anim.activity_scale_down);
                                sharedHelper.putServerNewsOnStartup(false);
                            }

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
}
