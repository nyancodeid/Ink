package ink.va.view_holders;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.ink.va.R;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import ink.va.interfaces.FeedItemClick;
import ink.va.models.FeedModel;
import ink.va.utils.CircleTransform;
import ink.va.utils.Constants;
import ink.va.utils.FileUtils;
import ink.va.utils.SharedHelper;
import ink.va.utils.Time;

/**
 * Created by PC-Comp on 12/20/2016.
 */

public class FeedViewHolder extends RecyclerView.ViewHolder {
    private static final int SHIMMER_MOVE_DURATION = 3000;
    public TextView feedContent, userPostedTitle,
            whenPosted, feedAddress, feedAttachmentName, likesCountTV;
    private ImageView feedUserImage, likeIcon, commentIcon;
    private CardView feedItemCard;
    private RelativeLayout feedAddressLayout, feedAttachmentLayout, likeWrapper, commentWrapper;
    private ImageView feedMoreIcon;
    private ImageView imageHolder;
    public View actionDivider;
    private TextView commentCountTV;
    private Context mContext;
    private FeedModel feedModel;
    private SharedHelper sharedHelper;
    private FeedItemClick mOnClickListener;
    private View feedRootLayout;
    private ShimmerFrameLayout feedShimmer;
    private View spacing;

    public FeedViewHolder(View view) {
        super(view);
        userPostedTitle = (TextView) view.findViewById(R.id.userPostedTitle);
        spacing = view.findViewById(R.id.spacing);
        feedShimmer = (ShimmerFrameLayout) view.findViewById(R.id.feedShimmer);
        feedRootLayout = view.findViewById(R.id.feedRootLayout);
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

    public void initData(Context context, FeedModel feedModel, int position,
                         FeedItemClick feedItemClick, int maxCount) {
        sharedHelper = new SharedHelper(context);
        if (position == (maxCount - 1)) {
            spacing.setVisibility(View.VISIBLE);
        } else {
            spacing.setVisibility(View.GONE);
        }

        if (sharedHelper.shallShowPostShimmer()) {
            feedShimmer.setDuration(SHIMMER_MOVE_DURATION);
            feedShimmer.startShimmerAnimation();
        } else {
            feedShimmer.stopShimmerAnimation();
        }

        mOnClickListener = feedItemClick;
        mContext = context;
        this.feedModel = feedModel;
        switch (feedModel.getType()) {
            case Constants.WALL_TYPE_POST:
                handlePosts(position);
                break;
            case Constants.WALL_TYPE_GROUP_MESSAGE:
                handleGroupMessages(position);
                break;
            default:
                hideActions();

        }
    }

    private void hideActions() {
        commentWrapper.setVisibility(View.GONE);
        likeWrapper.setVisibility(View.GONE);
        feedMoreIcon.setVisibility(View.GONE);
        actionDivider.setVisibility(View.GONE);
        whenPosted.setVisibility(View.GONE);
        feedAddressLayout.setVisibility(View.GONE);
        imageHolder.setVisibility(View.GONE);
        feedAttachmentLayout.setVisibility(View.GONE);
        commentCountTV.setVisibility(View.INVISIBLE);
        likesCountTV.setVisibility(View.INVISIBLE);
    }

    private void handleGroupMessages(final int position) {
        commentWrapper.setVisibility(View.GONE);
        likeWrapper.setVisibility(View.GONE);
        feedMoreIcon.setVisibility(View.GONE);
        actionDivider.setVisibility(View.GONE);
        whenPosted.setVisibility(View.GONE);
        feedAddressLayout.setVisibility(View.GONE);
        imageHolder.setVisibility(View.GONE);
        feedAttachmentLayout.setVisibility(View.GONE);
        commentCountTV.setVisibility(View.INVISIBLE);
        likesCountTV.setVisibility(View.INVISIBLE);

        userPostedTitle.setText(feedModel.getFirstName() + " " + feedModel.getLastName() + " > " + feedModel.getGroupName());

        feedContent.setMovementMethod(LinkMovementMethod.getInstance());
        feedContent.setText(feedModel.getContent());

        if (feedModel.getGroupMessageFileName().isEmpty()) {
            imageHolder.setVisibility(View.GONE);
        } else {
            imageHolder.setVisibility(View.VISIBLE);
            String encodedImage = Uri.encode(feedModel.getGroupMessageFileName());
            Ion.with(mContext).load(Constants.MAIN_URL + Constants.UPLOADED_FILES_DIR + encodedImage).asBitmap().setCallback(new FutureCallback<Bitmap>() {
                @Override
                public void onCompleted(Exception e, Bitmap result) {
                    if (e == null) {
                        imageHolder.setImageBitmap(result);
                    } else {
                        imageHolder.setVisibility(View.GONE);
                    }
                }
            });
        }


        feedContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnClickListener != null) {
                    mOnClickListener.onCardViewClick(position, feedModel.getType());
                }
            }
        });
        feedItemCard.setOnClickListener(new View.OnClickListener() {
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
                        .intoImageView(feedUserImage);
            } else {
                String encodedImage = Uri.encode(feedModel.getUserImage());
                Ion.with(mContext).load(Constants.MAIN_URL + Constants.USER_IMAGES_FOLDER + encodedImage)
                        .withBitmap().placeholder(R.drawable.no_background_image).transform(new CircleTransform())
                        .intoImageView(feedUserImage);
            }

        } else {
            Ion.with(mContext).load(Constants.ANDROID_DRAWABLE_DIR + "no_image")
                    .withBitmap().transform(new CircleTransform())
                    .intoImageView(feedUserImage);
        }

    }

    private void handlePosts(final int position) {
        commentWrapper.setVisibility(View.VISIBLE);
        likeWrapper.setVisibility(View.VISIBLE);
        actionDivider.setVisibility(View.VISIBLE);
        feedMoreIcon.setVisibility(View.VISIBLE);
        whenPosted.setVisibility(View.VISIBLE);
        imageHolder.setVisibility(View.VISIBLE);
        feedAddressLayout.setVisibility(View.VISIBLE);
        feedAttachmentLayout.setVisibility(View.VISIBLE);
        commentCountTV.setVisibility(View.VISIBLE);


        try {
            if (Integer.valueOf(feedModel.getCommentsCount()) <= 1) {

                commentCountTV.setText(feedModel.getCommentsCount() + " " + mContext.getString(R.string.comment_count_text));
            } else {
                commentCountTV.setText(feedModel.getCommentsCount() + " " + mContext.getString(R.string.comment_count_text));
            }
        } catch (Exception e) {
            commentCountTV.setText(feedModel.getCommentsCount() + " " + mContext.getString(R.string.comment_count_text));
        }


        if (feedModel.getUserImage() != null && !feedModel.getUserImage().isEmpty()) {
            if (feedModel.isSocialAccount()) {
                Ion.with(mContext).load(feedModel.getUserImage())
                        .withBitmap().placeholder(R.drawable.no_background_image).transform(new CircleTransform())
                        .intoImageView(feedUserImage);
            } else {
                String encodedImage = Uri.encode(feedModel.getUserImage());
                Ion.with(mContext).load(Constants.MAIN_URL + Constants.USER_IMAGES_FOLDER + encodedImage)
                        .withBitmap().placeholder(R.drawable.no_background_image).transform(new CircleTransform())
                        .intoImageView(feedUserImage);
            }

        } else {
            Ion.with(mContext).load(Constants.ANDROID_DRAWABLE_DIR + "no_image")
                    .withBitmap().transform(new CircleTransform())
                    .intoImageView(feedUserImage);
        }
        if (feedModel.getPosterId().equals(sharedHelper.getUserId())) {
            feedModel.setPostOwner(true);
        } else {
            feedModel.setPostOwner(false);
        }
        feedMoreIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnClickListener != null) {
                    mOnClickListener.onMoreClicked(position, feedMoreIcon);
                }
            }
        });
        feedContent.setMovementMethod(LinkMovementMethod.getInstance());
        feedContent.setText(feedModel.getContent());
        whenPosted.setText(Time.convertToLocalTime(feedModel.getDatePosted()));
        userPostedTitle.setText(feedModel.getFirstName() + " " + feedModel.getLastName());
        if (feedModel.isLiked()) {
            likeIcon.setBackgroundResource(R.drawable.like_active);
        } else {
            likeIcon.setBackgroundResource(R.drawable.like_inactive);
        }

        if (feedModel.getFileName() != null && !feedModel.getFileName().isEmpty()) {
            feedModel.setHasAttachment(true);
            feedAttachmentLayout.setVisibility(View.VISIBLE);
            String fileName = feedModel.getFileName();
            int index = fileName.indexOf(":");
            feedAttachmentName.setText(fileName.substring(index + 1, fileName.length()));

            if (FileUtils.isImageType(feedModel.getFileName())) {
                imageHolder.setVisibility(View.VISIBLE);
                feedAttachmentLayout.setVisibility(View.GONE);

                Ion.with(mContext).load(Constants.MAIN_URL + Constants.UPLOADED_FILES_DIR + Uri.encode(feedModel.getFileName())).withBitmap().placeholder(R.drawable.big_image_place_holder)
                        .intoImageView(imageHolder).setCallback(new FutureCallback<ImageView>() {
                    @Override
                    public void onCompleted(Exception e, ImageView result) {
                        if (e != null) {
                            e.printStackTrace();
                        }
                    }
                });
            } else {
                feedAttachmentLayout.setVisibility(View.VISIBLE);
                imageHolder.setVisibility(View.GONE);
            }
        } else {
            imageHolder.setVisibility(View.GONE);
            feedModel.setHasAttachment(false);
            feedAttachmentLayout.setVisibility(View.GONE);
        }

        feedContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnClickListener != null) {
                    mOnClickListener.onCardViewClick(position, feedModel.getType());
                }
            }
        });
        if (feedModel.getAddress() != null && !feedModel.getAddress().isEmpty()) {
            feedModel.setHasAddress(true);
            feedAddressLayout.setVisibility(View.VISIBLE);
            feedAddress.setText(feedModel.getAddress());
        } else {
            feedModel.setHasAddress(false);
            feedAddressLayout.setVisibility(View.GONE);
        }
        if (!feedModel.getLikesCount().equals("0")) {
            likesCountTV.setVisibility(View.VISIBLE);
            if (Integer.parseInt(feedModel.getLikesCount()) > 1) {
                likesCountTV.setText(feedModel.getLikesCount() + " " + mContext.getString(R.string.likesText));
            } else {
                likesCountTV.setText(feedModel.getLikesCount() + " " + mContext.getString(R.string.singleLikeText));
            }
        } else {
            likesCountTV.setVisibility(View.INVISIBLE);
        }
        //listeners
        feedItemCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnClickListener != null) {
                    mOnClickListener.onCardViewClick(position, feedModel.getType());
                }
            }
        });
        feedAttachmentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnClickListener != null) {
                    mOnClickListener.onAttachmentClick(position);
                }
            }
        });
        feedAddressLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnClickListener != null) {
                    mOnClickListener.onAddressClick(position);
                }
            }
        });
        feedItemCard.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (mOnClickListener != null) {
                    mOnClickListener.onCardLongClick(position);
                }
                return true;
            }
        });
        likeWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnClickListener != null) {
                    mOnClickListener.onLikeClick(position, likeIcon, likesCountTV, likeWrapper);
                }
            }
        });
        commentWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnClickListener != null) {
                    mOnClickListener.onCommentClicked(position, commentIcon);
                }
            }
        });
        imageHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnClickListener != null) {
                    mOnClickListener.onImageClicked(position);
                }
            }
        });
    }

    public View getViewToAnimate() {
        return feedRootLayout;
    }
}
