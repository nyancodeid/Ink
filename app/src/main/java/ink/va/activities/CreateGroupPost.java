package ink.va.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fab.FloatingActionButton;
import ink.va.interfaces.RequestCallback;
import ink.va.service.SocketService;
import ink.va.utils.Constants;
import ink.va.utils.FileUtils;
import ink.va.utils.ImageLoader;
import ink.va.utils.ProgressRequestBody;
import ink.va.utils.Retrofit;
import ink.va.utils.SharedHelper;
import ink.va.utils.User;
import okhttp3.ResponseBody;

import static ink.va.activities.MakePost.MAX_FILE_SIZE;
import static ink.va.utils.Constants.EVENT_SEND_GROUP_MESSAGE;

public class CreateGroupPost extends BaseActivity implements ProgressRequestBody.UploadCallbacks {

    private static final int PICK_FILE_REQUEST_CODE = 5;
    @BindView(R.id.currentUserName)
    TextView currentUserName;
    @BindView(R.id.currentUserImage)
    ImageView currentUserImage;
    @BindView(R.id.groupInputField)
    EditText groupInputField;
    @BindView(R.id.sendGroupMessage)
    FloatingActionButton sendGroupMessageIcon;
    @BindView(R.id.groupMessageSpin)
    SpinKitView groupMessageSpin;

    @BindView(R.id.imageChooserIV)
    ImageView imageChooserIV;

    @BindView(R.id.image_picker_view)
    ImageView imagePickerIV;

    @BindView(R.id.imageChosenWrapper)
    RelativeLayout imageChosenWrapper;

