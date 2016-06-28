package ink.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionMenu;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;

import com.ink.R;

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
    @Bind(R.id.userName)
    EditText mUsername;
    @Bind(R.id.profileFab)
    FloatingActionMenu mProfileFab;
    @Bind(R.id.profileImage)
    ImageView profileImage;
    private boolean isEditing;
    @Bind(R.id.editProfile)
    FloatingActionButton mEditSaveButton;
    private Menu mCancelMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(R.string.myProfileText));
        }
        ButterKnife.bind(this);
        mSharedHelper = new SharedHelper(this);
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
                    String firstName = jsonObject.optString("first_name");
                    String lastName = jsonObject.optString("last_name");
                    String gender = jsonObject.optString("gender");
                    String phoneNumber = jsonObject.optString("phone_number");
                    String facebookProfile = jsonObject.optString("facebook_profile");
                    String imageLink = jsonObject.optString("image_link");
                    String skype = jsonObject.optString("skype");
                    String address = jsonObject.optString("address");
                    String relationship = jsonObject.optString("relationship");
                    String status = jsonObject.optString("status");

                    mUsername.setText(firstName + " " + lastName);
                    if (status.isEmpty()) {
                        mStatusText.setText(getString(R.string.noStatusText));
                    } else {
                        mStatusText.setText(status);
                    }
                    if (!gender.isEmpty()) {
                        if (gender.equals(Constants.GENDER_FEMALE)) {
                            mGenderImageView.setBackgroundResource(R.drawable.ic_gender_female);
                        }
                    } else {
                        gender = getString(R.string.noGender);
                    }

                    if (phoneNumber.isEmpty()) {
                        phoneNumber = getString(R.string.noPhone);
                    }
                    if (facebookProfile.isEmpty()) {
                        facebookProfile = getString(R.string.noFacebook);
                    }
                    if (skype.isEmpty()) {
                        skype = getString(R.string.noSkype);
                    }
                    if (address.isEmpty()) {
                        address = getString(R.string.noAddress);
                    }
                    if (relationship.isEmpty()) {
                        relationship = getString(R.string.noRelationship);
                    }
                    mPhone.setText(phoneNumber);
                    mFacebook.setText(facebookProfile);
                    mSkype.setText(skype);
                    mAddress.setText(address);
                    mRelationship.setText(relationship);
                    mGender.setText(gender);
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

    @OnClick(R.id.editProfile)
    public void editProfile() {
        mProfileFab.close(true);
        if (!isEditing) {
            enableEdit();
            isEditing = true;
        }
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
    }
}
