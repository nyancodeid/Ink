package ink.va.activities;

import android.os.Bundle;
import android.widget.TextView;

import com.ink.va.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import ink.va.utils.Version;

public class About extends BaseActivity {
    @BindView(R.id.versionTV)
    TextView versionTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);
        String versionName = Version.getVersionName(this);
        versionTV.setText(getString(R.string.versionName, versionName));
    }
}
