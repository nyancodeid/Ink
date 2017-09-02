package kashmirr.social.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kashmirr.social.R;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import kashmirr.social.interfaces.MyMessagesItemClickListener;
import kashmirr.social.models.UserMessagesModel;
import kashmirr.social.utils.SharedHelper;
import kashmirr.social.view_holders.UserMessagesViewHolder;
import lombok.Getter;

/**
 * Created by USER on 2016-07-02.
 */
public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    @Getter
    private List<UserMessagesModel> userMessagesModels;
    private Context mContext;
    private SharedHelper mSharedHelper;
    private MyMessagesItemClickListener onItemClickListener;

    public MessagesAdapter(Context context) {
        mContext = context;
        userMessagesModels = new LinkedList<>();
        mSharedHelper = new SharedHelper(context);
    }

    public void setUserMessagesModels(List<UserMessagesModel> userMessagesModels) {
        Collections.sort(userMessagesModels);
        this.userMessagesModels.clear();
        this.userMessagesModels.addAll(userMessagesModels);
        notifyDataSetChanged();
    }

    public void clear() {
        this.userMessagesModels.clear();
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(MyMessagesItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_messages_single_item, parent, false);
        return new UserMessagesViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((UserMessagesViewHolder) holder).initData(userMessagesModels.get(position), mContext, onItemClickListener);
    }

    @Override
    public int getItemCount() {
        return userMessagesModels.size();
    }

}
