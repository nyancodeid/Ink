package ink.va.view_holders;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ink.va.R;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import ink.va.interfaces.RecyclerItemClickListener;
import ink.va.models.ChatModel;
import ink.va.utils.Constants;
import ink.va.utils.Dp;
import ink.va.utils.FileUtils;
import ink.va.utils.Regex;
import ink.va.utils.SharedHelper;

/**
 * Created by PC-Comp on 1/31/2017.
 */

public class ChatViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.messageContainer)
    TextView message;
    @BindView(R.id.deliveryStatus)
    TextView deliveryStatus;
    @BindView(R.id.chatViewBubble)
    LinearLayout chatViewBubble;
    @BindView(R.id.gifChatView)
    ImageView imageView;
    @BindView(R.id.singleGifViewWrapper)
    LinearLayout imageViewWrapper;
    private SharedHelper sharedHelper;
    private boolean updating = false;
    private int percentage;
    private Context mContext;
    private RecyclerItemClickListener onItemClickListener;
    private int position;

    public ChatViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void initData(ChatModel chatModel, Context context, int position, int maxSize, RecyclerItemClickListener onItemClickListener) {
        mContext = context;
        this.position = position;
        this.onItemClickListener = onItemClickListener;
        if (sharedHelper == null) {
            sharedHelper = new SharedHelper(context);
        }
        String currentUserId = sharedHelper.getUserId();
        String messageBody = chatModel.getMessage();
        if (chatModel.getMessage().contains(":")) {
            int index = chatModel.getMessage().indexOf(":");
            messageBody = chatModel.getMessage().substring(index + 1, chatModel.getMessage().length());
        }


        message.setMovementMethod(LinkMovementMethod.getInstance());
        message.setText(messageBody.replaceAll(Constants.TYPE_MESSAGE_ATTACHMENT, ""));
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) chatViewBubble.getLayoutParams();
        LinearLayout.LayoutParams deliveryStatusParams = (LinearLayout.LayoutParams) deliveryStatus.getLayoutParams();
        LinearLayout.LayoutParams gifChatViewLayoutParams = (LinearLayout.LayoutParams) imageViewWrapper.getLayoutParams();


        if (currentUserId.equals(chatModel.getUserId())) {
            if (sharedHelper.getOwnTextColor() != null) {
                message.setTextColor(Color.parseColor(sharedHelper.getOwnTextColor()));
            }
            layoutParams.gravity = Gravity.RIGHT;
            layoutParams.rightMargin = Dp.toDps(context, 16);
            deliveryStatusParams.gravity = Gravity.RIGHT;
            gifChatViewLayoutParams.gravity = Gravity.RIGHT;

            chatViewBubble.setLayoutParams(layoutParams);
            imageView.setLayoutParams(gifChatViewLayoutParams);

            chatViewBubble.setBackground(ContextCompat.getDrawable(context, R.drawable.outgoing_message_bg));

            if (sharedHelper.getOwnBubbleColor() != null) {
                chatViewBubble.getBackground().setColorFilter(Color.parseColor(sharedHelper.getOwnBubbleColor()), PorterDuff.Mode.SRC_ATOP);
            }
            if (chatModel.getDeliveryStatus().equals(Constants.STATUS_DELIVERED)) {
                if (position >= maxSize) {
                    deliveryStatus.setVisibility(View.VISIBLE);
                } else {
                    deliveryStatus.setVisibility(View.INVISIBLE);
                }
                deliveryStatus.setText(context.getString(R.string.sentText));
            } else if (chatModel.getDeliveryStatus().equals(Constants.STATUS_NOT_DELIVERED)) {
                deliveryStatus.setVisibility(View.VISIBLE);
                if (updating) {
                    deliveryStatus.setText(context.getString(R.string.sendingNowText) + percentage + " %");
                } else {
                    deliveryStatus.setText(context.getString(R.string.sendingNowText));
                }

            }
        } else {
            chatViewBubble.setBackground(ContextCompat.getDrawable(context, R.drawable.incoming_message_bg));
            if (sharedHelper.getOpponentTextColor() != null) {
                message.setTextColor(Color.parseColor(sharedHelper.getOpponentTextColor()));
            }
            if (sharedHelper.getOpponentBubbleColor() != null) {
                chatViewBubble.getBackground().setColorFilter(Color.parseColor(sharedHelper.getOpponentBubbleColor()), PorterDuff.Mode.SRC_ATOP);
            }
            layoutParams.gravity = Gravity.LEFT;
            gifChatViewLayoutParams.gravity = Gravity.LEFT;

            chatViewBubble.setLayoutParams(layoutParams);
            imageView.setLayoutParams(gifChatViewLayoutParams);
            deliveryStatus.setVisibility(View.INVISIBLE);
        }
        checkForSticker(chatModel);

    }

    @OnClick(R.id.chatItemRootLayout)
    public void rootClicked() {
        onItemClickListener.onItemClicked(position, itemView);
    }

    @OnLongClick(R.id.chatItemRootLayout)
    public boolean longClicked() {
        onItemClickListener.onItemLongClick(position);
        return false;
    }

    private void checkForSticker(final ChatModel chatModel) {
        if (chatModel.hasSticker()) {
            imageView.setImageResource(0);
            imageViewWrapper.setVisibility(View.VISIBLE);

            Ion.with(mContext).load(Constants.MAIN_URL + chatModel.getStickerUrl()).withBitmap().placeholder(R.drawable.time_loading_vector).intoImageView(imageView)
                    .setCallback(new FutureCallback<ImageView>() {
                        @Override
                        public void onCompleted(Exception e, ImageView result) {
                        }
                    });

            if (chatModel.getMessage().trim().isEmpty()) {
                chatViewBubble.setVisibility(View.GONE);
            } else {
                chatViewBubble.setVisibility(View.VISIBLE);
            }
        } else if (Regex.isAttachment(chatModel.getMessage())) {

            if (FileUtils.isImageType(chatModel.getMessage())) {
                imageView.setImageResource(0);
                imageView.setBackgroundResource(R.drawable.time_loading_vector);
                imageViewWrapper.setVisibility(View.VISIBLE);

                String encoded = Uri.encode(chatModel.getMessage());
                Ion.with(mContext).load(Constants.MAIN_URL + Constants.UPLOADED_FILES_DIR + encoded).asBitmap().setCallback(new FutureCallback<Bitmap>() {
                    @Override
                    public void onCompleted(Exception e, Bitmap result) {
                        if (e == null) {
                            imageView.setImageResource(0);
                            imageView.setImageBitmap(result);
                        } else {
                            imageView.setBackgroundResource(R.drawable.chat_attachment_icon);
                            imageViewWrapper.setVisibility(View.VISIBLE);
                        }

                    }
                });
                imageViewWrapper.setVisibility(View.VISIBLE);
            } else {
                imageView.setImageResource(0);
                if (chatModel.getMessage().trim().isEmpty()) {
                    chatViewBubble.setVisibility(View.GONE);
                } else {
                    chatViewBubble.setVisibility(View.VISIBLE);
                }
                imageViewWrapper.setVisibility(View.GONE);
            }

        } else {
            imageView.setImageResource(0);
            if (chatModel.getMessage().trim().isEmpty()) {
                chatViewBubble.setVisibility(View.GONE);
            } else {
                chatViewBubble.setVisibility(View.VISIBLE);
            }
            imageViewWrapper.setVisibility(View.GONE);
        }
    }
}
