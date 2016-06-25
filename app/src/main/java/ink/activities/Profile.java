package ink.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionMenu;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ink.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.utils.Constants;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by USER on 2016-06-22.
 */
public class Profile extends AppCompatActivity {
    private String mFriendId;
    private String mFirstName;
    private String mLastName;
    private Target mTarget;
    private ImageView mProfileImage;
    private CardView imageCard;
    private ViewGroup.LayoutParams mCardNewLayoutParams;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_view);
        ButterKnife.bind(this);
        ButterKnife.setDebug(true);
        mProfileFab.setEnabled(false);
        Bundle extras = getIntent().getExtras();
        mProfileImage = (ImageView) findViewById(R.id.profileImage);
        imageCard = (CardView) findViewById(R.id.imageCard);
        mCardNewLayoutParams = imageCard.getLayoutParams();
        Picasso.with(this).load(R.drawable.no_image).into(getTarget());
        ActionBar actionBar = getSupportActionBar();
        if (extras != null) {
            mFriendId = extras.getString("id");
            mFirstName = extras.getString("firstName");
            mLastName = extras.getString("lastName");
            mUsername.setText(mFirstName + " " + mLastName);
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setTitle(mFirstName + " " + mLastName);
            }
        }
        getSingleUser();
    }


    @OnClick(R.id.sendMessage)
    public void WriteMessage() {
        Intent intent = new Intent(getApplicationContext(), Chat.class);
        intent.putExtra("firstName", mFirstName);
        intent.putExtra("opponentId", mFriendId);
        startActivity(intent);
        mProfileFab.close(true);
    }

    @OnClick(R.id.block)
    public void block() {
        mProfileFab.close(true);
    }

    private void getSingleUser() {
        Call<ResponseBody> call = ink.utils.Retrofit.getInstance().getInkService().getSingleUserDetails(mFriendId);
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
                            String facebookProfile = jsonObject.optString("facebook_profile");
                            String imageLink = jsonObject.optString("image_link");
                            String skype = jsonObject.optString("skype");
                            String address = jsonObject.optString("address");
                            String relationship = jsonObject.optString("relationship");
                            String status = jsonObject.optString("status");

                            if (status.isEmpty()) {
                                mTriangleView.setVisibility(View.GONE);
                                mCardView.setVisibility(View.GONE);
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
                            AlertDialog.Builder builder = new AlertDialog.Builder(Profile.this);
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
                mProfileFab.setEnabled(true);
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        supportFinishAfterTransition();
        return super.onOptionsItemSelected(item);
    }


    private Target getTarget() {
        mTarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                mCardNewLayoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                imageCard.setLayoutParams(mCardNewLayoutParams);
                mProfileImage.setBackground(new BitmapDrawable(getResources(), bitmap));
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };
        return mTarget;
    }
}
