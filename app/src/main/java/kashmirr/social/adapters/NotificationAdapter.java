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
import kashmirr.social.models.UserNotificationModel;
import kashmirr.social.view_holders.NotificationViewHolder;
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
    private Context context;

    public NotificationAdapter() {
        userNotificationModels = new LinkedList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_single_item, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((NotificationViewHolder) holder).setOnItemClickListener(onItemClickListener);
        ((NotificationViewHolder) holder).initData(userNotificationModels.get(position),context);
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
