package ink.va.activities;

import android.app.DownloadManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.ink.va.R;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.senab.photoview.PhotoViewAttacher;


public class FullscreenActivity extends BaseActivity {
    private String fullUrlToLoad;

    @Bind(R.id.fullscreen_content)
    ImageView mImageView;
    @Bind(R.id.imageLoadingProgress)
    LinearLayout imageLoadingProgress;
    @Bind(R.id.loadingProgressBar)
    ProgressBar loadingProgressBar;
    @Bind(R.id.gifHolder)
    ImageView gifHolder;
    private boolean mVisible;
    private ActionBar actionBar;
    private boolean isDataLoaded;
    private PhotoViewAttacher mAttacher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);
        ButterKnife.bind(this);
        Bundle extras = getIntent().getExtras();
        mVisible = true;


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
            isDataLoaded = false;
            Ion.with(this).load(fullUrlToLoad).progressHandler(new ProgressCallback() {
                @Override
                public void onProgress(long downloaded, long total) {
                    loadingProgressBar.setMax((int) total);
                    loadingProgressBar.setProgress((int) downloaded);
                }
            }).withBitmap().asBitmap().setCallback(new FutureCallback<Bitmap>() {
                @Override
                public void onCompleted(Exception e, Bitmap result) {
                    if (e != null) {
                        e.printStackTrace();
                    }
                    if (result != null) {
                        mImageView.setImageBitmap(result);
                        imageLoadingProgress.setVisibility(View.GONE);
                        isDataLoaded = true;
                        mAttacher = new PhotoViewAttacher(mImageView);
                    }

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

    @OnClick(R.id.download_icon)
    public void downloadClicked() {
        if (!isDataLoaded) {
            Snackbar.make(loadingProgressBar, getString(R.string.pleaseWaitDownload), Snackbar.LENGTH_SHORT).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            }).show();
        } else {
            queDownload(fullUrlToLoad);
        }
    }


    private void queDownload(String fullUrlToLoad) {
        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(
                Uri.parse(fullUrlToLoad));
        request.setTitle("file_ink-" + System.currentTimeMillis());

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setVisibleInDownloadsUi(true);
        downloadManager.enqueue(request);

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
