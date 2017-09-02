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
import kashmirr.social.models.UserModel;
import kashmirr.social.view_holders.LikerViewHolder;
import lombok.Setter;

/**
 * Created by USER on 2017-04-12.
 */

public class LikerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    List<UserModel> userModels;
    private Context context;
    @Setter
    RecyclerItemClickListener onItemClickListener;

    public LikerAdapter() {
        userModels = new LinkedList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.liker_single_item, parent, false);
        return new LikerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((LikerViewHolder) holder).initData(userModels.get(position), context, onItemClickListener);
    }

    @Override
    public int getItemCount() {
        return userModels.size();
    }

    public void setUserModels(List<UserModel> userModels) {
        this.userModels.clear();
        this.userModels.addAll(userModels);
        notifyDataSetChanged();
    }
}
