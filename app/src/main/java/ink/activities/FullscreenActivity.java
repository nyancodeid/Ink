package ink.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.ink.R;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FullscreenActivity extends BaseActivity {
    private String fullUrlToLoad;

    @Bind(R.id.fullscreen_content)
    ImageView mContentView;
    @Bind(R.id.imageLoadingProgress)
    LinearLayout imageLoadingProgress;
    @Bind(R.id.loadingProgressBar)
    ProgressBar loadingProgressBar;
    private boolean mVisible;
    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);
        ButterKnife.bind(this);
        Bundle extras = getIntent().getExtras();
        mVisible = true;
        mContentView = (ImageView) findViewById(R.id.fullscreen_content);

        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (extras != null) {
            fullUrlToLoad = extras.getString("link");
            Ion.with(this).load(fullUrlToLoad).progressHandler(new ProgressCallback() {
                @Override
                public void onProgress(long downloaded, long total) {
                    loadingProgressBar.setMax((int) total);
                    loadingProgressBar.setProgress((int) downloaded);
                }
            }).intoImageView(mContentView).setCallback(new FutureCallback<ImageView>() {
                @Override
                public void onCompleted(Exception e, ImageView result) {
                    imageLoadingProgress.setVisibility(View.GONE);
                }
            });
        }

        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        if (actionBar != null) {
            actionBar.hide();
        }
        mVisible = false;
    }

    private void show() {
        if (actionBar != null) {
            actionBar.show();
        }
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
