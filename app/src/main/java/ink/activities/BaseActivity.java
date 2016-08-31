package ink.activities;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionMenu;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.ink.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import fab.FloatingActionButton;
import ink.interfaces.AccountDeleteListener;
import ink.utils.SharedHelper;

/**
 * Created by USER on 2016-07-24.
 */
public abstract class BaseActivity extends AppCompatActivity {


    @Nullable
    @Bind(R.id.sendChatMessage)
    FloatingActionButton sendChatMessage;

    @Nullable
    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Nullable
    @Bind(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;

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
    FloatingActionButton connectDisconnectButton;

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

        if (sharedHelper.getActionBarColor() != null) {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(sharedHelper.getActionBarColor())));
            }
            if (mToolbar != null) {
                mToolbar.setBackgroundColor(Color.parseColor(sharedHelper.getActionBarColor()));
            }
            if (collapsingToolbarLayout != null) {
                collapsingToolbarLayout.setBackgroundColor(Color.parseColor(sharedHelper.getActionBarColor()));
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
                connectDisconnectButton.setColorNormal(Color.parseColor(sharedHelper.getMenuButtonColor()));
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


    protected boolean isSocialAccountRegistered() {
        return sharedHelper.isRegistered();
    }

    protected boolean isSocialAccount() {
        return sharedHelper.isSocialAccount();
    }

}
