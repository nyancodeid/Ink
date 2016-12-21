package ink.va.activities;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.Html;
import android.widget.TextView;

import com.ink.va.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.va.utils.Constants;

public class ServerNotification extends BaseActivity {


    @Bind(R.id.serverNewsContent)
    TextView serverNewsContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_notification);
        ButterKnife.bind(this);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.serverNewsTitle);
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String content = bundle.getString(Constants.SERVER_NOTIFICATION_CONTENT_BUNDLE_KEY);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                serverNewsContent.setText(Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY));
            } else {
                serverNewsContent.setText(Html.fromHtml(content));
            }

        }
    }


    @OnClick(R.id.closeServerNotification)
    public void close() {
        finish();
        overridePendingTransition(R.anim.activity_scale_up, R.anim.activity_scale_down);
    }

    @Override
    public void onBackPressed() {

    }
}
