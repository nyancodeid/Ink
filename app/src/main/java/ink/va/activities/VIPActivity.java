package ink.va.activities;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.ink.va.R;

public class VIPActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vip);
        setStatusBarColor(R.color.vip_status_bar_color);
        setStatusBarColor(R.color.vip_status_bar_color);
        ActionBar actionBar = getSupportActionBar();
        getWindow().setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.vip_background));
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(R.string.vip_chat_title));
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }
}
