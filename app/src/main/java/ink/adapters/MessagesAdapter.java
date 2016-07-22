package ink.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ink.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

import ink.models.UserMessagesModel;
import ink.utils.CircleTransform;
import ink.utils.Constants;
import ink.utils.Time;

/**
 * Created by USER on 2016-07-02.
 */
public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> {

    private List<UserMessagesModel> userMessagesModels;
    private Context mContext;
    private boolean shouldStartAnimation;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView messagesUserName, messageBody, messageDate;
        public ImageView messagesImage;

        public ViewHolder(View view) {
            super(view);
            messagesUserName = (TextView) view.findViewById(R.id.messagesUserName);
            messageDate = (TextView) view.findViewById(R.id.messageDate);
            messageBody = (TextView) view.findViewById(R.id.messageBody);
            messagesImage = (ImageView) view.findViewById(R.id.messagesImage);
        }
    }


    public MessagesAdapter(List<UserMessagesModel> friendsModelList, Context context) {
        mContext = context;
        this.userMessagesModels = friendsModelList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_messages_single_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        UserMessagesModel userMessagesModel = userMessagesModels.get(position);
        holder.messagesUserName.setText(userMessagesModel.getFirstName() + " " + userMessagesModel.getLastName());
        holder.messageBody.setText(userMessagesModel.getMessage());
        holder.messageDate.setText(userMessagesModel.getDate());
        if (!userMessagesModel.getImageName().isEmpty()) {
            String url = Constants.MAIN_URL + Constants.USER_IMAGES_FOLDER + userMessagesModel.getImageLink();
            Picasso.with(mContext).load(url)
                    .error(R.drawable.image_laoding_error)
                    .placeholder(R.drawable.no_image_yet_state).transform(new CircleTransform()).fit().centerCrop()
                    .into(holder.messagesImage, picassoCallback(url, holder.messagesImage));
        } else {
            Picasso.with(mContext).load(R.drawable.no_image)
                    .transform(new CircleTransform()).fit().centerCrop()
                    .into(holder.messagesImage);
        }
    }


    private com.squareup.picasso.Callback picassoCallback(final String link, final ImageView view) {
        com.squareup.picasso.Callback callback = new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {
                Picasso.with(mContext).load(link).transform(new CircleTransform()).into(view);
            }
        };
        return callback;
    }

    public void setShouldStartAnimation(boolean shouldStartAnimation) {
        this.shouldStartAnimation = shouldStartAnimation;
    }

    @Override
    public int getItemCount() {
        return userMessagesModels.size();
    }
}
