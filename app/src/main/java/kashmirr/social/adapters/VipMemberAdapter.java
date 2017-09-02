package kashmirr.social.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kashmirr.social.R;

import java.util.LinkedList;
import java.util.List;

import kashmirr.social.interfaces.VipMemberItemClickListener;
import kashmirr.social.models.UserModel;
import kashmirr.social.view_holders.VipMemberViewHolder;

/**
 * Created by PC-Comp on 1/9/2017.
 */

public class VipMemberAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<UserModel> userModels;
    private VipMemberItemClickListener itemClickListener;
    private Context context;

    public VipMemberAdapter(VipMemberItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
        userModels = new LinkedList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_vip_member_view, parent, false);
        return new VipMemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((VipMemberViewHolder) holder).initData(userModels.get(position), position,
                itemClickListener, context, userModels.size() - 1);
    }

    @Override
    public int getItemCount() {
        return userModels.size();
    }

    public void setUsers(List<UserModel> userModels) {
        this.userModels.clear();
        this.userModels.addAll(userModels);
        notifyDataSetChanged();
    }

    public void addSingleUser(UserModel userModel) {
        userModels.add(userModel);
        int insertedItemPosition = userModels.indexOf(userModel);
        notifyItemInserted(insertedItemPosition);
    }

    public void clear() {
        userModels.clear();
        notifyDataSetChanged();
    }
}
