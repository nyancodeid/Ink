package ink.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.adobe.creativesdk.aviary.AdobeImageIntent;
import com.ink.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fab.FloatingActionButton;
import ink.utils.SharedHelper;

public class ImageEditor extends BaseActivity {

    private static final int RESULT_IMAGE_DONE = 1;
    private static final int RESULT_IMAGE_PICKED = 2;
    @Bind(R.id.finalResultImage)
    ImageView finalResultImage;
    @Bind(R.id.pickImageButton)
    FloatingActionButton pickImageButton;
    @Bind(R.id.editorHintLayout)
    RelativeLayout editorHintLayout;
    @Bind(R.id.downArrow)
    ImageView downArrow;
    @Bind(R.id.imageChooseStatus)
    TextView imageChooseStatus;
    private Animation fadeInAnimation;
    private Animation slideDownAnimation;
    private SharedHelper sharedHelper;
    private Uri lastEditedImageUri;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_editor);
        ButterKnife.bind(this);
        sharedHelper = new SharedHelper(this);
        fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(R.string.imageEditor));
        }
        progressDialog = new ProgressDialog(ImageEditor.this);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setTitle(getString(R.string.loadingText));
        progressDialog.setMessage(getString(R.string.loading_image));
        slideDownAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_arrow_down);
        if (!sharedHelper.isEditorHintShown()) {
            editorHintLayout.startAnimation(fadeInAnimation);
            editorHintLayout.setVisibility(View.VISIBLE);
            downArrow.setVisibility(View.VISIBLE);
            downArrow.startAnimation(slideDownAnimation);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        switch (requestCode) {
                /* 4) Make a case for the request code we passed to startActivityForResult() */
            case RESULT_IMAGE_DONE:
                    /* 5) Show the image! */
                imageChooseStatus.setText(getString(R.string.clickOnImageToEdit));
                lastEditedImageUri = data.getParcelableExtra(AdobeImageIntent.EXTRA_OUTPUT_URI);
                finalResultImage.setImageURI(lastEditedImageUri);
                break;
            case RESULT_IMAGE_PICKED:
                if (data != null) {
                    progressDialog.show();
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            Uri pickedImageUri = data.getData();
                            openEditor(pickedImageUri);
                        }
                    }, 1000);
                    System.gc();
                } else {
                    Snackbar.make(downArrow, getString(R.string.imageNotChosen), Snackbar.LENGTH_LONG).show();
                }
                break;
        }

        if (lastEditedImageUri == null) {
            imageChooseStatus.setText(getString(R.string.noImageChosen));
        }
    }

    @OnClick(R.id.finalResultImage)
    public void finalResultImage() {
        if (lastEditedImageUri != null) {
            openEditor(lastEditedImageUri);
        }
    }

    @OnClick(R.id.pickImageButton)
    public void pickImageButton() {
        if (!sharedHelper.isEditorHintShown()) {
            sharedHelper.putEditorHintShow(true);
            disableHint();
        }
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, RESULT_IMAGE_PICKED);
    }

    @OnClick(R.id.neverShowEditorHint)
    public void neverShowEditorHint() {
        disableHint();
    }

    private void disableHint() {
        sharedHelper.putEditorHintShow(true);
        downArrow.clearAnimation();
        downArrow.setVisibility(View.GONE);
        editorHintLayout.setVisibility(View.GONE);
    }


    private void openEditor(Uri imageUri) {
        Intent imageEditorIntent = new AdobeImageIntent.Builder(this)
                .setData(imageUri)
                .build();
        startActivityForResult(imageEditorIntent, RESULT_IMAGE_DONE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}
