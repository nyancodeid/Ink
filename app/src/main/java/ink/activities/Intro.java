package ink.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.ink.R;

import ink.animations.DepthPageTransformer;
import ink.fragments.FirstIntroFragment;
import ink.fragments.SecondIntroFragment;
import ink.fragments.ThirdIntroFragment;
import ink.utils.SharedHelper;

/**
 * Created by USER on 2016-06-19.
 */
public class Intro extends AppCompatActivity {

    private Intent mLoginIntent;
    private SharedHelper mSharedHelper;
    private static final int NUM_PAGES = 3;
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    private FirstIntroFragment firstIntroFragment;
    private SecondIntroFragment secondIntroFragment;
    private ThirdIntroFragment thirdIntroFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro_view);
        mLoginIntent = new Intent(getApplicationContext(), Login.class);
        mSharedHelper = new SharedHelper(this);
        firstIntroFragment = FirstIntroFragment.create();
        secondIntroFragment = SecondIntroFragment.create();
        thirdIntroFragment = ThirdIntroFragment.create();

        if (mSharedHelper.shouldShowIntro()) {
            startActivity(mLoginIntent);
            finish();
        } else {
            mPager = (ViewPager) findViewById(R.id.pager);
            mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
            mPager.setAdapter(mPagerAdapter);
            mPager.setPageTransformer(true, new DepthPageTransformer());
            mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    switch (position) {
                        case 0:
                            firstIntroFragment.startAnimation();
                            break;
                        case 1:
                            secondIntroFragment.startAnimation();
                            break;
                        case 2:
                            thirdIntroFragment.startAnimation();
                            break;
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }
//        startActivity(mLoginIntent);
//        mSharedHelper.putShouldShowIntro(true);
//        finish();
    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return firstIntroFragment;
                case 1:
                    return secondIntroFragment;
                case 2:
                    return thirdIntroFragment;
                default:
                    return firstIntroFragment;
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }
}
