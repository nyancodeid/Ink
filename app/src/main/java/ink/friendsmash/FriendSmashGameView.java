package ink.friendsmash;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ink.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FriendSmashGameView extends AppCompatActivity {

    @Bind(R.id.smashPlayerText)
    TextView smashPlayerText;
    @Bind(R.id.scoreText)
    TextView scoreText;
    @Bind(R.id.gameFrameView)
    RelativeLayout gameFrameView;
    private JSONArray friends;
    private Runnable fireImagesRunnable;
    private String friendToSmashId;
    private Handler handler;
    private ArrayList<UserImageView> userImageViews;
    private int iconWidth;
    private int screenWidth;
    private int screenHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_smash_game_view);
        ButterKnife.bind(this);
        friends = FriendSmashHelper.get().getFriends();
        setUpImages();
        Random random = new Random();
        userImageViews = new ArrayList<>();
        int randomFriendId = random.nextInt(friends.length());
        JSONObject friendToSmashObject = FriendSmashHelper.get().getFriend(randomFriendId);
        friendToSmashId = friendToSmashObject.optString("id");
        String friendToSmashName = friendToSmashObject.optString("name");
        handler = new Handler(Looper.getMainLooper());
        smashPlayerText.setText(getString(R.string.smash_player_text, friendToSmashName));
        iconWidth = getResources().getDimensionPixelSize(R.dimen.icon_width);
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;
        fireImagesRunnable = new Runnable() {
            @Override
            public void run() {
                setUpImages();
            }
        };
    }

    private void setUpImages() {
        Random random = new Random();
        int friendIndex = random.nextInt(friends.length());
        JSONObject friendObject = FriendSmashHelper.get().getFriend(friendIndex);
        String imageUrl = friendObject.optString("image");
        String friendId = friendObject.optString("id");
        boolean shouldSmash = false;
        boolean isCoin = false;
        if (friendId.equals(friendToSmashId)) {
            shouldSmash = true;
        }

        final UserImageView userImageView = (new UserImageView(getApplicationContext(), shouldSmash, isCoin));
        userImageViews.add(userImageView);
        final boolean finalShouldSmash = shouldSmash;
        userImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (finalShouldSmash) {
                    Log.d("Fasfasfsafasf", "onClick: should smash");
                } else {
                    Log.d("Fasfasfsafasf", "onClick: should not smash");
                }
                userImageViews.remove(view);
            }
        });
        userImageView.setLayoutParams(new LinearLayout.LayoutParams(iconWidth, iconWidth));
        gameFrameView.addView(userImageView);
        fireImages(userImageView);
        handler.postDelayed(fireImagesRunnable, 700);
    }


    private void fireImages(final UserImageView imageView) {
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

                    hideAndRemove(imageView);
                }
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationStart(Animator animation) {
            }
        });
    }

    private void hideAndRemove(UserImageView userImageView) {
        if (userImageView.getVisibility() == View.VISIBLE) {
            userImageView.setVisibility(View.GONE);
        }

        gameFrameView.removeView(userImageView);

        userImageViews.remove(userImageView);
    }

}
