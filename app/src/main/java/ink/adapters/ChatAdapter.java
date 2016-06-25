package ink.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ink.R;

import java.util.List;

import ink.models.ChatModel;
import ink.utils.SharedHelper;

/**
 * Created by USER on 2016-06-24.
 */
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private List<ChatModel> chatModelList;
    private Context mContext;
    private String mCurrentUserId;

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView message;
        private LinearLayout chatViewBubble;

        public ViewHolder(View view) {
            super(view);
            message = (TextView) view.findViewById(R.id.messageContainer);
            chatViewBubble = (LinearLayout) view.findViewById(R.id.chatViewBubble);
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
    public void onBindViewHolder(ViewHolder holder, int position) {
        ChatModel chatModel = chatModelList.get(position);
        holder.message.setText(chatModel.getMessage());
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) holder.chatViewBubble.getLayoutParams();
        if (mCurrentUserId.equals(chatModel.getUserId())) {
            layoutParams.gravity = Gravity.RIGHT;
            holder.chatViewBubble.setLayoutParams(layoutParams);
            holder.chatViewBubble.setBackground(ContextCompat.getDrawable(mContext, R.drawable.outgoing_message_bg));
        } else {
            holder.chatViewBubble.setBackground(ContextCompat.getDrawable(mContext, R.drawable.incoming_message_bg));
            layoutParams.gravity = Gravity.LEFT;
            holder.chatViewBubble.setLayoutParams(layoutParams);
        }
    }


    @Override
    public int getItemCount() {
        return chatModelList.size();
    }
}
