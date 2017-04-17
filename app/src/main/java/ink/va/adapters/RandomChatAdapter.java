package ink.va.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ink.va.R;

import java.util.List;

import ink.va.models.RandomChatModel;
import ink.va.utils.Dp;
import ink.va.utils.SharedHelper;

/**
 * Created by USER on 2016-11-09.
 */

public class RandomChatAdapter extends RecyclerView.Adapter<RandomChatAdapter.ViewHolder> {

    private static final int MY_MESSAGE = 1;
    private static final int OPPONENT_MESSAGE = 2;
    private static final String LOADED = "LOADED";
    private static final int ANIMATED = 1;
    private static final int COLOR_APPLIED = 1;
    private List<RandomChatModel> chatModelList;
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


    public RandomChatAdapter(List<RandomChatModel> chatModels, Context context) {
        mContext = context;
        mCurrentUserId = new SharedHelper(mContext).getUserId();
        this.chatModelList = chatModels;
        sharedHelper = new SharedHelper(context);
        showAsRainbow = sharedHelper.isRainbowMessageActivated();
    }

    @Override
    public RandomChatAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_chat_item, parent, false);

        return new RandomChatAdapter.ViewHolder(itemView);
    }

    @Override
    public int getItemViewType(int position) {
        RandomChatModel item = chatModelList.get(position);
        if (mCurrentUserId.equals(item.isMine())) {
            return MY_MESSAGE;
        } else {
            return OPPONENT_MESSAGE;
        }
    }

    @Override
    public void onBindViewHolder(final RandomChatAdapter.ViewHolder holder, final int position) {
        RandomChatModel chatModel = chatModelList.get(position);
        String messageBody = chatModel.getMessage();
        if (chatModel.getMessage().contains(":")) {
            int index = chatModel.getMessage().indexOf(":");
            messageBody = chatModel.getMessage().substring(index + 1, chatModel.getMessage().length());
        }


        holder.message.setMovementMethod(LinkMovementMethod.getInstance());
        holder.message.setText(messageBody);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) holder.chatViewBubble.getLayoutParams();
        LinearLayout.LayoutParams deliveryStatusParams = (LinearLayout.LayoutParams) holder.deliveryStatus.getLayoutParams();
        LinearLayout.LayoutParams gifChatViewLayoutParams = (LinearLayout.LayoutParams) holder.imageViewWrapper.getLayoutParams();


        if (chatModel.isMine()) {
            if (sharedHelper.getOwnTextColor() != null) {
                holder.message.setTextColor(Color.parseColor(sharedHelper.getOwnTextColor()));
            }
            layoutParams.gravity = Gravity.RIGHT;
            layoutParams.rightMargin = Dp.toDps(mContext, 16);
            layoutParams.topMargin = Dp.toDps(mContext, 10);
            deliveryStatusParams.gravity = Gravity.RIGHT;
            gifChatViewLayoutParams.gravity = Gravity.RIGHT;

            holder.chatViewBubble.setLayoutParams(layoutParams);
            holder.imageView.setLayoutParams(gifChatViewLayoutParams);

            holder.chatViewBubble.setBackground(ContextCompat.getDrawable(mContext, R.drawable.outgoing_message_bg));

            if (sharedHelper.getOwnBubbleColor() != null) {
                holder.chatViewBubble.getBackground().setColorFilter(Color.parseColor(sharedHelper.getOwnBubbleColor()), PorterDuff.Mode.SRC_ATOP);
            }
        } else {
            holder.chatViewBubble.setBackground(ContextCompat.getDrawable(mContext, R.drawable.incoming_message_bg));
            if (sharedHelper.getOpponentTextColor() != null) {
                holder.message.setTextColor(Color.parseColor(sharedHelper.getOpponentTextColor()));
            }
            if (sharedHelper.getOpponentBubbleColor() != null) {
                holder.chatViewBubble.getBackground().setColorFilter(Color.parseColor(sharedHelper.getOpponentBubbleColor()), PorterDuff.Mode.SRC_ATOP);
            }
            layoutParams.leftMargin = Dp.toDps(mContext, 16);
            layoutParams.topMargin = Dp.toDps(mContext, 10);
            layoutParams.gravity = Gravity.LEFT;
            gifChatViewLayoutParams.gravity = Gravity.LEFT;

            holder.chatViewBubble.setLayoutParams(layoutParams);
            holder.imageView.setLayoutParams(gifChatViewLayoutParams);
            holder.deliveryStatus.setVisibility(View.INVISIBLE);
        }
        holder.imageView.setImageResource(0);
        if (chatModel.getMessage().trim().isEmpty()) {
            holder.chatViewBubble.setVisibility(View.GONE);
        } else {
            holder.chatViewBubble.setVisibility(View.VISIBLE);
        }
        holder.imageViewWrapper.setVisibility(View.GONE);
//        checkForSticker(chatModel, holder);

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
