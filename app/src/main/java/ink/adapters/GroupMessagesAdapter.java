package ink.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ink.R;
import com.koushikdutta.ion.Ion;

import java.util.List;

import ink.interfaces.RecyclerItemClickListener;
import ink.models.GroupMessagesModel;
import ink.utils.CircleTransform;
import ink.utils.Constants;
import ink.utils.SharedHelper;

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
        private ImageView groupMessageMoreIcon;
        private CardView groupMessageCard;

        public ViewHolder(View view) {
            super(view);
            messageSenderName = (TextView) view.findViewById(R.id.messageSenderName);
            groupMessageBody = (TextView) view.findViewById(R.id.groupMessageBody);
            groupMessageCard = (CardView) view.findViewById(R.id.groupMessageCard);
            messageSenderImage = (ImageView) view.findViewById(R.id.messageSenderImage);
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

        if (!groupMessagesModel.getSenderImage().isEmpty()) {
            Ion.with(mContext).load(Constants.MAIN_URL + Constants.USER_IMAGES_FOLDER +
                    groupMessagesModel.getSenderImage()).withBitmap().transform(new CircleTransform()).intoImageView(holder.messageSenderImage);
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

