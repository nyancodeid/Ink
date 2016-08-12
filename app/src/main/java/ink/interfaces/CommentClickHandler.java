package ink.interfaces;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by USER on 2016-07-06.
 */
public interface CommentClickHandler {
    void onLikeClicked(int position, TextView likesCountTV, ImageView likeView);

    void onAddressClick(int position);

    void onAttachmentClick(int position);

    void onMoreClick(int position, View view);
}
