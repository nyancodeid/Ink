package ink.va.view_holders;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ink.va.R;
import com.koushikdutta.ion.Ion;

import ink.va.interfaces.RecyclerItemClickListener;
import ink.va.models.FriendsModel;
import ink.va.utils.CircleTransform;
import ink.va.utils.Constants;

/**
 * Created by USER on 2016-12-20.
 */

public class FriendsViewHolder extends RecyclerView.ViewHolder {

    public TextView name;
    public ImageView friendImage;
    private ImageView friendMoreIcon;
    private View cardView;
    private RelativeLayout friendsRootView;
    private View spacing;

    public FriendsViewHolder(View view) {
        super(view);
        name = (TextView) view.findViewById(R.id.friendName);
        spacing = view.findViewById(R.id.spacing);
        friendImage = (ImageView) view.findViewById(R.id.friendImage);
        friendMoreIcon = (ImageView) view.findViewById(R.id.friendMoreIcon);
        friendsRootView = (RelativeLayout) view.findViewById(R.id.friendsRootView);
        cardView = view.findViewById(R.id.friendsCardView);
    }

    public void init(Context context, FriendsModel friendsModel,
                     final int position,
                     final RecyclerItemClickListener recyclerItemClickListener, int maxCount) {

        if (position == (maxCount - 1)) {
            spacing.setVisibility(View.VISIBLE);
        } else {
            spacing.setVisibility(View.GONE);
        }
        name.setText(friendsModel.getFullName());

        if (!friendsModel.getImageLink().isEmpty()) {

            final String url;
            if (friendsModel.isSocialAccount()) {
                url = friendsModel.getImageLink();
            } else {
                String encodedImage = Uri.encode(friendsModel.getImageLink());
                url = Constants.MAIN_URL + Constants.USER_IMAGES_FOLDER + encodedImage;
            }

            Ion.with(context).load(url)
                    .withBitmap().placeholder(R.drawable.no_background_image).transform(new CircleTransform()).
                    intoImageView(friendImage);
        } else {
            Ion.with(context).load(Constants.ANDROID_DRAWABLE_DIR + "no_image")
                    .withBitmap().transform(new CircleTransform()).intoImageView(friendImage);
        }

        if (friendsModel.isFriend()) {
            friendMoreIcon.setVisibility(View.VISIBLE);
        } else {
            friendMoreIcon.setVisibility(View.GONE);
        }
        friendMoreIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (recyclerItemClickListener != null) {
                    recyclerItemClickListener.onAdditionItemClick(position, friendMoreIcon);
                }
            }
        });
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (recyclerItemClickListener != null) {
                    recyclerItemClickListener.onItemClicked(position, friendsRootView);
                }
            }
        });
    }

    public View getViewToAnimate() {
        return friendsRootView;
    }
}
