package ink.va.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.widget.ImageView;

import com.ink.va.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.sephiroth.android.library.picasso.Picasso;

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

        Picasso.with(this).load(R.drawable.vip_background).into(rootImageView);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }
}
