package ink.va.activities;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ink.va.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static ink.va.utils.MembershipTypes.MEMBERSHIP_TYPE_BLACK;
import static ink.va.utils.MembershipTypes.MEMBERSHIP_TYPE_GOLD;
import static ink.va.utils.MembershipTypes.MEMBERSHIP_TYPE_RED;


public class VIPActivity extends BaseActivity {

    @Bind(R.id.rootImageView)
    ImageView rootImageView;

    @Bind(R.id.exploreVip)
    Button exploreButton;

    @Bind(R.id.vipGlobalChat)
    Button globalChatButton;

    @Bind(R.id.vipRootTitle)
    TextView vipRootTitle;

    @Bind(R.id.giftWrapper)
    View giftWrapper;

    @Bind(R.id.closeGiftView)
    Button closeGiftView;

    @Bind(R.id.membershipTypeTV)
    TextView membershipTypeTV;

    @Bind(R.id.membershipTitle)
    TextView membershipTitle;

    @Bind(R.id.giftImageView)
    ImageView giftImageView;

    private Animation slideInWithFade;
    private Animation slideOutWithFade;
    private Typeface typeface;
    private String giftType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vip);
        ButterKnife.bind(this);
        setStatusBarColor(R.color.vip_status_bar_color);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        slideInWithFade = AnimationUtils.loadAnimation(this, R.anim.slide_in_with_fade);
        slideOutWithFade = AnimationUtils.loadAnimation(this, R.anim.slide_out_with_fade);

        typeface = Typeface.createFromAsset(getAssets(), "fonts/vip_regular.ttf");
        vipRootTitle.setTypeface(typeface);

        Bundle extras = getIntent().getExtras();
        boolean isFirstVipLogin = extras != null ? extras.getBoolean("firstVipLogin") : false;
        boolean hasGift = extras != null ? extras.getBoolean("hasGift") : false;
        giftType = extras != null ? extras.getString("giftType") : null;
        int resourceId = R.drawable.black_member_card;
        if (giftType != null) {
            switch (giftType) {
                case MEMBERSHIP_TYPE_GOLD:
                    resourceId = R.drawable.gold_member_card;
                    break;
                case MEMBERSHIP_TYPE_RED:
                    resourceId = R.drawable.red_member_card;
                    break;
                case MEMBERSHIP_TYPE_BLACK:
                    resourceId = R.drawable.black_member_card;
                    break;
            }
        }
        if (hasGift) {
            giftImageView.setImageResource(resourceId);
        }

        membershipTypeTV.setText(giftType != null ? getString(R.string.membershipType, giftType) : "");
        if (isFirstVipLogin) {
            changeButtonVisibility(true);
            showIntro(hasGift);
        } else {
            changeButtonVisibility(false);
            loadMenu();
        }
    }

    private void loadMenu() {
        exploreButton.startAnimation(slideInWithFade);
        globalChatButton.startAnimation(slideInWithFade);
    }

    private void showIntro(boolean hasGift) {
        if (hasGift) {
            closeGiftView.setClickable(false);
            closeGiftView.setEnabled(false);
            giftWrapper.setVisibility(View.VISIBLE);
            slideInWithFade.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    closeGiftView.setClickable(true);
                    closeGiftView.setEnabled(true);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            giftWrapper.startAnimation(slideInWithFade);
        } else {

        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.vipGlobalChat)
    public void globeChatClicked() {
        showIntro(true);
    }

    @OnClick(R.id.exploreVip)
    public void exploreClicked() {

    }

    @OnClick(R.id.closeGiftView)
    public void closeCLicked() {
        giftWrapper.clearAnimation();
        closeGiftView.setClickable(false);
        closeGiftView.setEnabled(false);
        giftWrapper.startAnimation(slideOutWithFade);
        slideOutWithFade.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                giftWrapper.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void changeButtonVisibility(boolean hide) {
        exploreButton.setVisibility(hide ? View.GONE : View.VISIBLE);
        globalChatButton.setVisibility(hide ? View.GONE : View.VISIBLE);
    }
}
