package ink.va.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ink.va.R;

import java.util.LinkedList;
import java.util.List;

import ink.va.interfaces.RecyclerItemClickListener;
import ink.va.models.ChatModel;
import ink.va.view_holders.ChatViewHolder;

/**
 * Created by USER on 2016-06-24.
 */
public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ChatModel> chatModelList;
    private Context mContext;
    private RecyclerItemClickListener onItemClickListener;

    public ChatAdapter() {
        chatModelList = new LinkedList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_chat_item, parent, false);
        return new ChatViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        ChatModel chatModel = chatModelList.get(position);
        ((ChatViewHolder) holder).initData(chatModel, mContext, position, chatModelList.size() - 1, onItemClickListener);
    }


    @Override
    public int getItemCount() {
        return chatModelList.size();
    }


    public void setOnItemClickListener(RecyclerItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setChatModelList(List<ChatModel> chatModelList) {
        this.chatModelList.clear();
        this.chatModelList.addAll(chatModelList);
        notifyDataSetChanged();
    }

    public void insertChatModel(ChatModel chatModel) {
        this.chatModelList.add(chatModel);
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        this.chatModelList.remove(position);
        notifyItemRemoved(position);
    }

    public void removeItem(ChatModel chatModel) {
        int position = this.chatModelList.indexOf(chatModel);
        this.chatModelList.remove(chatModel);
        notifyItemRemoved(position);
    }

    public List<ChatModel> getChatModelList() {
        return chatModelList;
    }
}
