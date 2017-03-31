package ink.va.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.ink.va.R;
import com.koushikdutta.ion.Ion;

import butterknife.BindView;
import butterknife.ButterKnife;
import ink.va.utils.CircleTransform;
import ink.va.utils.Constants;
import ink.va.utils.Version;

public class About extends BaseActivity {
    @BindView(R.id.versionTV)
    TextView versionTV;
    @BindView(R.id.developerCredit)
    ImageView developerCredit;
    @BindView(R.id.uxCredit)
    ImageView uxCredit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);
        String versionName = Version.getVersionName(this);
        versionTV.setText(getString(R.string.versionName, versionName));
        getSupportActionBar().setTitle(getString(R.string.aboutTitle));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Ion.with(this).load(Constants.ANDROID_DRAWABLE_DIR + "me")
                .withBitmap().placeholder(R.drawable.user_image_placeholder).transform(new CircleTransform()).intoImageView(developerCredit);

        Ion.with(this).load(Constants.ANDROID_DRAWABLE_DIR + "jean")
                .withBitmap().placeholder(R.drawable.user_image_placeholder).transform(new CircleTransform()).intoImageView(uxCredit);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);

    }
}