    private String groupId;
    private boolean isFileChosen;
    private File chosenFile;
    private SharedHelper sharedHelper;
    private SocketService socketService;
    private String groupNme;
    private String mGroupName;
    private String mGroupColor;
    private String mGroupImage;
    private String mGroupDescription;
    private String mGroupOwnerId;
    private String mGroupOwnerName;
    private String mCount;
    private String mOwnerImage;
    private boolean isSocialAccount;
    private boolean isMember;
    private boolean isFriendWithOwner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group_post);
        ButterKnife.bind(this);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            groupId = bundle.getString("groupId");
            groupNme = bundle.getString("groupNme");
            mGroupName = bundle.getString("groupName");
            mGroupColor = bundle.getString("groupColor");
            mGroupImage = bundle.getString("groupImage");
            mGroupDescription = bundle.getString("groupDescription");
            mGroupOwnerId = bundle.getString("groupOwnerId");
            mGroupOwnerName = bundle.getString("groupOwnerName");
            mCount = bundle.getString("count");
            mOwnerImage = bundle.getString("ownerImage");
            isSocialAccount = bundle.getBoolean("isSocialAccount");
            isMember = bundle.getBoolean("isMember");
            isFriendWithOwner = bundle.getBoolean("isFriend");
        }


        sharedHelper = new SharedHelper(this);
        if (!sharedHelper.getImageLink().isEmpty()) {
            if (isSocialAccount()) {
                ImageLoader.loadImage(this, true, false, sharedHelper.getImageLink(),
                        0, R.drawable.user_image_placeholder, currentUserImage, null);

            } else {
                String encodedImage = Uri.encode(sharedHelper.getImageLink());
                ImageLoader.loadImage(this, true, false, Constants.MAIN_URL + Constants.USER_IMAGES_FOLDER + encodedImage,
                        0, R.drawable.user_image_placeholder, currentUserImage, null);
            }
        } else {
            ImageLoader.loadImage(this, true, true, null,
                    R.drawable.no_image, R.drawable.user_image_placeholder, currentUserImage, null);
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
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");

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

        Map<String, ProgressRequestBody> map = new HashMap<>();
        ProgressRequestBody requestBody = new ProgressRequestBody(chosenFile, this);
        if (chosenFile != null) {
            map.put("file\"; filename=\"" + chosenFile.getName() + "\"", requestBody);
        }
        final String message = groupInputField.getText().toString().trim();

        makeRequest(Retrofit.getInstance().getInkService().sendGroupMessage(map, groupId,
                message, sharedHelper.getUserId(),
                sharedHelper.getImageLink(), sharedHelper.getFirstName() + " " + sharedHelper.getLastName()),
                groupMessageSpin, true, new RequestCallback() {
                    @Override
                    public void onRequestSuccess(Object result) {
                        try {
                            String responseBody = ((ResponseBody) result).string();
                            JSONObject jsonObject = new JSONObject(responseBody);
                            boolean success = jsonObject.optBoolean("success");
                            if (success) {

                                JSONObject groupJson = new JSONObject();
                                groupJson.put("senderName", sharedHelper.getFirstName() + " " + sharedHelper.getLastName());
                                groupJson.put("senderId", sharedHelper.getUserId());
                                groupJson.put("message", message);

                                groupJson.put("groupId", groupId);
                                groupJson.put("groupName", mGroupName);
                                groupJson.put("groupColor", mGroupColor);
                                groupJson.put("groupImage", mGroupImage);
                                groupJson.put("groupDescription", mGroupDescription);
                                groupJson.put("groupOwnerId", mGroupOwnerId);
                                groupJson.put("groupOwnerName", mGroupOwnerName);
                                groupJson.put("count", mCount);
                                groupJson.put("ownerImage", mOwnerImage);
                                groupJson.put("isSocialAccount", isSocialAccount);
                                groupJson.put("isMember", isMember);
                                groupJson.put("isFriend", isFriendWithOwner);

                                JSONArray participantsArray = new JSONArray();
                                for (String eachParticipantId : User.get().getParticipantIds()) {
                                    participantsArray.put(eachParticipantId);
                                }

                                groupJson.put("groupParticipants", participantsArray);

                                socketService.emit(EVENT_SEND_GROUP_MESSAGE, groupJson);
                                groupJson = null;
                                overridePendingTransition(R.anim.activity_scale_up, R.anim.activity_scale_down);
                                LocalBroadcastManager.getInstance(CreateGroupPost.this).sendBroadcast(new Intent(getPackageName() +
                                        "SingleGroupView"));
                                finish();
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
                    public void onRequestFailed(Object[] result) {

                    }
                });
        groupInputField.setText("");
        groupMessageSpin.setVisibility(View.VISIBLE);
        groupInputField.setEnabled(false);

    }

    @Override
    public void onServiceConnected(SocketService socketService) {
        super.onServiceConnected(socketService);
        this.socketService = socketService;
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


            if (uri.toString().startsWith("content://com.google")) {
                InputStream is = null;
                try {
                    is = getContentResolver().openInputStream(uri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                Bitmap pictureBitmap = BitmapFactory.decodeStream(is);

                OutputStream fOut = null;

                File outputDir = getCacheDir();
                File file = null;

                try {
                    file = File.createTempFile("google_photos", ".jpg", outputDir);
                    fOut = new FileOutputStream(file);
                    pictureBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                    fOut.flush();
                    fOut.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                path = file.getAbsolutePath();
            } else {
                try {
                    path = FileUtils.getPath(this, uri);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                    fileErrorDialog.show();
                    isFileChosen = false;
                    return;
                }
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

                ImageLoader.loadImage(this, false, false, chosenFile,
                        0, R.drawable.user_image_placeholder, imagePickerIV, new ImageLoader.ImageLoadedCallback() {
                            @Override
                            public void onImageLoaded(Object result, Exception e) {
                                if (e != null) {
                                    Snackbar.make(imagePickerIV, getString(R.string.com_facebook_image_download_unknown_error), Snackbar.LENGTH_LONG).setAction("OK", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                        }
                                    }).show();
                                    imageChosenWrapper.setVisibility(View.GONE);
                                } else {
                                    enableButtons();
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

    @Override
    public void onProgressUpdate(int percentage) {

    }

    @Override
    public void onError() {

    }

    @Override
    public void onFinish() {

    }
}
