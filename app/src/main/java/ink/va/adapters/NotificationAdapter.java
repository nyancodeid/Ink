package ink.va.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.LinkedList;
import java.util.List;

import ink.va.models.UserNotificationModel;

/**
 * Created by USER on 2017-03-23.
 */

public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    List<UserNotificationModel> userNotificationModels;

    public NotificationAdapter() {
        userNotificationModels = new LinkedList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public void setUserNotificationModels(List<UserNotificationModel> userNotificationModels) {
        this.userNotificationModels.clear();
        this.userNotificationModels.addAll(userNotificationModels);
    }

}
