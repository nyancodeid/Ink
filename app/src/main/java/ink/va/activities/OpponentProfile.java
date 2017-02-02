package ink.va.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionMenu;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ink.va.R;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fab.FloatingActionButton;
import ink.va.utils.Constants;
import ink.va.utils.DimDialog;
import ink.va.utils.RealmHelper;
import ink.va.utils.Retrofit;
import ink.va.utils.ScrollAwareFABBehavior;
import ink.va.utils.SharedHelper;
import it.sephiroth.android.library.picasso.NetworkPolicy;
import it.sephiroth.android.library.picasso.Picasso;
import it.sephiroth.android.library.picasso.Target;
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

    private String mFacebookLink;
    private boolean isSocialAccount;
    private boolean hasFriendRequested;
    private boolean isDataLoaded;
    private SharedHelper sharedHelper;

    //Butter knife binders.
    @BindView(R.id.profileImage)
    ImageView mProfileImage;
    @BindView(R.id.addressTV)
    TextView mAddress;
    @BindView(R.id.phoneTV)
    TextView mPhone;
    @BindView(R.id.relationshipTV)
    TextView mRelationship;
    @BindView(R.id.genderTV)
    TextView mGender;
    @BindView(R.id.facebookTV)
    TextView mFacebook;
    @BindView(R.id.facebookWrapper)
    RelativeLayout mFacebookWrapper;
    @BindView(R.id.skypeTV)
    TextView mSkype;
    @BindView(R.id.triangleView)
    ImageView mTriangleView;
    @BindView(R.id.statusCard)
    CardView mCardView;
    @BindView(R.id.genderIcon)
    ImageView mGenderImageView;
    @BindView(R.id.statusText)
    TextView mStatusText;
    @BindView(R.id.userName)
    TextView mUsername;
    @BindView(R.id.profileFab)
    FloatingActionMenu mProfileFab;
    @BindView(R.id.opponentImageLoading)
    AVLoadingIndicatorView mOpponentImageLoading;
    @BindView(R.id.addressWrapper)
    RelativeLayout mAddressWrapper;
    @BindView(R.id.sendMessage)
    FloatingActionButton sendMessage;
    @BindView(R.id.removeFriend)
    FloatingActionButton removeFriend;
    @BindView(R.id.callUserPhone)
    ImageView callUserPhone;
    @BindView(R.id.singleUserBadge)
    ImageView singleUserBadge;
    private String mOpponentImage;
    private boolean isFriend;
    private Target target;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.opponent_profile);
        ButterKnife.bind(this);
        ButterKnife.setDebug(true);
        mProfileFab.setEnabled(false);
        Bundle extras = getIntent().getExtras();
        sharedHelper = new SharedHelper(this);

        ActionBar actionBar = getSupportActionBar();
        if (extras != null) {
            mOpponentId = extras.getString("id");
            mFirstName = extras.getString("firstName");
            mLastName = extras.getString("lastName");
            isFriend = extras.getBoolean("isFriend");
            if (extras.containsKey("disableButton")) {
                if (extras.getBoolean("disableButton") && !isFriend) {
                    sendMessage.setVisibility(View.VISIBLE);
                } else if (extras.getBoolean("disableButton")) {
                    sendMessage.setVisibility(View.GONE);
                }
            }
            enableButton();
            mUsername.setText(mFirstName + " " + mLastName);
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setTitle(mFirstName + " " + mLastName);
            }
        }
        if (!isFriend) {
            sendMessage.setImageResource(R.drawable.request_friend_icon);
            sendMessage.setLabelText(getString(R.string.sendFriendRequest));
            removeFriend.setVisibility(View.GONE);
        }
        getSingleUser();
    }

    private void enableButton() {
        mProfileFab.setVisibility(View.VISIBLE);
        CoordinatorLayout.LayoutParams p = (CoordinatorLayout.LayoutParams) mProfileFab.getLayoutParams();
        p.setBehavior(new ScrollAwareFABBehavior(this));
        mProfileFab.setLayoutParams(p);
    }


    @OnClick(R.id.removeFriend)
    public void removeFriend() {
        removeFriend(mOpponentId);
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
        mProfileFab.close(true);
        if (isFriend) {
            Intent intent = new Intent(getApplicationContext(), Chat.class);
            intent.putExtra("firstName", mFirstName);
            intent.putExtra("lastName", mLastName);
            intent.putExtra("opponentId", mOpponentId);
            intent.putExtra("isSocialAccount", isSocialAccount);
            intent.putExtra("opponentImage", mOpponentImage);
            startActivity(intent);
        } else {
            if (!isDataLoaded) {
                Snackbar.make(mTriangleView, getString(R.string.waitTillLoad), Snackbar.LENGTH_LONG).setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                }).show();
                mProfileFab.close(true);
            } else {
                if (isDataLoaded) {
                    if (hasFriendRequested) {
                        Snackbar.make(mTriangleView, getString(R.string.youHaveSentAlreadyRequest), Snackbar.LENGTH_LONG).setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                            }
                        }).show();
                        mProfileFab.close(true);
                    } else {
                        mProfileFab.close(true);
                        requestFriend();
                    }
                } else {
                    Snackbar.make(mTriangleView, getString(R.string.waitTillLoad), Snackbar.LENGTH_LONG).setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    }).show();
                    mProfileFab.close(true);
                }

            }
        }
    }

    @OnClick(R.id.callUserPhone)
    public void callUserPhone() {
        String phoneNumber = mPhone.getText().toString();
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(intent);

    }

    private void removeFriend(final String friendId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.removeFriend));
        builder.setMessage(getString(R.string.removefriendHint));
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                DimDialog.showDimDialog(OpponentProfile.this, getString(R.string.removingFriend));
                RealmHelper.getInstance().removeMessage(friendId, sharedHelper.getUserId());
                Call<ResponseBody> removeFriendCall = Retrofit.getInstance().getInkService().removeFriend(sharedHelper.getUserId(), friendId);
                removeFriendCall.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response == null) {
                            removeFriend(friendId);
                            return;
                        }
                        if (response.body() == null) {
                            removeFriend(friendId);
                            return;
                        }
                        try {
                            String responseBody = response.body().string();
                            JSONObject jsonObject = new JSONObject(responseBody);
                            boolean success = jsonObject.optBoolean("success");
                            DimDialog.hideDialog();
                            if (success) {
                                Toast.makeText(OpponentProfile.this, getString(R.string.friendRemoved), Toast.LENGTH_SHORT).show();

                                LocalBroadcastManager.getInstance(OpponentProfile.this).sendBroadcast(new Intent(getPackageName() + "Comments"));
                                LocalBroadcastManager.getInstance(OpponentProfile.this).sendBroadcast(new Intent(getPackageName() + "HomeActivity"));

                                LocalBroadcastManager.getInstance(OpponentProfile.this).sendBroadcast(new Intent(getPackageName() + ".Chat"));
                                LocalBroadcastManager.getInstance(OpponentProfile.this).sendBroadcast(new Intent(getPackageName() + "MyFriends"));
                                finish();
                            }
                        } catch (IOException e) {
                            DimDialog.hideDialog();
                            e.printStackTrace();
                        } catch (JSONException e) {
                            DimDialog.hideDialog();
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        removeFriend(friendId);
                    }
                });
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

    private void requestFriend() {
        DimDialog.showDimDialog(this, getString(R.string.sendingFriendRequest));
        Call<ResponseBody> requestFriendCall = Retrofit.getInstance().getInkService().requestFriend(sharedHelper.getUserId(), mOpponentId,
                sharedHelper.getFirstName() + " " + sharedHelper.getLastName());
        requestFriendCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    requestFriend();
                    return;
                }
                if (response.body() == null) {
                    requestFriend();
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    DimDialog.hideDialog();
                    if (success) {
                        hasFriendRequested = true;
                        Snackbar.make(mTriangleView, getString(R.string.requestSent), Snackbar.LENGTH_SHORT).show();
                    } else {
                        Snackbar.make(mTriangleView, getString(R.string.errorSendingRequest), Snackbar.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    DimDialog.hideDialog();
                    Snackbar.make(mTriangleView, getString(R.string.errorSendingRequest), Snackbar.LENGTH_SHORT).show();
                    e.printStackTrace();
                } catch (JSONException e) {
                    DimDialog.hideDialog();
                    Snackbar.make(mTriangleView, getString(R.string.errorSendingRequest), Snackbar.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                requestFriend();
            }
        });
    }


    private void getSingleUser() {
        Call<ResponseBody> call = ink.va.utils.Retrofit.getInstance().getInkService().getSingleUserDetails(mOpponentId, sharedHelper.getUserId());
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
                            String badgeName = jsonObject.optString("badge_name");
                            String phoneNumber = jsonObject.optString("phone_number");
                            mFacebookLink = jsonObject.optString("facebook_profile");
                            mOpponentImage = jsonObject.optString("image_link");
                            String skype = jsonObject.optString("skype");
                            String address = jsonObject.optString("address");
                            String relationship = jsonObject.optString("relationship");
                            String status = jsonObject.optString("status");
                            String facebookName = jsonObject.optString("facebook_name");
                            hasFriendRequested = jsonObject.optBoolean("hasFriendRequested");

                            Ion.with(OpponentProfile.this).load(Constants.MAIN_URL + badgeName).asBitmap().setCallback(new FutureCallback<Bitmap>() {
                                @Override
                                public void onCompleted(Exception e, Bitmap result) {
                                    if (e == null) {
                                        singleUserBadge.setImageBitmap(result);
                                    } else {
                                        e.printStackTrace();
                                    }
                                }
                            });

                            boolean shouldHighlightFacebook = true;
                            boolean shouldHighlightAddress = true;
                            isSocialAccount = jsonObject.optBoolean("isSocialAccount");
                            if (mOpponentImage != null && !mOpponentImage.isEmpty()) {
                                if (isSocialAccount) {
                                    Picasso.with(getApplicationContext()).load(mOpponentImage).networkPolicy(NetworkPolicy.NO_CACHE).into(getTarget());
                                } else {
                                    String encodedImage = Uri.encode(mOpponentImage);
                                    Picasso.with(getApplicationContext()).load(Constants.MAIN_URL +
                                            Constants.USER_IMAGES_FOLDER + encodedImage).networkPolicy(NetworkPolicy.NO_CACHE).into(getTarget());
                                }
                            } else {
                                Picasso.with(getApplicationContext()).load(R.drawable.no_image).into(getTarget());
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
                            } else {
                                callUserPhone.setVisibility(View.VISIBLE);
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
                    isDataLoaded = true;
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

    private Target getTarget() {
        if (target == null) {
            target = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
                    mOpponentImageLoading.setVisibility(View.GONE);
                    mProfileImage.setImageBitmap(bitmap);
                }

                @Override
                public void onBitmapFailed(Drawable drawable) {
                    mOpponentImageLoading.setVisibility(View.GONE);
                }

                @Override
                public void onPrepareLoad(Drawable drawable) {

                }
            };
        }
        return target;
    }


    @OnClick(R.id.profileImage)
    public void profileImage() {
        Intent intent = new Intent(getApplicationContext(), FullscreenActivity.class);
        if (mOpponentImage != null && !mOpponentImage.isEmpty()) {
            if (isSocialAccount) {
                intent.putExtra("link", mOpponentImage);
            } else {
                String encodedFileName = Uri.encode(mOpponentImage);
                intent.putExtra("link", Constants.MAIN_URL +
                        Constants.USER_IMAGES_FOLDER + encodedFileName);
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

    @Override
    protected void onDestroy() {
        DimDialog.hideDialog();
        super.onDestroy();
    }

}
