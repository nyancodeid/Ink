package kashmirr.social.view_holders;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.kashmirr.social.R;
import com.wang.avi.AVLoadingIndicatorView;

import kashmirr.social.interfaces.RecyclerItemClickListener;
import kashmirr.social.models.StickerModel;
import kashmirr.social.utils.Constants;
import kashmirr.social.utils.ImageLoader;

/**
 * Created by PC-Comp on 10/28/2016.
 */

public class StickerChooserViewHolder extends RecyclerView.ViewHolder {

    private ImageView stickerView;
    private AVLoadingIndicatorView stickerLoadingSingleItem;
    private RelativeLayout stickerWrapper;
    private Button choose;

    public StickerChooserViewHolder(View itemView) {
        super(itemView);
        stickerView = (ImageView) itemView.findViewById(R.id.stickerNotAnimatedView);
        choose = (Button) itemView.findViewById(R.id.choose);
        stickerWrapper = (RelativeLayout) itemView.findViewById(R.id.sticker_not_animated_parent);
        stickerLoadingSingleItem = (AVLoadingIndicatorView) itemView.findViewById(R.id.gifLoadingSingleItem);
    }


    public void init(Context context, final StickerModel stickerModel,
                     @Nullable final RecyclerItemClickListener recyclerItemClickListener, boolean hideChooser) {
        if (hideChooser) {
            choose.setVisibility(View.GONE);
        } else {
            choose.setVisibility(View.VISIBLE);
        }

        stickerWrapper.setVisibility(View.VISIBLE);
        ImageLoader.loadImage(context, false, false, Constants.MAIN_URL + stickerModel.getStickerUrl(), 0, 0, stickerView, new ImageLoader.ImageLoadedCallback() {
            @Override
            public void onImageLoaded(Object result, Exception e) {
                stickerLoadingSingleItem.setVisibility(View.GONE);
            }
        });
        choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (recyclerItemClickListener != null) {
                    recyclerItemClickListener.onItemClicked(stickerModel);
                }
            }
        });
    }
}
