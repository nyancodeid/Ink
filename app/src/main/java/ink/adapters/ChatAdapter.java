package ink.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.ink.R;

import java.util.List;

import ink.animations.RainbowAnimation;
import ink.models.ChatModel;
import ink.utils.Constants;
import ink.utils.Dp;
import ink.utils.Regex;
import ink.utils.SharedHelper;

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

        public ViewHolder(View view) {
            super(view);
            message = (TextView) view.findViewById(R.id.messageContainer);
            chatViewBubble = (LinearLayout) view.findViewById(R.id.chatViewBubble);
            imageViewWrapper = (LinearLayout) view.findViewById(R.id.singleGifViewWrapper);

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
    public void onBindViewHolder(final ViewHolder holder, int position) {
        ChatModel chatModel = chatModelList.get(position);

        System.gc();

        String messageBody = chatModel.getMessage().replaceAll("userid=" + chatModel.getUserId() + ":" + Constants.TYPE_MESSAGE_ATTACHMENT, "").replaceAll("userid=" + chatModel.getOpponentId() + ":" + Constants.TYPE_MESSAGE_ATTACHMENT, "");
        holder.message.setText(messageBody);
        if (showAsRainbow) {
            if (holder.message.getTag() == null) {
                RainbowAnimation.get().startRainbowAnimation(mContext, messageBody, holder.message);
                holder.message.setTag(ANIMATED);
            }
        } else {
            RainbowAnimation.get().stopRainbowAnimation();
            holder.message.setTag(null);
        }

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) holder.chatViewBubble.getLayoutParams();
        LinearLayout.LayoutParams deliveryStatusParams = (LinearLayout.LayoutParams) holder.deliveryStatus.getLayoutParams();
        LinearLayout.LayoutParams gifChatViewLayoutParams = (LinearLayout.LayoutParams) holder.imageViewWrapper.getLayoutParams();

        checkForGif(chatModel, holder);

        if (mCurrentUserId.equals(chatModel.getUserId())) {
            if (sharedHelper.getOwnTextColor() != null) {
                holder.message.setTextColor(Color.parseColor(sharedHelper.getOwnTextColor()));
            }
            layoutParams.gravity = Gravity.RIGHT;
            layoutParams.rightMargin = Dp.toDps(mContext, 16);
            deliveryStatusParams.gravity = Gravity.RIGHT;
            gifChatViewLayoutParams.gravity = Gravity.RIGHT;

            holder.chatViewBubble.setLayoutParams(layoutParams);
            holder.imageView.setLayoutParams(gifChatViewLayoutParams);

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

            holder.chatViewBubble.setLayoutParams(layoutParams);
            holder.imageView.setLayoutParams(gifChatViewLayoutParams);
            holder.deliveryStatus.setVisibility(View.INVISIBLE);
        }

    }

    private void checkForGif(final ChatModel chatModel, final ChatAdapter.ViewHolder holder) {
        System.gc();
        if (chatModel.hasGif()) {
            holder.imageView.setImageResource(0);
            holder.imageViewWrapper.setVisibility(View.VISIBLE);
            if (holder.imageView.getTag() == null) {
                Glide.with(mContext).load(Constants.MAIN_URL + Constants.ANIMATED_STICKERS_FOLDER + chatModel.getGifUrl()).asGif().listener(new RequestListener<String, GifDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GifDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GifDrawable resource, String model, Target<GifDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        holder.imageView.setTag(LOADED);
                        return false;
                    }
                }).placeholder(R.drawable.time_loading_vector).into(holder.imageView);
            } else if (!holder.imageView.getTag().equals(LOADED)) {
                Glide.with(mContext).load(Constants.MAIN_URL + Constants.ANIMATED_STICKERS_FOLDER + chatModel.getGifUrl()).asGif().listener(new RequestListener<String, GifDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GifDrawable> target, boolean isFirstResource) {
                        holder.imageView.setTag(LOADED);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GifDrawable resource, String model, Target<GifDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        return false;
                    }
                }).placeholder(R.drawable.time_loading_vector).into(holder.imageView);
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
            holder.imageView.setImageResource(0);
            holder.imageView.setBackgroundResource(R.drawable.chat_attachment_icon);
            holder.imageViewWrapper.setVisibility(View.VISIBLE);

        } else {
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
