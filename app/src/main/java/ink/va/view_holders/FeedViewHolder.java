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

import com.ink.va.R;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import butterknife.BindView;
import butterknife.ButterKnife;
import ink.va.interfaces.FeedItemClick;
import ink.va.models.FeedModel;
import ink.va.utils.Animations;
import ink.va.utils.CircleTransform;
import ink.va.utils.Constants;
import ink.va.utils.FileUtils;
import ink.va.utils.SharedHelper;
import ink.va.utils.Time;
import lombok.Setter;

/**
 * Created by PC-Comp on 12/20/2016.
 */

public class FeedViewHolder extends RecyclerView.ViewHolder {
    private static final int SHIMMER_MOVE_DURATION = 3000;

    @BindView(R.id.feedContent)
    TextView feedContent;
    @BindView(R.id.userPostedTitle)
    TextView userPostedTitle;
    @BindView(R.id.whenPosted)
    TextView whenPosted;
    @BindView(R.id.feedAddress)
    TextView feedAddress;
    @BindView(R.id.feedAttachmentName)
    TextView feedAttachmentName;
    @BindView(R.id.likesCountTV)
    TextView likesCountTV;
    @BindView(R.id.commentCountTV)
    TextView commentCountTV;

    @BindView(R.id.feedUserImage)
    ImageView feedUserImage;
    @BindView(R.id.likeIcon)
    ImageView likeIcon;
    @BindView(R.id.commentIcon)
    ImageView commentIcon;
    @BindView(R.id.feedMoreIcon)
    ImageView feedMoreIcon;
    @BindView(R.id.imageHolder)
    ImageView imageHolder;
    @BindView(R.id.postVisibilityIcon)
    ImageView postVisibilityIcon;
    @BindView(R.id.shareIcon)
    ImageView shareIcon;

    @BindView(R.id.feedItemCard)
    CardView feedItemCard;

    @BindView(R.id.feedAddressLayout)
    RelativeLayout feedAddressLayout;
    @BindView(R.id.feedAttachmentLayout)
    RelativeLayout feedAttachmentLayout;
    @BindView(R.id.likeWrapper)
    RelativeLayout likeWrapper;
    @BindView(R.id.commentWrapper)
    RelativeLayout commentWrapper;
    @BindView(R.id.shareWrapper)
    RelativeLayout shareWrapper;


    Context mContext;
    FeedModel feedModel;
    SharedHelper sharedHelper;
    FeedItemClick mOnClickListener;

    @BindView(R.id.feedRootLayout)
    View feedRootLayout;
    @BindView(R.id.spacing)
    View spacing;
    @BindView(R.id.actionDivider)
    View actionDivider;
    @BindView(R.id.feedItemWrapper)
    View feedItemWrapper;
    @BindView(R.id.noPostOrErrorWrapper)
    View noPostOrErrorWrapper;
    @BindView(R.id.noPostIconOrError)
    ImageView noPostIconOrError;
    @BindView(R.id.noPostOrErrorTV)
    TextView noPostOrErrorTV;
    @Setter
    private boolean showNoFeedOrError;
    @Setter
    private boolean hasServerError;

