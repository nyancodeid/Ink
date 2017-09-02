package kashmirr.social.view_holders;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kashmirr.social.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import kashmirr.social.interfaces.VipGlobalChatClickListener;
import kashmirr.social.models.UserModel;
import kashmirr.social.models.VipGlobalChatModel;
import kashmirr.social.utils.Constants;
import kashmirr.social.utils.ImageLoader;
import kashmirr.social.utils.SharedHelper;

import static kashmirr.social.utils.MembershipTypes.MEMBERSHIP_TYPE_BLACK;
import static kashmirr.social.utils.MembershipTypes.MEMBERSHIP_TYPE_GOLD;
import static kashmirr.social.utils.MembershipTypes.MEMBERSHIP_TYPE_RED;

/**
 * Created by PC-Comp on 1/11/2017.
 */

public class VipGlobalChatViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.globalVipMemberName)
    TextView globalVipMemberName;
    @BindView(R.id.globalVipChatContent)
    TextView globalVipChatContent;
    @BindView(R.id.globalVipChatWrapper)
    RelativeLayout globalVipChatWrapper;
    @BindView(R.id.topSpacing)
    View topSpacing;
    @BindView(R.id.globalVipMemberImage)
    ImageView globalVipMemberImage;
    @BindView(R.id.globalVipMemberRoot)
    RelativeLayout globalVipMemberRoot;
    @BindView(R.id.vipGlobalChatMoreIcon)
    ImageView vipGlobalChatMoreIcon;
    @BindView(R.id.globalChatBottomSpace)
    View globalChatBottomSpace;
    @BindView(R.id.membershipTypeTV)
    TextView membershipTypeTV;
    @BindView(R.id.genderTV)
    TextView genderTV;
    @BindView(R.id.moreInfoLayout)
    RelativeLayout moreInfoLayout;
    @BindView(R.id.membershipTypeImage)
    ImageView membershipTypeImage;
    @BindView(R.id.sendCoins)
    Button sendCoins;
    @BindView(R.id.sendMessageVip)
    Button sendMessageVip;

    private SharedHelper sharedHelper;

    private Context context;
    private UserModel userModel;
    private VipGlobalChatClickListener itemClickListener;
    private VipGlobalChatModel vipGlobalChatModel;
    private boolean isInfoVisible;

    public VipGlobalChatViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void initData(VipGlobalChatModel vipGlobalChatModel, Context context, VipGlobalChatClickListener vipGlobalChatClickListener, int position, int size) {
        this.itemClickListener = vipGlobalChatClickListener;
        this.vipGlobalChatModel = vipGlobalChatModel;
        this.context = context;
        userModel = vipGlobalChatModel.getUser();
        if (sharedHelper == null) {
            sharedHelper = new SharedHelper(context);
        }

        if (sharedHelper.getUserId().equals(userModel.getUserId())) {
            sendMessageVip.setAlpha((float) 0.5);
            sendCoins.setAlpha((float) 0.5);
        } else {
            sendCoins.setAlpha(1);
            sendMessageVip.setAlpha(1);
        }

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

        globalChatBottomSpace.setVisibility(position == size ? View.VISIBLE : View.GONE);
        topSpacing.setVisibility(position <= 0 ? View.VISIBLE : View.GONE);

        vipGlobalChatMoreIcon.setVisibility(userModel.getUserId().equals(sharedHelper.getUserId()) ? View.VISIBLE : View.GONE);

        globalVipMemberName.setText(vipGlobalChatModel.getUser().getFirstName() + " " + vipGlobalChatModel.getUser().getLastName());
        globalVipChatContent.setText(vipGlobalChatModel.getMessage());
        globalVipChatWrapper.getBackground().setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_ATOP);
        configureUserImage();
    }

    @OnClick(R.id.vipGlobalChatMoreIcon)
    public void moreClicked() {
        if (itemClickListener != null) {
            itemClickListener.onMoreIconClicked(vipGlobalChatMoreIcon, vipGlobalChatModel);
        }
    }

    private void configureUserImage() {
        if (userModel.getImageUrl().isEmpty()) {
            ImageLoader.loadImage(context, true, true, null, R.drawable.vip_image_placeholder, R.drawable.vip_image_placeholder, globalVipMemberImage, null);
        } else {
            if (userModel.isSocialAccount()) {
                ImageLoader.loadImage(context, true, false, userModel.getImageUrl(), 0, R.drawable.vip_image_placeholder, globalVipMemberImage, null);
            } else {
                String encodedImage = Uri.encode(userModel.getImageUrl());

                ImageLoader.loadImage(context, true, false, Constants.MAIN_URL + Constants.USER_IMAGES_FOLDER + encodedImage, 0, R.drawable.vip_image_placeholder, globalVipMemberImage, null);
            }
        }
    }

    @OnClick(R.id.globalVipMemberRoot)
    public void clicked() {
        if (itemClickListener != null) {
            itemClickListener.onItemClicked(vipGlobalChatModel);
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
        if (itemClickListener != null && !userModel.getUserId().equals(sharedHelper.getUserId())) {
            itemClickListener.onSendMessageClicked(userModel);
        }
    }

    @OnClick(R.id.sendCoins)
    public void vipSendCoins() {
        if (itemClickListener != null && !userModel.getUserId().equals(sharedHelper.getUserId())) {
            itemClickListener.onSendCoinsClicked(userModel);
        }
    }

}
