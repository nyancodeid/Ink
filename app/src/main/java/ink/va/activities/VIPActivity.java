package ink.va.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.widget.ImageView;

import com.ink.va.R;
import com.koushikdutta.ion.Ion;

import butterknife.Bind;
import butterknife.ButterKnife;
import ink.va.utils.Constants;

public class VIPActivity extends BaseActivity {

    @Bind(R.id.rootImageView)
    ImageView rootImageView;

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

        Ion.with(this).load(Constants.ANDROID_DRAWABLE_DIR + "vip_background").intoImageView(rootImageView);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }
}
