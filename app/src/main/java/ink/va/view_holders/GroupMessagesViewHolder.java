package ink.va.view_holders;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ink.va.R;
import com.koushikdutta.ion.Ion;

import ink.va.interfaces.RecyclerItemClickListener;
import ink.va.models.GroupMessagesModel;
import ink.va.utils.CircleTransform;
import ink.va.utils.Constants;
import ink.va.utils.SharedHelper;

/**
 * Created by PC-Comp on 12/20/2016.
 */

public class GroupMessagesViewHolder extends RecyclerView.ViewHolder {

    private Context context;
    public TextView messageSenderName, groupMessageBody;
    public ImageView messageSenderImage;
    public ImageView groupImageView;
    private ImageView groupMessageMoreIcon;
    private RelativeLayout groupMessageCard;
    private SharedHelper sharedHelper;
    private View rootLayout;

    public GroupMessagesViewHolder(View view) {
        super(view);
        messageSenderName = (TextView) view.findViewById(R.id.messageSenderName);
        rootLayout = view.findViewById(R.id.rootLayout);
        groupMessageBody = (TextView) view.findViewById(R.id.groupMessageBody);
        groupMessageCard = (RelativeLayout) view.findViewById(R.id.groupMessageCard);
        messageSenderImage = (ImageView) view.findViewById(R.id.messageSenderImage);
        groupImageView = (ImageView) view.findViewById(R.id.group_image);
        groupMessageMoreIcon = (ImageView) view.findViewById(R.id.groupMessageMoreIcon);
    }


    public void initData(Context context, GroupMessagesModel groupMessagesModel,
                         final RecyclerItemClickListener onClickListener, final int position) {
        this.context = context;
        sharedHelper = new SharedHelper(context);
        messageSenderImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onClickListener != null) {
                    onClickListener.onItemClicked(position);
                }
            }
        });
        if (groupMessagesModel.getFileName().isEmpty()) {
            groupImageView.setVisibility(View.GONE);
        } else {
            groupImageView.setVisibility(View.VISIBLE);
            String encodedImage = Uri.encode(groupMessagesModel.getFileName());
            if (encodedImage.isEmpty()) {
                groupImageView.setVisibility(View.GONE);
            } else {
                Ion.with(context).load(Constants.MAIN_URL + Constants.UPLOADED_FILES_DIR + encodedImage).withBitmap().placeholder(R.drawable.big_image_place_holder).intoImageView(groupImageView);
            }

        }
        if (!groupMessagesModel.getSenderImage().isEmpty()) {
            String encodedImage = Uri.encode(groupMessagesModel.getSenderImage());

            Ion.with(context).load(Constants.MAIN_URL + Constants.USER_IMAGES_FOLDER +
                    encodedImage).withBitmap().placeholder(R.drawable.user_image_placeholder).transform(new CircleTransform()).intoImageView(messageSenderImage);
        } else {
            Ion.with(context).load(Constants.ANDROID_DRAWABLE_DIR + "no_image").withBitmap().transform(new CircleTransform()).intoImageView(messageSenderImage);
        }

        groupMessageBody.setMovementMethod(LinkMovementMethod.getInstance());
        if (groupMessagesModel.getGroupMessage().isEmpty()) {
            groupMessageBody.setVisibility(View.GONE);
        } else {
            groupMessageBody.setVisibility(View.VISIBLE);
        }
        groupMessageBody.setText(groupMessagesModel.getGroupMessage());
        messageSenderName.setText(groupMessagesModel.getSenderName());
        groupMessageMoreIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onClickListener != null) {
                    onClickListener.onAdditionItemClick(position, groupMessageMoreIcon);
                }
            }
        });
        if (sharedHelper.getUserId().equals(groupMessagesModel.getSenderId())) {
            groupMessageMoreIcon.setVisibility(View.VISIBLE);
        } else {
            groupMessageMoreIcon.setVisibility(View.GONE);
        }
        groupMessageCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onClickListener != null) {
                    onClickListener.onItemClicked(position, messageSenderImage);
                }
            }
        });
    }


    public View getViewToAnimate() {
        return rootLayout;
    }
}
