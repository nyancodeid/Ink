package ink.va.view_holders;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ink.va.R;
import com.koushikdutta.ion.Ion;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.va.interfaces.VipMemberItemClickListener;
import ink.va.models.UserModel;
import ink.va.utils.CircleTransform;
import ink.va.utils.Constants;

import static ink.va.utils.MembershipTypes.MEMBERSHIP_TYPE_BLACK;
import static ink.va.utils.MembershipTypes.MEMBERSHIP_TYPE_GOLD;
import static ink.va.utils.MembershipTypes.MEMBERSHIP_TYPE_RED;


/**
 * Created by PC-Comp on 1/9/2017.
 */

public class VipMemberViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.vipMemberImage)
    ImageView vipMemberImage;
    @BindView(R.id.vipMemberName)
    TextView vipMemberName;
    @BindView(R.id.bottomSpacing)
    View bottomSpacing;
    @BindView(R.id.membershipTypeTV)
    TextView membershipTypeTV;
    @BindView(R.id.genderTV)
    TextView genderTV;
    @BindView(R.id.moreInfoLayout)
    RelativeLayout moreInfoLayout;
    @BindView(R.id.membershipTypeImage)
    ImageView membershipTypeImage;

    private VipMemberItemClickListener itemClickListener;
    private UserModel userModel;
    private Context context;
    private boolean isInfoVisible;

    public VipMemberViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
    public void initData(UserModel userModel, int position, VipMemberItemClickListener itemClickListener,
                         Context context, int maxSize) {
        switch (userModel.getVipMembershipType()) {
            case MEMBERSHIP_TYPE_GOLD:
                membershipTypeImage.setBackground(ContextCompat.getDrawable(context, R.drawable.gold_member_card_small));
                break;
            case MEMBERSHIP_TYPE_RED:
                membershipTypeImage.setBackground(ContextCompat.getDrawable(context, R.drawable.red_member_card_small));
                break;
            case MEMBERSHIP_TYPE_BLACK:
                membershipTypeImage.setBackground(ContextCompat.getDrawable(context, R.drawable.black_member_card_small));
                break;
            default:
                membershipTypeImage.setBackground(ContextCompat.getDrawable(context, R.drawable.no_membership_small));
        }
        this.context = context;
        this.userModel = userModel;
        this.itemClickListener = itemClickListener;
        vipMemberName.setText(userModel.getFirstName() + " " + userModel.getLastName());

        configureUserImage();

        bottomSpacing.setVisibility(position == maxSize ? View.VISIBLE : View.GONE);
        membershipTypeTV.setText(context.getString(R.string.membershipType, userModel.getVipMembershipType()));
        if (userModel.getGender().isEmpty()) {
            genderTV.setText(context.getString(R.string.noGender));
        } else {
            switch (userModel.getGender()) {
                case Constants.GENDER_FEMALE:
                    genderTV.setText(context.getString(R.string.femaleGender));
                    break;
                case Constants.GENDER_MALE:
                    genderTV.setText(context.getString(R.string.maleGender));
                    break;
            }
        }
    }

    private void configureUserImage() {
        if (userModel.getImageUrl().isEmpty()) {
            Ion.with(context).load(Constants.ANDROID_DRAWABLE_DIR + "vip_image_placeholder")
                    .withBitmap().transform(new CircleTransform())
                    .intoImageView(vipMemberImage);
        } else {
            if (userModel.isSocialAccount()) {
                Ion.with(context).load(userModel.getImageUrl()).
                        withBitmap().placeholder(R.drawable.vip_image_placeholder).transform(new CircleTransform())
                        .intoImageView(vipMemberImage);
            } else {
                String encodedImage = Uri.encode(userModel.getImageUrl());
                Ion.with(context).load(Constants.MAIN_URL + Constants.USER_IMAGES_FOLDER + encodedImage).
                        withBitmap().placeholder(R.drawable.vip_image_placeholder).transform(new CircleTransform())
                        .intoImageView(vipMemberImage);
            }
        }
    }


    @OnClick(R.id.vipMemberRoot)
    public void clicked() {
        if (itemClickListener != null) {
            itemClickListener.onItemClicked(userModel);
        }
        if (!isInfoVisible) {
            moreInfoLayout.setVisibility(View.VISIBLE);
            isInfoVisible = true;
        } else {
            moreInfoLayout.setVisibility(View.GONE);
            isInfoVisible = false;
        }
    }

    @OnClick(R.id.sendMessageVip)
    public void vipSendMessage() {
        if (itemClickListener != null) {
            itemClickListener.onSendMessageClicked(userModel);
        }
    }

    @OnClick(R.id.sendCoins)
    public void vipSendCoins() {
        if (itemClickListener != null) {
            itemClickListener.onSendCoinsClicked(userModel);
        }
    }
}
