package ink.va.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ink.va.R;

import java.util.LinkedList;
import java.util.List;

import ink.va.interfaces.VipGlobalChatClickListener;
import ink.va.models.VipGlobalChatModel;
import ink.va.view_holders.VipGlobalChatViewHolder;

/**
 * Created by PC-Comp on 1/11/2017.
 */

public class VipGlobalChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<VipGlobalChatModel> chatModels;
    private Context context;
    private VipGlobalChatClickListener vipGlobalChatClickListener;

    public VipGlobalChatAdapter(Context context) {
        chatModels = new LinkedList<>();
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.global_vip_chat_single_view, parent, false);
        return new VipGlobalChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((VipGlobalChatViewHolder) holder).initData(chatModels.get(position), context, vipGlobalChatClickListener,position,chatModels.size()-1);
    }

    @Override
    public int getItemCount() {
        return chatModels.size();
    }

    public void setChatModels(List<VipGlobalChatModel> chatModels) {
        this.chatModels.clear();
        this.chatModels.addAll(chatModels);
        notifyDataSetChanged();
    }

    public void setVipGlobalChatClickListener(VipGlobalChatClickListener vipGlobalChatClickListener) {
        this.vipGlobalChatClickListener = vipGlobalChatClickListener;
    }

    public void removeItem(VipGlobalChatModel item) {
        int itemPosition = chatModels.indexOf(item);
        chatModels.remove(item);
        notifyItemRemoved(itemPosition);
    }

    public void insertItem(VipGlobalChatModel item) {
        chatModels.add(item);
        int itemPosition = chatModels.indexOf(item);
        notifyItemInserted(itemPosition);
    }

    public void clear() {
        chatModels.clear();
        notifyDataSetChanged();
    }

    public boolean isListEmpty() {
        return chatModels.isEmpty();
    }
}