    public FeedViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void initData(Context context, FeedModel feedModel, int position,
                         FeedItemClick feedItemClick, int maxCount) {
        sharedHelper = new SharedHelper(context);

        if (showNoFeedOrError) {
            if (feedItemWrapper.getVisibility() == View.VISIBLE) {
                feedItemWrapper.setVisibility(View.GONE);
            }
            if (noPostOrErrorWrapper.getVisibility() == View.GONE) {
                noPostOrErrorWrapper.setVisibility(View.VISIBLE);
            }
            if (hasServerError) {
                noPostIconOrError.setBackgroundResource(R.drawable.network_error);
                noPostOrErrorTV.setText(context.getString(R.string.feedError));
            } else {
                noPostIconOrError.setBackgroundResource(R.drawable.no_posts);
                noPostOrErrorTV.setText(context.getString(R.string.noPostsYet));
            }
        } else {
            if (feedItemWrapper.getVisibility() == View.GONE) {
                feedItemWrapper.setVisibility(View.VISIBLE);
            }
            if (noPostOrErrorWrapper.getVisibility() == View.VISIBLE) {
                noPostOrErrorWrapper.setVisibility(View.GONE);
            }

            if (position == (maxCount - 1)) {
                spacing.setVisibility(View.VISIBLE);
            } else {
                spacing.setVisibility(View.GONE);
            }

            postVisibilityIcon.setImageResource(feedModel.isGlobalPost() ? R.drawable.global_icon_greyed_out : R.drawable.local_icon_greyed_out);

            mOnClickListener = feedItemClick;
            mContext = context;
            this.feedModel = feedModel;
            switch (feedModel.getType()) {
                case Constants.WALL_TYPE_POST:
                    handlePosts();
                    break;
                case Constants.WALL_TYPE_GROUP_MESSAGE:
                    handleGroupMessages();
                    break;
                default:
                    hideActions();

            }
        }
    }

    private void hideActions() {
        commentWrapper.setVisibility(View.GONE);
        shareWrapper.setVisibility(View.GONE);
        likeWrapper.setVisibility(View.GONE);
        feedMoreIcon.setVisibility(View.INVISIBLE);
        actionDivider.setVisibility(View.GONE);
        whenPosted.setVisibility(View.GONE);
        feedAddressLayout.setVisibility(View.GONE);
        imageHolder.setVisibility(View.GONE);
        feedAttachmentLayout.setVisibility(View.GONE);
        commentCountTV.setVisibility(View.INVISIBLE);
        likesCountTV.setVisibility(View.INVISIBLE);
    }

