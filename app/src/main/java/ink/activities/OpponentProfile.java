package ink.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionMenu;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ink.R;
import com.koushikdutta.ion.Ion;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fab.FloatingActionButton;
import ink.utils.Constants;
import ink.utils.ScrollAwareFABBehavior;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by USER on 2016-06-22.
 */
public class OpponentProfile extends BaseActivity {
    private String mOpponentId;
    private String mFirstName;
    private String mLastName;
    private ImageView mProfileImage;
    private CardView imageCard;
    private String mFacebookLink;
    private boolean isSocialAccount;

    //Butter knife binders.
    @Bind(R.id.addressTV)
    TextView mAddress;
    @Bind(R.id.phoneTV)
    TextView mPhone;
    @Bind(R.id.relationshipTV)
    TextView mRelationship;
    @Bind(R.id.genderTV)
    TextView mGender;
    @Bind(R.id.facebookTV)
    TextView mFacebook;
    @Bind(R.id.facebookWrapper)
    RelativeLayout mFacebookWrapper;
    @Bind(R.id.skypeTV)
    TextView mSkype;
    @Bind(R.id.triangleView)
    ImageView mTriangleView;
    @Bind(R.id.statusCard)
    CardView mCardView;
    @Bind(R.id.genderIcon)
    ImageView mGenderImageView;
    @Bind(R.id.statusText)
    TextView mStatusText;
    @Bind(R.id.userName)
    TextView mUsername;
    @Bind(R.id.profileFab)
    FloatingActionMenu mProfileFab;
    @Bind(R.id.opponentImageLoading)
    AVLoadingIndicatorView mOpponentImageLoading;
    @Bind(R.id.addressWrapper)
    RelativeLayout mAddressWrapper;
    @Bind(R.id.sendMessage)
    FloatingActionButton sendMessage;
    @Bind(R.id.block)
    FloatingActionButton block;
    private String mOpponentImage;
    private boolean isFriend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.opponent_profile);
        ButterKnife.bind(this);
        ButterKnife.setDebug(true);
        mProfileFab.setEnabled(false);
        Bundle extras = getIntent().getExtras();
        mProfileImage = (ImageView) findViewById(R.id.profileImage);
        imageCard = (CardView) findViewById(R.id.imageCard);
        ViewGroup.LayoutParams mCardNewLayoutParams = imageCard.getLayoutParams();
        ActionBar actionBar = getSupportActionBar();
        if (extras != null) {
            mOpponentId = extras.getString("id");
            mFirstName = extras.getString("firstName");
            mLastName = extras.getString("lastName");
            isFriend = extras.getBoolean("isFriend");
            if (extras.containsKey("disableButton")) {
                if (extras.getBoolean("disableButton")) {
                    mProfileFab.setVisibility(View.GONE);
                } else {

                }
            } else {
                enableButton();
            }
            mUsername.setText(mFirstName + " " + mLastName);
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setTitle(mFirstName + " " + mLastName);
            }
        }
        if (!isFriend) {
            sendMessage.setImageResource(R.drawable.request_friend_icon);
            sendMessage.setLabelText(getString(R.string.sendFriendRequest));
        }
        getSingleUser();
    }

    private void enableButton() {
        mProfileFab.setVisibility(View.VISIBLE);
        CoordinatorLayout.LayoutParams p = (CoordinatorLayout.LayoutParams) mProfileFab.getLayoutParams();
        p.setBehavior(new ScrollAwareFABBehavior(this));
        mProfileFab.setLayoutParams(p);
    }


    @OnClick(R.id.facebookWrapper)
    public void facebook() {
        if (!mFacebook.getText().toString().equals(getString(R.string.loadingText)) && !mFacebook.getText().toString().equals(getString(R.string.noFacebook))) {
            Snackbar.make(mFacebookWrapper, getString(R.string.openingFacebook), Snackbar.LENGTH_SHORT);
            String addressToPass = mFacebookLink;
            openFacebookPage(addressToPass);
        }
    }

    @OnClick(R.id.addressWrapper)
    public void addressWrapper() {
        if (!mAddress.getText().toString().equals(getString(R.string.loadingText)) && !mAddress.getText().toString().equals(getString(R.string.noAddress))) {
            Snackbar.make(mAddressWrapper, getString(R.string.openingGoogle), Snackbar.LENGTH_SHORT);
            String addressToPass = mAddress.getText().toString();
            openGoogleMaps(addressToPass);
        }
    }

    @OnClick(R.id.sendMessage)
    public void WriteMessage() {
        Intent intent = new Intent(getApplicationContext(), Chat.class);
        intent.putExtra("firstName", mFirstName);
        intent.putExtra("lastName", mLastName);
        intent.putExtra("opponentId", mOpponentId);
        intent.putExtra("isSocialAccount", isSocialAccount);
        intent.putExtra("opponentImage", mOpponentImage);
        startActivity(intent);
        mProfileFab.close(true);
    }

    @OnClick(R.id.block)
    public void block() {
        mProfileFab.close(true);
    }

    private void getSingleUser() {
        Call<ResponseBody> call = ink.utils.Retrofit.getInstance().getInkService().getSingleUserDetails(mOpponentId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                mProfileFab.setEnabled(true);
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
                            mFacebookLink = jsonObject.optString("facebook_profile");
                            mOpponentImage = jsonObject.optString("image_link");
                            String skype = jsonObject.optString("skype");
                            String address = jsonObject.optString("address");
                            String relationship = jsonObject.optString("relationship");
                            String status = jsonObject.optString("status");
                            String facebookName = jsonObject.optString("facebook_name");
                            boolean shouldHighlightFacebook = true;
                            boolean shouldHighlightAddress = true;
                            isSocialAccount = jsonObject.optBoolean("isSocialAccount");
                            if (mOpponentImage != null && !mOpponentImage.isEmpty()) {
                                if (isSocialAccount) {
                                    Ion.with(getApplicationContext()).load(mOpponentImage).intoImageView(mProfileImage);
                                } else {
                                    Ion.with(getApplicationContext()).load(Constants.MAIN_URL +
                                            Constants.USER_IMAGES_FOLDER + mOpponentImage).intoImageView(mProfileImage);
                                }
                            } else {
                                mProfileImage.setBackgroundResource(R.drawable.no_image);
                                mOpponentImageLoading.setVisibility(View.GONE);
                            }

                            if (status.isEmpty()) {
                                mStatusText.setText(getString(R.string.shortNoStatus));
                            } else {
                                mStatusText.setText(status);
                            }
                            if (!gender.isEmpty()) {
                                if (gender.equals(Constants.GENDER_FEMALE)) {
                                    mGenderImageView.setBackground(null);
                                    mGenderImageView.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_gender_female));
                                } else {
                                    mGenderImageView.setBackground(null);
                                    mGenderImageView.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_gender_male));
                                }
                            } else {
                                gender = getString(R.string.noGender);
                            }

                            if (phoneNumber.isEmpty()) {
                                phoneNumber = getString(R.string.noPhone);
                            }
                            if (mFacebookLink.isEmpty()) {
                                mFacebookLink = getString(R.string.noFacebook);
                                facebookName = getString(R.string.noFacebook);
                                shouldHighlightFacebook = false;
                            }
                            if (skype.isEmpty()) {
                                skype = getString(R.string.noSkype);
                            }
                            if (address.isEmpty()) {
                                address = getString(R.string.noAddress);
                                shouldHighlightAddress = false;
                            }
                            if (relationship.isEmpty()) {
                                relationship = getString(R.string.noRelationship);
                            }
                            mPhone.setText(phoneNumber);
                            if (shouldHighlightFacebook) {
                                mFacebook.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                            }
                            if (shouldHighlightAddress) {
                                mAddress.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                            }
                            mFacebook.setText(facebookName);
                            mSkype.setText(skype);
                            mAddress.setText(address);
                            mRelationship.setText(relationship);
                            mGender.setText(gender);
                        } else {
                            mOpponentImageLoading.setVisibility(View.GONE);
                            AlertDialog.Builder builder = new AlertDialog.Builder(OpponentProfile.this);
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

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                getSingleUser();
            }
        });
    }


    @OnClick(R.id.profileImage)
    public void profileImage() {
        Intent intent = new Intent(getApplicationContext(), FullscreenActivity.class);
        if (mOpponentImage != null && !mOpponentImage.isEmpty()) {
            if (isSocialAccount) {
                intent.putExtra("link", mOpponentImage);
            } else {
                intent.putExtra("link", Constants.MAIN_URL +
                        Constants.USER_IMAGES_FOLDER + mOpponentImage);
            }
        } else {
            intent.putExtra("link", Constants.NO_IMAGE_URL);
        }
        startActivity(intent);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        supportFinishAfterTransition();
        return super.onOptionsItemSelected(item);
    }

    private void openGoogleMaps(String address) {
        String uri = "geo:0,0?q=" + address;
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }

    }

    private Intent getOpenFacebookIntent(String page) {

        try {
            getPackageManager()
                    .getPackageInfo("com.facebook.katana", 0);
            return new Intent(Intent.ACTION_VIEW,
                    Uri.parse("fb://facewebmodal/f?href=" + page));
        } catch (Exception e) {
            return new Intent(Intent.ACTION_VIEW,
                    Uri.parse(page));
        }
    }

    private void openFacebookPage(String page) {
        Intent facebookIntent = getOpenFacebookIntent(page);
        startActivity(facebookIntent);
    }

}
