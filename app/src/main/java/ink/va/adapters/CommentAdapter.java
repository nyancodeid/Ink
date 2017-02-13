package ink.va.adapters;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.danikula.videocache.HttpProxyCacheServer;
import com.ink.va.R;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.mikhaellopez.hfrecyclerview.HFRecyclerView;

import java.util.List;

import ink.StartupApplication;
import ink.va.interfaces.CommentClickHandler;
import ink.va.interfaces.RecyclerItemClickListener;
import ink.va.models.CommentModel;
import ink.va.utils.CircleTransform;
import ink.va.utils.Constants;
import ink.va.utils.FileUtils;
import ink.va.utils.SharedHelper;

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
    private boolean showingNoComments = false;

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
            handleComments(position, holder);
        } else if (holder instanceof HeaderViewHolder) {
            handleHeaderView(position, holder);
        }
    }

    private void handleHeaderView(final int position, final RecyclerView.ViewHolder holder) {
        final HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
        if (ownerId.equals(sharedHelper.getUserId())) {
        } else {
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
                String encodedImage = Uri.encode(ownerImage);
                Ion.with(context).load(Constants.MAIN_URL + Constants.USER_IMAGES_FOLDER +
                        encodedImage).withBitmap().placeholder(R.drawable.no_background_image).transform(new CircleTransform()).intoImageView(headerViewHolder.postOwnerImage);
            }
        } else {
            Ion.with(context).load(Constants.ANDROID_DRAWABLE_DIR + "no_image").withBitmap().transform(new CircleTransform()).intoImageView(headerViewHolder.postOwnerImage);
        }
        headerViewHolder.postBody.setMovementMethod(LinkMovementMethod.getInstance());
        headerViewHolder.postBody.setText(ownerPostBody);

        headerViewHolder.postBody.setText(ownerPostBody);

        headerViewHolder.postDate.setText(date);


        if (attachment != null && !attachment.isEmpty()) {
            headerViewHolder.commentAttachmentLayout.setVisibility(View.VISIBLE);
            String fileName = attachment;
            int index = fileName.indexOf(":");
            headerViewHolder.commentAttachmentName.setText(fileName.substring(index + 1, fileName.length()));

            if (FileUtils.isImageType(attachment)) {
                headerViewHolder.imageHolder.setVisibility(View.VISIBLE);
                String encodedImage = Uri.encode(attachment);
                ((HeaderViewHolder) holder).commentAttachmentLayout.setVisibility(View.GONE);
                Ion.with(context).load(Constants.MAIN_URL + Constants.UPLOADED_FILES_DIR + encodedImage).withBitmap().placeholder(R.drawable.big_image_place_holder)
                        .intoImageView(headerViewHolder.imageHolder);
            } else {
                ((HeaderViewHolder) holder).commentAttachmentLayout.setVisibility(View.VISIBLE);
                headerViewHolder.imageHolder.setVisibility(View.GONE);
            }

        } else {
            headerViewHolder.imageHolder.setVisibility(View.GONE);
            headerViewHolder.commentAttachmentLayout.setVisibility(View.GONE);
        }

        headerViewHolder.imageHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (commentClickHandler != null) {
                    commentClickHandler.onImageClicked(position);
                }
            }
        });
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
                            headerViewHolder.likeIcon, headerViewHolder.commentLikeWrapper);
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
        if (showingNoComments) {
            headerViewHolder.noCommentWrapper.setVisibility(View.VISIBLE);
        } else {
            headerViewHolder.noCommentWrapper.setVisibility(View.GONE);
        }
    }

    private void handleComments(final int position, RecyclerView.ViewHolder holder) {
        final ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
        CommentModel commentModel = getItem(position);
        checkForSticker(commentModel, itemViewHolder);

        itemViewHolder.commenterBody.setText(commentModel.getCommentBody());
        itemViewHolder.commenterBody.setMovementMethod(LinkMovementMethod.getInstance());
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
                String encodedImage = Uri.encode(commentModel.getCommenterImage());

                Ion.with(context).load(Constants.MAIN_URL + Constants.USER_IMAGES_FOLDER +
                        encodedImage).withBitmap().placeholder(R.drawable.no_background_image).transform(new CircleTransform()).intoImageView(itemViewHolder.commenterImage);
            }
        } else {
            Ion.with(context).load(Constants.ANDROID_DRAWABLE_DIR + "no_image").withBitmap().placeholder(R.drawable.no_background_image).transform(new CircleTransform()).intoImageView(itemViewHolder.commenterImage);
        }
        itemViewHolder.commentMoreIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onItemClickListener != null) {
                    onItemClickListener.onAdditionItemClick(position, itemViewHolder.commentMoreIcon);
                }
            }
        });
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {
        private ImageView postOwnerImage, likeIcon, imageHolder;
        private TextView postBody, postDate, commenterName, commentAttachmentName, commentAddress, likesCountTV;
        private RelativeLayout commentLikeWrapper, commentAddressLayout, commentAttachmentLayout;
        private ImageView commentMoreIcon;
        private RelativeLayout noCommentWrapper;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            postOwnerImage = (ImageView) itemView.findViewById(R.id.postOwnerImage);
            likeIcon = (ImageView) itemView.findViewById(R.id.commentLikeIcon);
            imageHolder = (ImageView) itemView.findViewById(R.id.imageHolder);
            postBody = (TextView) itemView.findViewById(R.id.postBody);
            commentAttachmentName = (TextView) itemView.findViewById(R.id.commentAttachmentName);
            commentMoreIcon = (ImageView) itemView.findViewById(R.id.commentMoreIcon);
            postDate = (TextView) itemView.findViewById(R.id.postDate);
            commentAddress = (TextView) itemView.findViewById(R.id.commentAddress);
            commenterName = (TextView) itemView.findViewById(R.id.commenterName);
            likesCountTV = (TextView) itemView.findViewById(R.id.commentLikesCount);
            commentLikeWrapper = (RelativeLayout) itemView.findViewById(R.id.commentLikeWrapper);
            commentAddressLayout = (RelativeLayout) itemView.findViewById(R.id.commentAddresslayout);
            noCommentWrapper = (RelativeLayout) itemView.findViewById(R.id.noCommentWrapper);
            commentAttachmentLayout = (RelativeLayout) itemView.findViewById(R.id.commentAttachmentLayout);
        }
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        private TextView commenterBody;
        private ImageView commenterImage;
        private ImageView commentMoreIcon;
        private TextView commenterName;
        private RelativeLayout commentRootLayout;
        private ImageView imageView;
        private LinearLayout imageViewWrapper;
        private RelativeLayout chatVideoWrapper;
        private VideoView chatVideo;
        private ProgressBar videoLoadingProgress;

        public ItemViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.stickerView);
            imageViewWrapper = (LinearLayout) itemView.findViewById(R.id.stickerWrapper);
            chatVideoWrapper = (RelativeLayout) itemView.findViewById(R.id.stickerVideoWrapper);
            chatVideo = (VideoView) itemView.findViewById(R.id.stickerVideo);
            videoLoadingProgress = (ProgressBar) itemView.findViewById(R.id.stickerVideoLoading);

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

    public void setShowingNoComments(boolean showingNoComments) {
        this.showingNoComments = showingNoComments;
    }

    public void setIsLiked(boolean isLiked) {
        this.isLiked = isLiked;
    }

    private void checkForSticker(final CommentModel commentModel, final ItemViewHolder holder) {
        holder.imageViewWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commentClickHandler.onStickerClicked(commentModel);
            }
        });
        if (commentModel.hasSticker()) {
            if (commentModel.isAnimated()) {
                holder.imageView.setImageResource(0);
                holder.imageViewWrapper.setVisibility(View.GONE);
                holder.chatVideo.setVisibility(View.VISIBLE);
                holder.chatVideoWrapper.setVisibility(View.VISIBLE);


                HttpProxyCacheServer proxy = StartupApplication.getProxy(context);
                String proxyUrl = proxy.getProxyUrl(Constants.MAIN_URL + commentModel.getStickerUrl());
                holder.chatVideo.setVideoPath(proxyUrl);


                holder.chatVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        mediaPlayer.seekTo(1000);
                        holder.chatVideo.seekTo(1000);
                        holder.videoLoadingProgress.setVisibility(View.GONE);
                    }
                });
                holder.chatVideo.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        if (!holder.chatVideo.isPlaying()) {
                            holder.chatVideo.setBackground(null);
                            holder.chatVideo.start();
                        }
                        return false;
                    }
                });

            } else {
                holder.chatVideo.setVisibility(View.GONE);
                holder.chatVideoWrapper.setVisibility(View.GONE);
                holder.imageView.setImageResource(0);
                holder.imageViewWrapper.setVisibility(View.VISIBLE);

                Ion.with(context).load(Constants.MAIN_URL + commentModel.getStickerUrl()).withBitmap().placeholder(R.drawable.time_loading_vector).intoImageView(holder.imageView)
                        .setCallback(new FutureCallback<ImageView>() {
                            @Override
                            public void onCompleted(Exception e, ImageView result) {
                            }
                        });
            }
        } else {
            holder.chatVideo.setVisibility(View.GONE);
            holder.chatVideoWrapper.setVisibility(View.GONE);
            holder.imageView.setImageResource(0);
            holder.imageViewWrapper.setVisibility(View.GONE);
        }
    }
}
