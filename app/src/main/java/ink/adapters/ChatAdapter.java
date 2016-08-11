package ink.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ink.R;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.List;

import ink.models.ChatModel;
import ink.utils.Constants;
import ink.utils.Dp;
import ink.utils.SharedHelper;

/**
 * Created by USER on 2016-06-24.
 */
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private static final int MY_MESSAGE = 1;
    private static final int OPPONENT_MESSAGE = 2;
    private static final String LOADED = "LOADED";
    private List<ChatModel> chatModelList;
    private Context mContext;
    private String mCurrentUserId;

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView message;
        private TextView deliveryStatus;
        private LinearLayout chatViewBubble;
        private ImageView gifChatView;
        private LinearLayout singleGifViewWrapper;

        public ViewHolder(View view) {
            super(view);
            message = (TextView) view.findViewById(R.id.messageContainer);
            chatViewBubble = (LinearLayout) view.findViewById(R.id.chatViewBubble);
            singleGifViewWrapper = (LinearLayout) view.findViewById(R.id.singleGifViewWrapper);

            deliveryStatus = (TextView) view.findViewById(R.id.deliveryStatus);
            gifChatView = (ImageView) view.findViewById(R.id.gifChatView);
        }
    }


    public ChatAdapter(List<ChatModel> chatModels, Context context) {
        mContext = context;
        mCurrentUserId = new SharedHelper(mContext).getUserId();
        this.chatModelList = chatModels;
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
        holder.message.setText(chatModel.getMessage());
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) holder.chatViewBubble.getLayoutParams();
        LinearLayout.LayoutParams deliveryStatusParams = (LinearLayout.LayoutParams) holder.deliveryStatus.getLayoutParams();
        LinearLayout.LayoutParams gifChatViewLayoutParams = (LinearLayout.LayoutParams) holder.singleGifViewWrapper.getLayoutParams();

        checkForGif(chatModel, holder);
        if (mCurrentUserId.equals(chatModel.getUserId())) {
            layoutParams.gravity = Gravity.RIGHT;
            layoutParams.rightMargin = Dp.toDps(mContext, 16);
            deliveryStatusParams.gravity = Gravity.RIGHT;
            gifChatViewLayoutParams.gravity = Gravity.RIGHT;

            holder.chatViewBubble.setLayoutParams(layoutParams);
            holder.gifChatView.setLayoutParams(gifChatViewLayoutParams);

            holder.chatViewBubble.setBackground(ContextCompat.getDrawable(mContext, R.drawable.outgoing_message_bg));
            if (chatModel.getDeliveryStatus().equals(Constants.STATUS_DELIVERED)) {
                if (position >= chatModelList.size() - 1) {
                    holder.deliveryStatus.setVisibility(View.VISIBLE);
                } else {
                    holder.deliveryStatus.setVisibility(View.INVISIBLE);
                }
                holder.deliveryStatus.setText(mContext.getString(R.string.sentText));
            } else if (chatModel.getDeliveryStatus().equals(Constants.STATUS_NOT_DELIVERED)) {
                holder.deliveryStatus.setVisibility(View.VISIBLE);
                holder.deliveryStatus.setText(mContext.getString(R.string.sendingNowText));
            }
        } else {
            holder.chatViewBubble.setBackground(ContextCompat.getDrawable(mContext, R.drawable.incoming_message_bg));
            layoutParams.gravity = Gravity.LEFT;
            gifChatViewLayoutParams.gravity = Gravity.LEFT;

            holder.chatViewBubble.setLayoutParams(layoutParams);
            holder.gifChatView.setLayoutParams(gifChatViewLayoutParams);
            holder.deliveryStatus.setVisibility(View.INVISIBLE);
        }

    }

    private void checkForGif(final ChatModel chatModel, final ChatAdapter.ViewHolder holder) {
        System.gc();
        if (chatModel.hasGif()) {
            holder.singleGifViewWrapper.setVisibility(View.VISIBLE);
            if (holder.gifChatView.getTag() == null) {
                Ion.with(mContext).load(Constants.MAIN_URL + Constants.ANIMATED_STICKERS_FOLDER + chatModel.getGifUrl()).intoImageView(holder.gifChatView)
                        .setCallback(new FutureCallback<ImageView>() {
                            @Override
                            public void onCompleted(Exception e, ImageView result) {
                                holder.gifChatView.setTag(LOADED);
                            }
                        });
            } else if (!holder.gifChatView.getTag().equals(LOADED)) {
                Ion.with(mContext).load(Constants.MAIN_URL + Constants.ANIMATED_STICKERS_FOLDER + chatModel.getGifUrl()).intoImageView(holder.gifChatView)
                        .setCallback(new FutureCallback<ImageView>() {
                            @Override
                            public void onCompleted(Exception e, ImageView result) {
                                holder.gifChatView.setTag(LOADED);
                            }
                        });
            }
            if (chatModel.getMessage().trim().isEmpty()) {
                holder.chatViewBubble.setVisibility(View.GONE);
            } else {
                holder.chatViewBubble.setVisibility(View.VISIBLE);
            }
        } else {
            if (chatModel.getMessage().trim().isEmpty()) {
                holder.chatViewBubble.setVisibility(View.GONE);
            } else {
                holder.chatViewBubble.setVisibility(View.VISIBLE);
            }
            holder.singleGifViewWrapper.setVisibility(View.GONE);
        }
    }


    @Override
    public int getItemCount() {
        return chatModelList.size();
    }


}
