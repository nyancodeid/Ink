package ink.interfaces;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by USER on 2016-07-04.
 */
public interface FeedItemClick {
    void onCardViewClick(int position);

    void onAddressClick(int position);

    void onAttachmentClick(int position);

    void onCardLongClick(int position);

    void onLikeClick(int position, ImageView likeView, TextView likeCountTV, View likeWrapper);

    void onCommentClicked(int position, View commentView);

    void onMoreClicked(int position, View view);

    void onImageClicked(int position);
}
