package ink.va.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ink.va.R;
import com.koushikdutta.ion.Ion;

import java.util.List;

import ink.va.interfaces.FeedItemClick;
import ink.va.models.FeedModel;
import ink.va.utils.CircleTransform;
import ink.va.utils.Constants;
import ink.va.utils.FileUtils;
import ink.va.utils.SharedHelper;
import ink.va.utils.Time;


/**
 * Created by USER on 2016-06-20.
 */
public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.ViewHolder> {

    private List<FeedModel> feedList;
    private Context mContext;
    private FeedItemClick mOnClickListener;
    private SharedHelper sharedHelper;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView feedContent, userPostedTitle,
                whenPosted, feedAddress, feedAttachmentName, likesCountTV;
        private ImageView feedUserImage, likeIcon, commentIcon;
        private CardView feedItemCard;
        private RelativeLayout feedAddressLayout, feedAttachmentLayout, likeWrapper, commentWrapper;
        private ImageView feedMoreIcon;
        private ImageView imageHolder;
        public View actionDivider;
        private TextView commentCountTV;

        public ViewHolder(View view) {
            super(view);
            userPostedTitle = (TextView) view.findViewById(R.id.userPostedTitle);
            commentCountTV = (TextView) view.findViewById(R.id.commentCountTV);
            actionDivider = view.findViewById(R.id.actionDivider);
            whenPosted = (TextView) view.findViewById(R.id.whenPosted);
            feedAddress = (TextView) view.findViewById(R.id.feedAddress);
            likesCountTV = (TextView) view.findViewById(R.id.likesCountTV);
            feedAttachmentName = (TextView) view.findViewById(R.id.feedAttachmentName);
            feedContent = (TextView) view.findViewById(R.id.feedContent);
            feedUserImage = (ImageView) view.findViewById(R.id.feedUserImage);
            commentIcon = (ImageView) view.findViewById(R.id.commentIcon);
            likeIcon = (ImageView) view.findViewById(R.id.likeIcon);
            feedMoreIcon = (ImageView) view.findViewById(R.id.feedMoreIcon);
            imageHolder = (ImageView) view.findViewById(R.id.imageHolder);
            feedAddressLayout = (RelativeLayout) view.findViewById(R.id.feedAddressLayout);
            likeWrapper = (RelativeLayout) view.findViewById(R.id.likeWrapper);
            commentWrapper = (RelativeLayout) view.findViewById(R.id.commentWrapper);
            feedAttachmentLayout = (RelativeLayout) view.findViewById(R.id.feedAttachmentLayout);
            feedItemCard = (CardView) view.findViewById(R.id.feedItemCard);


        }
    }


    public FeedAdapter(List<FeedModel> feedList, Context context) {
        mContext = context;
        this.feedList = feedList;
        sharedHelper = new SharedHelper(context);
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.feed_single_view, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        FeedModel feedModel = feedList.get(position);

        switch (feedModel.getType()) {
            case Constants.WALL_TYPE_POST:
                handlePosts(holder, position, feedModel);
                break;
            case Constants.WALL_TYPE_GROUP_MESSAGE:
                handleGroupMessages(holder, position, feedModel);
                break;
            default:
                hideActions(holder);

        }


    }

    private void hideActions(ViewHolder holder) {
        holder.commentWrapper.setVisibility(View.GONE);
        holder.likeWrapper.setVisibility(View.GONE);
        holder.feedMoreIcon.setVisibility(View.GONE);
        holder.actionDivider.setVisibility(View.GONE);
        holder.whenPosted.setVisibility(View.GONE);
        holder.feedAddressLayout.setVisibility(View.GONE);
        holder.imageHolder.setVisibility(View.GONE);
        holder.feedAttachmentLayout.setVisibility(View.GONE);
        holder.commentCountTV.setVisibility(View.INVISIBLE);
        holder.likesCountTV.setVisibility(View.INVISIBLE);
    }

    private void handleGroupMessages(ViewHolder holder, final int position, final FeedModel feedModel) {
        holder.commentWrapper.setVisibility(View.GONE);
        holder.likeWrapper.setVisibility(View.GONE);
        holder.feedMoreIcon.setVisibility(View.GONE);
        holder.actionDivider.setVisibility(View.GONE);
        holder.whenPosted.setVisibility(View.GONE);
        holder.feedAddressLayout.setVisibility(View.GONE);
        holder.imageHolder.setVisibility(View.GONE);
        holder.feedAttachmentLayout.setVisibility(View.GONE);
        holder.commentCountTV.setVisibility(View.INVISIBLE);
        holder.likesCountTV.setVisibility(View.INVISIBLE);

        holder.userPostedTitle.setText(feedModel.getFirstName() + " " + feedModel.getLastName() + " > " + feedModel.getGroupName());

        holder.feedContent.setMovementMethod(LinkMovementMethod.getInstance());
        holder.feedContent.setText(feedModel.getContent());

        holder.feedItemCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnClickListener != null) {
                    mOnClickListener.onCardViewClick(position, feedModel.getType());
                }
            }
        });
        if (feedModel.getUserImage() != null && !feedModel.getUserImage().isEmpty()) {
            if (feedModel.isSocialAccount()) {
                Ion.with(mContext).load(feedModel.getUserImage())
                        .withBitmap().placeholder(R.drawable.no_background_image).transform(new CircleTransform())
                        .intoImageView(holder.feedUserImage);
            } else {
                Ion.with(mContext).load(Constants.MAIN_URL + Constants.USER_IMAGES_FOLDER + feedModel.getUserImage())
                        .withBitmap().placeholder(R.drawable.no_background_image).transform(new CircleTransform())
                        .intoImageView(holder.feedUserImage);
            }

        } else {
            Ion.with(mContext).load(Constants.ANDROID_DRAWABLE_DIR + "no_image")
                    .withBitmap().transform(new CircleTransform())
                    .intoImageView(holder.feedUserImage);
        }

    }

    private void handlePosts(final ViewHolder holder, final int position, FeedModel feedModel) {
        holder.commentWrapper.setVisibility(View.VISIBLE);
        holder.likeWrapper.setVisibility(View.VISIBLE);
        holder.actionDivider.setVisibility(View.VISIBLE);
        holder.feedMoreIcon.setVisibility(View.VISIBLE);
        holder.whenPosted.setVisibility(View.VISIBLE);
        holder.imageHolder.setVisibility(View.VISIBLE);
        holder.feedAddressLayout.setVisibility(View.VISIBLE);
        holder.feedAttachmentLayout.setVisibility(View.VISIBLE);
        holder.commentCountTV.setVisibility(View.VISIBLE);

        try {
            if (Integer.valueOf(feedModel.getCommentsCount()) <= 1) {

                holder.commentCountTV.setText(feedModel.getCommentsCount() + " " + mContext.getString(R.string.comment_count_text));
            } else {
                holder.commentCountTV.setText(feedModel.getCommentsCount() + " " + mContext.getString(R.string.comment_count_text));
            }
        } catch (Exception e) {
            holder.commentCountTV.setText(feedModel.getCommentsCount() + " " + mContext.getString(R.string.comment_count_text));
        }


        if (feedModel.getUserImage() != null && !feedModel.getUserImage().isEmpty()) {
            if (feedModel.isSocialAccount()) {
                Ion.with(mContext).load(feedModel.getUserImage())
                        .withBitmap().placeholder(R.drawable.no_background_image).transform(new CircleTransform())
                        .intoImageView(holder.feedUserImage);
            } else {
                Ion.with(mContext).load(Constants.MAIN_URL + Constants.USER_IMAGES_FOLDER + feedModel.getUserImage())
                        .withBitmap().placeholder(R.drawable.no_background_image).transform(new CircleTransform())
                        .intoImageView(holder.feedUserImage);
            }

        } else {
            Ion.with(mContext).load(Constants.ANDROID_DRAWABLE_DIR + "no_image")
                    .withBitmap().transform(new CircleTransform())
                    .intoImageView(holder.feedUserImage);
        }
        if (feedModel.getPosterId().equals(sharedHelper.getUserId())) {
            feedModel.setPostOwner(true);
        } else {
            feedModel.setPostOwner(false);
        }
        holder.feedMoreIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnClickListener != null) {
                    mOnClickListener.onMoreClicked(position, holder.feedMoreIcon);
                }
            }
        });
        holder.feedContent.setMovementMethod(LinkMovementMethod.getInstance());
        holder.feedContent.setText(feedModel.getContent());
        holder.whenPosted.setText(Time.convertToLocalTime(feedModel.getDatePosted()));
        holder.userPostedTitle.setText(feedModel.getFirstName() + " " + feedModel.getLastName());
        if (feedModel.isLiked()) {
            holder.likeIcon.setBackgroundResource(R.drawable.like_active);
        } else {
            holder.likeIcon.setBackgroundResource(R.drawable.like_inactive);
        }

        if (feedModel.getFileName() != null && !feedModel.getFileName().isEmpty()) {
            feedModel.setHasAttachment(true);
            holder.feedAttachmentLayout.setVisibility(View.VISIBLE);
            String fileName = feedModel.getFileName();
            int index = fileName.indexOf(":");
            holder.feedAttachmentName.setText(fileName.substring(index + 1, fileName.length()));

            if (FileUtils.isImageType(feedModel.getFileName())) {
                holder.imageHolder.setVisibility(View.VISIBLE);
                Ion.with(mContext).load(Constants.MAIN_URL + Constants.UPLOADED_FILES_DIR + feedModel.getFileName()).withBitmap().placeholder(R.drawable.big_image_place_holder)
                        .intoImageView(holder.imageHolder);
            } else {
                holder.imageHolder.setVisibility(View.GONE);
            }
        } else {
            holder.imageHolder.setVisibility(View.GONE);
            feedModel.setHasAttachment(false);
            holder.feedAttachmentLayout.setVisibility(View.GONE);
        }

        if (feedModel.getAddress() != null && !feedModel.getAddress().isEmpty()) {
            feedModel.setHasAddress(true);
            holder.feedAddressLayout.setVisibility(View.VISIBLE);
            holder.feedAddress.setText(feedModel.getAddress());
        } else {
            feedModel.setHasAddress(false);
            holder.feedAddressLayout.setVisibility(View.GONE);
        }
        if (!feedModel.getLikesCount().equals("0")) {
            holder.likesCountTV.setVisibility(View.VISIBLE);
            if (Integer.parseInt(feedModel.getLikesCount()) > 1) {
                holder.likesCountTV.setText(feedModel.getLikesCount() + " " + mContext.getString(R.string.likesText));
            } else {
                holder.likesCountTV.setText(feedModel.getLikesCount() + " " + mContext.getString(R.string.singleLikeText));
            }
        } else {
            holder.likesCountTV.setVisibility(View.INVISIBLE);
        }
        //listeners
        holder.feedItemCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnClickListener != null) {
                    mOnClickListener.onCardViewClick(position, feedList.get(position).getType());
                }
            }
        });
        holder.feedAttachmentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnClickListener != null) {
                    mOnClickListener.onAttachmentClick(position);
                }
            }
        });
        holder.feedAddressLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnClickListener != null) {
                    mOnClickListener.onAddressClick(position);
                }
            }
        });
        holder.feedItemCard.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (mOnClickListener != null) {
                    mOnClickListener.onCardLongClick(position);
                }
                return true;
            }
        });
        holder.likeWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnClickListener != null) {
                    mOnClickListener.onLikeClick(position, holder.likeIcon, holder.likesCountTV, holder.likeWrapper);
                }
            }
        });
        holder.commentWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnClickListener != null) {
                    mOnClickListener.onCommentClicked(position, holder.commentIcon);
                }
            }
        });
        holder.imageHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnClickListener != null) {
                    mOnClickListener.onImageClicked(position);
                }
            }
        });
    }


    public void setOnFeedClickListener(FeedItemClick mOnClickListener) {
        this.mOnClickListener = mOnClickListener;
    }


    @Override
    public int getItemCount() {
        return feedList.size();
    }
}
