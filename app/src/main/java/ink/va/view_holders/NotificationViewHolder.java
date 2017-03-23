package ink.va.view_holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.ink.va.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.va.interfaces.RecyclerItemClickListener;
import ink.va.models.UserNotificationModel;
import lombok.Setter;

/**
 * Created by USER on 2017-03-23.
 */

public class NotificationViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.notificationTitleTV)
    TextView notificationTitleTV;
    @BindView(R.id.notificationMessageTV)
    TextView notificationMessageTV;

    private UserNotificationModel userNotificationModel;

    @Setter
    private RecyclerItemClickListener onItemClickListener;

    public NotificationViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }


    public void initData(UserNotificationModel userNotificationModel) {
        this.userNotificationModel = userNotificationModel;
        notificationTitleTV.setText(userNotificationModel.getNotificationTitle());
        notificationMessageTV.setText(userNotificationModel.getNotificationText());
    }

    @OnClick(R.id.removeNotificationIV)
    public void removeNotificationIVClicked() {
        if (onItemClickListener != null) {
            onItemClickListener.onAdditionalItemClicked(userNotificationModel);
        }
    }

    @OnClick(R.id.notificationParentLayout)
    public void notificationParentLayoutClicked(){
        if (onItemClickListener != null) {
            onItemClickListener.onItemClicked(userNotificationModel);
        }
    }
}
