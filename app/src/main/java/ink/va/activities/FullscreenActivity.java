package ink.va.activities;

import android.Manifest;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.va.utils.DialogUtils;
import ink.va.utils.Download;
import ink.va.utils.PermissionsChecker;
import uk.co.senab.photoview.PhotoViewAttacher;


public class FullscreenActivity extends BaseActivity {
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 5;
    private String fullUrlToLoad;

    @BindView(R.id.fullscreen_content)
    ImageView mImageView;
    @BindView(R.id.imageLoadingProgress)
    LinearLayout imageLoadingProgress;
    @BindView(R.id.loadingProgressBar)
    ProgressBar loadingProgressBar;
    @BindView(R.id.gifHolder)
    ImageView gifHolder;
    @BindView(R.id.download_icon)
    View downloadIcon;
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

            if (fullUrlToLoad.contains("Sticker")) {
                downloadIcon.setVisibility(View.GONE);
            } else {
                downloadIcon.setVisibility(View.VISIBLE);
            }

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
            if (!PermissionsChecker.isStoragePermissionGranted(this)) {
                DialogUtils.showDialog(this, getString(R.string.feather_attention), getString(R.string.storagePermissions), true, new DialogUtils.DialogListener() {
                    @Override
                    public void onNegativeClicked() {

                    }

                    @Override
                    public void onDialogDismissed() {

                    }

                    @Override
                    public void onPositiveClicked() {
                        ActivityCompat.requestPermissions(FullscreenActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                STORAGE_PERMISSION_REQUEST_CODE);
                    }
                }, true, getString(R.string.cancel));
            } else {
                queDownload(fullUrlToLoad);
            }
        }
    }


    private void queDownload(String fullUrlToLoad) {
        Download.downloadFiled(this, fullUrlToLoad, new Download.DownloadCallback() {
            @Override
            public void onPermissionNeeded() {
                ActivityCompat.requestPermissions(FullscreenActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST_CODE);
            }
        });
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
            overridePendingTransition(R.anim.slide_up_calm, R.anim.slide_down_slow);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_up_calm, R.anim.slide_down_slow);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case STORAGE_PERMISSION_REQUEST_CODE:
                if (PermissionsChecker.isStoragePermissionGranted(this)) {
                    queDownload(fullUrlToLoad);
                }
                break;
        }
    }
}
