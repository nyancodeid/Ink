package ink.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.util.Linkify;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ink.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.utils.FileUtils;
import ink.utils.Retrofit;
import ink.utils.SharedHelper;
import ink.utils.Time;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MakePost extends AppCompatActivity {
    private static final int PICKFILE_REQUEST_CODE = 5584;
    private static final int STORAGE_PERMISSION_REQUEST = 45485;
    private static final long MAX_FILE_SIZE = 20971520;
    @Bind(R.id.closeWrapper)
    RelativeLayout mCloseWrapper;
    @Bind(R.id.checkWrapper)
    RelativeLayout mCheckWrapper;
    @Bind(R.id.postBody)
    EditText mPostBody;
    @Bind(R.id.addedAttachmentLayout)
    LinearLayout mAddedAttachmentLayout;
    @Bind(R.id.addressWrapper)
    RelativeLayout mAddressWrapper;
    @Bind(R.id.fileAttachmentWrapper)
    RelativeLayout fileAttachmentWrapper;
    @Bind(R.id.addressHint)
    TextView mAddressHint;
    @Bind(R.id.attachmentHint)
    TextView mAttachmentHint;
    private boolean canProceed;
    private BroadcastReceiver mBroadcastReceiver;
    private SharedHelper mSharedHelper;
    private boolean isFileChosen;
    private File chosenFile;
    private String mGoogleAddress = "";
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_post);
        ButterKnife.bind(this);
        mSharedHelper = new SharedHelper(this);
        Toolbar makePostToolbar = (Toolbar) findViewById(R.id.makePostToolbar);
        setSupportActionBar(makePostToolbar);
        Linkify.addLinks(mPostBody, Linkify.WEB_URLS);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.posting));
        progressDialog.setMessage(getString(R.string.postingYourShare));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);

        if (!isPermissionsGranted()) {
            requestPermission();
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


    @OnClick(R.id.checkWrapper)
    public void checkWrapper() {
        String inputtedText = mPostBody.getText().toString().trim();
        if (inputtedText.isEmpty() || inputtedText.length() <= 0) {
            mPostBody.setError(getString(R.string.fieldEmptyError));
        } else {
            doShare();
        }
    }

    private void doShare() {
        progressDialog.show();
        if (isFileChosen) {
            if (chosenFile != null) {
                makePost(chosenFile, mPostBody.getText().toString(), mGoogleAddress);
            } else {
                progressDialog.dismiss();
            }
        } else {
            makePost(null, mPostBody.getText().toString(), mGoogleAddress);
        }
    }

    @OnClick(R.id.removeAttachment)
    public void removeAttachment() {
        fileAttachmentWrapper.setVisibility(View.GONE);
        isFileChosen = false;
        chosenFile = null;

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
        showAttachmentDialog();
    }

    private void showAttachmentDialog() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");      //all files
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), PICKFILE_REQUEST_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
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
        } else {
            showWarning(getString(R.string.discardChanges),
                    getString(R.string.discardChangesQuestion));
        }
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
            try {
                path = FileUtils.getPath(this, uri);
            } catch (URISyntaxException e) {
                e.printStackTrace();
                fileErrorDialog.show();
                isFileChosen = false;
                return;
            }
            // Get the file instance
            // File file = new File(path);
            // Initiate the upload

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
            Snackbar.make(mCheckWrapper, getString(R.string.storagePermissions), Snackbar.LENGTH_LONG).show();
        }
    }


    private void makePost(File chosenFile, String postBody, String chosenGoogleAddress) {
        if (chosenFile != null) {
            Map<String, RequestBody> map = new HashMap<>();
            RequestBody requestBody = RequestBody.create(MediaType.parse("*/*"), chosenFile);
            map.put("file\"; filename=\"" + chosenFile.getName() + "\"", requestBody);
            callToServerWithBody(map, postBody, chosenGoogleAddress);
        } else {
            callToServerWithoutBody(postBody, chosenGoogleAddress);
        }
    }

    private void callToServerWithoutBody(final String postBody, final String googleAdress) {
        Call<ResponseBody> responseBodyCall = Retrofit.getInstance().getInkService().makePost(mSharedHelper.getUserId(),
                postBody, googleAdress, mSharedHelper.getImageLink(), mSharedHelper.getFirstName(), mSharedHelper.getLastName(),
                Time.getTimeZone());
        responseBodyCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    if (success) {
                        finish();
                    } else {
                        showFailureDialog();
                    }
                } catch (IOException e) {
                    showFailureDialog();
                    e.printStackTrace();
                } catch (JSONException e) {
                    showFailureDialog();
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                callToServerWithoutBody(postBody, googleAdress);
            }
        });
    }

    private void callToServerWithBody(final Map<String, RequestBody> map, final String postBody, final String googleAddress) {
        Call<ResponseBody> responseBodyCall = Retrofit.getInstance().getInkService().makePost(map,
                mSharedHelper.getUserId(), postBody, googleAddress, mSharedHelper.getImageLink(),
                mSharedHelper.getFirstName(), mSharedHelper.getLastName(), Time.getTimeZone());
        responseBodyCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    if (success) {
                        showSuccessDialog();
                    } else {
                        showFailureDialog();
                    }
                } catch (IOException e) {
                    showFailureDialog();
                    e.printStackTrace();
                } catch (JSONException e) {
                    showFailureDialog();
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                callToServerWithBody(map, postBody, googleAddress);
            }
        });
    }


    private void showFailureDialog() {
        System.gc();
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(MakePost.this);
        builder.setTitle(getString(R.string.somethingWentWrong));
        builder.setMessage(getString(R.string.notPosted));
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        builder.show();
    }

    private void showSuccessDialog() {
        System.gc();
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
            }
        });
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                finish();
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                finish();
            }
        });
        builder.show();

    }
}
