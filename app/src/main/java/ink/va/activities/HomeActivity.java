package ink.va.activities;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.FloatingActionMenu;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.login.LoginManager;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.ink.va.R;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import fab.FloatingActionButton;
import ink.va.fragments.Feed;
import ink.va.fragments.MyFriends;
import ink.va.interfaces.AccountDeleteListener;
import ink.va.interfaces.ColorChangeListener;
import ink.va.models.CoinsResponse;
import ink.va.service.BackgroundTaskService;
import ink.va.service.LocationRequestSessionDestroyer;
import ink.va.service.SendTokenService;
import ink.va.utils.CircleTransform;
import ink.va.utils.Constants;
import ink.va.utils.DeviceChecker;
import ink.va.utils.ErrorCause;
import ink.va.utils.FileUtils;
import ink.va.utils.IonCache;
import ink.va.utils.Keyboard;
import ink.va.utils.PingHelper;
import ink.va.utils.RealmHelper;
import ink.va.utils.Retrofit;
import ink.va.utils.SharedHelper;
import ink.va.utils.User;
import me.leolin.shortcutbadger.ShortcutBadger;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, AccountDeleteListener {

    private static final String TAG = HomeActivity.class.getSimpleName();
    public static final int PROFILE_RESULT_CODE = 836;

    private FloatingActionMenu mFab;
    private ImageView mProfileImage;
    private ActionBarDrawerToggle toggle;
    private TextView coinsText;
    private SharedHelper mSharedHelper;
    private FloatingActionButton mMessages;
    private FloatingActionButton mNewPost;
    private Feed mFeed;
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
    private ProgressDialog progressDialog;
    private Menu menuItem;
    private boolean activityForResult;
    private int lastRequestCode;
    private ColorChangeListener colorChangeListener;
    private RelativeLayout panelHeader;
    private TextView messagesCountTV;


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
        if (!mSharedHelper.isMessagesDownloaded()) {
            startMessageDownloadService();
        }

        if (!mSharedHelper.isSecurityQuestionSet() && isAccountRecoverable()) {
            View warningView = getLayoutInflater().inflate(R.layout.app_warning_view, null);
            final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
            bottomSheetDialog.setContentView(warningView);
            Button warningButton = (Button) warningView.findViewById(R.id.warningButton);
            ImageView closeWarning = (ImageView) warningView.findViewById(R.id.closeWarning);
            warningButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomSheetDialog.hide();
                    startActivity(new Intent(getApplicationContext(), MyProfile.class));
                }
            });
            closeWarning.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomSheetDialog.hide();
                }
            });
            bottomSheetDialog.show();
        }

        PingHelper.get().startPinging(mSharedHelper.getUserId());
        User.get().setUserName(mSharedHelper.getFirstName() + " " + mSharedHelper.getLastName());
        User.get().setUserId(mSharedHelper.getUserId());
        mFab = (FloatingActionMenu) findViewById(R.id.fab);
        mMessages = (FloatingActionButton) findViewById(R.id.messages);
        mMakePost = (FloatingActionButton) findViewById(R.id.makePost);
        mNewPost = (FloatingActionButton) findViewById(R.id.makePost);
        mFeed = Feed.newInstance();
        mMessages.setOnClickListener(this);
        mMakePost.setOnClickListener(this);
        mNewPost.setOnClickListener(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.loggingout));
        progressDialog.setMessage(getString(R.string.loggingoutPleaseWait));
        setOnAccountDeleteListener(this);
        mSharedHelper.putShouldLoadImage(true);

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
                Keyboard.hideKeyboard(HomeActivity.this, getWindow().getDecorView().getRootView() != null ? getWindow().getDecorView().getRootView() : mDrawer);
                super.onDrawerOpened(drawerView);
            }
        };
        if (mDrawer != null) {
            mDrawer.addDrawerListener(toggle);
        }
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        messagesCountTV = (TextView) MenuItemCompat.getActionView(navigationView.getMenu().findItem(R.id.messages));

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


    private void initializeCountDrawer(final TextView messages) {
        messages.setGravity(Gravity.CENTER_VERTICAL);
        if (mSharedHelper.getLeftSlidingPanelHeaderColor() != null) {
            messages.setTextColor(Color.parseColor(mSharedHelper.getLeftSlidingPanelHeaderColor()));
        } else {
            messages.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
        }

        RealmHelper.getInstance().getMessagesCount(new RealmHelper.QueryReadyListener() {
            @Override
            public void onQueryReady(Object result) {
                int notificationCount = (int) result;
                messages.setText(
                        String.valueOf(notificationCount != 0 ?
                                notificationCount : "")

                );
                if (notificationCount == 0) {
                    ShortcutBadger.removeCount(getApplicationContext());
                }
            }
        });
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
        coinsText.setText(getString(R.string.updating));
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
                        User.get().setCoins(String.valueOf(coinsResponse.coins));
                        coinsText.setText(getString(R.string.coinsText, coinsResponse.coins));
                        User.get().setCoinsLoaded(true);
                        User.get().setCoins(String.valueOf(coinsResponse.coins));
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
        switch (item.getItemId()) {
            case R.id.notifications:
                startActivity(new Intent(getApplicationContext(), RequestsView.class));
                break;
            case R.id.shop:
                startActivity(new Intent(getApplicationContext(), Shop.class));
                break;
            case R.id.news:
                startActivity(new Intent(getApplicationContext(), NewsAndTrendsActivity.class));
                break;
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
                lastRequestCode = PROFILE_RESULT_CODE;
                setLastClassToOpen(MyProfile.class, true);
                break;
            case R.id.myCollection:
                shouldOpenActivity = true;
                setLastClassToOpen(MyCollection.class, false);
                break;
            case R.id.vipChat:
                callToVipServer(Constants.TYPE_ENTER_VIP);
                break;
            case R.id.whoViewed:
                shouldOpenActivity = true;
                setLastClassToOpen(WhoViewedActivity.class, false);
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
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, MyFriends.newInstance()).commit();
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
                setLastClassToOpen(Settings.class, true);
                break;

            case R.id.customizeApp:
                shouldOpenActivity = true;
                lastRequestCode = Constants.REQUEST_CUSTOMIZE_MADE;
                setLastClassToOpen(CustomizeLook.class, true);
                break;
            case R.id.nav_share:
                shouldOpenActivity = false;

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.ink.va");
                startActivity(Intent.createChooser(shareIntent, getString(R.string.share_ink_with)));
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

    private void callToVipServer(final String type) {
        progressDialog.setTitle(getString(R.string.logging));
        progressDialog.setMessage(getString(R.string.loggingIntoVip));
        progressDialog.show();

        Call<ResponseBody> responseBodyCall = Retrofit.getInstance().getInkService().callVipServer(mSharedHelper.getUserId(), type);
        responseBodyCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    callToVipServer(type);
                    return;
                }
                if (response.body() == null) {
                    callToVipServer(type);
                    return;
                }
                progressDialog.dismiss();
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    if (success) {
                        final boolean firstVipLogin = jsonObject.optBoolean("isFirstVipLogin");
                        final boolean hasGift = jsonObject.optBoolean("hasGift");
                        final String giftType = jsonObject.optString("giftType");

                        final Bundle bundle = new Bundle();
                        bundle.putBoolean("firstVipLogin", firstVipLogin);
                        bundle.putBoolean("hasGift", hasGift);
                        bundle.putString("giftType", giftType);

                        if (type.equals(Constants.TYPE_BUY_VIP)) {
                            int remainingCoins = jsonObject.optInt("remainingCoins");
                            User.get().setCoins(String.valueOf(remainingCoins));
                            coinsText.setText(getString(R.string.coinsText, User.get().getCoins()));
                            AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                            builder.setTitle(getString(R.string.congratulation));
                            builder.setMessage(getString(R.string.vip_bought_Text));
                            builder.setCancelable(false);
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            });
                            final AlertDialog alertDialog = builder.create();
                            alertDialog.show();
                            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    alertDialog.dismiss();

                                    openVipRoom(bundle);
                                }
                            });
                        } else {
                            openVipRoom(bundle);
                        }
                    } else {
                        String cause = jsonObject.optString("cause");
                        switch (cause) {
                            case ErrorCause.SERVER_ERROR:
                                Snackbar.make(mToolbar, getString(R.string.serverErrorText), Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                    }
                                }).show();
                                break;
                            case ErrorCause.NOT_VIP_ERROR:
                                int vipPrice = jsonObject.optInt("vipPrice");
                                String shortInfo = jsonObject.optString("vipDescription");
                                AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                                builder.setTitle(getString(R.string.notVipText));
                                builder.setMessage(getString(R.string.youAreNotVipText, vipPrice) + shortInfo);
                                builder.setPositiveButton(getString(R.string.buy_text), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                });
                                builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                });
                                builder.setCancelable(false);
                                final AlertDialog alertDialog = builder.create();
                                alertDialog.show();
                                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        alertDialog.dismiss();
                                        callToVipServer(Constants.TYPE_BUY_VIP);
                                    }
                                });
                                alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        alertDialog.dismiss();
                                    }
                                });
                                break;
                            case ErrorCause.NOT_ENOUGH_COINS:
                                builder = new AlertDialog.Builder(HomeActivity.this);
                                builder.setMessage(getString(R.string.not_enough_coins));
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                });
                                builder.show();
                                break;
                        }
                    }
                } catch (IOException e) {
                    progressDialog.dismiss();
                    Snackbar.make(mToolbar, getString(R.string.vip_enter_error), Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    }).show();
                    e.printStackTrace();
                } catch (JSONException e) {
                    progressDialog.dismiss();
                    Snackbar.make(mToolbar, getString(R.string.vip_enter_error), Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    }).show();
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressDialog.dismiss();
                Snackbar.make(mToolbar, getString(R.string.vip_enter_error), Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                }).show();
            }
        });
    }

    private void openVipRoom(@Nullable Bundle bundle) {
        Intent intent = new Intent(getApplicationContext(), VIPActivity.class);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
    }

    private void logoutUser() {
        progressDialog.setTitle(getString(R.string.loggingout));
        progressDialog.setMessage(getString(R.string.loggingoutPleaseWait));
        progressDialog.show();
        shouldOpenActivity = false;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                LoginManager.getInstance().logOut();
                RealmHelper.getInstance().clearDatabase(getApplicationContext());
                IonCache.clearIonCache(getApplicationContext());
                FileUtils.clearApplicationData(getApplicationContext());
                boolean editorHintValue = mSharedHelper.isEditorHintShown();
                mSharedHelper.clean();
                mSharedHelper.putShouldShowIntro(false);
                if (editorHintValue) {
                    mSharedHelper.putEditorHintShow(true);
                }
                mSharedHelper.putWarned(true);

                try {
                    FirebaseInstanceId.getInstance().deleteInstanceId();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            startActivity(new Intent(getApplicationContext(), Login.class));
                            finish();
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            startActivity(new Intent(getApplicationContext(), Login.class));
                            finish();
                        }
                    });

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
                overridePendingTransition(R.anim.activity_scale_up, R.anim.activity_scale_down);
                break;
            case R.id.profileImage:
                shouldOpenActivity = true;
                lastRequestCode = PROFILE_RESULT_CODE;
                setLastClassToOpen(MyProfile.class, true);
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
        if (messagesCountTV != null) {
            initializeCountDrawer(messagesCountTV);
        }
        if (mSharedHelper.isTokenRefreshed()) {
            startTokenService();
        }
        if (User.get().getCoins() != null) {
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
                    String encodedImage = Uri.encode(mSharedHelper.getImageLink());
                    Ion.with(getApplicationContext()).load(Constants.MAIN_URL +
                            Constants.USER_IMAGES_FOLDER + encodedImage).withBitmap().placeholder(R.drawable.no_background_image).transform(new CircleTransform()).intoImageView(mProfileImage)
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
        PingHelper.get().destroyPinging();
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
                                    menuItem.getItem(1).setIcon(ContextCompat.getDrawable(getApplicationContext(),
                                            R.drawable.notification_icon));
                                } else {
                                    menuItem.getItem(1).getIcon().setColorFilter(Color.parseColor(mSharedHelper.getNotificationIconColor()),
                                            PorterDuff.Mode.SRC_ATOP);
                                }
                            }
                        } else {
                            if (menuItem != null) {
                                menuItem.getItem(1).setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.active_notification_icon));
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
            case PROFILE_RESULT_CODE:
                getCoins();
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

        if (mSharedHelper.getTrendColor() != null) {
            menuItem.getItem(0).getIcon().setColorFilter(Color.parseColor(mSharedHelper.getTrendColor()),
                    PorterDuff.Mode.SRC_ATOP);
        }

        if (mSharedHelper.getNotificationIconColor() != null) {
            menuItem.getItem(1).getIcon().setColorFilter(Color.parseColor(mSharedHelper.getNotificationIconColor()),
                    PorterDuff.Mode.SRC_ATOP);
        }

        if (mSharedHelper.getShopIconColor() != null) {
            menuItem.getItem(2).getIcon().setColorFilter(Color.parseColor(mSharedHelper.getShopIconColor()),
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
        if (mSharedHelper.getTrendColor() == null) {
            menuItem.getItem(0).getIcon().setColorFilter(null);
        }

        if (mSharedHelper.getNotificationIconColor() == null) {
            menuItem.getItem(1).getIcon().setColorFilter(null);
        }
        if (mSharedHelper.getShopIconColor() == null) {
            menuItem.getItem(2).getIcon().setColorFilter(null);
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
