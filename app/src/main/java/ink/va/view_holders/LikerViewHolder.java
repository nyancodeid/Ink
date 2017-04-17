package ink.va.view_holders;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.ink.va.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.va.interfaces.RecyclerItemClickListener;
import ink.va.models.UserModel;
import ink.va.utils.Constants;
import ink.va.utils.ImageLoader;

/**
 * Created by USER on 2017-04-12.
 */

public class LikerViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.likerImage)
    ImageView likerImage;
    private RecyclerItemClickListener recyclerItemClickListener;
    private UserModel userModel;

    public LikerViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void initData(UserModel userModel, Context context, @Nullable RecyclerItemClickListener recyclerItemClickListener) {
        this.recyclerItemClickListener = recyclerItemClickListener;
        this.userModel = userModel;
        if (userModel.getImageUrl() != null && !userModel.getImageUrl().isEmpty()) {
            if (userModel.isSocialAccount()) {
                ImageLoader.loadImage(context, true, false, userModel.getImageUrl(),
                        0, R.drawable.user_image_placeholder, likerImage, null);

            } else {
                String encodedImage = Uri.encode(userModel.getImageUrl());
                ImageLoader.loadImage(context, true, false, Constants.MAIN_URL + Constants.USER_IMAGES_FOLDER + encodedImage,
                        0, R.drawable.user_image_placeholder, likerImage, null);
            }

        } else {
            ImageLoader.loadImage(context, true, true, null,
                    R.drawable.no_image, R.drawable.user_image_placeholder, likerImage, null);
        }
    }

    @OnClick(R.id.likerImage)
    public void likerImageClicked() {
        if (recyclerItemClickListener != null) {
            recyclerItemClickListener.onItemClicked(userModel);
        }
    }
}
