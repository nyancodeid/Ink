package ink.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
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
import ink.utils.Constants;
import ink.utils.SharedHelper;
import yuku.ambilwarna.AmbilWarnaDialog;

/**
 * Created by USER on 2016-08-30.
 */

public class CustomizeLook extends AppCompatActivity {

    @Bind(R.id.customizeToolbar)
    Toolbar customizeToolbar;

    @Bind(R.id.customizeFabMenu)
    android.support.design.widget.FloatingActionMenu customizeFabMenu;

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
    private boolean feedColorPicked;
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
    private String oldShopIconColor;
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
        sharedHelper = new SharedHelper(this);
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
        oldShopIconColor = sharedHelper.getShopIconColor();
        oldLeftSlidingPanelHeaderColor = sharedHelper.getLeftSlidingPanelHeaderColor();
        oldFeedColor = sharedHelper.getFeedColor();
        oldFriendsColor = sharedHelper.getFriendsColor();
        oldMessagesColor = sharedHelper.getMessagesColor();
        oldChatColor = sharedHelper.getChatColor();
        oldMyRequestColor = sharedHelper.getMyRequestColor();


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
                if (!statusBarColorPicked && !fabMenuButtonColorPicked && !pickNotificationIconColorPicked && !pickShopIconColorPicked &&
                        !pickLeftDrawerColorPicked && !feedColorPicked && !messagesBackgroundColorPicked && !friendsBackgroundColorPicked &&
                        !chatBackgroundColorPicked && !requestBackgroundColorPicked && !opponentBubbleColorPicked && !ownBubbleColorPicked) {
                    Intent intent = new Intent();
                    intent.putExtra("anythingChanged", false);
                    setResult(Constants.REQUEST_CUSTOMIZE_MADE, intent);
                    finish();
                } else {
                    Intent intent = new Intent();
                    intent.putExtra("anythingChanged", true);
                    setResult(Constants.REQUEST_CUSTOMIZE_MADE, intent);
                    finish();
                }
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
                customizeToolbar.setBackgroundColor(Color.parseColor(s));
                statusBarCleaner.setVisibility(View.VISIBLE);
                sharedHelper.putStatusBarColor(s);
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
                customizeFabMenu.setMenuButtonColorNormal(Color.parseColor(s));
                sharedHelper.putMenuButtonColor(s);
                fabButtonCleaner.setVisibility(View.VISIBLE);
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
                notificationIcon.setColorFilter(Color.parseColor(s), PorterDuff.Mode.SRC_ATOP);
                notificationCleaner.setVisibility(View.VISIBLE);
                sharedHelper.putNotificationIconColor(s);
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
                shopCleaner.setVisibility(View.VISIBLE);
                shopIcon.setColorFilter(Color.parseColor(s), PorterDuff.Mode.SRC_ATOP);
                sharedHelper.putShopIconColor(s);
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
                leftPanelCleaner.setVisibility(View.VISIBLE);
                sharedHelper.putLeftSlidingPanelColor(s);
                Snackbar.make(friendsCleaner, getString(R.string.colorSet), Snackbar.LENGTH_SHORT).show();
                pickLeftDrawerColorPicked = true;
            }

            @Override
            public void onFailure(String s) {

            }
        });
    }

    @OnClick(R.id.feedBackgroundColor)
    public void feedBackgroundColor() {
        showColorPicker(new GeneralCallback<String>() {
            @Override
            public void onSuccess(String s) {
                feedCleaner.setVisibility(View.VISIBLE);
                Snackbar.make(feedCleaner, getString(R.string.colorSet), Snackbar.LENGTH_SHORT).show();
                sharedHelper.putFeedColor(s);
                feedColorPicked = true;
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
                friendsCleaner.setVisibility(View.VISIBLE);
                Snackbar.make(friendsCleaner, getString(R.string.colorSet), Snackbar.LENGTH_SHORT).show();
                sharedHelper.putFriendsColor(s);
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
                messagesCleaner.setVisibility(View.VISIBLE);
                Snackbar.make(messagesCleaner, getString(R.string.colorSet), Snackbar.LENGTH_SHORT).show();
                sharedHelper.putMessagesColor(s);
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
                chatCleaner.setVisibility(View.VISIBLE);
                Snackbar.make(chatCleaner, getString(R.string.colorSet), Snackbar.LENGTH_SHORT).show();
                sharedHelper.putChatColor(s);
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
                requestCleaner.setVisibility(View.VISIBLE);
                Snackbar.make(requestCleaner, getString(R.string.colorSet), Snackbar.LENGTH_SHORT).show();
                sharedHelper.putMyRequestColor(s);
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
        statusBarCleaner.setVisibility(View.GONE);
        sharedHelper.putStatusBarColor(oldStatusBarColor);
        if (oldStatusBarColor != null) {
            customizeToolbar.setBackgroundColor(Color.parseColor(oldStatusBarColor));
        } else {
            customizeToolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        }
    }

    @OnClick(R.id.fabButtonCleaner)
    public void fabButtonCleaner() {
        fabMenuButtonColorPicked = false;
        fabButtonCleaner.setVisibility(View.GONE);
        sharedHelper.putMenuButtonColor(oldMenuButtonColor);
        if (oldMenuButtonColor != null) {
            customizeFabMenu.setMenuButtonColorNormal(Color.parseColor(oldMenuButtonColor));
        } else {
            customizeFabMenu.setMenuButtonColorNormal(ContextCompat.getColor(this, R.color.colorPrimary));
        }
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
        pickNotificationIconColorPicked = false;
        notificationCleaner.setVisibility(View.GONE);
        notificationIcon.setColorFilter(null);
        sharedHelper.putNotificationIconColor(oldNotificationIconColor);
    }

    @OnClick(R.id.shopCleaner)
    public void shopCleaner() {
        pickShopIconColorPicked = false;
        shopCleaner.setVisibility(View.GONE);
        shopIcon.setColorFilter(null);
        sharedHelper.putShopIconColor(oldShopIconColor);
    }

    @OnClick(R.id.leftPanelCleaner)
    public void leftPanelCleaner() {
        pickLeftDrawerColorPicked = false;
        leftPanelCleaner.setVisibility(View.GONE);
        sharedHelper.putLeftSlidingPanelColor(oldLeftSlidingPanelHeaderColor);
        Snackbar.make(leftPanelCleaner, getString(R.string.colorRemoved), Snackbar.LENGTH_SHORT).show();
    }

    @OnClick(R.id.feedCleaner)
    public void feedCleaner() {
        feedColorPicked = false;
        feedCleaner.setVisibility(View.GONE);
        Snackbar.make(feedCleaner, getString(R.string.colorRemoved), Snackbar.LENGTH_SHORT).show();
        sharedHelper.putFeedColor(oldFeedColor);
    }

    @OnClick(R.id.friendsCleaner)
    public void friendsCleaner() {
        friendsBackgroundColorPicked = false;
        friendsCleaner.setVisibility(View.GONE);
        Snackbar.make(friendsCleaner, getString(R.string.colorRemoved), Snackbar.LENGTH_SHORT).show();
        sharedHelper.putFriendsColor(oldFriendsColor);
    }

    @OnClick(R.id.messagesCleaner)
    public void messagesCleaner() {
        messagesBackgroundColorPicked = false;
        messagesCleaner.setVisibility(View.GONE);
        Snackbar.make(messagesCleaner, getString(R.string.colorRemoved), Snackbar.LENGTH_SHORT).show();
        sharedHelper.putMessagesColor(oldMessagesColor);
    }

    @OnClick(R.id.chatCleaner)
    public void chatCleaner() {
        chatBackgroundColorPicked = false;
        chatCleaner.setVisibility(View.GONE);
        Snackbar.make(chatCleaner, getString(R.string.colorRemoved), Snackbar.LENGTH_SHORT).show();
        sharedHelper.putChatColor(oldChatColor);
    }

    @OnClick(R.id.requestCleaner)
    public void requestCleaner() {
        requestBackgroundColorPicked = false;
        requestCleaner.setVisibility(View.GONE);
        Snackbar.make(requestCleaner, getString(R.string.colorRemoved), Snackbar.LENGTH_SHORT).show();
        sharedHelper.putMyRequestColor(oldMyRequestColor);
    }

    @Override
    public void onBackPressed() {
        if (!statusBarColorPicked && !fabMenuButtonColorPicked && !pickNotificationIconColorPicked && !pickShopIconColorPicked &&
                !pickLeftDrawerColorPicked && !feedColorPicked && !messagesBackgroundColorPicked && !friendsBackgroundColorPicked &&
                !chatBackgroundColorPicked && !requestBackgroundColorPicked && !opponentBubbleColorPicked && !ownBubbleColorPicked) {
            Intent intent = new Intent();
            intent.putExtra("anythingChanged", false);
            setResult(Constants.REQUEST_CUSTOMIZE_MADE, intent);
            super.onBackPressed();
        } else {
            Intent intent = new Intent();
            intent.putExtra("anythingChanged", true);
            setResult(Constants.REQUEST_CUSTOMIZE_MADE, intent);
            super.onBackPressed();
        }

    }
}
