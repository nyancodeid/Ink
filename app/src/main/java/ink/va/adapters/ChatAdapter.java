package ink.va.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
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

import com.ink.va.R;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.List;

import ink.va.models.ChatModel;
import ink.va.utils.Constants;
import ink.va.utils.Dp;
import ink.va.utils.Regex;
import ink.va.utils.SharedHelper;

/**
 * Created by USER on 2016-06-24.
 */
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private static final int MY_MESSAGE = 1;
    private static final int OPPONENT_MESSAGE = 2;
    private static final String LOADED = "LOADED";
    private static final int ANIMATED = 1;
    private static final int COLOR_APPLIED = 1;
    private List<ChatModel> chatModelList;
    private Context mContext;
    private String mCurrentUserId;
    private int percentage;
    private boolean updating = false;
    private boolean showAsRainbow;
    private SharedHelper sharedHelper;

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView message;
        private TextView deliveryStatus;
        private LinearLayout chatViewBubble;
        private ImageView imageView;
        private LinearLayout imageViewWrapper;
        private RelativeLayout chatVideoWrapper;
        private VideoView chatVideo;
        private ProgressBar videoLoadingProgress;

        public ViewHolder(View view) {
            super(view);
            message = (TextView) view.findViewById(R.id.messageContainer);
            chatViewBubble = (LinearLayout) view.findViewById(R.id.chatViewBubble);
            chatVideoWrapper = (RelativeLayout) view.findViewById(R.id.chatVideoWrapper);
            chatVideo = (VideoView) view.findViewById(R.id.chatVideo);
            imageViewWrapper = (LinearLayout) view.findViewById(R.id.singleGifViewWrapper);
            videoLoadingProgress = (ProgressBar) view.findViewById(R.id.video_loading_progress);

            deliveryStatus = (TextView) view.findViewById(R.id.deliveryStatus);
            imageView = (ImageView) view.findViewById(R.id.gifChatView);
        }
    }


    public ChatAdapter(List<ChatModel> chatModels, Context context) {
        mContext = context;
        mCurrentUserId = new SharedHelper(mContext).getUserId();
        this.chatModelList = chatModels;
        sharedHelper = new SharedHelper(context);
        showAsRainbow = sharedHelper.isRainbowMessageActivated();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_chat_item, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public int getItemViewType(int position) {
        ChatModel item = chatModelList.get(position);
        if (mCurrentUserId.equals(item.getUserId())) {
            return MY_MESSAGE;
        } else {
            return OPPONENT_MESSAGE;
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        ChatModel chatModel = chatModelList.get(position);

        System.gc();

        String messageBody = chatModel.getMessage().replaceAll("userid=" + chatModel.getUserId() + ":" + Constants.TYPE_MESSAGE_ATTACHMENT, "").replaceAll("userid=" + chatModel.getOpponentId() + ":" + Constants.TYPE_MESSAGE_ATTACHMENT, "");
        holder.message.setText(messageBody);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) holder.chatViewBubble.getLayoutParams();
        LinearLayout.LayoutParams deliveryStatusParams = (LinearLayout.LayoutParams) holder.deliveryStatus.getLayoutParams();
        LinearLayout.LayoutParams gifChatViewLayoutParams = (LinearLayout.LayoutParams) holder.imageViewWrapper.getLayoutParams();
        LinearLayout.LayoutParams chatVideoLayoutParams = (LinearLayout.LayoutParams) holder.chatVideoWrapper.getLayoutParams();


        if (mCurrentUserId.equals(chatModel.getUserId())) {
            if (sharedHelper.getOwnTextColor() != null) {
                holder.message.setTextColor(Color.parseColor(sharedHelper.getOwnTextColor()));
            }
            layoutParams.gravity = Gravity.RIGHT;
            layoutParams.rightMargin = Dp.toDps(mContext, 16);
            deliveryStatusParams.gravity = Gravity.RIGHT;
            gifChatViewLayoutParams.gravity = Gravity.RIGHT;
            chatVideoLayoutParams.gravity = Gravity.RIGHT;

            holder.chatViewBubble.setLayoutParams(layoutParams);
            holder.imageView.setLayoutParams(gifChatViewLayoutParams);
            holder.chatVideoWrapper.setLayoutParams(chatVideoLayoutParams);

            holder.chatViewBubble.setBackground(ContextCompat.getDrawable(mContext, R.drawable.outgoing_message_bg));

            if (sharedHelper.getOwnBubbleColor() != null) {
                holder.chatViewBubble.getBackground().setColorFilter(Color.parseColor(sharedHelper.getOwnBubbleColor()), PorterDuff.Mode.SRC_ATOP);
            }
            if (chatModel.getDeliveryStatus().equals(Constants.STATUS_DELIVERED)) {
                if (position >= chatModelList.size() - 1) {
                    holder.deliveryStatus.setVisibility(View.VISIBLE);
                } else {
                    holder.deliveryStatus.setVisibility(View.INVISIBLE);
                }
                holder.deliveryStatus.setText(mContext.getString(R.string.sentText));
            } else if (chatModel.getDeliveryStatus().equals(Constants.STATUS_NOT_DELIVERED)) {
                holder.deliveryStatus.setVisibility(View.VISIBLE);
                if (updating) {
                    holder.deliveryStatus.setText(mContext.getString(R.string.sendingNowText) + percentage + " %");
                } else {
                    holder.deliveryStatus.setText(mContext.getString(R.string.sendingNowText));
                }

            }
        } else {
            holder.chatViewBubble.setBackground(ContextCompat.getDrawable(mContext, R.drawable.incoming_message_bg));
            if (sharedHelper.getOpponentTextColor() != null) {
                holder.message.setTextColor(Color.parseColor(sharedHelper.getOpponentTextColor()));
            }
            if (sharedHelper.getOpponentBubbleColor() != null) {
                holder.chatViewBubble.getBackground().setColorFilter(Color.parseColor(sharedHelper.getOpponentBubbleColor()), PorterDuff.Mode.SRC_ATOP);
            }
            layoutParams.gravity = Gravity.LEFT;
            gifChatViewLayoutParams.gravity = Gravity.LEFT;
            chatVideoLayoutParams.gravity = Gravity.LEFT;

            holder.chatViewBubble.setLayoutParams(layoutParams);
            holder.imageView.setLayoutParams(gifChatViewLayoutParams);
            holder.chatVideoWrapper.setLayoutParams(chatVideoLayoutParams);
            holder.deliveryStatus.setVisibility(View.INVISIBLE);
        }
        checkForSticker(chatModel, holder);

    }


    private void checkForSticker(final ChatModel chatModel, final ChatAdapter.ViewHolder holder) {
        System.gc();
        if (chatModel.hasSticker()) {
            if (chatModel.isAnimated()) {
                holder.imageView.setImageResource(0);
                holder.imageViewWrapper.setVisibility(View.GONE);
                holder.chatVideo.setVisibility(View.VISIBLE);
                holder.chatVideoWrapper.setVisibility(View.VISIBLE);
                Uri video = Uri.parse(Constants.MAIN_URL + chatModel.getStickerUrl());
                holder.chatVideo.setVideoURI(video);

                Bitmap thumb = ThumbnailUtils.createVideoThumbnail("http://www.joomlaworks.net/images/demos/galleries/abstract/7.jpg",
                        MediaStore.Images.Thumbnails.MINI_KIND);

                BitmapDrawable thumbAsDrawable = new BitmapDrawable(mContext.getResources(), thumb);
                holder.chatVideo.setBackground(thumbAsDrawable);

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
                if (holder.imageView.getTag() == null) {
                    Ion.with(mContext).load(Constants.MAIN_URL + chatModel.getStickerUrl()).withBitmap().placeholder(R.drawable.time_loading_vector).intoImageView(holder.imageView)
                            .setCallback(new FutureCallback<ImageView>() {
                                @Override
                                public void onCompleted(Exception e, ImageView result) {
                                    holder.imageView.setTag(LOADED);
                                }
                            });
                } else if (!holder.imageView.getTag().equals(LOADED)) {
                    Ion.with(mContext).load(Constants.MAIN_URL + chatModel.getStickerUrl()).withBitmap().placeholder(R.drawable.time_loading_vector).intoImageView(holder.imageView)
                            .setCallback(new FutureCallback<ImageView>() {
                                @Override
                                public void onCompleted(Exception e, ImageView result) {
                                    holder.imageView.setTag(LOADED);
                                }
                            });
                }
            }

            if (chatModel.getMessage().trim().isEmpty()) {
                holder.chatViewBubble.setVisibility(View.GONE);
            } else {
                holder.chatViewBubble.setVisibility(View.VISIBLE);
            }
        } else if (Regex.isAttachment(chatModel.getMessage())) {
//            if (FileUtils.isImageType(chatModel.getMessage())) {
//                holder.imageView.setImageResource(0);
//                Ion.with(mContext).load(Constants.MAIN_URL + Constants.UPLOADED_FILES_DIR + chatModel.getMessage()).withBitmap().placeholder(R.drawable.no_background_image).
//                        intoImageView(holder.imageView);
//                holder.imageViewWrapper.setVisibility(View.VISIBLE);
//            } else {
//
//            }
            holder.chatVideo.setVisibility(View.GONE);
            holder.chatVideoWrapper.setVisibility(View.GONE);
            holder.imageView.setImageResource(0);
            holder.imageView.setBackgroundResource(R.drawable.chat_attachment_icon);
            holder.imageViewWrapper.setVisibility(View.VISIBLE);

        } else {
            holder.chatVideo.setVisibility(View.GONE);
            holder.chatVideoWrapper.setVisibility(View.GONE);
            holder.imageView.setImageResource(0);
            if (chatModel.getMessage().trim().isEmpty()) {
                holder.chatViewBubble.setVisibility(View.GONE);
            } else {
                holder.chatViewBubble.setVisibility(View.VISIBLE);
            }
            holder.imageViewWrapper.setVisibility(View.GONE);
        }
    }


    @Override
    public int getItemCount() {
        return chatModelList.size();
    }

    public void setUpdate(int percentage) {
        this.percentage = percentage;
        updating = true;
    }

    public void stopUpdate() {
        updating = false;
    }

    public boolean isShowAsRainbow() {
        return sharedHelper.isRainbowMessageActivated();
    }

    public void setShowAsRainbow(boolean showAsRainbow) {
        this.showAsRainbow = showAsRainbow;
        sharedHelper.putRainbowMessageActivated(showAsRainbow);
    }
}
