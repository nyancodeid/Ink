package ink.adapters;

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

import com.ink.R;
import com.koushikdutta.ion.Ion;

import java.util.List;

import ink.interfaces.FeedItemClick;
import ink.models.FeedModel;
import ink.utils.CircleTransform;
import ink.utils.Constants;
import ink.utils.SharedHelper;
import ink.utils.Time;


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
            feedMoreIcon = (ImageView) view.findViewById(R.id.feedMoreIcon);
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
            holder.feedMoreIcon.setVisibility(View.VISIBLE);
        } else {
            holder.feedMoreIcon.setVisibility(View.GONE);
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
            holder.feedAttachmentName.setText(fileName.substring(index+1, fileName.length()));
        } else {
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
    }

    public void setOnFeedClickListener(FeedItemClick mOnClickListener) {
        this.mOnClickListener = mOnClickListener;
    }


    @Override
    public int getItemCount() {
        return feedList.size();
    }
}
