package ink.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionMenu;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.ink.R;
import com.squareup.picasso.Picasso;

import fab.FloatingActionButton;
import ink.fragments.Feed;
import ink.fragments.MyFriends;
import ink.utils.CircleTransform;
import ink.utils.SharedHelper;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private FloatingActionMenu mFab;
    private ImageView mProfileImage;
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
        mFab = (FloatingActionMenu) findViewById(R.id.fab);
        mMessages = (FloatingActionButton) findViewById(R.id.messages);
        mNewPost = (FloatingActionButton) findViewById(R.id.makePost);
        mFeed = Feed.newInstance();
        mMyFriends = MyFriends.newInstance();
        mMessages.setOnClickListener(this);
        mNewPost.setOnClickListener(this);


        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        if (mDrawer != null) {
            mDrawer.addDrawerListener(toggle);
        }
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);

        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, mFeed).commit();

        mProfileImage = (ImageView) headerView.findViewById(R.id.profileImage);
        if (mSharedHelper.hasImage()) {

        } else {
            Picasso.with(getApplicationContext()).load(R.mipmap.ic_launcher).transform(new CircleTransform()).into(mProfileImage);
        }
        navigationView.setNavigationItemSelectedListener(this);
    }

    public FloatingActionMenu getFab() {
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
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.profile:
                if (!mToolbar.getTitle().equals(PROFILE)) {
                    mToolbar.setTitle(getString(R.string.profileText));
                }
                break;
            case R.id.feeds:
                if (!mToolbar.getTitle().equals(FEED)) {
                    mToolbar.setTitle(getString(R.string.feedText));
                    getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, mFeed).commit();
                }
                break;
            case R.id.messages:
                if (!mToolbar.getTitle().equals(MESSAGES)) {
                    mToolbar.setTitle(getString(R.string.messageText));
                }
                break;
            case R.id.groups:
                if (!mToolbar.getTitle().equals(GROUPS)) {
                    mToolbar.setTitle(getString(R.string.groupsText));
                }
                break;
            case R.id.friends:
                if (!mToolbar.getTitle().equals(FRIENDS)) {
                    mToolbar.setTitle(getString(R.string.friendsText));
                    getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, mMyFriends).commit();
                }
                break;
            case R.id.settings:
                if (!mToolbar.getTitle().equals(SETTINGS)) {
                    mToolbar.setTitle(getString(R.string.settingsString));
                }
                break;
            case R.id.nav_share:
                break;
            case R.id.nav_send:
                break;
            case R.id.logout:
                mSharedHelper.clean();
                mSharedHelper.putShouldShowIntro(false);
                startActivity(new Intent(getApplicationContext(), Login.class));
                finish();
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
                break;
            case R.id.makePost:
                mFab.close(true);
                break;
        }
    }
}
