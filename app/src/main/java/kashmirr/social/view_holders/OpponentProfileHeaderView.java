package kashmirr.social.view_holders;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kashmirr.social.R;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.sephiroth.android.library.picasso.NetworkPolicy;
import it.sephiroth.android.library.picasso.Picasso;
import it.sephiroth.android.library.picasso.Target;
import kashmirr.social.activities.FullscreenActivity;
import kashmirr.social.utils.Constants;
import kashmirr.social.utils.ImageLoader;
import kashmirr.social.utils.SharedHelper;

/**
 * Created by PC-Comp on 4/10/2017.
 */

public class OpponentProfileHeaderView extends RecyclerView.ViewHolder {

    private String mFacebookLink;
    private boolean isSocialAccount;


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
    @BindView(R.id.opponentImageLoading)
    AVLoadingIndicatorView mOpponentImageLoading;
    @BindView(R.id.addressWrapper)
    RelativeLayout mAddressWrapper;
    @BindView(R.id.userName)
    TextView userName;

    @BindView(R.id.callUserPhone)
    ImageView callUserPhone;
    @BindView(R.id.singleUserBadge)
    ImageView singleUserBadge;
    private String mOpponentImage;
    private Context context;
    private Target target;
    private boolean isFriend;
    @BindView(R.id.sendMessageFab)
    android.support.design.widget.FloatingActionButton sendMessageFab;
    @BindView(R.id.friendUnfriendFab)
    android.support.design.widget.FloatingActionButton friendUnfriendFab;
    private String mFirstName;
    private String mLastName;
    HeaderViewClickListener headerViewClickListener;
    private SharedHelper sharedHelper;

