package ink.va.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

import com.ink.va.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class VIPActivity extends BaseActivity {

    @Bind(R.id.rootImageView)
    ImageView rootImageView;

    @Bind(R.id.exploreVip)
    Button vipShimmerButton;

    @Bind(R.id.vipGlobalChat)
    Button vipGlobalChat;

    private Animation slideInWithFade;

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


        Bundle extras = getIntent().getExtras();
        boolean isFirstVipLogin = extras != null ? extras.getBoolean("firstVipLogin") : false;
        boolean hasGift = extras != null ? extras.getBoolean("hasGift") : false;
        if (isFirstVipLogin) {
            showIntro();
        } else {
            loadMenu();
        }
    }

    private void loadMenu() {
        vipShimmerButton.startAnimation(slideInWithFade);
        vipGlobalChat.startAnimation(slideInWithFade);
    }

    private void showIntro() {

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.vipGlobalChat)
    public void globeChatClicked() {

    }

    @OnClick(R.id.exploreVip)
    public void exploreClicked() {

    }
}
