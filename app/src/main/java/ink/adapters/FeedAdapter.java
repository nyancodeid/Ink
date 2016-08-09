package ink.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ink.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import ink.interfaces.FeedItemClick;
import ink.models.FeedModel;
import ink.utils.CircleTransform;
import ink.utils.Constants;
import ink.utils.Time;

/**
 * Created by USER on 2016-06-20.
 */
public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.ViewHolder> {

    private List<FeedModel> feedList;
    private Context mContext;
    private FeedItemClick mOnClickListener;
    private boolean shouldStartAnimation;
    private int lastPosition;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView feedContent, userPostedTitle,
                whenPosted, feedAddress, feedAttachmentName, likesCountTV;
        private ImageView feedUserImage, likeIcon, commentIcon;
        private CardView feedItemCard;
        private RelativeLayout feedAddressLayout, feedAttachmentLayout, likeWrapper, commentWrapper;

        public ViewHolder(View view) {
            super(view);
            userPostedTitle = (TextView) view.findViewById(R.id.userPostedTitle);
            whenPosted = (TextView) view.findViewById(R.id.whenPosted);
            feedAddress = (TextView) view.findViewById(R.id.feedAddress);
            likesCountTV = (TextView) view.findViewById(R.id.likesCountTV);
            feedAttachmentName = (TextView) view.findViewById(R.id.feedAttachmentName);
            feedContent = (TextView) view.findViewById(R.id.feedContent);
            feedUserImage = (ImageView) view.findViewById(R.id.feedUserImage);
            commentIcon = (ImageView) view.findViewById(R.id.commentIcon);
            likeIcon = (ImageView) view.findViewById(R.id.likeIcon);
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

        if (feedModel.getUserImage() != null && !feedModel.getUserImage().isEmpty()) {
            if (feedModel.isSocialAccount()) {
                Picasso.with(mContext).load(feedModel.getUserImage())
                        .error(R.drawable.image_laoding_error)
                        .placeholder(R.drawable.no_image_yet_state).transform(new CircleTransform())
                        .fit().centerCrop().into(holder.feedUserImage);
            } else {
                Picasso.with(mContext).load(Constants.MAIN_URL + Constants.USER_IMAGES_FOLDER + feedModel.getUserImage())
                        .error(R.drawable.image_laoding_error)
                        .placeholder(R.drawable.no_image_yet_state).transform(new CircleTransform())
                        .fit().centerCrop().into(holder.feedUserImage);
            }

        } else {
            Picasso.with(mContext).load(R.drawable.no_image)
                    .transform(new CircleTransform())
                    .fit().centerCrop().into(holder.feedUserImage);
        }

        holder.feedContent.setText(feedModel.getContent());
        holder.whenPosted.setText(Time.convertToLocalTime(feedModel.getDatePosted()));
        holder.userPostedTitle.setText(feedModel.getFirstName() + " " + feedModel.getLastName());
        if (feedModel.isLiked()) {
            holder.likeIcon.setBackgroundResource(R.drawable.like_active);
        } else {
            holder.likeIcon.setBackgroundResource(R.drawable.like_inactive);
        }

        if (feedModel.getFileName() != null && !feedModel.getFileName().isEmpty()) {
            holder.feedAttachmentLayout.setVisibility(View.VISIBLE);
            holder.feedAttachmentName.setText(feedModel.getFileName());
        } else {
            holder.feedAttachmentLayout.setVisibility(View.GONE);
        }

        if (feedModel.getAddress() != null && !feedModel.getAddress().isEmpty()) {
            holder.feedAddressLayout.setVisibility(View.VISIBLE);
            holder.feedAddress.setText(feedModel.getAddress());
        } else {
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
            holder.likesCountTV.setVisibility(View.GONE);
        }
        //listeners
        holder.feedItemCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnClickListener != null) {
                    mOnClickListener.onCardViewClick(position);
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
                    mOnClickListener.onLikeClick(position, holder.likeIcon, holder.likesCountTV);
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
    }

    public void setOnFeedClickListener(FeedItemClick mOnClickListener) {
        this.mOnClickListener = mOnClickListener;
    }



    @Override
    public int getItemCount() {
        return feedList.size();
    }


    public void setShouldStartAnimation(boolean shouldStartAnimation) {
        this.shouldStartAnimation = shouldStartAnimation;
    }
}
