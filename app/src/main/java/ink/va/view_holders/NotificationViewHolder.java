package ink.va.view_holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import butterknife.ButterKnife;
import ink.va.interfaces.RecyclerItemClickListener;
import ink.va.models.UserNotificationModel;
import lombok.Setter;

/**
 * Created by USER on 2017-03-23.
 */

public class NotificationViewHolder extends RecyclerView.ViewHolder {

    @Setter
    private RecyclerItemClickListener onItemClickListener;

    public NotificationViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }


    public void initData(UserNotificationModel userNotificationModel) {

    }
}
