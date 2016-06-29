package ink.activities;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionMenu;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.ink.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fab.FloatingActionButton;
import ink.utils.Constants;
import ink.utils.Retrofit;
import ink.utils.SharedHelper;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyProfile extends AppCompatActivity {

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
    @Bind(R.id.editProfile)
    FloatingActionButton mEditSaveButton;
    private Menu mCancelMenuItem;
    @Bind(R.id.editImageNameFab)
    FloatingActionButton mEditImageNameFab;

    @Bind(R.id.collapsing_toolbar)
    CollapsingToolbarLayout mCollapsingToolbar;
    @Bind(R.id.toolbar)
    Toolbar mToolbar;

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
    private View mDialogView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);
        ButterKnife.bind(this);
        mEditImageNameFab.hide(false);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mSharedHelper = new SharedHelper(this);
        mFirstNameToSend = mSharedHelper.getFirstName();
        mLastNameToSend = mSharedHelper.getLastName();
        mCollapsingToolbar.setTitle(mFirstNameToSend + " " + mLastNameToSend);
        mProfileFab.setEnabled(false);
        mCollapsingToolbar.setExpandedTitleColor(Color.parseColor("#99000000"));
        getMyData();
    }


    private void getMyData() {
        Call<ResponseBody> myDataResponse = Retrofit.getInstance()
                .getInkService().getSingleUserDetails(mSharedHelper.getUserId());
        myDataResponse.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
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
                    mImageLinkToSend = jsonObject.optString("image_link");
                    mSkypeToSend = jsonObject.optString("skype");
                    mAddressToSend = jsonObject.optString("address");
                    mRelationshipToSend = jsonObject.optString("relationship");
                    mStatusToSend = jsonObject.optString("status");

                    attachValues();
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
        mProfileFab.setEnabled(true);
    }

    private void attachValues() {
        if (mStatusToSend.isEmpty()) {
            mStatusText.setText(getString(R.string.noStatusText));
        } else {
            mStatusText.setText(mStatusToSend);
        }
        if (!mGenderToSend.isEmpty()) {
            if (mGenderToSend.equals(Constants.GENDER_FEMALE)) {
                mGenderImageView.setBackgroundResource(R.drawable.ic_gender_female);
            }
        } else {
            mGenderToSend = getString(R.string.noGender);
        }

        if (mPhoneNumberToSend.isEmpty()) {
            mPhoneNumberToSend = getString(R.string.noPhone);
        }
        if (mFacebookProfileToSend.isEmpty()) {
            mFacebookProfileToSend = getString(R.string.noFacebook);
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
            Picasso.with(getApplicationContext()).load(Constants.MAIN_URL + Constants.USER_IMAGES_FOLDER + mImageLinkToSend).into(getProfileTarget(profileImage));
        } else {
            profileImage.setBackgroundResource(R.drawable.no_image);
        }
        mPhone.setText(mPhoneNumberToSend);
        mFacebook.setText(mFacebookProfileToSend);
        mSkype.setText(mSkypeToSend);
        mAddress.setText(mAddressToSend);
        mRelationship.setText(mRelationshipToSend);
        mGender.setText(mGenderToSend);
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

    }

    private void openNameChanger() {
        System.gc();
        promptBuilder = new AlertDialog.Builder(this);
        promptBuilder.setCancelable(false);
        LayoutInflater inflater = this.getLayoutInflater();
        mDialogView = inflater.inflate(R.layout.name_view, null);

        promptBuilder.setView(mDialogView);
        promptBuilder.setPositiveButton(getString(R.string.saveText), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

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
                item.setVisible(false);
                saveEdit();
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
        mRelationship.setFocusable(true);
        mRelationship.setFocusableInTouchMode(true);
        mGender.setFocusable(true);
        mGender.setFocusableInTouchMode(true);
        mFacebook.setFocusable(true);
        mFacebook.setFocusableInTouchMode(true);
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
        mFacebook.setFocusable(false);
        mFacebook.setFocusableInTouchMode(false);
        mSkype.setFocusable(false);
        mSkype.setFocusableInTouchMode(false);

        if (!mStatusText.getText().toString().isEmpty()) {
            mStatusToSend = mStatusText.getText().toString();
            if (mStatusText.getText().toString().equals(getString(R.string.noStatusText))) {
                mStatusToSend = getString(R.string.noStatusWasWritte);
            }
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
            mFacebookProfileToSend = mFacebook.getText().toString();
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
        String finalPrompt = getString(R.string.correctnessDetailViewText, mStatusToSend, mFirstNameToSend,
                mLastNameToSend, mAddressToSend, mPhoneNumberToSend,
                mRelationshipToSend, mGenderToSend,
                mFacebookProfileToSend, mSkypeToSend);
        promptBuilder.setTitle(getString(R.string.checkCorrectness));
        promptBuilder.setMessage(finalPrompt);
        promptBuilder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mProfileFab.showMenuButton(true);
            }
        });
        promptBuilder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                sendUpdatesToServer();
                mProfileFab.showMenuButton(true);
            }
        });
        promptBuilder.show();

    }

    private void sendUpdatesToServer() {
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
        final Call<ResponseBody> updateCall = Retrofit.getInstance().getInkService().updateUserDetails(mSharedHelper.getUserId(), mFirstNameToSend, mLastNameToSend,
                mAddressToSend, mPhoneNumberToSend, mRelationshipToSend, mGenderToSend, mFacebookProfileToSend, mSkypeToSend);
        updateCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String body = response.body().string();
                    Log.d("onResponse", "onResponse: " + body);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                updateCall.enqueue(this);
            }
        });
    }


    private Target getProfileTarget(final ImageView imageView) {
        return new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                imageView.setImageBitmap(bitmap);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };
    }
}
