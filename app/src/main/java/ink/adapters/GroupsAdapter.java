package ink.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ink.R;

import java.util.List;

import ink.models.GroupsModel;

/**
 * Created by USER on 2016-07-06.
 */
public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.ViewHolder> {

    private List<GroupsModel> groupsModelList;
    private Context mContext;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView groupName, followersCount;
        public ImageView groupImage;

        public ViewHolder(View view) {
            super(view);
            groupName = (TextView) view.findViewById(R.id.groupName);
            followersCount = (TextView) view.findViewById(R.id.followersCount);
            groupImage = (ImageView) view.findViewById(R.id.groupImage);
        }
    }


    public GroupsAdapter(List<GroupsModel> friendsModelList, Context context) {
        mContext = context;
        this.groupsModelList = friendsModelList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friend_single_view, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        GroupsModel groupsModel = groupsModelList.get(position);
    }


    @Override
    public int getItemCount() {
        return groupsModelList.size();
    }
}

