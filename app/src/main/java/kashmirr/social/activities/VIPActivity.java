package kashmirr.social.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.kashmirr.social.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import kashmirr.social.interfaces.RequestCallback;
import kashmirr.social.utils.DialogUtils;
import kashmirr.social.utils.Retrofit;
import kashmirr.social.utils.SharedHelper;
import okhttp3.ResponseBody;

import static kashmirr.social.utils.MembershipTypes.MEMBERSHIP_TYPE_BLACK;
import static kashmirr.social.utils.MembershipTypes.MEMBERSHIP_TYPE_GOLD;
import static kashmirr.social.utils.MembershipTypes.MEMBERSHIP_TYPE_RED;


public class VIPActivity extends BaseActivity {

    @BindView(R.id.exploreVip)
    Button exploreButton;

    @BindView(R.id.vipGlobalChat)
    Button globalChatButton;

    @BindView(R.id.vipRootTitle)
    TextView vipRootTitle;

    @BindView(R.id.giftWrapper)
    View giftWrapper;

    @BindView(R.id.acceptGift)
    Button closeGiftView;

    @BindView(R.id.membershipTypeTV)
    TextView membershipTypeTV;

    @BindView(R.id.membershipTitle)
    TextView membershipTitle;

    @BindView(R.id.giftImageView)
    ImageView giftImageView;

    private Animation slideInWithFade;
    private Animation slideOutWithFade;
    private Typeface typeface;
    private String giftType;
    private String chosenMembership;
    private SharedHelper sharedHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vip);
        ButterKnife.bind(this);
        setStatusBarColor(R.color.vip_status_bar_color);
        sharedHelper = new SharedHelper(this);
        hideActionBar();
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
                    chosenMembership = MEMBERSHIP_TYPE_GOLD;
                    resourceId = R.drawable.gold_member_card;
                    break;
                case MEMBERSHIP_TYPE_RED:
                    chosenMembership = MEMBERSHIP_TYPE_RED;
                    resourceId = R.drawable.red_member_card;
                    break;
                case MEMBERSHIP_TYPE_BLACK:
                    chosenMembership = MEMBERSHIP_TYPE_BLACK;
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
            chosenMembership = extras != null ? extras.containsKey("membershipType") ? extras.getString("membershipType") : null : null;
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
            changeButtonVisibility(false);
            loadMenu();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        overrideActivityAnimation();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
        overrideActivityAnimation();
    }

    @OnClick(R.id.vipGlobalChat)
    public void globeChatClicked() {
        Intent intent = new Intent(this, GlobalVipChat.class);
        intent.putExtra("membershipType", chosenMembership);
        startActivity(intent);
        overrideActivityAnimation();
    }

    @OnClick(R.id.exploreVip)
    public void exploreClicked() {
        Intent intent = new Intent(this, ExploreVipActivity.class);
        intent.putExtra("membershipType", chosenMembership);
        startActivity(intent);
        overrideActivityAnimation();
    }

    @OnClick(R.id.acceptGift)
    public void acceptGiftClicked() {
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
                showVipLoading();
                changeMembership();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void changeMembership() {
        makeRequest(Retrofit.getInstance().getInkService().changeMembership(sharedHelper.getUserId(), chosenMembership), null, new RequestCallback() {
            @Override
            public void onRequestSuccess(Object result) {
                hideVipLoading();
                changeButtonVisibility(false);
                loadMenu();
                try {
                    String responseBody = ((ResponseBody) result).string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    if (!success) {
                        DialogUtils.showDialog(VIPActivity.this, getString(R.string.error), getString(R.string.membershipChangeError), false, null, false, null);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onRequestFailed(Object[] result) {
                hideVipLoading();
            }
        });
    }

    private void changeButtonVisibility(boolean hide) {
        exploreButton.setVisibility(hide ? View.GONE : View.VISIBLE);
        globalChatButton.setVisibility(hide ? View.GONE : View.VISIBLE);
    }


}
