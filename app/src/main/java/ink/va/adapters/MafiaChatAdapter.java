package ink.va.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ink.va.R;

import java.util.List;

import ink.va.models.MafiaMessageModel;
import ink.va.view_holders.MafiaChatViewHolder;

/**
 * Created by PC-Comp on 3/13/2017.
 */

public class MafiaChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<MafiaMessageModel> mafiaMessageModels;
    private Context context;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mafia_chat_single_item, parent, false);
        return new MafiaChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((MafiaChatViewHolder) holder).initData(mafiaMessageModels.get(position),context);
    }

    @Override
    public int getItemCount() {
        return mafiaMessageModels.size();
    }

    public void setMafiaMessageModels(List<MafiaMessageModel> mafiaMessageModels) {
        this.mafiaMessageModels.clear();
        this.mafiaMessageModels.addAll(mafiaMessageModels);
        notifyDataSetChanged();
    }
}
