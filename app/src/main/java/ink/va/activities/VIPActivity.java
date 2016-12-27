package ink.va.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.ink.va.R;
import com.romainpiel.shimmer.ShimmerButton;

import butterknife.Bind;
import butterknife.ButterKnife;

public class VIPActivity extends BaseActivity {

    @Bind(R.id.rootImageView)
    ImageView rootImageView;

    @Bind(R.id.vipShimmerButton)
    ShimmerButton vipShimmerButton;

    private Animation animation;

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
        animation = AnimationUtils.loadAnimation(this, R.anim.slide_in_with_fade);
        vipShimmerButton.animate();
        vipShimmerButton.startAnimation(animation);

        Bundle extras = getIntent().getExtras();
        boolean isFirstVipLogin = extras != null ? extras.getBoolean("firstVipLogin") : false;
        if (isFirstVipLogin) {
            showIntro();
        } else {
            loadMenu();
        }
    }

    private void loadMenu() {

    }

    private void showIntro() {

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }
}
