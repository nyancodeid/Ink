package ink.va.activities;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.ink.va.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.va.utils.Animations;
import ink.va.utils.Dp;
import ink.va.utils.ProcessManager;


public class BlackJack extends BaseActivity {

    @BindView(R.id.playerSecondCard)
    ImageView playerSecondCard;
    @BindView(R.id.playerLayout)
    LinearLayout playerLayout;
    @BindView(R.id.dealerLayout)
    LinearLayout dealerLayout;
    private int layoutNegativeMargin;
    private int imageViewWidth;
    private int imageViewHeight;
    private Animation scaleInAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_black_jack);
        ButterKnife.bind(this);
        layoutNegativeMargin = Dp.toDps(this, 60);
        imageViewWidth = Dp.toDps(this, 120);
        imageViewHeight = Dp.toDps(this, 160);
        scaleInAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_in_fast);

        if (ProcessManager.hasHacks(this)) {

        } else {

        }
    }

    @OnClick(R.id.flipIt)
    public void flipItClicked() {
        final ImageView imageView = new ImageView(this);
        imageView.setBackground(ContextCompat.getDrawable(this, R.drawable.card_background));
        imageView.setLayoutParams(new LinearLayout.LayoutParams((int) getResources().getDimension(R.dimen.image_view_width), (int) getResources().getDimension(R.dimen.image_view_height)));
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) imageView.getLayoutParams();
        layoutParams.setMargins((int) getResources().getDimension(R.dimen.image_view_negative_margin), 0, 0, 0);
        imageView.setLayoutParams(layoutParams);
        playerLayout.addView(imageView);
        scaleInAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Animations.flip(imageView, ContextCompat.getDrawable(BlackJack.this, R.drawable.ace_black_heart));
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        imageView.startAnimation(scaleInAnimation);

    }
}
