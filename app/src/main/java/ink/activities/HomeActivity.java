package ink.activities;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.support.design.widget.FloatingActionMenu;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.login.LoginManager;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.ink.R;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;

import fab.FloatingActionButton;
import ink.callbacks.GeneralCallback;
import ink.fragments.Feed;
import ink.fragments.MyFriends;
import ink.friendsmash.FriendSmashLoginView;
import ink.interfaces.AccountDeleteListener;
import ink.interfaces.ColorChangeListener;
import ink.models.CoinsResponse;
import ink.service.BackgroundTaskService;
import ink.service.LocationRequestSessionDestroyer;
import ink.service.SendTokenService;
import ink.utils.AppWarning;
import ink.utils.CircleTransform;
import ink.utils.Constants;
import ink.utils.DeviceChecker;
import ink.utils.FileUtils;
import ink.utils.IonCache;
import ink.utils.PingHelper;
import ink.utils.RealmHelper;
import ink.utils.Retrofit;
import ink.utils.SharedHelper;
import ink.utils.User;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, AccountDeleteListener {

    private static final String TAG = HomeActivity.class.getSimpleName();

    private FloatingActionMenu mFab;
    private ImageView mProfileImage;
    private ActionBarDrawerToggle toggle;
    private TextView coinsText;
    private SharedHelper mSharedHelper;
    private FloatingActionButton mMessages;
    private FloatingActionButton mNewPost;
    private Feed mFeed;
    private MyFriends mMyFriends;
    private Toolbar mToolbar;
    private DrawerLayout mDrawer;
    public static String PROFILE;
    public static String FEED;
    public static String MESSAGES;
    public static String GROUPS;
    public static String FRIENDS;
    public static String SETTINGS;
    private TextView mUserNameTV;
    private Class<?> mLastClassToOpen;
    private boolean shouldOpenActivity;
    private FloatingActionButton mMakePost;
    private FloatingActionButton searchFriend;
    private ProgressDialog progressDialog;
    private Menu menuItem;
    private boolean activityForResult;
    private int lastRequestCode;
    private ColorChangeListener colorChangeListener;
    private RelativeLayout panelHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        PROFILE = getString(R.string.profileText);
        FEED = getString(R.string.feedText);
        MESSAGES = getString(R.string.messageText);
        GROUPS = getString(R.string.groupsText);
        FRIENDS = getString(R.string.friendsText);
        SETTINGS = getString(R.string.settingsString);
        mToolbar.setTitle(FEED);
        mSharedHelper = new SharedHelper(this);
        if (!mSharedHelper.isSecurityWarningShown()) {
            AppWarning.get().showWarning(this, getString(R.string.securityQuestionNotSetWarning), getString(R.string.setSecurityQuestion), new GeneralCallback() {
                @Override
                public void onSuccess(Object o) {
                    startActivity(new Intent(getApplicationContext(), MyProfile.class));
                }

                @Override
                public void onFailure(Object o) {

                }
            });
        }
        if (!mSharedHelper.isMessagesDownloaded()) {
            startMessageDownloadService();
        }


        if (!PingHelper.get().isPinging()) {
            PingHelper.get().startPinging(mSharedHelper.getUserId());
        }
        User.get().setUserName(mSharedHelper.getFirstName() + " " + mSharedHelper.getLastName());
        User.get().setUserId(mSharedHelper.getUserId());
        mFab = (FloatingActionMenu) findViewById(R.id.fab);
        mMessages = (FloatingActionButton) findViewById(R.id.messages);
        mMakePost = (FloatingActionButton) findViewById(R.id.makePost);
        searchFriend = (FloatingActionButton) findViewById(R.id.searchPerson);
        mNewPost = (FloatingActionButton) findViewById(R.id.makePost);
        mFeed = Feed.newInstance();
        mMyFriends = MyFriends.newInstance();
        mMessages.setOnClickListener(this);
        mMakePost.setOnClickListener(this);
        mNewPost.setOnClickListener(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.loggingout));
        progressDialog.setMessage(getString(R.string.loggingoutPleaseWait));
        setOnAccountDeleteListener(this);
        mSharedHelper.putShouldLoadImage(true);
        try {
            testTimezone();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        FileUtils.deleteDirectoryTree(getApplicationContext().getCacheDir());


        checkIsWarned();
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        toggle = new ActionBarDrawerToggle(this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            public void onDrawerClosed(View view) {
                if (shouldOpenActivity) {
                    shouldOpenActivity = false;
                    if (activityForResult) {
                        activityForResult = false;
                        startActivityForResult(new Intent(getApplicationContext(), getLastKnownClass()), lastRequestCode);
                    } else {
                        startActivity(new Intent(getApplicationContext(), getLastKnownClass()));
                    }

                }
                super.onDrawerClosed(view);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        if (mDrawer != null) {
            mDrawer.addDrawerListener(toggle);
        }
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, mFeed).commit();


        mProfileImage = (ImageView) headerView.findViewById(R.id.profileImage);
        coinsText = (TextView) headerView.findViewById(R.id.coinsText);
        getCoins();
        mProfileImage.setOnClickListener(this);
        mUserNameTV = (TextView) headerView.findViewById(R.id.userNameTextView);
        panelHeader = (RelativeLayout) headerView.findViewById(R.id.panelHeader);
        navigationView.setNavigationItemSelectedListener(this);
        LocalBroadcastManager.getInstance(this).registerReceiver(feedUpdateReceiver, new IntentFilter(getPackageName() + "HomeActivity"));
    }


    private void testTimezone() throws ParseException {

    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    private void checkIsWarned() {
        if (!mSharedHelper.isDeviceWarned()) {
            if (DeviceChecker.isHuawei()) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                builder.setTitle(getString(R.string.caution));
                builder.setMessage(getString(R.string.huaweiWarning));
                builder.setCancelable(false);
                builder.setPositiveButton(getString(R.string.dontShowAgain), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //just override for dialog not to close automatically
                    }
                });
                builder.setNegativeButton(getString(R.string.navigateToSettings), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //just override for dialog not to close automatically
                    }
                });
                final AlertDialog dialog = builder.create();
                dialog.show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        mSharedHelper.putWarned(true);
                    }
                });
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
                    }
                });
            }
        }
    }

    private BroadcastReceiver feedUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mFeed != null) {
                mFeed.triggerFeedUpdate();
            }
        }
    };

    private void getCoins() {
        Call<ResponseBody> coinsCall = Retrofit.getInstance().getInkService().getCoins(mSharedHelper.getUserId());
        coinsCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    getCoins();
                    return;
                }
                if (response.body() == null) {
                    getCoins();
                    return;
                }
                Gson gson = new Gson();
                try {
                    CoinsResponse coinsResponse = gson.fromJson(response.body().string(), CoinsResponse.class);
                    if (coinsResponse.success) {
                        User.get().setCoins(coinsResponse.coins);
                        coinsText.setText(getString(R.string.coinsText, coinsResponse.coins));
                        User.get().setCoinsLoaded(true);
                        User.get().setCoins(coinsResponse.coins);
                    } else {
                        getCoins();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                getCoins();
            }
        });
    }

    private Class<?> getLastKnownClass() {
        return mLastClassToOpen;
    }


    public FloatingActionMenu getHomeFab() {
        return mFab;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        menuItem = menu;
        setHomeActivityColors();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.notifications) {
            startActivity(new Intent(getApplicationContext(), RequestsView.class));
        } else if (id == R.id.shop) {
            startActivity(new Intent(getApplicationContext(), Shop.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Intent intent;
        switch (id) {
            case R.id.profile:
                shouldOpenActivity = true;
                setLastClassToOpen(MyProfile.class, false);
                break;

            case R.id.feeds:
                shouldOpenActivity = false;
                if (!mToolbar.getTitle().equals(FEED)) {
                    mToolbar.setTitle(getString(R.string.feedText));
                    getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, mFeed).commit();
                }
                break;

            case R.id.messages:
                shouldOpenActivity = true;
                setLastClassToOpen(Messages.class, false);
                break;

            case R.id.groups:
                setLastClassToOpen(Groups.class, false);
                shouldOpenActivity = true;
                break;

            case R.id.chatRoulette:
                setLastClassToOpen(ChatRoulette.class, false);
                shouldOpenActivity = true;
                break;

            case R.id.friends:
                shouldOpenActivity = false;
                if (!mToolbar.getTitle().equals(FRIENDS)) {
                    mToolbar.setTitle(getString(R.string.friendsText));
                    getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, mMyFriends).commit();
                }
                break;

            case R.id.music:
                shouldOpenActivity = true;
                setLastClassToOpen(Music.class, false);
                break;

            case R.id.imageEdit:
                shouldOpenActivity = true;
                setLastClassToOpen(ImageEditor.class, false);
                break;

            case R.id.settings:
                shouldOpenActivity = true;
                setLastClassToOpen(Settings.class, false);
                break;

            case R.id.customizeApp:
                shouldOpenActivity = true;
                lastRequestCode = Constants.REQUEST_CUSTOMIZE_MADE;
                setLastClassToOpen(CustomizeLook.class, true);
                break;
            case R.id.nav_share:
                shouldOpenActivity = false;
                break;

            case R.id.friendSmashGame:
                shouldOpenActivity = true;
                setLastClassToOpen(FriendSmashLoginView.class, false);
                break;
            case R.id.sendFeedback:
                shouldOpenActivity = true;
                setLastClassToOpen(SendFeedback.class, false);
                break;

            case R.id.contactSupport:
                shouldOpenActivity = true;
                setLastClassToOpen(ContactSupport.class, false);
                break;

            case R.id.logout:
                System.gc();
                AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                builder.setTitle(getString(R.string.warning));
                builder.setMessage(getString(R.string.logoutWaring));
                builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        logoutUser();
                    }
                });
                builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.show();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logoutUser() {
        progressDialog.show();
        shouldOpenActivity = false;
        FileUtils.clearApplicationData(getApplicationContext());
        boolean editorHintValue = mSharedHelper.isEditorHintShown();
        mSharedHelper.clean();
        mSharedHelper.putShouldShowIntro(false);
        if (editorHintValue) {
            mSharedHelper.putEditorHintShow(true);
        }
        mSharedHelper.putWarned(true);
        LoginManager.getInstance().logOut();
        RealmHelper.getInstance().clearDatabase(getApplicationContext());
        IonCache.clearIonCache(getApplicationContext());
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                try {
                    FirebaseInstanceId.getInstance().deleteInstanceId();
                    progressDialog.dismiss();
                    startActivity(new Intent(getApplicationContext(), Login.class));
                    finish();
                } catch (IOException e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                    startActivity(new Intent(getApplicationContext(), Login.class));
                    finish();
                }
            }
        });
        thread.start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.messages:
                mFab.close(true);
                openMessages();
                break;
            case R.id.makePost:
                mFab.close(true);
                System.gc();
                startActivity(new Intent(getApplicationContext(), MakePost.class));
                break;
            case R.id.profileImage:
                shouldOpenActivity = true;
                setLastClassToOpen(MyProfile.class, false);
                mDrawer.closeDrawer(Gravity.LEFT);
                break;
        }
    }

    private void openMessages() {
        startActivity(new Intent(getApplicationContext(), Messages.class));
    }

    private void startMessageDownloadService() {
        startService(new Intent(getApplicationContext(), BackgroundTaskService.class));
    }

    private void startTokenService() {
        startService(new Intent(getApplicationContext(), SendTokenService.class));
    }

    @Override
    protected void onResume() {
        getMyRequests();
        if (mSharedHelper.isTokenRefreshed()) {
            startTokenService();
        }
        if (User.get().getCoins() != 0) {
            if (coinsText != null) {
                coinsText.setText(getString(R.string.coinsText, User.get().getCoins()));
            }
        }
        String lastSessionId = mSharedHelper.getLastSessionUserId();
        if (lastSessionId != null) {
            Intent intent = new Intent(getApplicationContext(), LocationRequestSessionDestroyer.class);
            intent.putExtra("opponentId", lastSessionId);
            mSharedHelper.removeLastSessionUserId();
            startService(intent);
        }
        mUserNameTV.setText(mSharedHelper.getFirstName() + " " + mSharedHelper.getLastName());
        if (mSharedHelper.shouldLoadImage()) {
            loadImage();
        }
        super.onResume();
    }

    private void loadImage() {
        if (mSharedHelper.hasImage()) {

            if (!mSharedHelper.getImageLink().isEmpty()) {
                if (isSocialAccount()) {
                    Ion.with(getApplicationContext()).load(mSharedHelper.getImageLink()).withBitmap().placeholder(R.drawable.no_background_image).transform(new CircleTransform()).intoImageView(mProfileImage).setCallback(new FutureCallback<ImageView>() {
                        @Override
                        public void onCompleted(Exception e, ImageView result) {
                            mSharedHelper.putShouldLoadImage(false);
                        }
                    });
                } else {
                    Ion.with(getApplicationContext()).load(Constants.MAIN_URL +
                            Constants.USER_IMAGES_FOLDER + mSharedHelper.getImageLink()).withBitmap().placeholder(R.drawable.no_background_image).transform(new CircleTransform()).intoImageView(mProfileImage)
                            .setCallback(new FutureCallback<ImageView>() {
                                @Override
                                public void onCompleted(Exception e, ImageView result) {
                                    mSharedHelper.putShouldLoadImage(false);
                                }
                            });
                }
            }
        } else {
            Ion.with(getApplicationContext()).load(Constants.ANDROID_DRAWABLE_DIR + "no_image").withBitmap().transform(new CircleTransform()).intoImageView(mProfileImage)
                    .setCallback(new FutureCallback<ImageView>() {
                        @Override
                        public void onCompleted(Exception e, ImageView result) {
                            mSharedHelper.putShouldLoadImage(false);
                        }
                    });
        }
    }

    public FloatingActionButton getSearchFriend() {
        return searchFriend;
    }

    private void setLastClassToOpen(Class<?> classToOpen, boolean activityForResult) {
        mLastClassToOpen = classToOpen;
        this.activityForResult = activityForResult;
    }

    private void setShouldOpenActivity(boolean shouldOpenActivity) {
        this.shouldOpenActivity = shouldOpenActivity;
    }


    public Toolbar getToolbar() {
        return mToolbar;
    }


    @Override
    protected void onDestroy() {
        if (PingHelper.get().isPinging()) {
            PingHelper.get().destroyPinging();
        }
        super.onDestroy();
    }

    private void getMyRequests() {
        Call<ResponseBody> myRequestsCall = Retrofit.getInstance().getInkService().getMyRequests(mSharedHelper.getUserId());
        myRequestsCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    getMyRequests();
                    return;
                }
                if (response.body() == null) {
                    getMyRequests();
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    if (success) {
                        JSONArray jsonArray = jsonObject.optJSONArray("requests");
                        if (jsonArray.length() <= 0) {
                            if (menuItem != null) {
                                if (mSharedHelper.getNotificationIconColor() == null) {
                                    menuItem.getItem(0).setIcon(ContextCompat.getDrawable(getApplicationContext(),
                                            R.drawable.notification_icon));
                                } else {
                                    menuItem.getItem(0).getIcon().setColorFilter(Color.parseColor(mSharedHelper.getNotificationIconColor()),
                                            PorterDuff.Mode.SRC_ATOP);
                                }
                            }
                        } else {
                            if (menuItem != null) {
                                menuItem.getItem(0).setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.active_notification_icon));
                            }
                        }
                    } else {
                        getMyRequests();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                getMyRequests();
            }
        });
    }

    @Override
    public void onAccountDeleted() {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.REQUEST_CUSTOMIZE_MADE:
                boolean anythingChanged = data.getExtras().getBoolean("anythingChanged");
                if (anythingChanged) {
                    triggerColorChangeListener();
                    setHomeActivityColors();
                } else if (data.getExtras().containsKey("reset")) {
                    boolean reset = data.getExtras().getBoolean("reset");
                    if (reset) {
                        triggerReset();
                        resetColors();
                    }
                }
                break;
        }
    }


    private void triggerReset() {
        if (colorChangeListener != null) {
            colorChangeListener.onColorReset();
        }
    }

    public void setOnColorChangeListener(ColorChangeListener colorChangeListener) {
        this.colorChangeListener = colorChangeListener;
    }

    private void triggerColorChangeListener() {
        if (colorChangeListener != null) {
            colorChangeListener.onColorChanged();
        }
    }

    private void setHomeActivityColors() {
        if (mSharedHelper.getHamburgerColor() != null) {
            toggle.getDrawerArrowDrawable().setColor(Color.parseColor(mSharedHelper.getHamburgerColor()));
            toggle.syncState();
        }
        if (mSharedHelper.getMenuButtonColor() != null) {
            mFab.setMenuButtonColorNormal(Color.parseColor(mSharedHelper.getMenuButtonColor()));
            mFab.setMenuButtonColorPressed(Color.parseColor("#cccccc"));
        }
        if (mSharedHelper.getNotificationIconColor() != null) {
            menuItem.getItem(0).getIcon().setColorFilter(Color.parseColor(mSharedHelper.getNotificationIconColor()),
                    PorterDuff.Mode.SRC_ATOP);
        }

        if (mSharedHelper.getShopIconColor() != null) {
            menuItem.getItem(1).getIcon().setColorFilter(Color.parseColor(mSharedHelper.getShopIconColor()),
                    PorterDuff.Mode.SRC_ATOP);
        }
        if (mSharedHelper.getActionBarColor() != null) {
            mToolbar.setBackgroundColor(Color.parseColor(mSharedHelper.getActionBarColor()));
        }

        if (mSharedHelper.getStatusBarColor() != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.parseColor(mSharedHelper.getStatusBarColor()));
            }
        }
        if (mSharedHelper.getLeftSlidingPanelHeaderColor() != null) {
            panelHeader.setBackgroundColor(Color.parseColor(mSharedHelper.getLeftSlidingPanelHeaderColor()));
        }
        System.gc();
    }


    private void resetColors() {
        if (mSharedHelper.getHamburgerColor() == null) {
            toggle.getDrawerArrowDrawable().setColor(Color.WHITE);
        }
        if (mSharedHelper.getMenuButtonColor() == null) {
            mFab.setMenuButtonColorNormal(ContextCompat.getColor(this, R.color.colorPrimary));
            mFab.setMenuButtonColorPressed(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }
        if (mSharedHelper.getNotificationIconColor() == null) {
            menuItem.getItem(0).getIcon().setColorFilter(null);
        }
        if (mSharedHelper.getShopIconColor() == null) {
            menuItem.getItem(1).getIcon().setColorFilter(null);
        }
        if (mSharedHelper.getActionBarColor() == null) {
            mToolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        }
        if (mSharedHelper.getLeftSlidingPanelHeaderColor() == null) {
            panelHeader.setBackground(ContextCompat.getDrawable(this, R.drawable.side_nav_bar));
        }
        if (mSharedHelper.getStatusBarColor() == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
            }
        }
        System.gc();
    }
}
