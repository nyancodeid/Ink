package ink.va.view_holders;

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

import com.ink.va.R;
import com.koushikdutta.ion.Ion;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.va.interfaces.VipGlobalChatClickListener;
import ink.va.models.UserModel;
import ink.va.models.VipGlobalChatModel;
import ink.va.utils.CircleTransform;
import ink.va.utils.Constants;
import ink.va.utils.SharedHelper;

import static ink.va.utils.MembershipTypes.MEMBERSHIP_TYPE_BLACK;
import static ink.va.utils.MembershipTypes.MEMBERSHIP_TYPE_GOLD;
import static ink.va.utils.MembershipTypes.MEMBERSHIP_TYPE_RED;

/**
 * Created by PC-Comp on 1/11/2017.
 */

public class VipGlobalChatViewHolder extends RecyclerView.ViewHolder {
    @Bind(R.id.globalVipMemberName)
    TextView globalVipMemberName;
    @Bind(R.id.globalVipChatContent)
    TextView globalVipChatContent;
    @Bind(R.id.globalVipChatWrapper)
    RelativeLayout globalVipChatWrapper;
    @Bind(R.id.globalVipMemberImage)
    ImageView globalVipMemberImage;
    @Bind(R.id.globalVipMemberRoot)
    RelativeLayout globalVipMemberRoot;
    @Bind(R.id.vipGlobalChatMoreIcon)
    ImageView vipGlobalChatMoreIcon;
    @Bind(R.id.globalChatBottomSpace)
    View globalChatBottomSpace;
    @Bind(R.id.membershipTypeTV)
    TextView membershipTypeTV;
    @Bind(R.id.genderTV)
    TextView genderTV;
    @Bind(R.id.moreInfoLayout)
    RelativeLayout moreInfoLayout;
    @Bind(R.id.membershipTypeImage)
    ImageView membershipTypeImage;
    @Bind(R.id.sendCoins)
    Button sendCoins;
    @Bind(R.id.sendMessageVip)
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

        if(sharedHelper.getUserId().equals(userModel.getUserId())){
            sendMessageVip.setAlpha((float) 0.5);
            sendCoins.setAlpha((float) 0.5);
        }else{
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

    @OnClick(R.id.globalVipMemberRoot)
    public void itemClicked() {
        if (itemClickListener != null) {
            itemClickListener.onItemClicked(vipGlobalChatModel);
        }
    }

    private void configureUserImage() {
        if (userModel.getImageUrl().isEmpty()) {
            Ion.with(context).load(Constants.ANDROID_DRAWABLE_DIR + "vip_image_placeholder")
                    .withBitmap().transform(new CircleTransform())
                    .intoImageView(globalVipMemberImage);
        } else {
            if (userModel.isSocialAccount()) {
                Ion.with(context).load(userModel.getImageUrl()).
                        withBitmap().placeholder(R.drawable.vip_image_placeholder).transform(new CircleTransform())
                        .intoImageView(globalVipMemberImage);
            } else {
                String encodedImage = Uri.encode(userModel.getImageUrl());
                Ion.with(context).load(Constants.MAIN_URL + Constants.USER_IMAGES_FOLDER + encodedImage).
                        withBitmap().placeholder(R.drawable.vip_image_placeholder).transform(new CircleTransform())
                        .intoImageView(globalVipMemberImage);
            }
        }
    }

    @OnClick(R.id.vipMemberRoot)
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
