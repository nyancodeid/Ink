package ink.va.activities;

import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.TextView;

import com.ink.va.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ExploreVipActivity extends BaseActivity {

    @Bind(R.id.exploreVipRootTitle)
    TextView exploreVipRootTitle;
    private Typeface typeface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore_vip);
        ButterKnife.bind(this);
        hideActionBar();
        typeface = Typeface.createFromAsset(getAssets(), "fonts/vip_regular.ttf");
        exploreVipRootTitle.setTypeface(typeface);
    }
}