    public OpponentProfileHeaderView(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void initData(JSONObject jsonObject, Context context, boolean disableSendMessage, boolean enableButtons, @Nullable HeaderViewClickListener headerViewClickListener) {
        if (sharedHelper == null) {
            sharedHelper = new SharedHelper(context);
        }
        this.headerViewClickListener = headerViewClickListener;
        this.context = context;
        if (disableSendMessage) {
            sendMessageFab.setVisibility(View.INVISIBLE);
        } else if (isFriend) {
            sendMessageFab.setVisibility(View.VISIBLE);
        } else {
            sendMessageFab.setVisibility(View.INVISIBLE);
        }

        if (!enableButtons) {
            friendUnfriendFab.setEnabled(false);
        } else {
            friendUnfriendFab.setEnabled(true);
            if (isFriend) {
                friendUnfriendFab.setImageResource(R.drawable.remove_friend_icon);
            } else {
                friendUnfriendFab.setImageResource(R.drawable.request_friend_icon);
            }
        }

        if (sharedHelper.getMenuButtonColor() != null) {
            sendMessageFab.setBackgroundTintList((ColorStateList.valueOf(Color.parseColor(sharedHelper.getMenuButtonColor()))));
            friendUnfriendFab.setBackgroundTintList((ColorStateList.valueOf(Color.parseColor(sharedHelper.getMenuButtonColor()))));
        }

        String status = jsonObject.optString("status");
        mFirstName = jsonObject.optString("first_name");
        mLastName = jsonObject.optString("last_name");
        String gender = jsonObject.optString("gender");
        String badgeName = jsonObject.optString("badge_name");
        String phoneNumber = jsonObject.optString("phoneNumber");
        mFacebookLink = jsonObject.optString("facebook_profile");
        String facebookName = jsonObject.optString("facebook_name");
        mOpponentImage = jsonObject.optString("image_link");
        String skype = jsonObject.optString("skype");
        String address = jsonObject.optString("address");
        String relationship = jsonObject.optString("relationship");
        isFriend = jsonObject.optBoolean("isFriend");


        ImageLoader.loadImage(context, true, false, Constants.MAIN_URL + badgeName, 0, R.drawable.badge_placeholder, singleUserBadge, null);
        userName.setText(mFirstName + " " + mLastName);
        boolean shouldHighlightFacebook = true;
        boolean shouldHighlightAddress = true;
        isSocialAccount = jsonObject.optBoolean("isSocialAccount");

        if (mOpponentImage != null && !mOpponentImage.isEmpty()) {
            if (isSocialAccount) {
                Picasso.with(context).load(mOpponentImage).networkPolicy(NetworkPolicy.NO_CACHE).into(getTarget());
            } else {
                String encodedImage = Uri.encode(mOpponentImage);
                Picasso.with(context).load(Constants.MAIN_URL +
                        Constants.USER_IMAGES_FOLDER + encodedImage).networkPolicy(NetworkPolicy.NO_CACHE).into(getTarget());
            }
        } else {
            Picasso.with(context).load(R.drawable.no_image).into(getTarget());
        }

        if (status.isEmpty()) {
            mStatusText.setText(context.getString(R.string.shortNoStatus));
        } else {
            mStatusText.setText(status);
        }
        if (!gender.isEmpty()) {
            if (gender.equals(Constants.GENDER_FEMALE)) {
                mGenderImageView.setBackground(null);
                mGenderImageView.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_gender_female));
            } else {
                mGenderImageView.setBackground(null);
                mGenderImageView.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_gender_male));
            }
        } else {
            gender = context.getString(R.string.noGender);
        }

        if (phoneNumber.isEmpty()) {
            phoneNumber = context.getString(R.string.noPhone);
        } else {
            callUserPhone.setVisibility(View.VISIBLE);
        }
        if (mFacebookLink.isEmpty()) {
            mFacebookLink = context.getString(R.string.noFacebook);
            facebookName = context.getString(R.string.noFacebook);
            shouldHighlightFacebook = false;
        }
        if (skype.isEmpty()) {
            skype = context.getString(R.string.noSkype);
        }
        if (address.isEmpty()) {
            address = context.getString(R.string.noAddress);
            shouldHighlightAddress = false;
        }
        if (relationship.isEmpty()) {
            relationship = context.getString(R.string.noRelationship);
        }
        mPhone.setText(phoneNumber);
        if (shouldHighlightFacebook) {
            mFacebook.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
        }
        if (shouldHighlightAddress) {
            mAddress.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
        }
        mFacebook.setText(facebookName);
        mSkype.setText(skype);
        mAddress.setText(address);
        mRelationship.setText(relationship);
        mGender.setText(gender);
    }

    @OnClick(R.id.facebookWrapper)
    public void facebook() {
        if (!mFacebook.getText().toString().equals(context.getString(R.string.loadingText)) && !mFacebook.getText().toString().equals(context.getString(R.string.noFacebook))) {
            Snackbar.make(mFacebookWrapper, context.getString(R.string.openingFacebook), Snackbar.LENGTH_SHORT);
            String addressToPass = mFacebookLink;
            openFacebookPage(addressToPass);
        }
    }

    @OnClick(R.id.addressWrapper)
    public void addressWrapper() {
        if (!mAddress.getText().toString().equals(context.getString(R.string.loadingText)) && !mAddress.getText().toString().equals(context.getString(R.string.noAddress))) {
            Snackbar.make(mAddressWrapper, context.getString(R.string.openingGoogle), Snackbar.LENGTH_SHORT);
            String addressToPass = mAddress.getText().toString();
            openGoogleMaps(addressToPass);
        }
    }

    @OnClick(R.id.sendMessageFab)
    public void WriteMessage() {
        if (headerViewClickListener != null) {
            headerViewClickListener.onSendMessageClicked();
        }
    }

    @OnClick(R.id.friendUnfriendFab)
    public void removeFriend() {
        if (headerViewClickListener != null) {
            headerViewClickListener.onFriendUnfriendClicked();
        }
    }


    @OnClick(R.id.profileImage)
    public void profileImage() {
        Intent intent = new Intent(context, FullscreenActivity.class);
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
        context.startActivity(intent);
    }


    private void openFacebookPage(String page) {
        Intent facebookIntent = getOpenFacebookIntent(page);
        context.startActivity(facebookIntent);
    }

    private Intent getOpenFacebookIntent(String page) {

        try {
            context.getPackageManager()
                    .getPackageInfo("com.facebook.katana", 0);
            return new Intent(Intent.ACTION_VIEW,
                    Uri.parse("fb://facewebmodal/f?href=" + page));
        } catch (Exception e) {
            return new Intent(Intent.ACTION_VIEW,
                    Uri.parse(page));
        }
    }


    private void openGoogleMaps(String address) {
        String uri = "geo:0,0?q=" + address;
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(mapIntent);
        }

    }

    @OnClick(R.id.callUserPhone)
    public void callUserPhone() {
        String phoneNumber = mPhone.getText().toString();
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        context.startActivity(intent);
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

    public interface HeaderViewClickListener {
        void onFriendUnfriendClicked();

        void onSendMessageClicked();
    }
}
