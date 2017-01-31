package ink.va.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ink.va.R;

import java.util.List;

import ink.va.models.ChatModel;
import ink.va.view_holders.ChatViewHolder;

/**
 * Created by USER on 2016-06-24.
 */
public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ChatModel> chatModelList;
    private Context mContext;

    public ChatAdapter(List<ChatModel> chatModelList, Context mContext) {
        this.chatModelList = chatModelList;
        this.mContext = mContext;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_chat_item, parent, false);
        return new ChatViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        ChatModel chatModel = chatModelList.get(position);
        ((ChatViewHolder) holder).initData(chatModel, mContext, position, chatModelList.size() - 1);
    }


    @Override
    public int getItemCount() {
        return chatModelList.size();
    }

    public void setUpdate(int percentage) {

    }

    public void stopUpdate() {

    }


}