    private void handleGroupMessages() {
        commentWrapper.setVisibility(View.GONE);
        shareWrapper.setVisibility(View.GONE);
        likeWrapper.setVisibility(View.GONE);
        feedMoreIcon.setVisibility(View.INVISIBLE);
        actionDivider.setVisibility(View.GONE);
        whenPosted.setVisibility(View.GONE);
        feedAddressLayout.setVisibility(View.GONE);
        imageHolder.setVisibility(View.GONE);
        feedAttachmentLayout.setVisibility(View.GONE);
        commentCountTV.setVisibility(View.INVISIBLE);
        likesCountTV.setVisibility(View.INVISIBLE);

        userPostedTitle.setText(feedModel.getFirstName() + " " + feedModel.getLastName() + " > " + feedModel.getGroupName());

        feedContent.setMovementMethod(LinkMovementMethod.getInstance());
        feedContent.setText(mContext.getString(R.string.quoteOpen) + feedModel.getContent() + mContext.getString(R.string.quoteClose));

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
                    mOnClickListener.onCardViewClick(feedModel, feedModel.getType());
                }
            }
        });
        feedItemCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnClickListener != null) {
                    mOnClickListener.onCardViewClick(feedModel, feedModel.getType());
                }
            }
        });
        if (feedModel.getUserImage() != null && !feedModel.getUserImage().isEmpty()) {
            if (feedModel.isSocialAccount()) {
                Ion.with(mContext).load(feedModel.getUserImage())
                        .withBitmap().placeholder(R.drawable.user_image_placeholder).transform(new CircleTransform())
                        .intoImageView(feedUserImage);
            } else {
                String encodedImage = Uri.encode(feedModel.getUserImage());
                Ion.with(mContext).load(Constants.MAIN_URL + Constants.USER_IMAGES_FOLDER + encodedImage)
                        .withBitmap().placeholder(R.drawable.user_image_placeholder).transform(new CircleTransform())
                        .intoImageView(feedUserImage);
            }

        } else {
            Ion.with(mContext).load(Constants.ANDROID_DRAWABLE_DIR + "no_image")
                    .withBitmap().transform(new CircleTransform())
                    .intoImageView(feedUserImage);
        }

    }

    private void handlePosts() {
        commentWrapper.setVisibility(View.VISIBLE);
        shareWrapper.setVisibility(View.VISIBLE);
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
                        .withBitmap().placeholder(R.drawable.user_image_placeholder).transform(new CircleTransform())
                        .intoImageView(feedUserImage);
            } else {
                String encodedImage = Uri.encode(feedModel.getUserImage());
                Ion.with(mContext).load(Constants.MAIN_URL + Constants.USER_IMAGES_FOLDER + encodedImage)
                        .withBitmap().placeholder(R.drawable.user_image_placeholder).transform(new CircleTransform())
                        .intoImageView(feedUserImage);
            }

        } else {
            Ion.with(mContext).load(Constants.ANDROID_DRAWABLE_DIR + "no_image")
                    .withBitmap().transform(new CircleTransform())
                    .intoImageView(feedUserImage);
        }
        if (feedModel.getPosterId().equals(sharedHelper.getUserId())) {
            sharedHelper.putOwnPostId(feedModel.getId());
            feedModel.setPostOwner(true);
        } else {
            feedModel.setPostOwner(false);
        }
        feedMoreIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnClickListener != null) {
                    mOnClickListener.onMoreClicked(feedModel, feedMoreIcon);
                }
            }
        });

        feedContent.setMovementMethod(LinkMovementMethod.getInstance());
        feedContent.setText(mContext.getString(R.string.quoteOpen) + feedModel.getContent() + mContext.getString(R.string.quoteClose));
        whenPosted.setText(Time.convertToLocalTime(feedModel.getDatePosted()));
        userPostedTitle.setText(feedModel.getFirstName() + " " + feedModel.getLastName());
        if (feedModel.isLiked()) {
            sharedHelper.putLikedPostId(feedModel.getId());
            likeIcon.setBackgroundResource(R.drawable.like_active);
        } else {
            sharedHelper.removeLikedPostId(feedModel.getId());
            likeIcon.setBackgroundResource(R.drawable.like_inactive);
        }

        if (feedModel.getFileName() != null && !feedModel.getFileName().isEmpty()) {
            feedModel.setAttachmentPresent(true);
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
            feedModel.setAttachmentPresent(false);
            feedAttachmentLayout.setVisibility(View.GONE);
        }

        feedContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnClickListener != null) {
                    mOnClickListener.onCardViewClick(feedModel, feedModel.getType());
                }
            }
        });
        if (feedModel.getAddress() != null && !feedModel.getAddress().isEmpty()) {
            feedModel.setAddressPresent(true);
            feedAddressLayout.setVisibility(View.VISIBLE);
            feedAddress.setText(feedModel.getAddress());
        } else {
            feedModel.setAddressPresent(false);
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
                    mOnClickListener.onCardViewClick(feedModel, feedModel.getType());
                }
            }
        });
        feedAttachmentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnClickListener != null) {
                    mOnClickListener.onAttachmentClick(feedModel);
                }
            }
        });
        feedAddressLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnClickListener != null) {
                    mOnClickListener.onAddressClick(feedModel);
                }
            }
        });
        feedItemCard.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (mOnClickListener != null) {
                    mOnClickListener.onCardLongClick(feedModel);
                }
                return true;
            }
        });
        likeWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnClickListener != null) {
                    mOnClickListener.onLikeClick(feedModel, likeIcon, likesCountTV, likeWrapper);
                }
            }
        });
        commentWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnClickListener != null) {
                    mOnClickListener.onCommentClicked(feedModel, commentIcon);
                }
            }
        });
        shareWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animations.animateCircular(shareIcon);

                if (mOnClickListener != null) {
                    mOnClickListener.onShareClicked(feedModel);
                }
            }
        });

        imageHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnClickListener != null) {
                    mOnClickListener.onImageClicked(feedModel);
                }
            }
        });
    }

    public View getViewToAnimate() {
        return feedRootLayout;
    }
}
