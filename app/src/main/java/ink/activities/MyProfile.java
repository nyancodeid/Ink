package ink.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionMenu;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.ink.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.PicassoTools;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fab.FloatingActionButton;
import ink.callbacks.GeneralCallback;
import ink.utils.Constants;
import ink.utils.FileUtils;
import ink.utils.Retrofit;
import ink.utils.SharedHelper;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyProfile extends AppCompatActivity {

    private static final int PICK_IMAGE_RESULT_CODE = 1547;
    private SharedHelper mSharedHelper;
    //Butter knife binders.
    @Bind(R.id.addressTV)
    EditText mAddress;
    @Bind(R.id.phoneTV)
    EditText mPhone;
    @Bind(R.id.relationshipTV)
    EditText mRelationship;
    @Bind(R.id.genderTV)
    EditText mGender;
    @Bind(R.id.facebookTV)
    EditText mFacebook;
    @Bind(R.id.skypeTV)
    EditText mSkype;
    @Bind(R.id.triangleView)
    ImageView mTriangleView;
    @Bind(R.id.statusCard)
    CardView mCardView;
    @Bind(R.id.genderIcon)
    ImageView mGenderImageView;
    @Bind(R.id.statusText)
    EditText mStatusText;
    @Bind(R.id.profileFab)
    FloatingActionMenu mProfileFab;
    @Bind(R.id.profileImage)
    ImageView profileImage;
    private boolean isEditing;
    private boolean isDataDownloaded;
    @Bind(R.id.editProfile)
    FloatingActionButton mEditSaveButton;
    @Bind(R.id.imageLoading)
    AVLoadingIndicatorView mImageLoading;
    private Menu mCancelMenuItem;
    @Bind(R.id.editImageNameFab)
    FloatingActionButton mEditImageNameFab;

    @Bind(R.id.collapsing_toolbar)
    CollapsingToolbarLayout mCollapsingToolbar;
    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    private Snackbar updateSnackbar;
    private String mFirstNameToSend;
    private String mLastNameToSend;
    private String mGenderToSend;
    private String mPhoneNumberToSend;
    private String mFacebookProfileToSend;
    private String mImageLinkToSend;
    private String mSkypeToSend;
    private String mAddressToSend;
    private String mRelationshipToSend;
    private String mStatusToSend;
    private AlertDialog.Builder promptBuilder;
    private PopupMenu mEditPopUp;
    private Thread mWorkerThread;
    private View mDialogView;
    private CallbackManager mCallbackManager;
    private ProgressDialog facebookAttachDialog;
    private boolean isImageChosen;
    private String mFacebookName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this);
        setContentView(R.layout.activity_my_profile);
        ButterKnife.bind(this);
        registerFacebookCallback();
        mEditImageNameFab.hide(false);
        setSupportActionBar(mToolbar);
        facebookAttachDialog = new ProgressDialog(this);
        facebookAttachDialog.setTitle("Please wait...");
        facebookAttachDialog.setMessage("Attaching facebook profile...");
        facebookAttachDialog.dismiss();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mSharedHelper = new SharedHelper(this);
        mFirstNameToSend = mSharedHelper.getFirstName();
        mLastNameToSend = mSharedHelper.getLastName();
        mCollapsingToolbar.setTitle(mFirstNameToSend + " " + mLastNameToSend);
        mCollapsingToolbar.setExpandedTitleColor(Color.parseColor("#99000000"));

        mProfileFab.setOnMenuButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isDataDownloaded) {
                    if (mProfileFab.isOpened()) {
                        mProfileFab.close(true);
                    } else {
                        mProfileFab.open(true);
                    }
                } else {
                    Snackbar.make(mProfileFab, getString(R.string.waitTillLoad), Snackbar.LENGTH_SHORT).show();
                }
            }
        });
        getMyData();
    }

    private void registerFacebookCallback() {
        mCallbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(mCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        facebookAttachDialog.show();
                        GraphRequest request = GraphRequest.newMeRequest(
                                loginResult.getAccessToken(),
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(
                                            JSONObject object,
                                            GraphResponse response) {
                                        String userLink = object.optString("link");
                                        mFacebookName = object.optString("name");
                                        mFacebook.setText(mFacebookName);
                                        mFacebookProfileToSend = userLink;
                                        facebookAttachDialog.dismiss();

                                    }
                                });
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "link,name");
                        request.setParameters(parameters);
                        request.executeAsync();

                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onError(FacebookException exception) {

                    }
                });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_RESULT_CODE) {
                Uri selectedImageUri = data.getData();
                String selectedImagePath;
                try {
                    selectedImagePath = getRealPathFromURI(selectedImageUri);
                } catch (Exception e) {
                    selectedImagePath = null;
                    AlertDialog.Builder builder = new AlertDialog.Builder(MyProfile.this);
                    builder.setTitle(getString(R.string.notSupported));
                    builder.setMessage(getString(R.string.notSupportedText));
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    builder.show();
                }
                if (selectedImagePath != null) {
                    mImageLinkToSend = selectedImagePath;
                    mCollapsingToolbar.setExpandedTitleColor(Color.parseColor("#ffffff"));
                    showImageLoading();
                    isImageChosen = true;
                    Picasso.with(getApplicationContext()).load(new File(selectedImagePath)).error(R.drawable.image_laoding_error).fit()
                            .centerInside().into(profileImage, picassoCallback(selectedImagePath, true));
                } else {
                    isImageChosen = false;
                }
            }
        }
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    private void getMyData() {
        final Call<ResponseBody> myDataResponse = Retrofit.getInstance()
                .getInkService().getSingleUserDetails(mSharedHelper.getUserId());
        myDataResponse.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    getMyData();
                    return;
                }
                if (response.body() == null) {
                    getMyData();
                    return;
                }
                fetchData(response);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                getMyData();
            }
        });
    }


    private void fetchData(Response<ResponseBody> response) {
        try {
            String responseString = response.body().string();
            try {
                JSONObject jsonObject = new JSONObject(responseString);
                boolean success = jsonObject.optBoolean("success");
                if (success) {
                    String userId = jsonObject.optString("userId");
                    mFirstNameToSend = jsonObject.optString("first_name");
                    mLastNameToSend = jsonObject.optString("last_name");
                    mGenderToSend = jsonObject.optString("gender");
                    mPhoneNumberToSend = jsonObject.optString("phone_number");
                    mFacebookProfileToSend = jsonObject.optString("facebook_profile");
                    mFacebookName = jsonObject.optString("facebook_name");
                    mImageLinkToSend = jsonObject.optString("image_link");
                    mSkypeToSend = jsonObject.optString("skype");
                    mAddressToSend = jsonObject.optString("address");
                    mRelationshipToSend = jsonObject.optString("relationship");
                    mStatusToSend = jsonObject.optString("status");

                    attachValues(true);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MyProfile.this);
                    builder.setTitle(getString(R.string.singleUserErrorTile));
                    builder.setMessage(getString(R.string.singleUserErrorMessage));
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void attachValues(boolean shouldLoadImage) {
        if (mStatusToSend.isEmpty()) {
            mStatusToSend = getString(R.string.noStatusText);
        }

        if (!mGenderToSend.isEmpty()) {
            if (mGenderToSend.equals(Constants.GENDER_FEMALE)) {
                mGenderImageView.setBackground(null);
                mGenderImageView.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_gender_female));
            } else {
                mGenderImageView.setBackground(null);
                mGenderImageView.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_gender_male));
            }
        } else {
            mGenderToSend = getString(R.string.noGender);
        }

        if (mPhoneNumberToSend.isEmpty()) {
            mPhoneNumberToSend = getString(R.string.noPhone);
        }
        if (mFacebookProfileToSend.isEmpty()) {
            mFacebookProfileToSend = getString(R.string.noFacebook);
            mFacebookName = getString(R.string.noFacebook);
        }
        if (mSkypeToSend.isEmpty()) {
            mSkypeToSend = getString(R.string.noSkype);
        }
        if (mAddressToSend.isEmpty()) {
            mAddressToSend = getString(R.string.noAddress);
        }
        if (mRelationshipToSend.isEmpty()) {
            mRelationshipToSend = getString(R.string.noRelationship);
        }

        if (mImageLinkToSend != null && !mImageLinkToSend.isEmpty()) {
            mCollapsingToolbar.setExpandedTitleColor(Color.parseColor("#ffffff"));
            if (shouldLoadImage) {
                Picasso.with(getApplicationContext()).load(Constants.MAIN_URL + Constants.USER_IMAGES_FOLDER + mImageLinkToSend).fit().centerInside().into(profileImage, picassoCallback(mImageLinkToSend, false));
            } else {
                mSharedHelper.putFirstName(mFirstNameToSend);
                mSharedHelper.putLastName(mLastNameToSend);
                mCollapsingToolbar.setTitle(mFirstNameToSend + " " + mLastNameToSend);
            }
        } else {
            profileImage.setBackgroundResource(R.drawable.no_image);
        }
        mPhone.setText(mPhoneNumberToSend);
        mFacebook.setText(mFacebookName);
        mSkype.setText(mSkypeToSend);
        mAddress.setText(mAddressToSend);
        mRelationship.setText(mRelationshipToSend);
        mStatusText.setText(mStatusToSend);
        mGender.setText(mGenderToSend);
        isDataDownloaded = true;
        hideImageLoading();
        hideSnack();
    }


    @OnClick(R.id.editProfile)
    public void editProfile() {
        mProfileFab.close(true);
        mProfileFab.hideMenuButton(true);
        if (!isEditing) {
            enableEdit();
            isEditing = true;
        }
    }


    @OnClick(R.id.relationshipTV)
    public void relationship() {
        if (isEditing) {
            PopupMenu popupMenu = new PopupMenu(MyProfile.this, mRelationship);
            popupMenu.getMenu().add(0, 0, 0, getString(R.string.single));
            popupMenu.getMenu().add(1, 1, 1, getString(R.string.inRelationship));
            popupMenu.getMenu().add(2, 2, 2, getString(R.string.engaged));
            popupMenu.getMenu().add(3, 3, 3, getString(R.string.married));
            popupMenu.getMenu().add(4, 4, 4, getString(R.string.complicated));
            popupMenu.getMenu().add(5, 5, 5, getString(R.string.openRelationship));
            popupMenu.getMenu().add(6, 6, 6, getString(R.string.divorced));
            popupMenu.show();
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case 0:
                            mRelationship.setText(getString(R.string.single));
                            break;
                        case 1:
                            mRelationship.setText(getString(R.string.inRelationship));
                            break;
                        case 2:
                            mRelationship.setText(getString(R.string.engaged));
                            break;
                        case 3:
                            mRelationship.setText(getString(R.string.married));
                            break;
                        case 4:
                            mRelationship.setText(getString(R.string.complicated));
                            break;
                        case 5:
                            mRelationship.setText(getString(R.string.openRelationship));
                            break;
                        case 6:
                            mRelationship.setText(getString(R.string.divorced));
                            break;
                    }
                    return true;
                }
            });
        }
    }


    @OnClick(R.id.facebookTV)
    public void facebook() {
        if (isEditing) {
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"));
        }
    }

    @OnClick(R.id.genderTV)
    public void gender() {
        if (isEditing) {
            PopupMenu popupMenu = new PopupMenu(MyProfile.this, mGender);
            popupMenu.getMenu().add(0, 0, 0, getString(R.string.femaleGender));
            popupMenu.getMenu().add(1, 1, 1, getString(R.string.maleGender));
            popupMenu.show();
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case 0:
                            mGender.setText(getString(R.string.femaleGender));
                            mGenderImageView.setBackground(null);
                            mGenderImageView.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_gender_female));
                            break;
                        case 1:
                            mGenderImageView.setBackground(null);
                            mGender.setText(getString(R.string.maleGender));
                            mGenderImageView.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_gender_male));
                            break;
                    }
                    return true;
                }
            });
        }
    }

    @OnClick(R.id.editImageNameFab)
    public void editImageName() {
        System.gc();
        mEditPopUp = new PopupMenu(MyProfile.this, mEditImageNameFab);
        mEditPopUp.getMenu().add(0, 0, 0, getString(R.string.changeImage));
        mEditPopUp.getMenu().add(1, 1, 1, getString(R.string.changeName));
        mEditPopUp.show();
        mEditPopUp.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case 0:
                        openGallery();
                        break;
                    case 1:
                        openNameChanger();
                        break;
                }
                return true;
            }
        });
    }


    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,
                getString(R.string.selectImage)), PICK_IMAGE_RESULT_CODE);
    }

    private void openNameChanger() {
        System.gc();
        promptBuilder = new AlertDialog.Builder(this);
        promptBuilder.setCancelable(false);
        LayoutInflater inflater = this.getLayoutInflater();
        mDialogView = inflater.inflate(R.layout.name_view, null);
        final EditText nameChange = (EditText) mDialogView.findViewById(R.id.firstNameChange);
        final EditText lastNameChange = (EditText) mDialogView.findViewById(R.id.lastNameChange);
        nameChange.setText(mFirstNameToSend);
        lastNameChange.setText(mLastNameToSend);
        nameChange.setSelection(mFirstNameToSend.length());
        promptBuilder.setView(mDialogView);
        promptBuilder.setPositiveButton(getString(R.string.saveText), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (!nameChange.getText().toString().isEmpty()) {
                    mFirstNameToSend = nameChange.getText().toString();
                }
                if (!lastNameChange.getText().toString().isEmpty()) {
                    mLastNameToSend = lastNameChange.getText().toString();
                }
                mCollapsingToolbar.setTitle(mFirstNameToSend + " " + mLastNameToSend);
            }
        });
        promptBuilder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        promptBuilder.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.save:
                promptUser();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my_profile_menu, menu);
        mCancelMenuItem = menu;
        menu.getItem(0).setVisible(false);
        return true;
    }

    private void enableEdit() {

        String status = mStatusText.getText().toString();
        String address = mAddress.getText().toString();
        String phone = mPhone.getText().toString();
        String relationship = mRelationship.getText().toString();
        String gender = mGender.getText().toString();
        String facebook = mFacebook.getText().toString();
        String skype = mSkype.getText().toString();

        mStatusText.setText("");
        mStatusText.setHint(status);
        mAddress.setText("");
        mAddress.setHint(address);
        mPhone.setText("");
        mPhone.setHint(phone);
        mRelationship.setText("");
        mRelationship.setHint(relationship);
        mGender.setText("");
        mGender.setHint(gender);
        mFacebook.setText("");
        mFacebook.setHint(facebook);
        mSkype.setText("");
        mSkype.setHint(skype);

        mEditImageNameFab.show(true);
        mCancelMenuItem.getItem(0).setVisible(true);
        mStatusText.setFocusable(true);
        mStatusText.setFocusableInTouchMode(true);
        mStatusText.requestFocus();
        mStatusText.requestFocusFromTouch();
        mStatusText.setSelection(mStatusText.getText().length());
        mAddress.setFocusable(true);
        mAddress.setFocusableInTouchMode(true);
        mPhone.setFocusable(true);
        mPhone.setFocusableInTouchMode(true);
        mGender.setClickable(true);
        mSkype.setFocusable(true);
        mSkype.setFocusableInTouchMode(true);
    }

    private void saveEdit() {
        isEditing = false;
        mEditImageNameFab.hide(true);
        mStatusText.setFocusable(false);
        mStatusText.setFocusableInTouchMode(false);
        mAddress.setFocusable(false);
        mAddress.setFocusableInTouchMode(false);
        mPhone.setFocusable(false);
        mPhone.setFocusableInTouchMode(false);
        mRelationship.setFocusable(false);
        mRelationship.setFocusableInTouchMode(false);
        mGender.setFocusable(false);
        mGender.setFocusableInTouchMode(false);
        mGender.setClickable(false);
        mFacebook.setFocusable(false);
        mFacebook.setFocusableInTouchMode(false);
        mSkype.setFocusable(false);
        mSkype.setFocusableInTouchMode(false);

    }


    private void promptUser() {
        if (!mStatusText.getText().toString().isEmpty()) {
            mStatusToSend = mStatusText.getText().toString();
        }

        if (!mAddress.getText().toString().isEmpty()) {
            mAddressToSend = mAddress.getText().toString();
        }

        if (!mPhone.getText().toString().isEmpty()) {
            mPhoneNumberToSend = mPhone.getText().toString();
        }

        if (!mRelationship.getText().toString().isEmpty()) {
            mRelationshipToSend = mRelationship.getText().toString();
        }

        if (!mGender.getText().toString().isEmpty()) {
            mGenderToSend = mGender.getText().toString();
        }

        if (!mFacebook.getText().toString().isEmpty()) {
            mFacebookName = mFacebook.getText().toString();
        }


        if (!mSkype.getText().toString().isEmpty()) {
            mSkypeToSend = mSkype.getText().toString();
        }


        showAlertWithValues();
    }

    private void showAlertWithValues() {
        System.gc();
        promptBuilder = new AlertDialog.Builder(this);
        promptBuilder.setCancelable(false);
        String statusText = mStatusToSend;
        if (mStatusToSend.equals(getString(R.string.noStatusText))) {
            statusText = getString(R.string.noStatusWritten);
        }
        String finalPrompt = getString(R.string.correctnessDetailViewText, statusText, mFirstNameToSend,
                mLastNameToSend, mAddressToSend, mPhoneNumberToSend,
                mRelationshipToSend, mGenderToSend,
                mFacebookName, mSkypeToSend);
        promptBuilder.setTitle(getString(R.string.checkCorrectness));
        promptBuilder.setMessage(finalPrompt);
        promptBuilder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                sendUpdatesToServer();
                mProfileFab.showMenuButton(true);
                saveEdit();
                mCancelMenuItem.getItem(0).setVisible(false);
            }
        });
        promptBuilder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mProfileFab.hideMenuButton(false);
            }
        });
        promptBuilder.show();

    }

    private void sendUpdatesToServer() {
        showImageLoading();
        showSnack(mProfileFab);
        isDataDownloaded = false;
        if (mStatusText.getText().toString().equals(getString(R.string.noStatusText))) {
            mStatusToSend = "";
        }
        if (mAddress.getText().toString().equals(getString(R.string.noAddress))) {
            mAddressToSend = "";
        }
        if (mPhone.getText().toString().equals(getString(R.string.noPhone))) {
            mPhoneNumberToSend = "";
        }
        if (mRelationship.getText().toString().equals(getString(R.string.noRelationship))) {
            mRelationshipToSend = "";
        }
        if (mGender.getText().toString().equals(getString(R.string.noGender))) {
            mGenderToSend = "";
        }
        if (mFacebook.getText().toString().equals(getString(R.string.noFacebook))) {
            mFacebookProfileToSend = "";
        }
        if (mSkype.getText().toString().equals(getString(R.string.noSkype))) {
            mSkypeToSend = "";
        }
        if (!mImageLinkToSend.isEmpty() && isImageChosen) {
            getBase64String(mImageLinkToSend, new GeneralCallback<String>() {
                @Override
                public void onSuccess(String s) {
                    callServer(s);
                }

                @Override
                public void onFailure(String s) {
                    sendUpdatesToServer();
                }
            });

        } else {
            callServer("");
        }
    }


    private void callServer(final String base64) {
        final Call<ResponseBody> updateCall = Retrofit.getInstance().getInkService().updateUserDetails(mSharedHelper.getUserId(),
                mFirstNameToSend.trim(), mLastNameToSend.trim(),
                mAddressToSend.equals(getString(R.string.noAddress)) ? "" : mAddressToSend.trim(), mPhoneNumberToSend.equals(getString(R.string.noPhone)) ? "" : mPhoneNumberToSend.trim(),
                mRelationshipToSend.equals(getString(R.string.noRelationship)) ? "" : mRelationshipToSend.trim(),
                mGenderToSend.equals(getString(R.string.noGender)) ? "" : mGenderToSend.trim(),
                mFacebookProfileToSend.equals(getString(R.string.noFacebook)) ? "" : mFacebookProfileToSend.trim(),
                mSkypeToSend.equals(getString(R.string.noSkype)) ? "" : mSkypeToSend.trim(), base64,
                mStatusToSend.equals(getString(R.string.noStatusText)) ? "" : mStatusToSend.trim(), mFacebookName, mImageLinkToSend);
        updateCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    sendUpdatesToServer();
                    return;
                }
                if (response.body() == null) {
                    sendUpdatesToServer();
                    return;
                }
                try {
                    String body = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(body);
                        boolean success = jsonObject.optBoolean("success");
                        if (success) {
                            mCollapsingToolbar.setExpandedTitleColor(Color.parseColor("#ffffff"));
                            attachValues(false);
                            hideImageLoading();
                            hideSnack();
                            Log.d("fasfsafasfas", "onResponse: " + jsonObject);
                            String imageId = jsonObject.optString("image_id");
                            if (imageId != null && !imageId.isEmpty()) {
                                String imageLink = mSharedHelper.getUserId() + ".png";
                                Picasso.with(getApplicationContext()).invalidate(mSharedHelper.getImageLink());
                                mSharedHelper.putImageLink(imageLink);
                                PicassoTools.clearCache(Picasso.with(getApplicationContext()));
                                FileUtils.deleteDirectoryTree(getApplicationContext().getCacheDir());
                                Picasso.with(getApplicationContext()).invalidate(Constants.MAIN_URL +
                                        Constants.USER_IMAGES_FOLDER + mSharedHelper.getImageLink());
                            }

                        } else {
                            hideImageLoading();
                            hideSnack();
                            promptBuilder = new AlertDialog.Builder(MyProfile.this);
                            promptBuilder.setTitle(getString(R.string.error));
                            promptBuilder.setMessage(getString(R.string.couldNotUpdate));
                            promptBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });
                            promptBuilder.show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                sendUpdatesToServer();
            }
        });


    }

    private void getBase64String(final String path, final GeneralCallback callback) {
        mWorkerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                bitmap = reduceBitmap(bitmap, 500);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] bytes = byteArrayOutputStream.toByteArray();
                String encodedImage = Base64.encodeToString(bytes, Base64.DEFAULT);
                if (encodedImage == null) {
                    callback.onFailure(encodedImage);
                } else {
                    callback.onSuccess(encodedImage);
                }
                bitmap.recycle();
                bitmap = null;
                mWorkerThread = null;
            }
        });
        mWorkerThread.start();
    }

    private Bitmap reduceBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(image, width, height, true);
    }


    @Override
    protected void onDestroy() {
        isDataDownloaded = false;
        super.onDestroy();
    }

    private com.squareup.picasso.Callback picassoCallback(final String imageLinkToSend, final boolean shouldUseFullPath) {
        System.gc();
        com.squareup.picasso.Callback callback = new com.squareup.picasso.Callback() {
            @Override
            public void onSuccess() {
                hideImageLoading();
            }

            @Override
            public void onError() {
                showImageLoading();
                String finalPath = imageLinkToSend;
                if (!shouldUseFullPath) {
                    finalPath = Constants.MAIN_URL +
                            Constants.USER_IMAGES_FOLDER + imageLinkToSend;
                }
                Picasso.with(getApplicationContext()).load(finalPath).fit().centerInside().into(profileImage, picassoCallback(imageLinkToSend, shouldUseFullPath));
            }
        };
        return callback;
    }


    private void showImageLoading() {
        mImageLoading.setVisibility(View.VISIBLE);
    }

    private void hideImageLoading() {
        mImageLoading.setVisibility(View.GONE);
    }

    private void showSnack(View view) {
        updateSnackbar = Snackbar.make(view, getString(R.string.updating), Snackbar.LENGTH_INDEFINITE);
        updateSnackbar.show();
    }

    private void hideSnack() {
        if (updateSnackbar != null) {
            if (updateSnackbar.isShown()) {
                updateSnackbar.setText(getString(R.string.saved));
                updateSnackbar.setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        updateSnackbar.dismiss();
                    }
                });
            }
        }
    }
}
