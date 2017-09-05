package kashmirr.social.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.kashmirr.social.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import kashmirr.social.utils.ImageLoader;
import kashmirr.social.utils.Version;


public class About extends BaseActivity {
    @BindView(R.id.versionTV)
    TextView versionTV;
    @BindView(R.id.developerCredit)
    ImageView developerCredit;
    @BindView(R.id.uxCredit)
    ImageView uxCredit;
    @BindView(R.id.adminIV)
    ImageView adminIV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);
        String versionName = Version.getVersionName(this);
        versionTV.setText(getString(R.string.versionName, versionName));
        getSupportActionBar().setTitle(getString(R.string.aboutTitle));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ImageLoader.loadImage(this, true, true, null, R.drawable.me, R.drawable.user_image_placeholder, developerCredit, null);
        ImageLoader.loadImage(this, true, true, null, R.drawable.jean, R.drawable.user_image_placeholder, uxCredit, null);
        ImageLoader.loadImage(this, true, true, null, R.drawable.azan, R.drawable.user_image_placeholder, adminIV, null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);

    }
}
