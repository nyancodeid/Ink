package ink.va.view_holders;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ink.va.R;

import ink.va.models.GroupsModel;
import ink.va.utils.Constants;
import ink.va.utils.ImageLoader;

/**
 * Created by PC-Comp on 12/21/2016.
 */

public class GroupsViewHolder extends RecyclerView.ViewHolder {

    public TextView groupName, followersCount;
    public ImageView groupImage, ownerImage;
    public CardView groupBackground;
    private ProgressBar singleItemLoading;
    private View joinedTV;

    public GroupsViewHolder(View view) {
        super(view);
        groupName = (TextView) view.findViewById(R.id.groupName);
        followersCount = (TextView) view.findViewById(R.id.followersCount);
        groupImage = (ImageView) view.findViewById(R.id.groupImage);
        ownerImage = (ImageView) view.findViewById(R.id.ownerImage);
        groupBackground = (CardView) view.findViewById(R.id.groupBackground);
        singleItemLoading = (ProgressBar) view.findViewById(R.id.singleItemLoading);
        joinedTV = view.findViewById(R.id.joinedTV);
    }

    public void initData(GroupsModel groupsModel, Context context) {
        String hexColor = groupsModel.getGroupColor();
        if (hexColor.isEmpty()) {
            hexColor = "#404040";
        } else {
            hexColor = groupsModel.getGroupColor();
        }
        if (isWhite(hexColor)) {
            followersCount.setTextColor(Color.parseColor("#000000"));
            groupName.setTextColor(Color.parseColor("#000000"));
        } else {
            followersCount.setTextColor(Color.parseColor("#ffffff"));
            groupName.setTextColor(Color.parseColor("#ffffff"));
        }

        joinedTV.setVisibility(groupsModel.isMember() ? View.VISIBLE : View.GONE);
        if (!groupsModel.getGroupImage().isEmpty()) {
            groupImage.setScaleType(ImageView.ScaleType.FIT_XY);
            String encodedImage = Uri.encode(groupsModel.getGroupImage());

            ImageLoader.loadImage(context, false, false, Constants.MAIN_URL + Constants.GROUP_IMAGES_FOLDER + encodedImage,
                    0, 0, groupImage, new ImageLoader.ImageLoadedCallback() {
                        @Override
                        public void onImageLoaded(Object result, Exception e) {
                            singleItemLoading.setVisibility(View.GONE);
                        }
                    });
        } else {
            groupImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
            groupImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.no_group_image));
            singleItemLoading.setVisibility(View.GONE);
        }
        if (groupsModel.getOwnerImage() != null && !groupsModel.getOwnerImage().isEmpty()) {
            if (groupsModel.isSocialAccount()) {

                ImageLoader.loadImage(context, true, false, groupsModel.getOwnerImage(),
                        0, R.drawable.user_image_placeholder, ownerImage, null);
            } else {
                String encodedImage = Uri.encode(groupsModel.getOwnerImage());
                ImageLoader.loadImage(context, true, false, Constants.MAIN_URL + Constants.USER_IMAGES_FOLDER +
                                encodedImage,
                        0, R.drawable.user_image_placeholder, ownerImage, null);
            }
        } else {
            ImageLoader.loadImage(context, true, true, null,
                    R.drawable.no_image, R.drawable.user_image_placeholder, ownerImage, null);
        }
        groupBackground.setCardBackgroundColor(Color.parseColor(hexColor));
        groupName.setText(groupsModel.getGroupName());
        followersCount.setText(groupsModel.getParticipantsCount() + " " + context.getString(R.string.participantText));
    }

    private boolean isWhite(String hexColor) {
        int color = Color.parseColor(hexColor);
        if (color == Color.WHITE) {
            return true;
        } else {
            return false;
        }
    }
}
