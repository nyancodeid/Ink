package ink.va.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.ink.va.R;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FullscreenActivity extends BaseActivity {
    private String fullUrlToLoad;

    @Bind(R.id.fullscreen_content)
    com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView mImageView;
    @Bind(R.id.imageLoadingProgress)
    LinearLayout imageLoadingProgress;
    @Bind(R.id.loadingProgressBar)
    ProgressBar loadingProgressBar;
    @Bind(R.id.gifHolder)
    ImageView gifHolder;
    private boolean mVisible;
    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);
        ButterKnife.bind(this);
        Bundle extras = getIntent().getExtras();
        mVisible = true;
        mImageView = (com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView) findViewById(R.id.fullscreen_content);

        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.hide();
            mVisible = false;
        }

        if (extras != null) {
            fullUrlToLoad = extras.getString("link");

            gifHolder.setVisibility(View.GONE);
            mImageView.setVisibility(View.VISIBLE);
            Ion.with(this).load(fullUrlToLoad).progressHandler(new ProgressCallback() {
                @Override
                public void onProgress(long downloaded, long total) {
                    loadingProgressBar.setMax((int) total);
                    loadingProgressBar.setProgress((int) downloaded);
                }
            }).withBitmap().asBitmap().setCallback(new FutureCallback<Bitmap>() {
                @Override
                public void onCompleted(Exception e, Bitmap result) {
                    mImageView.setImage(ImageSource.bitmap(result));
                    imageLoadingProgress.setVisibility(View.GONE);
                }
            });


        }


        mImageView.setOnClickListener(new View.OnClickListener() {
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
        mImageView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
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
