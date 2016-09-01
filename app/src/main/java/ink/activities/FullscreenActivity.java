package ink.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.ink.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import ink.utils.FileUtils;

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
        }

        if (extras != null) {
            fullUrlToLoad = extras.getString("link");

            if (FileUtils.isGif(fullUrlToLoad)) {
                gifHolder.setVisibility(View.VISIBLE);
                mImageView.setVisibility(View.GONE);

                Glide.with(this).load(fullUrlToLoad).asGif().listener(new RequestListener<String, GifDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GifDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GifDrawable resource, String model, Target<GifDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        imageLoadingProgress.setVisibility(View.GONE);
                        return false;
                    }
                }).into(gifHolder);
            } else {
                gifHolder.setVisibility(View.GONE);
                mImageView.setVisibility(View.VISIBLE);
                Glide.with(this).load(fullUrlToLoad).asBitmap().listener(new RequestListener<String, Bitmap>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        mImageView.setImage(ImageSource.bitmap(resource));
                        imageLoadingProgress.setVisibility(View.GONE);
                        return false;
                    }
                });
            }


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
