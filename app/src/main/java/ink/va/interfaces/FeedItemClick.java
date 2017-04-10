package ink.va.interfaces;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import ink.va.models.FeedModel;

/**
 * Created by USER on 2016-07-04.
 */
public interface FeedItemClick {
    void onCardViewClick(FeedModel feedModel, String type);

    void onAddressClick(FeedModel feedModel);

    void onAttachmentClick(FeedModel feedModel);

    void onCardLongClick(FeedModel feedModel);

    void onLikeClick(FeedModel feedModel, ImageView likeView, TextView likeCountTV, View likeWrapper);

    void onCommentClicked(FeedModel feedModel, View commentView);

    void onMoreClicked(FeedModel feedModel, View view);

    void onImageClicked(FeedModel feedModel);

    void onShareClicked(FeedModel feedModel);
}
