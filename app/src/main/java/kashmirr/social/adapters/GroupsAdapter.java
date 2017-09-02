package kashmirr.social.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kashmirr.social.R;

import java.util.List;

import kashmirr.social.models.GroupsModel;
import kashmirr.social.view_holders.GroupsViewHolder;


/**
 * Created by USER on 2016-07-06.
 */

public class GroupsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<GroupsModel> groupsModelList;
    private Context mContext;


    public GroupsAdapter(List<GroupsModel> friendsModelList, Context context) {
        mContext = context;
        this.groupsModelList = friendsModelList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_group_item, parent, false);
        return new GroupsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        GroupsModel groupsModel = groupsModelList.get(position);
        ((GroupsViewHolder) holder).initData(groupsModel, mContext);
    }


    @Override
    public int getItemCount() {
        return groupsModelList.size();
    }


}

