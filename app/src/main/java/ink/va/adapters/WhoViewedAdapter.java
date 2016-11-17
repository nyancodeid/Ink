package ink.va.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ink.va.R;

import java.util.List;

import ink.va.models.WhoViewedModel;
import ink.va.view_holders.WhoViewedViewHolder;

/**
 * Created by USER on 2016-11-18.
 */

public class WhoViewedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<WhoViewedModel> whoViewedModels;
    private OnItemClickListener onItemClickListener;

    public WhoViewedAdapter(Context context, List<WhoViewedModel> whoViewedModels) {
        this.context = context;
        this.whoViewedModels = whoViewedModels;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_who_viewed_view, parent, false);
        return new WhoViewedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((WhoViewedViewHolder) holder).init(context, whoViewedModels.get(position), onItemClickListener);
    }

    @Override
    public int getItemCount() {
        return whoViewedModels.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onMoreItemClicked(WhoViewedModel whoViewedModel, View view);

        void onCardItemClicked(WhoViewedModel whoViewedModel);
    }
}
