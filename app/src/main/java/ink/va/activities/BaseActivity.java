package ink.va.activities;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionMenu;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.ink.va.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import fab.FloatingActionButton;
import ink.StartupApplication;
import ink.va.interfaces.AccountDeleteListener;
import ink.va.utils.Retrofit;
import ink.va.utils.SharedHelper;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by USER on 2016-07-24.
 */
public abstract class BaseActivity extends AppCompatActivity {


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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.gc();
        sharedHelper = new SharedHelper(this);
        checkBan();
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
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                callToBanServer();
            }
        });
        thread.start();
    }

    private void callToBanServer() {
        Call<ResponseBody> banCall = Retrofit.getInstance().getInkService().checkBan(sharedHelper.getUserId());
        banCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    callToBanServer();
                    return;
                }
                if (response.body() == null) {
                    callToBanServer();
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean banned = jsonObject.optBoolean("banned");
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
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                callToBanServer();
            }
        });
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
}
