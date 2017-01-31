package ink.va.view_holders;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ink.va.R;
import com.koushikdutta.ion.Ion;

import org.apache.commons.lang3.StringEscapeUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import ink.va.interfaces.MyMessagesItemClickListener;
import ink.va.models.UserMessagesModel;
import ink.va.utils.CircleTransform;
import ink.va.utils.Constants;
import ink.va.utils.SharedHelper;

/**
 * Created by PC-Comp on 1/31/2017.
 */

public class UserMessagesViewHolder extends RecyclerView.ViewHolder {
    @Bind(R.id.messagesUserName)
    TextView messagesUserName;
    @Bind(R.id.messageBody)
    TextView messageBody;
    @Bind(R.id.messagesImage)
    ImageView messagesImage;
    private MyMessagesItemClickListener onItemClickListener;
    private UserMessagesModel userMessagesModel;
    private SharedHelper sharedHelper;

    public UserMessagesViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void initData(UserMessagesModel userMessagesModel, Context context, @Nullable MyMessagesItemClickListener onItemClickListener) {
        if (sharedHelper == null) {
            sharedHelper = new SharedHelper(context);
        }
        this.onItemClickListener = onItemClickListener;
        messagesUserName.setText(userMessagesModel.getFirstName() + " " + userMessagesModel.getLastName());
        this.userMessagesModel = userMessagesModel;

        String message = userMessagesModel.getMessage();
        String finalMessage;

        if (userMessagesModel.getUserId().equals(sharedHelper.getUserId())) {
            if (userMessagesModel.getMessage().isEmpty()) {
                finalMessage = context.getString(R.string.you) + context.getString(R.string.sentSticker);
            } else {
                finalMessage = context.getString(R.string.you) + message.replaceAll(Constants.TYPE_MESSAGE_ATTACHMENT, "");
            }

        } else {
            if (userMessagesModel.getMessage().isEmpty()) {
                finalMessage = userMessagesModel.getFirstName() + " " + userMessagesModel.getLastName() + " : " + context.getString(R.string.sentSticker);
            } else {
                finalMessage = userMessagesModel.getFirstName() + " " + userMessagesModel.getLastName() + " : " + message.replaceAll(Constants.TYPE_MESSAGE_ATTACHMENT, "");
            }
        }

        messageBody.setText(StringEscapeUtils.unescapeJava(finalMessage));
        if (!userMessagesModel.getImageName().isEmpty()) {
            String encodedImage = Uri.encode(userMessagesModel.getImageName());

            String url = Constants.MAIN_URL + Constants.USER_IMAGES_FOLDER + encodedImage;
            if (userMessagesModel.isSocialAccount()) {
                url = userMessagesModel.getImageName();
            }
            Ion.with(context).load(url)
                    .withBitmap().placeholder(R.drawable.no_background_image).transform(new CircleTransform()).intoImageView(messagesImage);
        } else {
            Ion.with(context).load(Constants.ANDROID_DRAWABLE_DIR + "no_image").withBitmap()
                    .transform(new CircleTransform()).intoImageView(messagesImage);
        }

    }

    @OnClick(R.id.myMessagesRootLayout)
    public void rootClicked() {
        if (onItemClickListener != null) {
            onItemClickListener.onItemClick(userMessagesModel);
        }
    }

    @OnLongClick(R.id.myMessagesRootLayout)
    public boolean longClicked() {
        if (onItemClickListener != null) {
            onItemClickListener.onItemLongClick(userMessagesModel);
        }
        return false;
    }

}
