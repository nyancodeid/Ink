package ink.activities;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ink.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.callbacks.GeneralCallback;
import ink.utils.SharedHelper;
import yuku.ambilwarna.AmbilWarnaDialog;

/**
 * Created by USER on 2016-08-30.
 */

public class CustomizeLook extends AppCompatActivity {

    @Bind(R.id.customizeToolbar)
    Toolbar customizeToolbar;


    @Bind(R.id.customizeFabMenuText)
    TextView customizeFabMenuText;

    @Bind(R.id.pickNotificationIconColorText)
    TextView pickNotificationIconColorText;

    @Bind(R.id.pickShopIconColorText)
    TextView pickShopIconColorText;

    @Bind(R.id.pickLeftDrawerColorText)
    TextView pickLeftDrawerColorText;

    @Bind(R.id.postBackgroundColorText)
    TextView postBackgroundColorText;

    @Bind(R.id.friendsBackgroundColorText)
    TextView friendsBackgroundColorText;

    @Bind(R.id.messagesBackgroundColorText)
    TextView messagesBackgroundColorText;

    @Bind(R.id.chatBackgroundColorText)
    TextView chatBackgroundColorText;

    @Bind(R.id.requestsBackgroundColorText)
    TextView requestsBackgroundColorText;

    @Bind(R.id.opponentBubbleIcon)
    ImageView opponentBubbleIcon;

    @Bind(R.id.ownBubbleIcon)
    ImageView ownBubbleIcon;

    @Bind(R.id.notificationIcon)
    ImageView notificationIcon;

    @Bind(R.id.shopIcon)
    ImageView shopIcon;

    @Bind(R.id.ownBubbleCleaner)
    ImageView ownBubbleCleaner;

    @Bind(R.id.opponentBubbleCleaner)
    ImageView opponentBubbleCleaner;

    @Bind(R.id.statusBarCleaner)
    ImageView statusBarCleaner;

    //to find

    @Bind(R.id.fabButtonCleaner)
    ImageView fabButtonCleaner;

    @Bind(R.id.notificationCleaner)
    ImageView notificationCleaner;

    @Bind(R.id.shopCleaner)
    ImageView shopCleaner;

    @Bind(R.id.leftPanelCleaner)
    ImageView leftPanelCleaner;

    @Bind(R.id.feedCleaner)
    ImageView feedCleaner;

    @Bind(R.id.friendsCleaner)
    ImageView friendsCleaner;

    @Bind(R.id.messagesCleaner)
    ImageView messagesCleaner;

    @Bind(R.id.chatCleaner)
    ImageView chatCleaner;

    @Bind(R.id.requestCleaner)
    ImageView requestCleaner;

    private boolean statusBarColorPicked;
    private boolean fabMenuButtonColorPicked;
    private boolean pickNotificationIconColorPicked;
    private boolean pickShopIconColorPicked;
    private boolean pickLeftDrawerColorPicked;
    private boolean postBackgroundColorPicked;
    private boolean messagesBackgroundColorPicked;
    private boolean friendsBackgroundColorPicked;
    private boolean chatBackgroundColorPicked;
    private boolean requestBackgroundColorPicked;
    private boolean opponentBubbleColorPicked;
    private boolean ownBubbleColorPicked;

