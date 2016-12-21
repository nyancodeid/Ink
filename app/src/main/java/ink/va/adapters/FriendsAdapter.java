package ink.va.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ink.va.R;

import java.util.List;

import ink.va.interfaces.RecyclerItemClickListener;
import ink.va.models.FriendsModel;
import ink.va.utils.Animations;
import ink.va.view_holders.FriendsViewHolder;

/**
 * Created by USER on 2016-06-22.
 */
public class FriendsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<FriendsModel> friendsModelList;
    private Context mContext;
    private RecyclerItemClickListener recyclerItemClickListener;
    private int lastPosition = -1;


    public FriendsAdapter(List<FriendsModel> friendsModelList, Context context) {
        mContext = context;
        this.friendsModelList = friendsModelList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friend_single_view, parent, false);
        return new FriendsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        FriendsModel friendsModel = friendsModelList.get(position);
        ((FriendsViewHolder) holder).init(mContext, friendsModel,
                position, recyclerItemClickListener, getItemCount());

        lastPosition = Animations.startRecyclerItemAnimation(mContext, ((FriendsViewHolder) holder).getViewToAnimate(),
                position, lastPosition, R.anim.slide_up_calm);
    }


    @Override
    public int getItemCount() {
        return friendsModelList.size();
    }

    public void setOnItemClickListener(RecyclerItemClickListener onItemClickListener) {
        this.recyclerItemClickListener = onItemClickListener;
    }

    @Override
    public void onViewDetachedFromWindow(final RecyclerView.ViewHolder holder) {
        ((FriendsViewHolder) holder).getViewToAnimate().clearAnimation();

    }
}
