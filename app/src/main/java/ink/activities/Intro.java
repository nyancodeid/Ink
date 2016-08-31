package ink.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.ink.R;

import ink.utils.SharedHelper;

/**
 * Created by USER on 2016-06-19.
 */
public class Intro extends AppIntro2 {

    private Intent mLoginIntent;
    private SharedHelper mSharedHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLoginIntent = new Intent(getApplicationContext(), Login.class);
        mSharedHelper = new SharedHelper(this);

        if (mSharedHelper.shouldShowIntro()) {
            startActivity(mLoginIntent);
            finish();
        } else {
            addSlide(AppIntroFragment.newInstance(getString(R.string.randomChatTitle), getString(R.string.randomChatFullText), R.drawable.random_chat_image, ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary)));
            addSlide(AppIntroFragment.newInstance(getString(R.string.newFriendsTitle), getString(R.string.newFriendsFullText), R.drawable.find_new_friends, ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary)));
            addSlide(AppIntroFragment.newInstance(getString(R.string.customizeAppTitle), getString(R.string.customizeAppFullText), R.drawable.redesign_icon, ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary)));
        }

    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        startActivity(mLoginIntent);
        mSharedHelper.putShouldShowIntro(true);
        finish();
        // Do something when users tap on Skip button.
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        // Do something when users tap on Done button.
        startActivity(mLoginIntent);
        mSharedHelper.putShouldShowIntro(true);
        finish();
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        // Do something when the slide changes.
    }
}
