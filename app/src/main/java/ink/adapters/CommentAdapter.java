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
import com.koushikdutta.ion.Ion;
import com.mikhaellopez.hfrecyclerview.HFRecyclerView;

import java.util.List;

import ink.interfaces.CommentClickHandler;
import ink.interfaces.RecyclerItemClickListener;
import ink.models.CommentModel;
import ink.utils.CircleTransform;
import ink.utils.Constants;
import ink.utils.SharedHelper;

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
    private SharedHelper sharedHelper;
    private boolean isOwnerSocialAccount;
    String ownerId;
    private RecyclerItemClickListener onItemClickListener;

    public CommentAdapter(String ownerId, List<CommentModel> data,
                          Context context, String ownerImage,
                          String ownerPostBody, String attachment,
                          String location, String date, String name,
                          String likesCount, boolean isLiked, boolean isOwnerSocialAccount) {
        super(data, true, false);
        this.context = context;
        this.isLiked = isLiked;
        this.ownerId = ownerId;
        this.likesCount = likesCount;
        this.name = name;
        this.date = date;
        this.attachment = attachment;
        this.location = location;
        this.isOwnerSocialAccount = isOwnerSocialAccount;
        this.ownerImage = ownerImage;
        this.ownerPostBody = ownerPostBody;
        commentModels = data;
        sharedHelper = new SharedHelper(context);
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
            final ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            CommentModel commentModel = getItem(position);
            itemViewHolder.commenterBody.setText(commentModel.getCommentBody());
            itemViewHolder.commenterName.setText(commentModel.getFirstName() + " " + commentModel.getLastName());
            itemViewHolder.commentRootLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClicked(position, null);
                    }
                }
            });
            itemViewHolder.commentRootLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemLongClick(position);
                    }
                    return true;
                }
            });

            if (sharedHelper.getUserId().equals(commentModel.getCommenterId())) {
                itemViewHolder.commentMoreIcon.setVisibility(View.VISIBLE);
            } else {
                itemViewHolder.commentMoreIcon.setVisibility(View.GONE);
            }
            if (commentModel.getCommenterImage() != null && !commentModel.getCommenterImage().isEmpty()) {
                if (commentModel.isSocialAccount()) {
                    Ion.with(context).load(commentModel.getCommenterImage()).withBitmap().placeholder(R.drawable.no_background_image).transform(new CircleTransform()).intoImageView(itemViewHolder.commenterImage);
                } else {
                    Ion.with(context).load(Constants.MAIN_URL + Constants.USER_IMAGES_FOLDER +
                            commentModel.getCommenterImage()).withBitmap().transform(new CircleTransform()).intoImageView(itemViewHolder.commenterImage);
                }
            } else {
                Ion.with(context).load(Constants.ANDROID_DRAWABLE_DIR + "no_image").withBitmap().transform(new CircleTransform()).intoImageView(itemViewHolder.commenterImage);
            }
            itemViewHolder.commentMoreIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onAdditionItemClick(position, itemViewHolder.commentMoreIcon);
                    }
                }
            });
        } else if (holder instanceof HeaderViewHolder) {
            final HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
            if (ownerId.equals(sharedHelper.getUserId())) {
                ((HeaderViewHolder) holder).commentMoreIcon.setVisibility(View.VISIBLE);
            } else {
                ((HeaderViewHolder) holder).commentMoreIcon.setVisibility(View.GONE);
            }
            ((HeaderViewHolder) holder).commentMoreIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (commentClickHandler != null) {
                        commentClickHandler.onMoreClick(position, ((HeaderViewHolder) holder).commentMoreIcon);
                    }
                }
            });
            if (ownerImage != null && !ownerImage.isEmpty()) {
                if (isOwnerSocialAccount) {
                    Ion.with(context).load(ownerImage).withBitmap().placeholder(R.drawable.no_background_image).transform(new CircleTransform()).intoImageView(headerViewHolder.postOwnerImage);
                } else {
                    Ion.with(context).load(Constants.MAIN_URL + Constants.USER_IMAGES_FOLDER +
                            ownerImage).withBitmap().placeholder(R.drawable.no_background_image).transform(new CircleTransform()).intoImageView(headerViewHolder.postOwnerImage);
                }
            } else {
                Ion.with(context).load(Constants.ANDROID_DRAWABLE_DIR + "no_image").withBitmap().transform(new CircleTransform()).intoImageView(headerViewHolder.postOwnerImage);
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
        private ImageView commentMoreIcon;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            postOwnerImage = (ImageView) itemView.findViewById(R.id.postOwnerImage);
            likeIcon = (ImageView) itemView.findViewById(R.id.commentLikeIcon);
            postBody = (TextView) itemView.findViewById(R.id.postBody);
            commentAttachmentName = (TextView) itemView.findViewById(R.id.commentAttachmentName);
            commentMoreIcon = (ImageView) itemView.findViewById(R.id.commentMoreIcon);
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
        private ImageView commentMoreIcon;
        private TextView commenterName;
        private RelativeLayout commentRootLayout;

        public ItemViewHolder(View itemView) {
            super(itemView);
            commentMoreIcon = (ImageView) itemView.findViewById(R.id.commentMoreIcon);
            commenterBody = (TextView) itemView.findViewById(R.id.commenterBody);
            commenterName = (TextView) itemView.findViewById(R.id.commenterName);
            commenterImage = (ImageView) itemView.findViewById(R.id.commenterImage);
            commentRootLayout = (RelativeLayout) itemView.findViewById(R.id.commentRootLayout);
        }
    }

    public void setOnLikeClickListener(CommentClickHandler onLikeClickListener) {
        this.commentClickHandler = onLikeClickListener;
    }

    public void setOnItemClickListener(RecyclerItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setIsLiked(boolean isLiked) {
        this.isLiked = isLiked;
    }
}
