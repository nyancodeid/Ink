package ink.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.ink.R;

import java.util.List;

import ink.models.FriendsModel;

/**
 * Created by USER on 2016-06-22.
 */
public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> {

    private List<FriendsModel> friendsModelList;
    private Context mContext;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name, phoneNumber;

        public ViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.friendName);
            phoneNumber = (TextView) view.findViewById(R.id.friendPhoneNumber);
        }
    }


    public FriendsAdapter(List<FriendsModel> friendsModelList, Context context) {
        mContext = context;
        this.friendsModelList = friendsModelList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friend_single_view, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        FriendsModel friendsModel = friendsModelList.get(position);
        holder.name.setText(friendsModel.getFullName());
        holder.phoneNumber.setText(friendsModel.getPhoneNumber());
    }


    public void animate(RecyclerView.ViewHolder viewHolder) {
        final Animation animAnticipateOvershoot = AnimationUtils.loadAnimation(mContext, R.anim.bounce_interpolator);
        if (viewHolder.itemView.getTag() == null) {
            viewHolder.itemView.setAnimation(animAnticipateOvershoot);
            viewHolder.itemView.setTag("Animated");
        }
    }

    @Override
    public int getItemCount() {
        return friendsModelList.size();
    }
}
