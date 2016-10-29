package ink.va.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ybq.android.spinkit.SpinKitView;
import com.ink.va.R;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fab.FloatingActionButton;
import ink.va.utils.CircleTransform;
import ink.va.utils.Constants;
import ink.va.utils.FileUtils;
import ink.va.utils.Retrofit;
import ink.va.utils.SharedHelper;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static ink.va.activities.MakePost.MAX_FILE_SIZE;

public class CreateGroupPost extends BaseActivity {

    private static final int PICK_FILE_REQUEST_CODE = 5;
    @Bind(R.id.currentUserName)
    TextView currentUserName;
    @Bind(R.id.currentUserImage)
    ImageView currentUserImage;
    @Bind(R.id.groupInputField)
    EditText groupInputField;
    @Bind(R.id.sendGroupMessage)
    FloatingActionButton sendGroupMessageIcon;
    @Bind(R.id.groupMessageSpin)
    SpinKitView groupMessageSpin;

    @Bind(R.id.imageChooserIV)
    ImageView imageChooserIV;

    @Bind(R.id.image_picker_view)
    ImageView imagePickerIV;

    @Bind(R.id.imageChosenWrapper)
    RelativeLayout imageChosenWrapper;

    private String groupId;
    private boolean isFileChosen;
    private File chosenFile;
    private SharedHelper sharedHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group_post);
        ButterKnife.bind(this);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            groupId = bundle.getString("groupId");
        }


        sharedHelper = new SharedHelper(this);
        if (!sharedHelper.getImageLink().isEmpty()) {
            if (isSocialAccount()) {
                Ion.with(this).load(sharedHelper.getImageLink()).withBitmap().transform(new CircleTransform()).intoImageView(currentUserImage);
            } else {
                Ion.with(this).load(Constants.MAIN_URL + Constants.USER_IMAGES_FOLDER + sharedHelper.getImageLink()).withBitmap().
                        transform(new CircleTransform()).intoImageView(currentUserImage);
            }
        } else {
            Ion.with(this).load(Constants.ANDROID_DRAWABLE_DIR + "no_image").
                    withBitmap().transform(new CircleTransform()).intoImageView(currentUserImage);
        }
        currentUserName.setText(sharedHelper.getFirstName() + " " + sharedHelper.getLastName());
        sendGroupMessageIcon.setShowAnimation(AnimationUtils.loadAnimation(this, R.anim.fab_scale_up));
        sendGroupMessageIcon.setHideAnimation(AnimationUtils.loadAnimation(this, R.anim.fab_scale_down));
        disableButton();
        groupInputField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().isEmpty() && !isFileChosen) {
                    disableButton();
                } else {
                    enableButtons();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void disableButton() {
        sendGroupMessageIcon.showButtonInMenu(true);
        sendGroupMessageIcon.setEnabled(false);
    }

    private void enableButtons() {
        sendGroupMessageIcon.showButtonInMenu(true);
        sendGroupMessageIcon.setEnabled(true);
    }

    @OnClick(R.id.removeGroupImageChosen)
    public void removeClicked() {
        removeImage();
    }

    private void removeImage() {
        imagePickerIV.setImageBitmap(null);
        imageChosenWrapper.setVisibility(View.GONE);
        isFileChosen = false;
        if (groupInputField.getText().toString().trim().isEmpty()) {
            disableButton();
        }
        try {
            imagePickerIV.setImageResource(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.imageChooserIV)
    public void choseImageClicked() {
        openImageChooser();
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");      //all files
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(Intent.createChooser(intent, "Select a Image"), PICK_FILE_REQUEST_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a AlertDialogView
            Toast.makeText(this, "Please install a Gallery application.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_scale_up, R.anim.activity_scale_down);
    }

    @OnClick(R.id.sendGroupMessage)
    public void postGroupMessageWrapper() {
        sendGroupMessage();
    }

    private void sendGroupMessage() {
        Call<ResponseBody> sendGroupMessageCall = Retrofit.getInstance().getInkService().sendGroupMessage(groupId,
                groupInputField.getText().toString().trim(), sharedHelper.getUserId(),
                sharedHelper.getImageLink(), sharedHelper.getFirstName() + " " + sharedHelper.getLastName());
        groupInputField.setText("");
        groupMessageSpin.setVisibility(View.VISIBLE);
        groupInputField.setEnabled(false);
        sendGroupMessageCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    sendGroupMessage();
                    return;
                }
                if (response.body() == null) {
                    sendGroupMessage();
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    if (success) {
                        finish();
                        overridePendingTransition(R.anim.activity_scale_up, R.anim.activity_scale_down);
                        LocalBroadcastManager.getInstance(CreateGroupPost.this).sendBroadcast(new Intent(getPackageName() +
                                "SingleGroupView"));
                    } else {
                        sendGroupMessage();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                sendGroupMessage();
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            Uri uri = data.getData();
            // Get the path
            String path;
            AlertDialog.Builder fileErrorDialog = new AlertDialog.Builder(CreateGroupPost.this);
            fileErrorDialog.setTitle(getString(R.string.fileError));
            fileErrorDialog.setMessage(getString(R.string.couldNotReadFile));
            fileErrorDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            try {
                path = FileUtils.getPath(this, uri);
            } catch (URISyntaxException e) {
                e.printStackTrace();
                fileErrorDialog.show();
                isFileChosen = false;
                return;
            }

            if (path != null) {
                File file = new File(path);
                if (!file.exists()) {
                    fileErrorDialog.show();
                    isFileChosen = false;
                    return;
                }
                if (file.length() > MAX_FILE_SIZE) {
                    isFileChosen = false;
                    AlertDialog.Builder builder = new AlertDialog.Builder(CreateGroupPost.this);
                    builder.setTitle(getString(R.string.sizeExceeded)).show();
                    builder.setMessage(getString(R.string.sizeExceededMessage));
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    builder.show();
                    return;
                }
                chosenFile = file;
                isFileChosen = true;
                imageChosenWrapper.setVisibility(View.VISIBLE);
                Ion.with(this).load(chosenFile).asBitmap().setCallback(new FutureCallback<Bitmap>() {
                    @Override
                    public void onCompleted(Exception e, Bitmap result) {
                        if (e == null) {
                            enableButtons();
                            imagePickerIV.setImageBitmap(result);
                        } else {
                            Snackbar.make(imagePickerIV, getString(R.string.com_facebook_image_download_unknown_error), Snackbar.LENGTH_LONG).setAction("OK", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                }
                            }).show();
                            imageChosenWrapper.setVisibility(View.GONE);
                        }
                    }
                });

            } else {
                isFileChosen = false;
                fileErrorDialog.show();
            }
        } else {
            isFileChosen = false;
        }
    }

}
