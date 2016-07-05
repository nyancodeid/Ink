package ink.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ink.R;
import com.mikhaellopez.hfrecyclerview.HFRecyclerView;
import com.squareup.picasso.Picasso;

import java.util.List;

import ink.interfaces.CommentClickHandler;
import ink.models.CommentModel;
import ink.utils.CircleTransform;
import ink.utils.Constants;

/**
 * Created by USER on 2016-07-05.
 */
public class CommentAdapter extends HFRecyclerView<CommentModel> {

    private List<CommentModel> commentModels;
    private Context context;
    private String ownerImage;
    private String ownerPostBody;
    private String attachment;
    private String location;
    private String date;
    private String name;
    private String likesCount;
    private CommentClickHandler commentClickHandler;
    private boolean isLiked;

    public CommentAdapter(List<CommentModel> data,
                          Context context, String ownerImage,
                          String ownerPostBody, String attachment,
                          String location, String date, String name,
                          String likesCount, boolean isLiked) {
        super(data, true, false);
        this.context = context;
        this.isLiked = isLiked;
        this.likesCount = likesCount;
        this.name = name;
        this.date = date;
        this.attachment = attachment;
        this.location = location;
        this.ownerImage = ownerImage;
        this.ownerPostBody = ownerPostBody;
        commentModels = data;
    }

    @Override
    protected RecyclerView.ViewHolder getItemView(LayoutInflater inflater, ViewGroup parent) {
        return new ItemViewHolder(inflater.inflate(R.layout.comment_single_view, parent, false));
    }

    @Override
    protected RecyclerView.ViewHolder getHeaderView(LayoutInflater inflater, ViewGroup parent) {
        return new HeaderViewHolder(inflater.inflate(R.layout.comment_header_view, parent, false));
    }

    @Override
    protected RecyclerView.ViewHolder getFooterView(LayoutInflater inflater, ViewGroup parent) {
        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof ItemViewHolder) {
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            CommentModel commentModel = getItem(position);
            itemViewHolder.commenterBody.setText(commentModel.getCommentBody());
            itemViewHolder.commenterName.setText(commentModel.getFirstName() + " " + commentModel.getLastName());
            if (commentModel.getCommenterImage() != null && !commentModel.getCommenterImage().isEmpty()) {
                Picasso.with(context).load(Constants.MAIN_URL + Constants.USER_IMAGES_FOLDER +
                        commentModel.getCommenterImage()).transform(new CircleTransform()).fit().centerCrop()
                        .into(itemViewHolder.commenterImage);
            } else {
                Picasso.with(context).load(R.drawable.no_image).transform(new CircleTransform()).fit().centerCrop()
                        .into(itemViewHolder.commenterImage);
            }
        } else if (holder instanceof HeaderViewHolder) {
            final HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
            if (ownerImage != null && !ownerImage.isEmpty()) {
                Picasso.with(context).load(Constants.MAIN_URL + Constants.USER_IMAGES_FOLDER +
                        ownerImage).transform(new CircleTransform()).fit().centerCrop()
                        .into(headerViewHolder.postOwnerImage);
            } else {
                Picasso.with(context).load(R.drawable.no_image).transform(new CircleTransform()).fit().centerCrop()
                        .into(headerViewHolder.postOwnerImage);
            }
            headerViewHolder.postBody.setText(ownerPostBody);
            headerViewHolder.postDate.setText(date);


            if (attachment != null && !attachment.isEmpty()) {
                headerViewHolder.commentAttachmentLayout.setVisibility(View.VISIBLE);
                headerViewHolder.commentAttachmentName.setText(attachment);
            } else {
                headerViewHolder.commentAttachmentLayout.setVisibility(View.GONE);
            }

            if (location != null && !location.isEmpty()) {
                headerViewHolder.commentAddressLayout.setVisibility(View.VISIBLE);
                headerViewHolder.commentAddress.setText(location);
            } else {
                headerViewHolder.commentAddressLayout.setVisibility(View.GONE);
            }

            if (!likesCount.equals("0")) {
                headerViewHolder.likesCountTV.setVisibility(View.VISIBLE);
                if (Integer.parseInt(likesCount) > 1) {
                    headerViewHolder.likesCountTV.setText(likesCount + " " + context.getString(R.string.likesText));
                } else {
                    headerViewHolder.likesCountTV.setText(likesCount + " " + context.getString(R.string.singleLikeText));
                }
            } else {
                headerViewHolder.likesCountTV.setVisibility(View.GONE);
            }

            if (isLiked) {
                headerViewHolder.likeIcon.setBackgroundResource(R.drawable.like_active);
            } else {
                headerViewHolder.likeIcon.setBackgroundResource(R.drawable.like_inactive);
            }

            headerViewHolder.commenterName.setText(name);

            headerViewHolder.commentLikeWrapper.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (commentClickHandler != null) {
                        int actualPosition = position - 1;
                        commentClickHandler.onLikeClicked(actualPosition, headerViewHolder.likesCountTV,
                                headerViewHolder.likeIcon);
                    }
                }
            });


            headerViewHolder.commentAttachmentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (commentClickHandler != null) {
                        int actualPosition = position - 1;
                        commentClickHandler.onAttachmentClick(actualPosition);
                    }
                }
            });
            headerViewHolder.commentAddressLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (commentClickHandler != null) {
                        int actualPosition = position - 1;
                        commentClickHandler.onAddressClick(actualPosition);
                    }
                }
            });
        }
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {
        private ImageView postOwnerImage, likeIcon;
        private TextView postBody, postDate, commenterName, commentAttachmentName, commentAddress, likesCountTV;
        private RelativeLayout commentLikeWrapper, commentAddressLayout, commentAttachmentLayout;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            postOwnerImage = (ImageView) itemView.findViewById(R.id.postOwnerImage);
            likeIcon = (ImageView) itemView.findViewById(R.id.commentLikeIcon);
            postBody = (TextView) itemView.findViewById(R.id.postBody);
            commentAttachmentName = (TextView) itemView.findViewById(R.id.commentAttachmentName);
            postDate = (TextView) itemView.findViewById(R.id.postDate);
            commentAddress = (TextView) itemView.findViewById(R.id.commentAddress);
            commenterName = (TextView) itemView.findViewById(R.id.commenterName);
            likesCountTV = (TextView) itemView.findViewById(R.id.commentLikesCount);
            commentLikeWrapper = (RelativeLayout) itemView.findViewById(R.id.commentLikeWrapper);
            commentAddressLayout = (RelativeLayout) itemView.findViewById(R.id.commentAddresslayout);
            commentAttachmentLayout = (RelativeLayout) itemView.findViewById(R.id.commentAttachmentLayout);
        }
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        private TextView commenterBody;
        private ImageView commenterImage;
        private TextView commenterName;

        public ItemViewHolder(View itemView) {
            super(itemView);
            commenterBody = (TextView) itemView.findViewById(R.id.commenterBody);
            commenterName = (TextView) itemView.findViewById(R.id.commenterName);
            commenterImage = (ImageView) itemView.findViewById(R.id.commenterImage);
        }
    }

    public void setOnLikeClickListener(CommentClickHandler onLikeClickListener) {
        this.commentClickHandler = onLikeClickListener;
    }

    public void setIsLiked(boolean isLiked) {
        this.isLiked = isLiked;
    }

}