    private SharedHelper sharedHelper;
    private String oldStatusBarColor;
    private String oldMenuButtonColor;
    private String oldNotificationIconColor;
    private String oldShopIconColo;
    private String oldLeftSlidingPanelHeaderColor;
    private String oldFeedColor;
    private String oldFriendsColor;
    private String oldMessagesColor;
    private String oldChatColor;
    private String oldMyRequestColor;
    private String oldOwnBubbleColor;
    private String oldOpponentBubbleColor;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customize_look_view);
        ButterKnife.bind(this);
        customizeToolbar = (Toolbar) findViewById(R.id.customizeToolbar);
        setSupportActionBar(customizeToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(R.string.hereYouWillSeeActionBar));
        }
        oldOwnBubbleColor = sharedHelper.getOwnBubbleColor();
        oldOpponentBubbleColor = sharedHelper.getOpponentBubbleColor();
        oldStatusBarColor = sharedHelper.getStatusBarColor();
        oldMenuButtonColor = sharedHelper.getMenuButtonColor();
        oldNotificationIconColor = sharedHelper.getNotificationIconColor();
        oldShopIconColo = sharedHelper.getShopIconColor();
        oldLeftSlidingPanelHeaderColor = sharedHelper.getLeftSlidingPanelHeaderColor();
        oldFeedColor = sharedHelper.getFeedColor();
        oldFriendsColor = sharedHelper.getFriendsColor();
        oldMessagesColor = sharedHelper.getMessagesColor();
        oldChatColor = sharedHelper.getChatColor();
        oldMyRequestColor = sharedHelper.getMyRequestColor();


        sharedHelper = new SharedHelper(this);
        if (sharedHelper.getOwnBubbleColor() != null) {
            ownBubbleIcon.setColorFilter(Color.parseColor(sharedHelper.getOwnBubbleColor()), PorterDuff.Mode.SRC_ATOP);
        }
        if (sharedHelper.getOpponentBubbleColor() != null) {
            opponentBubbleIcon.setColorFilter(Color.parseColor(sharedHelper.getOpponentBubbleColor()), PorterDuff.Mode.SRC_ATOP);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showColorPicker(final GeneralCallback<String> generalCallback) {
        AmbilWarnaDialog dialog = new AmbilWarnaDialog(this, Color.parseColor("#3F51B5"), new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                // color is the color selected by the user.
                String hexWithoutAlpha = Integer.toHexString(color).toUpperCase().substring(2);
                generalCallback.onSuccess("#" + hexWithoutAlpha);
            }

            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
                generalCallback.onFailure(null);
            }
        });
        dialog.show();

    }

    @OnClick(R.id.statusBarColorPicker)
    public void statusBarColorPicker() {
        showColorPicker(new GeneralCallback<String>() {
            @Override
            public void onSuccess(String s) {
                statusBarColorPicked = true;
            }

            @Override
            public void onFailure(String s) {

            }
        });
    }


    @OnClick(R.id.fabMenuButtonColorPicker)
    public void fabMenuButtonColorPicker() {
        showColorPicker(new GeneralCallback<String>() {
            @Override
            public void onSuccess(String s) {
                fabMenuButtonColorPicked = true;
            }

            @Override
            public void onFailure(String s) {

            }
        });
    }

    @OnClick(R.id.pickNotificationIconColor)
    public void pickNotificationIconColor() {
        showColorPicker(new GeneralCallback<String>() {
            @Override
            public void onSuccess(String s) {
                pickNotificationIconColorPicked = true;
            }

            @Override
            public void onFailure(String s) {

            }
        });
    }

    @OnClick(R.id.pickShopIconColor)
    public void pickShopIconColor() {
        showColorPicker(new GeneralCallback<String>() {
            @Override
            public void onSuccess(String s) {
                pickShopIconColorPicked = true;
            }

            @Override
            public void onFailure(String s) {

            }
        });
    }

    @OnClick(R.id.pickLeftDrawerColor)
    public void pickLeftDrawerColor() {
        showColorPicker(new GeneralCallback<String>() {
            @Override
            public void onSuccess(String s) {
                pickLeftDrawerColorPicked = true;
            }

            @Override
            public void onFailure(String s) {

            }
        });
    }

    @OnClick(R.id.postBackgroundColor)
    public void postBackgroundColor() {
        showColorPicker(new GeneralCallback<String>() {
            @Override
            public void onSuccess(String s) {
                postBackgroundColorPicked = true;
            }

            @Override
            public void onFailure(String s) {

            }
        });
    }

    @OnClick(R.id.friendsBackgroundColor)
    public void friendsBackgroundColor() {
        showColorPicker(new GeneralCallback<String>() {
            @Override
            public void onSuccess(String s) {
                friendsBackgroundColorPicked = true;
            }

            @Override
            public void onFailure(String s) {

            }
        });
    }

    @OnClick(R.id.messagesBackgroundColor)
    public void messagesBackgroundColor() {
        showColorPicker(new GeneralCallback<String>() {
            @Override
            public void onSuccess(String s) {
                messagesBackgroundColorPicked = true;
            }

            @Override
            public void onFailure(String s) {

            }
        });
    }

    @OnClick(R.id.chatBackgroundColor)
    public void chatBackgroundColor() {
        showColorPicker(new GeneralCallback<String>() {
            @Override
            public void onSuccess(String s) {
                chatBackgroundColorPicked = true;
            }

            @Override
            public void onFailure(String s) {

            }
        });
    }

    @OnClick(R.id.requestsBackgroundColor)
    public void requestsBackgroundColor() {
        showColorPicker(new GeneralCallback<String>() {
            @Override
            public void onSuccess(String s) {
                requestBackgroundColorPicked = true;
            }

            @Override
            public void onFailure(String s) {

            }
        });
    }


    @OnClick(R.id.opponentBubbleWrapper)
    public void opponentBubbleWrapper() {
        showColorPicker(new GeneralCallback<String>() {
            @Override
            public void onSuccess(String s) {
                opponentBubbleColorPicked = true;
                opponentBubbleColorPicked = true;
                opponentBubbleIcon.setColorFilter(Color.parseColor(s), PorterDuff.Mode.SRC_ATOP);
                opponentBubbleCleaner.setVisibility(View.VISIBLE);
                sharedHelper.putOpponentBubbleColor(s);

            }

            @Override
            public void onFailure(String s) {

            }
        });
    }


    @OnClick(R.id.ownBubbleWrapper)
    public void ownBubbleWrapper() {
        showColorPicker(new GeneralCallback<String>() {
            @Override
            public void onSuccess(String s) {
                ownBubbleColorPicked = true;
                ownBubbleIcon.setColorFilter(Color.parseColor(s), PorterDuff.Mode.SRC_ATOP);
                ownBubbleCleaner.setVisibility(View.VISIBLE);
                sharedHelper.putOwnBubbleColor(s);
            }

            @Override
            public void onFailure(String s) {

            }
        });
    }


    @OnClick(R.id.statusBarCleaner)
    public void statusBarColorPicked() {
        statusBarColorPicked = false;
    }

    @OnClick(R.id.fabButtonCleaner)
    public void fabButtonCleaner() {

    }

    @OnClick(R.id.ownBubbleCleaner)
    public void ownBubbleCleaner() {
        ownBubbleColorPicked = false;
        ownBubbleCleaner.setVisibility(View.GONE);
        ownBubbleIcon.setColorFilter(null);
        sharedHelper.putOwnBubbleColor(oldOwnBubbleColor);
    }

    @OnClick(R.id.opponentBubbleCleaner)
    public void opponentBubbleCleaner() {
        opponentBubbleColorPicked = false;
        opponentBubbleCleaner.setVisibility(View.GONE);
        opponentBubbleIcon.setColorFilter(null);
        sharedHelper.putOpponentBubbleColor(oldOpponentBubbleColor);
    }

    @OnClick(R.id.notificationCleaner)
    public void notificationCleaner() {

    }

    @OnClick(R.id.shopCleaner)
    public void shopCleaner() {

    }

    @OnClick(R.id.leftPanelCleaner)
    public void leftPanelCleaner() {

    }

    @OnClick(R.id.feedCleaner)
    public void feedCleaner() {

    }

    @OnClick(R.id.friendsCleaner)
    public void friendsCleaner() {

    }

    @OnClick(R.id.messagesCleaner)
    public void messagesCleaner() {

    }

    @OnClick(R.id.chatCleaner)
    public void chatCleaner() {

    }

    @OnClick(R.id.requestCleaner)
    public void requestCleaner() {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
