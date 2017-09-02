package kashmirr.social.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.kashmirr.social.R;

import kashmirr.social.animations.DepthPageTransformer;
import kashmirr.social.fragments.FirstIntroFragment;
import kashmirr.social.fragments.SecondIntroFragment;
import kashmirr.social.fragments.ThirdIntroFragment;
import kashmirr.social.utils.SharedHelper;

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
    private ImageView centerCircle;
    private ImageView leftCircle;
    private ImageView rightCircle;
    private Button skipIntroButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro_view);
        mLoginIntent = new Intent(getApplicationContext(), Login.class);
        mSharedHelper = new SharedHelper(this);
        firstIntroFragment = FirstIntroFragment.create();
        secondIntroFragment = SecondIntroFragment.create();
        thirdIntroFragment = ThirdIntroFragment.create();
        centerCircle = (ImageView) findViewById(R.id.centerCircle);
        leftCircle = (ImageView) findViewById(R.id.leftCircle);
        rightCircle = (ImageView) findViewById(R.id.rightCircle);
        skipIntroButton = (Button) findViewById(R.id.skipIntroButton);
        skipIntroButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(mLoginIntent);
                mSharedHelper.putShouldShowIntro(true);
                finish();
            }
        });
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
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
                            secondIntroFragment.hideItems();
                            thirdIntroFragment.hideItems();
                            firstIntroFragment.startAnimation();
                            leftCircle.setImageResource(R.drawable.circle_active);

                            leftCircle.setAlpha(1f);
                            centerCircle.setImageResource(R.drawable.circle_inactive);
                            rightCircle.setImageResource(R.drawable.circle_inactive);
                            break;
                        case 1:
                            firstIntroFragment.hideItems();
                            thirdIntroFragment.hideItems();
                            secondIntroFragment.startAnimation();

                            leftCircle.setImageResource(R.drawable.circle_inactive);
                            centerCircle.setAlpha(1f);
                            centerCircle.setImageResource(R.drawable.circle_active);
                            rightCircle.setImageResource(R.drawable.circle_inactive);

                            break;
                        case 2:
                            firstIntroFragment.hideItems();
                            secondIntroFragment.hideItems();
                            thirdIntroFragment.startAnimation();

                            leftCircle.setImageResource(R.drawable.circle_inactive);
                            centerCircle.setImageResource(R.drawable.circle_inactive);
                            rightCircle.setImageResource(R.drawable.circle_active);
                            rightCircle.setAlpha(1f);
                            break;
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }
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
