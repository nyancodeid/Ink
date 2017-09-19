package kashmirr.social.view_holders;

import android.content.Context;
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

import com.kashmirr.social.R;

import java.io.File;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import kashmirr.social.interfaces.RecyclerItemClickListener;
import kashmirr.social.models.ChatModel;
import kashmirr.social.utils.Constants;
import kashmirr.social.utils.Dp;
import kashmirr.social.utils.FileUtils;
import kashmirr.social.utils.ImageLoader;
import kashmirr.social.utils.Regex;
import kashmirr.social.utils.SharedHelper;

import static kashmirr.social.utils.Constants.STATUS_DELIVERED;
import static kashmirr.social.utils.Constants.STATUS_NOT_DELIVERED;


/**
 * Created by PC-Comp on 1/31/2017.
 */

public class ChatViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.messageContainer)
    TextView message;
    @BindView(R.id.deliveryStatus)
    TextView deliveryStatus;
    @BindView(R.id.dateTV)
    TextView dateTV;
    @BindView(R.id.chatViewBubble)
    LinearLayout chatViewBubble;
    @BindView(R.id.gifChatView)
    ImageView imageView;
    @BindView(R.id.singleGifViewWrapper)
    LinearLayout imageViewWrapper;
    @BindView(R.id.chatAttachmentWrapper)
    LinearLayout chatAttachmentWrapper;
    @BindView(R.id.attachmentNameTV)
    TextView attachmentNameTV;
    @BindView(R.id.downloadAttachmentIV)
    ImageView downloadAttachmentIV;
    @BindView(R.id.attachmentIV)
    ImageView attachmentIV;

    private SharedHelper sharedHelper;
    private Context mContext;
    private RecyclerItemClickListener onItemClickListener;
    private int position;
    private ChatModel chatModel;
    private boolean isDateVisible;

    public ChatViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void initData(ChatModel chatModel, Context context, int position, int maxSize, RecyclerItemClickListener onItemClickListener) {
        mContext = context;
        this.chatModel = chatModel;
        this.position = position;
        this.onItemClickListener = onItemClickListener;
        if (sharedHelper == null) {
            sharedHelper = new SharedHelper(context);
        }
        String currentUserId = sharedHelper.getUserId();
        String messageBody = chatModel.getMessage();

        message.setMovementMethod(LinkMovementMethod.getInstance());
        message.setText(messageBody);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) chatViewBubble.getLayoutParams();
        LinearLayout.LayoutParams deliveryStatusParams = (LinearLayout.LayoutParams) deliveryStatus.getLayoutParams();
        LinearLayout.LayoutParams dateTVParams = (LinearLayout.LayoutParams) dateTV.getLayoutParams();
        LinearLayout.LayoutParams gifChatViewLayoutParams = (LinearLayout.LayoutParams) imageViewWrapper.getLayoutParams();
        LinearLayout.LayoutParams chatAttachmentWrapperParams = (LinearLayout.LayoutParams) chatAttachmentWrapper.getLayoutParams();
        LinearLayout.LayoutParams attachmentIVParams = (LinearLayout.LayoutParams) attachmentIV.getLayoutParams();
        LinearLayout.LayoutParams downloadAttachmentIVParams = (LinearLayout.LayoutParams) downloadAttachmentIV.getLayoutParams();


        if (currentUserId.equals(chatModel.getUserId())) {
            if (sharedHelper.getOwnTextColor() != null) {
                message.setTextColor(Color.parseColor(sharedHelper.getOwnTextColor()));
            }
            checkForFile(chatModel, true);

            layoutParams.gravity = Gravity.RIGHT;
            layoutParams.rightMargin = Dp.toDps(context, 16);
            deliveryStatusParams.gravity = Gravity.RIGHT;
            gifChatViewLayoutParams.gravity = Gravity.RIGHT;
            chatAttachmentWrapperParams.gravity = Gravity.RIGHT;
            attachmentIVParams.gravity = Gravity.RIGHT;
            downloadAttachmentIVParams.gravity = Gravity.RIGHT;
            dateTVParams.gravity = Gravity.RIGHT;
            dateTVParams.rightMargin = Dp.toDps(context, 15);

            chatViewBubble.setLayoutParams(layoutParams);
            imageView.setLayoutParams(gifChatViewLayoutParams);
            chatAttachmentWrapper.setLayoutParams(chatAttachmentWrapperParams);
            attachmentIV.setLayoutParams(attachmentIVParams);
            downloadAttachmentIV.setLayoutParams(downloadAttachmentIVParams);
            dateTV.setLayoutParams(dateTVParams);

            chatViewBubble.setBackground(ContextCompat.getDrawable(context, R.drawable.outgoing_message_bg));

            if (sharedHelper.getOwnBubbleColor() != null) {
                chatViewBubble.getBackground().setColorFilter(Color.parseColor(sharedHelper.getOwnBubbleColor()), PorterDuff.Mode.SRC_ATOP);
            }

            checkDelivery(context, maxSize);
        } else {
            checkForFile(chatModel, false);
            chatViewBubble.setBackground(ContextCompat.getDrawable(context, R.drawable.incoming_message_bg));
            if (sharedHelper.getOpponentTextColor() != null) {
                message.setTextColor(Color.parseColor(sharedHelper.getOpponentTextColor()));
            }
            if (sharedHelper.getOpponentBubbleColor() != null) {
                chatViewBubble.getBackground().setColorFilter(Color.parseColor(sharedHelper.getOpponentBubbleColor()), PorterDuff.Mode.SRC_ATOP);
            }
            layoutParams.gravity = Gravity.LEFT;
            gifChatViewLayoutParams.gravity = Gravity.LEFT;
            chatAttachmentWrapperParams.gravity = Gravity.LEFT;
            dateTVParams.gravity = Gravity.LEFT;
            attachmentIVParams.gravity = Gravity.LEFT;
            downloadAttachmentIVParams.gravity = Gravity.LEFT;

            chatViewBubble.setLayoutParams(layoutParams);
            dateTV.setLayoutParams(dateTVParams);
            imageView.setLayoutParams(gifChatViewLayoutParams);
            chatAttachmentWrapper.setLayoutParams(chatAttachmentWrapperParams);
            attachmentIV.setLayoutParams(attachmentIVParams);
            downloadAttachmentIV.setLayoutParams(downloadAttachmentIVParams);
            deliveryStatus.setVisibility(View.INVISIBLE);
        }
        Date date = new Date();
        String finalDate;
        try {
            date.setTime(Long.valueOf(chatModel.getDate()));
            finalDate = date.toString();
        } catch (NumberFormatException e) {
            finalDate = chatModel.getDate();
        }

        dateTV.setVisibility(View.GONE);
        isDateVisible = false;
        dateTV.setText(finalDate);
        checkForSticker(chatModel);
    }

    private void checkForFile(ChatModel chatModel, boolean me) {
        String filePath = chatModel.getFilePath();
        File file = new File(filePath);
        String filenameArray[] = file.getName().split("\\.");
        String extension = filenameArray[filenameArray.length - 1];
        if (filePath == null) {
            filePath = "";
        }

        if (extension.equals("jpg") || extension.equals("png") || extension.equals("jpeg")) {
            attachmentIV.setImageResource(R.drawable.image_vector);
        } else if (extension.equals("pdf")) {
            attachmentIV.setImageResource(R.drawable.pdf_vector);
        } else if (extension.equals("apk")) {
            attachmentIV.setImageResource(R.drawable.android_icon);
        } else {
            attachmentIV.setImageResource(R.drawable.attachment_icon);
        }

        if (me) {
            if (filePath != null && !filePath.isEmpty()) {
                downloadAttachmentIV.setVisibility(View.GONE);
                chatAttachmentWrapper.setVisibility(View.VISIBLE);
                attachmentNameTV.setText(file.getName());
            } else {
                chatAttachmentWrapper.setVisibility(View.GONE);
            }
        } else {
            if (filePath != null && !filePath.isEmpty()) {
                downloadAttachmentIV.setVisibility(View.VISIBLE);
                chatAttachmentWrapper.setVisibility(View.VISIBLE);
                attachmentNameTV.setText(file.getName());
            } else {
                chatAttachmentWrapper.setVisibility(View.GONE);
            }
        }
    }

    private void checkDelivery(Context context, int maxSize) {
        switch (chatModel.getDeliveryStatus()) {
            case STATUS_DELIVERED:
                deliveryStatus.setText(context.getString(R.string.delivered));
                break;
            case STATUS_NOT_DELIVERED:
                deliveryStatus.setText(context.getString(R.string.not_delivered_yet));
                break;
        }
        if (position == maxSize) {
            deliveryStatus.setVisibility(View.VISIBLE);
        } else {
            deliveryStatus.setVisibility(View.INVISIBLE);
        }
    }


    @OnClick(R.id.chatViewBubble)
    public void chatClicked() {
        rootClicked();
    }

    @OnClick(R.id.downloadAttachmentIV)
    public void downloadAttachmentIVClicked() {
        if (onItemClickListener != null) {
            onItemClickListener.onAdditionalItemClicked(chatModel);
        }
    }

    @OnLongClick(R.id.chatViewBubble)
    public boolean chatLongClicked() {
        longClicked();
        return false;
    }

    @OnClick(R.id.messageContainer)
    public void containerClicked() {
        rootClicked();
    }

    @OnLongClick(R.id.messageContainer)
    public boolean containerLongClicked() {
        longClicked();
        return false;
    }

    @OnClick(R.id.chatItemRootLayout)
    public void rootClicked() {
        if (isDateVisible) {
            isDateVisible = false;
            dateTV.setVisibility(View.GONE);
        } else {
            isDateVisible = true;
            dateTV.setVisibility(View.VISIBLE);
        }
        onItemClickListener.onItemClicked(position, itemView);
    }

    @OnLongClick(R.id.chatItemRootLayout)
    public boolean longClicked() {
        onItemClickListener.onItemLongClick(chatModel);
        return false;
    }

    private void checkForSticker(final ChatModel chatModel) {
        if (chatModel.isStickerChosen()) {
            imageView.setImageResource(0);
            imageViewWrapper.setVisibility(View.VISIBLE);

            ImageLoader.loadImage(mContext, false, false, Constants.MAIN_URL + chatModel.getStickerUrl(), 0, R.drawable.time_loading_vector, imageView, null);

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

                ImageLoader.loadImage(mContext, false, false, Constants.MAIN_URL + Constants.UPLOADED_FILES_DIR + encoded, 0, R.drawable.chat_attachment_icon, imageView, new ImageLoader.ImageLoadedCallback() {
                    @Override
                    public void onImageLoaded(Object result, Exception e) {
                        if (e != null) {
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
