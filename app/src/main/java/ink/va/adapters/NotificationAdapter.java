package ink.va.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ink.va.R;

import java.util.LinkedList;
import java.util.List;

import ink.va.interfaces.RecyclerItemClickListener;
import ink.va.models.UserNotificationModel;
import ink.va.view_holders.NotificationViewHolder;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by USER on 2017-03-23.
 */

public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    @Setter
    private RecyclerItemClickListener onItemClickListener;

    @Getter
    List<UserNotificationModel> userNotificationModels;

    public NotificationAdapter() {
        userNotificationModels = new LinkedList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_single_item, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((NotificationViewHolder) holder).setOnItemClickListener(onItemClickListener);
        ((NotificationViewHolder) holder).initData(userNotificationModels.get(position));
    }

    @Override
    public int getItemCount() {
        return userNotificationModels.size();
    }

    public void setUserNotificationModels(List<UserNotificationModel> userNotificationModels) {
        this.userNotificationModels.clear();
        this.userNotificationModels.addAll(userNotificationModels);
        notifyDataSetChanged();
    }

    public void removeItem(UserNotificationModel userNotificationModel) {
        int index = userNotificationModels.indexOf(userNotificationModel);
        userNotificationModels.remove(index);
        notifyItemRemoved(index);
    }

    public void clearItems() {
        userNotificationModels.clear();
        notifyDataSetChanged();
    }
}
