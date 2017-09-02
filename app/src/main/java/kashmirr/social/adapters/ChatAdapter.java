package kashmirr.social.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kashmirr.social.R;

import java.util.LinkedList;
import java.util.List;

import kashmirr.social.interfaces.RecyclerItemClickListener;
import kashmirr.social.models.ChatModel;
import kashmirr.social.view_holders.ChatHeaderView;
import kashmirr.social.view_holders.ChatViewHolder;

import static kashmirr.social.utils.Constants.STATUS_DELIVERED;

/**
 * Created by USER on 2016-06-24.
 */
public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int HEADER_VIEW_TYPE = 2;
    private List<ChatModel> chatModelList;
    private Context mContext;
    private RecyclerItemClickListener onItemClickListener;
    private View headerView;

    public ChatAdapter() {
        chatModelList = new LinkedList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        if (viewType == HEADER_VIEW_TYPE) {
            headerView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_header_view, parent, false);
            return new ChatHeaderView(headerView);
        }
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_chat_item, parent, false);
        return new ChatViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (position != 0) {
            ChatModel chatModel = chatModelList.get(position - 1);
            ((ChatViewHolder) holder).initData(chatModel, mContext, position - 1, chatModelList.size() - 1, onItemClickListener);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return HEADER_VIEW_TYPE;
        }
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        if (chatModelList == null) {
            return 0;
        }
        if (chatModelList.size() == 0) {
            return 1;
        }

        return chatModelList.size() + 1;
    }

    public View getHeaderView() {
        View view = null;
        if (headerView != null) {
            view = headerView.findViewById(R.id.chatHeaderView);
        }

        return view != null ? view : new View(mContext);
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

    public void insertChatModelWithItemNotify(ChatModel chatModel, boolean firstPaging) {
        if (firstPaging) {
            this.chatModelList.add(chatModel);
            int currentIndex = this.chatModelList.indexOf(chatModel);
            notifyItemInserted(currentIndex);
        } else {
            this.chatModelList.add(0, chatModel);
        }
    }

    public void insertChatModelWithItemNotify(List<ChatModel> chatModel, boolean firstPaging) {
        if (firstPaging) {
            this.chatModelList.addAll(chatModel);
            notifyDataSetChanged();
        } else {
            this.chatModelList.addAll(0, chatModel);
            notifyItemRangeInserted(0, chatModel.size());
        }
    }

    public void removeItem(int position) {
        this.chatModelList.remove(position);
        notifyItemRemoved(position);
    }

    public void removeItem(ChatModel chatModel) {
        int position = this.chatModelList.indexOf(chatModel);
        this.chatModelList.remove(chatModel);
        int actualPosition = position + 1;
        notifyItemRemoved(actualPosition);
    }

    public List<ChatModel> getChatModelList() {
        return chatModelList;
    }

    public void setAllDelivered() {
        for (ChatModel chatModel : chatModelList) {
            chatModel.setDeliveryStatus(STATUS_DELIVERED);
            notifyDataSetChanged();
        }
    }
}
