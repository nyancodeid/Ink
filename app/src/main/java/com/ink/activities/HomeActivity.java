package com.ink.activities;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.ink.fragments.Feed;
import com.ink.R;
import com.ink.utils.CircleTransform;
import com.ink.utils.SharedHelper;
import com.squareup.picasso.Picasso;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private com.github.clans.fab.FloatingActionMenu mFab;
    private ImageView mProfileImage;
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
        mFab = (com.github.clans.fab.FloatingActionMenu) findViewById(R.id.fab);
        mMessages = (FloatingActionButton) findViewById(R.id.messages);
        mNewPost = (FloatingActionButton) findViewById(R.id.makePost);
        mFeed = Feed.newInstance();
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
                if (mToolbar.getTitle().equals(PROFILE)) {
                    mDrawer.closeDrawer(Gravity.LEFT);
                }
                break;
            case R.id.feeds:
                if (mToolbar.getTitle().equals(FEED)) {
                    mDrawer.closeDrawer(Gravity.LEFT);
                }
                break;
            case R.id.messages:

                break;
            case R.id.groups:
                break;
            case R.id.friends:
                break;
            case R.id.settings:
                break;
            case R.id.nav_share:
                break;
            case R.id.nav_send:
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
