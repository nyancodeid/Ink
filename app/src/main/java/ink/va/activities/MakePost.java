package ink.va.activities;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
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
import ink.va.interfaces.RequestCallback;
import ink.va.models.CoinsResponse;
import ink.va.service.SocketService;
import ink.va.utils.Constants;
import ink.va.utils.FileUtils;
import ink.va.utils.PermissionsChecker;
import ink.va.utils.ProgressRequestBody;
import ink.va.utils.Retrofit;
import ink.va.utils.SharedHelper;
import ink.va.utils.Time;
import ink.va.utils.User;
import okhttp3.ResponseBody;

import static ink.va.utils.Constants.EVENT_POST_MADE;
import static ink.va.utils.Constants.POST_TYPE_GLOBAL;
import static ink.va.utils.Constants.POST_TYPE_LOCAL;
import static ink.va.utils.ErrorCause.NOT_ENOUGH_COINS;

public class MakePost extends BaseActivity implements ProgressRequestBody.UploadCallbacks {
    private static final int PICK_FILE_REQUEST_CODE = 5584;
    private static final int STORAGE_PERMISSION_REQUEST = 45485;
    public static final long MAX_FILE_SIZE = 20971520;
    @BindView(R.id.closeWrapper)
    RelativeLayout mCloseWrapper;
    @BindView(R.id.checkWrapper)
    RelativeLayout mCheckWrapper;
    @BindView(R.id.postBody)
    EditText mPostBody;
    @BindView(R.id.addedAttachmentLayout)
    LinearLayout mAddedAttachmentLayout;
    @BindView(R.id.addressWrapper)
    RelativeLayout mAddressWrapper;
    @BindView(R.id.fileAttachmentWrapper)
    RelativeLayout fileAttachmentWrapper;
    @BindView(R.id.addressHint)
    TextView mAddressHint;
    @BindView(R.id.attachmentHint)
    TextView mAttachmentHint;
    @BindView(R.id.postVisibilityHint)
    TextView postVisibilityHint;
    @BindView(R.id.postVisibilityIV)
    ImageView postVisibilityIV;
    private boolean canProceed;
    private BroadcastReceiver mBroadcastReceiver;
    private SharedHelper mSharedHelper;
    private boolean isFileChosen;
    private File chosenFile;
    private String mGoogleAddress = "";
    private ProgressDialog progressDialog;
    private boolean isEditing;
    private String postId = "";
    private boolean hasAttachment;
    private boolean hasAddress;
    private String attachmentName = "";
    private String addressName;
    private String shouldDelete = "false";
    private String postType;
    private SocketService socketService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_post);
        ButterKnife.bind(this);
        mSharedHelper = new SharedHelper(this);
        postType = Constants.POST_TYPE_LOCAL;
        Linkify.addLinks(mPostBody, Linkify.WEB_URLS);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.posting));
        progressDialog.setMessage(getString(R.string.postingYourShare));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey("isEditing")) {
                isEditing = extras.getBoolean("isEditing");
                if (isEditing) {
                    disableVisibilityControls();
                }
                hasAttachment = extras.getBoolean("attachmentPresent");
                hasAddress = extras.getBoolean("addressPresent");
                postId = extras.getString("postId");
                if (hasAttachment) {
                    attachmentName = extras.getString("attachmentName");
                }
                if (hasAddress) {
                    addressName = extras.getString("addressName");
                }
                if (isEditing) {
                    mPostBody.setText(extras.getString("postBody"));
                    if (hasAttachment) {
                        if (fileAttachmentWrapper.getVisibility() == View.GONE) {
                            fileAttachmentWrapper.setVisibility(View.VISIBLE);
                        }
                        mAttachmentHint.setText(getString(R.string.addedAttachment) + " - " + attachmentName);
                    }
                    if (hasAddress) {
                        if (mAddressWrapper.getVisibility() == View.GONE) {
                            mAddressWrapper.setVisibility(View.VISIBLE);
                        }
                        mAddressHint.setText(getString(R.string.addedAddress) + " - " + addressName);
                        mGoogleAddress = addressName;
                    }
                }
            } else {
                // Get intent, action and MIME type
                Intent intent = getIntent();
                String action = intent.getAction();
                String type = intent.getType();

                if (Intent.ACTION_SEND.equals(action) && type != null) {
                    if ("text/plain".equals(type)) {
                        handleSendText(intent);
                    } else if (type.startsWith("image/")) {
                        handleSendImage(intent);
                    }
                }
            }
        }

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String value = intent.getExtras().getString("value");
                if (!value.equals(getString(R.string.nothingChosen))) {
                    mGoogleAddress = value;
                    if (mAddressWrapper.getVisibility() == View.GONE) {
                        mAddressWrapper.setVisibility(View.VISIBLE);
                    }
                    mAddressHint.setText(getString(R.string.addedAddress) + " - " + value);
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, new IntentFilter(getPackageName() + "MakePost"));

        mPostBody.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().isEmpty() || charSequence.toString().trim().length() <= 0) {
                    canProceed = false;
                } else {
                    canProceed = true;
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {
                Linkify.addLinks(editable, Linkify.WEB_URLS);
            }
        });
    }

    private void disableVisibilityControls() {
        postVisibilityIV.setEnabled(false);
        postVisibilityIV.setImageResource(R.drawable.local_icon_greyed_out);
        postVisibilityHint.setText(getString(R.string.visibilityDisabled));
    }

    private void handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            // Update UI to reflect text being shared
            Log.d("fasklhfklasflsa", "handleSendText: " + sharedText);
            mPostBody.setText(sharedText);
        }
    }

    private void handleSendImage(Intent intent) {
        Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            // Update UI to reflect image being shared
            Log.d("fasklhfklasflsa", "handleSendImage: " + imageUri);
            Intent receivedData = new Intent();
            receivedData.setData(imageUri);
            onActivityResult(PICK_FILE_REQUEST_CODE, Activity.RESULT_OK, receivedData);
        }
    }

    private boolean isPermissionsGranted() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PermissionChecker.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PermissionChecker.PERMISSION_GRANTED) {
            return false;
        } else {
            return true;
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                STORAGE_PERMISSION_REQUEST);
    }

    @OnClick(R.id.postVisibilityIV)
    public void postVisibilityIVClicked() {
        switch (postType) {
            case POST_TYPE_GLOBAL:
                postType = Constants.POST_TYPE_LOCAL;
                postVisibilityHint.setText(getString(R.string.localPostHint));
                postVisibilityIV.setImageResource(R.drawable.local_icon);
                break;
            case Constants.POST_TYPE_LOCAL:
                postVisibilityIV.setImageResource(R.drawable.global_icon);
                postType = POST_TYPE_GLOBAL;
                postVisibilityHint.setText(getString(R.string.loadingText));
                getCoinsToCharge();
                break;
        }
    }

    @OnClick(R.id.checkWrapper)
    public void checkWrapper() {
        String inputtedText = mPostBody.getText().toString().trim();
        if (inputtedText.isEmpty() && !isFileChosen) {
            mPostBody.setError(getString(R.string.fieldEmptyError));
        } else {
            makePost();
        }
    }

    private void makePost() {
        if (isEditing) {
            progressDialog.setTitle(getString(R.string.saving));
            progressDialog.setMessage(getString(R.string.savingChanges));
        }
        progressDialog.show();
        if (isFileChosen) {
            if (chosenFile != null) {
                makePost(chosenFile, mPostBody.getText().toString().trim(), mGoogleAddress);
            } else {
                progressDialog.dismiss();
            }
        } else {
            makePost(null, mPostBody.getText().toString().trim(), mGoogleAddress);
        }
    }

    @OnClick(R.id.removeAttachment)
    public void removeAttachment() {
        fileAttachmentWrapper.setVisibility(View.GONE);
        isFileChosen = false;
        chosenFile = null;
        if (isEditing && hasAttachment) {
            shouldDelete = "true";
        } else {
            attachmentName = "";
            shouldDelete = "false";
        }

    }

    @OnClick(R.id.removeAddress)
    public void removeAddress() {
        mGoogleAddress = "";
        mAddressWrapper.setVisibility(View.GONE);
    }

    @OnClick(R.id.closeWrapper)
    public void closeWrapper() {
        if (!canProceed) {
            finish();
            overridePendingTransition(R.anim.activity_scale_up, R.anim.activity_scale_down);
        } else {
            showWarning(getString(R.string.discardChanges),
                    getString(R.string.discardChangesQuestion));
        }
    }

    @OnClick(R.id.locationWidget)
    public void locationWidget() {
        startActivity(new Intent(getApplicationContext(), MapsActivity.class));
    }

    @OnClick(R.id.attachmentWidget)
    public void attachmentWidget() {
        if (PermissionsChecker.isStoragePermissionGranted(this)) {
            showAttachmentDialog();
        } else {
            if (!isPermissionsGranted()) {
                requestPermission();
            }
        }
    }

    private void showAttachmentDialog() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");      //all files
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), PICK_FILE_REQUEST_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a AlertDialogView
            Toast.makeText(this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onDestroy() {
        if (mBroadcastReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        }
        super.onDestroy();
    }

    private void showWarning(String tittle, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MakePost.this);
        builder.setTitle(tittle);
        builder.setMessage(message);
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                finish();
                overridePendingTransition(R.anim.activity_scale_up, R.anim.activity_scale_down);
            }
        });
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    @Override
    public void onBackPressed() {
        if (!canProceed) {

            finish();
            overridePendingTransition(R.anim.activity_scale_up, R.anim.activity_scale_down);
        } else {
            showWarning(getString(R.string.discardChanges),
                    getString(R.string.discardChangesQuestion));
        }
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
            String path = null;
            AlertDialog.Builder fileErrorDialog = new AlertDialog.Builder(MakePost.this);
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(MakePost.this);
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
                shouldDelete = "false";
                if (fileAttachmentWrapper.getVisibility() == View.GONE) {
                    fileAttachmentWrapper.setVisibility(View.VISIBLE);
                }
                mAttachmentHint.setText(getString(R.string.addedAttachment) + " - " + chosenFile.getName());
            } else {
                isFileChosen = false;
                fileErrorDialog.show();
            }
        } else {
            isFileChosen = false;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (!isPermissionsGranted()) {
            Toast.makeText(this, getString(R.string.storagePermissions), Toast.LENGTH_SHORT).show();
        } else {
            showAttachmentDialog();
        }
    }


    private void makePost(File chosenFile, String postBody, String chosenGoogleAddress) {
        if (chosenFile != null) {
            Map<String, ProgressRequestBody> map = new HashMap<>();
            ProgressRequestBody requestBody = new ProgressRequestBody(chosenFile, this);
            map.put("file\"; filename=\"" + chosenFile.getName() + "\"", requestBody);
            callToServerWithBody(map, postBody, chosenGoogleAddress);
        } else {
            callToServerWithoutBody(postBody, chosenGoogleAddress);
        }
    }

    private void callToServerWithoutBody(final String postBody, final String googleAdress) {
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(false);
        String finalType = Constants.POST_TYPE_CREATE;
        if (isEditing) {
            finalType = Constants.POST_TYPE_EDIT;
        }
        makeRequest(Retrofit.getInstance().getInkService().makePost(mSharedHelper.getUserId(),
                postBody,
                googleAdress,
                mSharedHelper.getImageLink(),
                mSharedHelper.getFirstName(),
                mSharedHelper.getLastName(),
                Time.getTimeZone(),
                finalType,
                attachmentName,
                postId,
                shouldDelete, postType), null, false, new RequestCallback() {
            @Override
            public void onRequestSuccess(Object result) {
                try {
                    String responseBody = ((ResponseBody) result).string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    if (success) {
                        if (!isEditing) {
                            String insertedPostId = jsonObject.optString("postId");
                            mSharedHelper.putOwnPostId(insertedPostId);
                            JSONObject postJson = new JSONObject();
                            switch (postType) {
                                case POST_TYPE_GLOBAL:
                                    postJson.put("posterId", mSharedHelper.getUserId());
                                    postJson.put("insertedPostId", insertedPostId);
                                    postJson.put("posterFirstName", mSharedHelper.getFirstName());
                                    postJson.put("posterLastName", mSharedHelper.getLastName());
                                    postJson.put("postType", postType);

                                    socketService.emit(EVENT_POST_MADE, postJson);
                                    postJson = null;
                                    break;
                                case POST_TYPE_LOCAL:
                                    postJson.put("posterId", mSharedHelper.getUserId());
                                    postJson.put("insertedPostId", insertedPostId);
                                    postJson.put("posterFirstName", mSharedHelper.getFirstName());
                                    postJson.put("posterLastName", mSharedHelper.getLastName());
                                    postJson.put("postType", postType);

                                    JSONArray friendsArray = new JSONArray();
                                    for (String friendId : User.get().getFriendIds()) {
                                        friendsArray.put(friendId);
                                    }

                                    postJson.put("friends", friendsArray);
                                    socketService.emit(EVENT_POST_MADE, postJson);
                                    postJson = null;
                                    break;
                            }
                        }
                        progressDialog.dismiss();
                        LocalBroadcastManager.getInstance(MakePost.this).sendBroadcast(new Intent(getPackageName() + "Comments"));
                        Intent intent = new Intent(getPackageName() + "HomeActivity");
                        intent.putExtra("updateFromPost", true);

                        LocalBroadcastManager.getInstance(MakePost.this).sendBroadcast(intent);

                        if (!isEditing) {
                            Toast.makeText(MakePost.this, getString(R.string.post_shared), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MakePost.this, getString(R.string.saved), Toast.LENGTH_SHORT).show();
                        }

                        finish();
                        overridePendingTransition(R.anim.activity_scale_up, R.anim.activity_scale_down);
                    } else {
                        String cause = jsonObject.optString("cause");
                        if (cause.equals(NOT_ENOUGH_COINS)) {
                            showFailureDialog(true);
                        } else {
                            showFailureDialog(false);
                        }

                    }
                } catch (IOException e) {
                    showFailureDialog(false);
                    e.printStackTrace();
                } catch (JSONException e) {
                    showFailureDialog(false);
                    e.printStackTrace();
                }
            }

            @Override
            public void onRequestFailed(Object[] result) {
                progressDialog.dismiss();
            }
        });
    }

    private void callToServerWithBody(final Map<String, ProgressRequestBody> map, final String postBody, final String googleAddress) {
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setIndeterminate(true);
        progressDialog.setProgress(0);
        progressDialog.setMax(100);
        String finalType = Constants.POST_TYPE_CREATE;
        if (isEditing) {
            finalType = Constants.POST_TYPE_EDIT;
        }
        makeRequest(Retrofit.getInstance().getInkService().makePost(map,
                mSharedHelper.getUserId(),
                postBody, googleAddress,
                mSharedHelper.getImageLink(),
                mSharedHelper.getFirstName(),
                mSharedHelper.getLastName(),
                Time.getTimeZone(),
                finalType, postId,
                shouldDelete, postType), null, false, new RequestCallback() {
            @Override
            public void onRequestSuccess(Object result) {
                try {
                    String responseBody = ((ResponseBody) result).string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    if (success) {
                        if (!isEditing) {
                            String insertedPostId = jsonObject.optString("postId");
                            mSharedHelper.putOwnPostId(insertedPostId);
                            JSONObject postJson = new JSONObject();
                            switch (postType) {
                                case POST_TYPE_GLOBAL:
                                    postJson.put("posterId", mSharedHelper.getUserId());
                                    postJson.put("insertedPostId", insertedPostId);
                                    postJson.put("posterFirstName", mSharedHelper.getFirstName());
                                    postJson.put("posterLastName", mSharedHelper.getLastName());
                                    postJson.put("postType", postType);

                                    socketService.emit(EVENT_POST_MADE, postJson);
                                    postJson = null;
                                    break;
                                case POST_TYPE_LOCAL:
                                    postJson.put("posterId", mSharedHelper.getUserId());
                                    postJson.put("insertedPostId", insertedPostId);
                                    postJson.put("posterFirstName", mSharedHelper.getFirstName());
                                    postJson.put("posterLastName", mSharedHelper.getLastName());
                                    postJson.put("postType", postType);

                                    JSONArray friendsArray = new JSONArray();
                                    for (String friendId : User.get().getFriendIds()) {
                                        friendsArray.put(friendId);
                                    }
                                    postJson.put("friends", friendsArray);

                                    socketService.emit(EVENT_POST_MADE, postJson);
                                    postJson = null;
                                    break;
                            }
                        }

                        progressDialog.dismiss();
                        LocalBroadcastManager.getInstance(MakePost.this).sendBroadcast(new Intent(getPackageName() + "Comments"));
                        LocalBroadcastManager.getInstance(MakePost.this).sendBroadcast(new Intent(getPackageName() + "HomeActivity"));
                        Toast.makeText(MakePost.this, getString(R.string.post_shared), Toast.LENGTH_SHORT).show();

                        finish();
                        overridePendingTransition(R.anim.activity_scale_up, R.anim.activity_scale_down);
                    } else {
                        String cause = jsonObject.optString("cause");
                        if (cause.equals(NOT_ENOUGH_COINS)) {
                            showFailureDialog(true);
                        } else {
                            showFailureDialog(false);
                        }
                    }
                } catch (IOException e) {
                    showFailureDialog(false);
                    e.printStackTrace();
                } catch (JSONException e) {
                    showFailureDialog(false);
                    e.printStackTrace();
                }
            }

            @Override
            public void onRequestFailed(Object[] result) {
                progressDialog.dismiss();
            }
        });
    }


    private void showFailureDialog(boolean coinsFailure) {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(MakePost.this);

        if (coinsFailure) {
            builder.setTitle(getString(R.string.error));
            builder.setMessage(getString(R.string.not_enough_coins));
        } else {
            builder.setTitle(getString(R.string.somethingWentWrong));
            builder.setMessage(getString(R.string.notPosted));
        }
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        builder.show();
    }

    private void showSuccessDialog() {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(MakePost.this);
        builder.setTitle(getString(R.string.success));
        builder.setMessage(getString(R.string.posted));
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                finish();
                overridePendingTransition(R.anim.activity_scale_up, R.anim.activity_scale_down);
            }
        });
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {

                finish();
                overridePendingTransition(R.anim.activity_scale_up, R.anim.activity_scale_down);
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {

                finish();
                overridePendingTransition(R.anim.activity_scale_up, R.anim.activity_scale_down);
            }
        });
        builder.show();

    }

    @Override
    public void onProgressUpdate(int percentage) {
        progressDialog.setProgress(percentage);
    }

    @Override
    public void onError() {

    }

    @Override
    public void onFinish() {
        if (progressDialog != null) {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }

    private void getCoinsToCharge() {
        makeRequest(Retrofit.getInstance().getInkService().getCoins(mSharedHelper.getUserId()), null, false, new RequestCallback() {
            @Override
            public void onRequestSuccess(Object result) {
                Gson gson = new Gson();
                try {
                    CoinsResponse coinsResponse = gson.fromJson(((ResponseBody) result).string(), CoinsResponse.class);
                    if (coinsResponse.success) {
                        postVisibilityHint.setText(getString(R.string.globalPostHint, coinsResponse.coinsDeducateForGlobal));
                    } else {
                        Toast.makeText(MakePost.this, getString(R.string.serverErrorText), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onRequestFailed(Object[] result) {

            }
        });
    }
}
