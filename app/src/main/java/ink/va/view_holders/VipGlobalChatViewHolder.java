package ink.va.view_holders;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ink.va.R;
import com.koushikdutta.ion.Ion;

import butterknife.Bind;
import butterknife.ButterKnife;
import ink.va.models.UserModel;
import ink.va.models.VipGlobalChatModel;
import ink.va.utils.CircleTransform;
import ink.va.utils.Constants;
import ink.va.utils.SharedHelper;

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
    private SharedHelper sharedHelper;

    private Context context;
    private UserModel userModel;

    public VipGlobalChatViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void initData(VipGlobalChatModel vipGlobalChatModel, Context context) {
        this.context = context;
        userModel = vipGlobalChatModel.getUser();
        if (sharedHelper == null) {
            sharedHelper = new SharedHelper(context);
        }

        vipGlobalChatMoreIcon.setVisibility(userModel.getUserId().equals(sharedHelper.getUserId()) ? View.VISIBLE : View.GONE);

        globalVipMemberName.setText(vipGlobalChatModel.getUser().getFirstName() + " " + vipGlobalChatModel.getUser().getLastName());
        globalVipChatContent.setText(vipGlobalChatModel.getMessage());
        globalVipChatWrapper.getBackground().setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_ATOP);
        configureUserImage();
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
}
