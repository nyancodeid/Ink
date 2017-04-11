package ink.va.view_holders;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.ink.va.R;
import com.koushikdutta.ion.Ion;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.va.interfaces.RecyclerItemClickListener;
import ink.va.models.UserModel;
import ink.va.utils.CircleTransform;
import ink.va.utils.Constants;

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
                Ion.with(context).load(userModel.getImageUrl())
                        .withBitmap().placeholder(R.drawable.user_image_placeholder).transform(new CircleTransform())
                        .intoImageView(likerImage);
            } else {
                String encodedImage = Uri.encode(userModel.getImageUrl());
                Ion.with(context).load(Constants.MAIN_URL + Constants.USER_IMAGES_FOLDER + encodedImage)
                        .withBitmap().placeholder(R.drawable.user_image_placeholder).transform(new CircleTransform())
                        .intoImageView(likerImage);
            }

        } else {
            Ion.with(context).load(Constants.ANDROID_DRAWABLE_DIR + "no_image")
                    .withBitmap().transform(new CircleTransform())
                    .intoImageView(likerImage);
        }
    }

    @OnClick(R.id.likerImage)
    public void likerImageClicked() {
        if (recyclerItemClickListener != null) {
            recyclerItemClickListener.onItemClicked(userModel);
        }
    }
}
