package ink.activities;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.support.design.widget.FloatingActionMenu;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.ink.R;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.IOException;
import java.text.ParseException;
import java.util.Timer;
import java.util.TimerTask;

import fab.FloatingActionButton;
import ink.fragments.Feed;
import ink.fragments.MyFriends;
import ink.interfaces.AccountDeleteListener;
import ink.models.CoinsResponse;
import ink.models.PingResponse;
import ink.service.BackgroundTaskService;
import ink.service.SendTokenService;
import ink.utils.CircleTransform;
import ink.utils.Constants;
import ink.utils.DeviceChecker;
import ink.utils.FileUtils;
import ink.utils.IonCache;
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

    private static final long PING_TIME = 50000;
    private static final String TAG = HomeActivity.class.getSimpleName();
    private FloatingActionMenu mFab;
    private ImageView mProfileImage;
    private TextView coinsText;
    private SharedHelper mSharedHelper;
    private FloatingActionButton mMessages;
    private FloatingActionButton mNewPost;
    private Feed mFeed;
    private MyFriends mMyFriends;
    private Toolbar mToolbar;
    private Timer timer = new Timer();
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
    private Thread mPingThread;
    private Gson gson;
    private ProgressDialog progressDialog;
    private AVLoadingIndicatorView profileImageLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        gson = new Gson();
        setSupportActionBar(mToolbar);
        PROFILE = getString(R.string.profileText);
        FEED = getString(R.string.feedText);
        MESSAGES = getString(R.string.messageText);
        GROUPS = getString(R.string.groupsText);
        FRIENDS = getString(R.string.friendsText);
        SETTINGS = getString(R.string.settingsString);
        mToolbar.setTitle(FEED);
        mSharedHelper = new SharedHelper(this);

        initThread();
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

        try {
            testTimezone();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        FileUtils.deleteDirectoryTree(getApplicationContext().getCacheDir());


        checkIsWarned();
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            public void onDrawerClosed(View view) {
                if (shouldOpenActivity) {
                    shouldOpenActivity = false;
                    startActivity(new Intent(getApplicationContext(), getLastKnownClass()));
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
        profileImageLoading = (AVLoadingIndicatorView) headerView.findViewById(R.id.profileImageLoading);
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, mFeed).commit();


        mProfileImage = (ImageView) headerView.findViewById(R.id.profileImage);
        coinsText = (TextView) headerView.findViewById(R.id.coinsText);
        getCoins();
        mProfileImage.setOnClickListener(this);
        mUserNameTV = (TextView) headerView.findViewById(R.id.userNameTextView);
        navigationView.setNavigationItemSelectedListener(this);
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

    private void initThread() {
        mPingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                if (mPingThread.getState() != Thread.State.TERMINATED) {
                    pingTime();
                }
            }
        });
        mPingThread.start();
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
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
                setLastClassToOpen(MyProfile.class);
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
                setLastClassToOpen(Messages.class);
                break;

            case R.id.groups:
                setLastClassToOpen(Groups.class);
                shouldOpenActivity = true;
                break;

            case R.id.chatRoulette:
                setLastClassToOpen(ChatRoulette.class);
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
                setLastClassToOpen(Music.class);
                break;

            case R.id.imageEdit:
                shouldOpenActivity = true;
                setLastClassToOpen(ImageEditor.class);
                break;

            case R.id.settings:
                shouldOpenActivity = true;
                setLastClassToOpen(Settings.class);
                break;

            case R.id.nav_share:
                shouldOpenActivity = false;
                break;

            case R.id.sendFeedback:
                shouldOpenActivity = true;
                setLastClassToOpen(SendFeedback.class);
                break;

            case R.id.contactSupport:
                shouldOpenActivity = true;
                setLastClassToOpen(ContactSupport.class);
                break;

            case R.id.logout:
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
                RealmHelper.getInstance().clearDatabase(getApplicationContext());
                IonCache.clearIonCache(getApplicationContext());
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                        try {
                            FirebaseInstanceId.getInstance().deleteInstanceId();
                            progressDialog.dismiss();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(HomeActivity.this, getString(R.string.loggedOutText), Toast.LENGTH_SHORT).show();
                                }
                            });
                            startActivity(new Intent(getApplicationContext(), Login.class));
                            finish();
                        } catch (IOException e) {
                            e.printStackTrace();
                            progressDialog.dismiss();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(HomeActivity.this, getString(R.string.loggedOutText), Toast.LENGTH_SHORT).show();
                                }
                            });
                            startActivity(new Intent(getApplicationContext(), Login.class));
                            finish();
                        }
                    }
                });
                thread.start();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
                setLastClassToOpen(MyProfile.class);
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
        if (!mSharedHelper.isMessagesDownloaded()) {
            startMessageDownloadService();
        }
        if (mSharedHelper.isTokenRefreshed()) {
            startTokenService();
        }
        if (User.get().getCoins() != 0) {
            if (coinsText != null) {
                coinsText.setText(getString(R.string.coinsText, User.get().getCoins()));
            }
        }
        mUserNameTV.setText(mSharedHelper.getFirstName() + " " + mSharedHelper.getLastName());
        if (mSharedHelper.hasImage()) {
            if (!mSharedHelper.getImageLink().isEmpty()) {
                if (isSocialAccount()) {
                    Ion.with(getApplicationContext()).load(mSharedHelper.getImageLink()).withBitmap().transform(new CircleTransform()).intoImageView(mProfileImage).setCallback(new FutureCallback<ImageView>() {
                        @Override
                        public void onCompleted(Exception e, ImageView result) {
                            hideImageLoading();
                        }
                    });
                } else {
                    Ion.with(getApplicationContext()).load(Constants.MAIN_URL +
                            Constants.USER_IMAGES_FOLDER + mSharedHelper.getImageLink()).withBitmap().transform(new CircleTransform()).intoImageView(mProfileImage)
                            .setCallback(new FutureCallback<ImageView>() {
                                @Override
                                public void onCompleted(Exception e, ImageView result) {
                                    hideImageLoading();
                                }
                            });
                }
            }
        } else {
            Ion.with(getApplicationContext()).load(Constants.ANDROID_DRAWABLE_DIR + "no_image").withBitmap().transform(new CircleTransform()).intoImageView(mProfileImage)
                    .setCallback(new FutureCallback<ImageView>() {
                        @Override
                        public void onCompleted(Exception e, ImageView result) {
                            hideImageLoading();
                        }
                    });
        }

        super.onResume();
    }

    private void hideImageLoading() {
        profileImageLoading.setVisibility(View.GONE);
    }

    public FloatingActionButton getSearchFriend() {
        return searchFriend;
    }

    private void setLastClassToOpen(Class<?> classToOpen) {
        mLastClassToOpen = classToOpen;
    }

    private void setShouldOpenActivity(boolean shouldOpenActivity) {
        this.shouldOpenActivity = shouldOpenActivity;
    }


    public Toolbar getToolbar() {
        return mToolbar;
    }

    private void pingTime() {
        Call<ResponseBody> pingTimeCall = Retrofit.getInstance().getInkService().pingTime(mSharedHelper.getUserId());
        pingTimeCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    pingTime();
                    return;
                }
                if (response.body() == null) {
                    pingTime();
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    try {
                        PingResponse pingResponse = gson.fromJson(responseBody, PingResponse.class);
                        if (pingResponse.success) {
                            if (timer == null) {
                                timer = new Timer();
                            }
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    pingTime();
                                }
                            }, PING_TIME);
                        } else {
                            pingTime();
                        }
                    } catch (Exception e) {
                        destroyPinger();
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                pingTime();
            }
        });
    }

    @Override
    protected void onDestroy() {
        destroyPinger();
        super.onDestroy();
    }

    private void destroyPinger() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (mPingThread != null) {
            mPingThread.interrupt();
            mPingThread = null;
        }
    }


    @Override
    public void onAccountDeleted() {
        destroyPinger();
    }
}
