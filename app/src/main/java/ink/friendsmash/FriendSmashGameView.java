package ink.friendsmash;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ink.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;
import ink.activities.BaseActivity;
import ink.animations.RainbowAnimation;

public class FriendSmashGameView extends BaseActivity {

    @Bind(R.id.smashPlayerText)
    TextView smashPlayerText;
    @Bind(R.id.scoreText)
    TextView scoreText;
    @Bind(R.id.gameFrameView)
    RelativeLayout gameFrameView;
    @Bind(R.id.liveHolder)
    LinearLayout liveHolder;
    @Bind(R.id.encourageText)
    TextView encourageText;
    private JSONArray friends;
    private Runnable fireImagesRunnable;
    private String friendToSmashId;
    private String friendToSmashImageUrl;
    private Handler handler;
    private ArrayList<UserImageView> userImageViews;
    private int iconWidth;
    private int screenWidth;
    private int screenHeight;
    public static final int IMAGES_MAX_FREQUENCY = 6;
    private long fireImagesSpeedTime = 1000;
    private int firedImagesCount = 0;
    private int desiredCurrentFrequency = 0;
    private int timesSmashed = 0;
    private Random frequencyRandom;
    private Random friendIndexRandom;
    private MediaPlayer boomShakaLakapLayer;
    private MediaPlayer impressivePlayer;
    private MediaPlayer fatalityPlayer;
    private int countTilLSound = 0;
    private boolean stopCalled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_smash_game_view);
        ButterKnife.bind(this);
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/cartoon_font.ttf");
        encourageText.setTypeface(tf, Typeface.BOLD);

        friends = FriendSmashHelper.get().getFriends();
        boomShakaLakapLayer = MediaPlayer.create(this, R.raw.boom_shakka_lakka);
        impressivePlayer = MediaPlayer.create(this, R.raw.impressive);
        fatalityPlayer = MediaPlayer.create(this, R.raw.fatality);
        Random random = new Random();
        userImageViews = new ArrayList<>();

        int randomFriendId = random.nextInt(friends.length());
        JSONObject friendToSmashObject = FriendSmashHelper.get().getFriend(randomFriendId);
        friendToSmashId = friendToSmashObject.optString("id");
        friendToSmashImageUrl = friendToSmashObject.optString("image");

        String friendToSmashName = friendToSmashObject.optString("name");
        handler = new Handler(Looper.getMainLooper());
        smashPlayerText.setText(getString(R.string.smash_player_text, friendToSmashName));
        iconWidth = getResources().getDimensionPixelSize(R.dimen.icon_width);
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        frequencyRandom = new Random();

        friendIndexRandom = new Random();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;
        fireImagesRunnable = new Runnable() {
            @Override
            public void run() {
                if (!stopCalled) {
                    setUpImages();
                }
            }
        };
        scoreText.setText(getString(R.string.score_text, 0));
        setUpImages();

    }

    @Override
    protected void onDestroy() {
        fireImagesRunnable = null;
        handler = null;
        super.onDestroy();
    }


    private void setUpImages() {
        System.gc();
        int friendIndex = friendIndexRandom.nextInt(friends.length());
        JSONObject friendObject = FriendSmashHelper.get().getFriend(friendIndex);


        String imageUrl = friendObject.optString("image");
        String friendId = friendObject.optString("id");

        if (firedImagesCount >= desiredCurrentFrequency) {
            imageUrl = friendToSmashImageUrl;
            friendId = friendToSmashId;
            firedImagesCount = 0;
            desiredCurrentFrequency = frequencyRandom.nextInt(IMAGES_MAX_FREQUENCY);
        }

        boolean shouldSmash = false;
        boolean isCoin = false;
        if (friendId.equals(friendToSmashId)) {
            shouldSmash = true;
        }


        final UserImageView userImageView = (new UserImageView(getApplicationContext(), shouldSmash, isCoin));
        final boolean finalShouldSmash = shouldSmash;
        userImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (finalShouldSmash) {
                    stopCalled = false;
                    view.setVisibility(View.GONE);
                    userImageViews.remove(view);
                    timesSmashed++;
                    countTilLSound++;
                    scoreText.setText(getString(R.string.score_text, timesSmashed));
                    switch (countTilLSound) {
                        case 2:
                            showEncourageText(getString(R.string.weGood));
                            break;
                        case 10:
                            showEncourageText(getString(R.string.impressive));
                            if (impressivePlayer.isPlaying()) {
                                impressivePlayer.stop();
                            }
                            impressivePlayer.start();
                            break;
                        case 15:
                            showEncourageText(getString(R.string.boomShakkaLakkaText));
                            if (boomShakaLakapLayer.isPlaying()) {
                                boomShakaLakapLayer.stop();
                            }
                            boomShakaLakapLayer.start();
                            break;
                        case 25:
                            showEncourageText(getString(R.string.whosYourDaddy));
                            countTilLSound = 0;
                            break;
                    }
                } else {
                    timesSmashed = 0;
                    wrongImageSmashed(userImageView);
                }

                return false;
            }
        });
        userImageView.setLayoutParams(new LinearLayout.LayoutParams(iconWidth, iconWidth));
        gameFrameView.addView(userImageView);
        userImageViews.add(userImageView);
        fetchFriendImage(imageUrl, userImageView);
    }

    private void fetchFriendImage(final String imageURLString, final UserImageView userImageView) {
        AsyncTask.execute(new Runnable() {
            public void run() {
                try {
                    URL imageURL = new URL(imageURLString);
                    final Bitmap friendToSmashBitmap = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            fireImages(userImageView, friendToSmashBitmap);
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void fireImages(final UserImageView imageView, final Bitmap friendBitmap) {
        imageView.setImageBitmap(friendBitmap);
        imageView.setupAndStartAnimations(iconWidth, iconWidth, screenWidth, screenHeight, new Animator.AnimatorListener() {
            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!imageView.isWrongImageSmashed()) {
                    if (imageView.getVisibility() == View.VISIBLE && imageView.shouldSmash() && !imageView.isVoid() && !imageView.isCoin()) {
                        // Image is still visible, so user didn't smash it and they should have done (and it isn't void), so decrement the lives by one
                        // TODO: 8/29/2016 reduce live by 1
                    }

                    hideAndRemove(imageView, friendBitmap);
                }
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationStart(Animator animation) {
                firedImagesCount++;
            }
        });
        handler.postDelayed(fireImagesRunnable, fireImagesSpeedTime);
    }

    private void hideAndRemove(UserImageView userImageView, Bitmap friendBitmap) {
        if (userImageView.getVisibility() == View.VISIBLE) {
            userImageView.setVisibility(View.GONE);
        }

        gameFrameView.removeView(userImageView);

        userImageViews.remove(userImageView);
        friendBitmap.recycle();
    }

    private void wrongImageSmashed(final UserImageView userImageView) {
        handler.removeCallbacks(fireImagesRunnable);
        userImageView.setWrongImageSmashed(true);
        userImageView.stopMovementAnimations();
        stopCalled = true;
        hideAllUserImageViewsExcept(userImageView);

        userImageView.scaleUp(new Animator.AnimatorListener() {
            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                userImageView.stopRotationAnimation();
                // TODO: 8/29/2016 set lives to 0
                if (userImageViews.contains(userImageView)) {
                    userImageViews.remove(userImageView);
                }
                userImageView.setVisibility(View.GONE);
                // TODO: 8/29/2016 show lose view


            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationStart(Animator animation) {
            }
        });
        gameFrameView.bringChildToFront(userImageView);
    }

    private void showEncourageText(String textToShow) {
        encourageText.setText(textToShow);
        encourageText.setVisibility(View.VISIBLE);


        ValueAnimator scaleAnimationX = ObjectAnimator.ofFloat(encourageText, "scaleX", 2f);
        ValueAnimator scaleAnimationY = ObjectAnimator.ofFloat(encourageText, "scaleY", 2f);
        scaleAnimationX.setDuration(2000);
        scaleAnimationY.setDuration(2000);
//        scaleAnimationX.setRepeatCount(1);
//        scaleAnimationY.setRepeatCount(1);
//        scaleAnimationY.setRepeatMode(ValueAnimator.RESTART);
//        scaleAnimationX.setRepeatMode(ValueAnimator.RESTART);
        scaleAnimationX.setInterpolator(new LinearInterpolator());
        scaleAnimationY.setInterpolator(new LinearInterpolator());
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleAnimationX, scaleAnimationY);

        RainbowAnimation.get().startRainbowAnimation(this, textToShow, encourageText);

        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                RainbowAnimation.get().stopRainbowAnimation();
                encourageText.setVisibility(View.GONE);
                encourageText.clearAnimation();

                ValueAnimator scaleAnimationX = ObjectAnimator.ofFloat(encourageText, "scaleX", 1f);
                ValueAnimator scaleAnimationY = ObjectAnimator.ofFloat(encourageText, "scaleY", 1f);
                scaleAnimationX.setInterpolator(new LinearInterpolator());
                scaleAnimationY.setInterpolator(new LinearInterpolator());
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.playTogether(scaleAnimationX, scaleAnimationY);
                scaleAnimationX.setDuration(100);
                scaleAnimationY.setDuration(100);
                animatorSet.start();

            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        animatorSet.start();


    }

    private void hideAllUserImageViewsExcept(UserImageView userImageView) {
        Iterator<UserImageView> userImageViewsIterator = userImageViews.iterator();
        while (userImageViewsIterator.hasNext()) {
            UserImageView currentUserImageView = userImageViewsIterator.next();
            if (!currentUserImageView.equals(userImageView)) {
                currentUserImageView.setVisibility(View.GONE);
            }
        }
    }
}
