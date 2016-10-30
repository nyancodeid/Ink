package ink.va.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ink.va.R;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.List;

import ink.va.interfaces.RecyclerItemClickListener;
import ink.va.models.GroupMessagesModel;
import ink.va.utils.CircleTransform;
import ink.va.utils.Constants;
import ink.va.utils.SharedHelper;

/**
 * Created by USER on 2016-07-10.
 */
public class GroupMessagesAdapter extends RecyclerView.Adapter<GroupMessagesAdapter.ViewHolder> {

    private List<GroupMessagesModel> groupMessagesModels;
    private Context mContext;
    private RecyclerItemClickListener onClickListener;
    private SharedHelper sharedHelper;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView messageSenderName, groupMessageBody;
        public ImageView messageSenderImage;
        public ImageView groupImageView;
        private ImageView groupMessageMoreIcon;
        private CardView groupMessageCard;

        public ViewHolder(View view) {
            super(view);
            messageSenderName = (TextView) view.findViewById(R.id.messageSenderName);
            groupMessageBody = (TextView) view.findViewById(R.id.groupMessageBody);
            groupMessageCard = (CardView) view.findViewById(R.id.groupMessageCard);
            messageSenderImage = (ImageView) view.findViewById(R.id.messageSenderImage);
            groupImageView = (ImageView) view.findViewById(R.id.group_image);
            groupMessageMoreIcon = (ImageView) view.findViewById(R.id.groupMessageMoreIcon);
        }
    }


    public GroupMessagesAdapter(List<GroupMessagesModel> groupMessagesModels, Context context) {
        mContext = context;
        this.groupMessagesModels = groupMessagesModels;
        sharedHelper = new SharedHelper(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_group_message_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        GroupMessagesModel groupMessagesModel = groupMessagesModels.get(position);

        if (groupMessagesModel.getFileName().isEmpty()) {
            holder.groupImageView.setVisibility(View.GONE);
        } else {
            holder.groupImageView.setVisibility(View.VISIBLE);
            String encodedImage = Uri.encode(groupMessagesModel.getFileName());
            Ion.with(mContext).load(Constants.MAIN_URL + Constants.UPLOADED_FILES_DIR + encodedImage).asBitmap().setCallback(new FutureCallback<Bitmap>() {
                @Override
                public void onCompleted(Exception e, Bitmap result) {
                    if (e == null) {
                        holder.groupImageView.setImageBitmap(result);
                    } else {
                        holder.groupImageView.setVisibility(View.GONE);
                    }
                }
            });
        }
        if (!groupMessagesModel.getSenderImage().isEmpty()) {
            String encodedImage = Uri.encode(groupMessagesModel.getSenderImage());
            Ion.with(mContext).load(Constants.MAIN_URL + Constants.USER_IMAGES_FOLDER +
                    encodedImage).withBitmap().transform(new CircleTransform()).intoImageView(holder.messageSenderImage);
        } else {
            Ion.with(mContext).load(Constants.ANDROID_DRAWABLE_DIR + "no_image").withBitmap().transform(new CircleTransform()).intoImageView(holder.messageSenderImage);
        }
        holder.groupMessageBody.setText(groupMessagesModel.getGroupMessage());
        holder.messageSenderName.setText(groupMessagesModel.getSenderName());
        holder.groupMessageMoreIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onClickListener != null) {
                    onClickListener.onAdditionItemClick(position, holder.groupMessageMoreIcon);
                }
            }
        });
        if (sharedHelper.getUserId().equals(groupMessagesModel.getSenderId())) {
            holder.groupMessageMoreIcon.setVisibility(View.VISIBLE);
        } else {
            holder.groupMessageMoreIcon.setVisibility(View.GONE);
        }
        holder.groupMessageCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onClickListener != null) {
                    onClickListener.onItemClicked(position, holder.messageSenderImage);
                }
            }
        });

    }


    @Override
    public int getItemCount() {
        return groupMessagesModels.size();
    }

    public void setOnClickListener(RecyclerItemClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
}

