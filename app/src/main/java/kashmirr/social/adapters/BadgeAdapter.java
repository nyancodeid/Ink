package kashmirr.social.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kashmirr.social.R;

import java.util.LinkedList;
import java.util.List;

import kashmirr.social.interfaces.BadgeClickListener;
import kashmirr.social.models.BadgeModel;
import kashmirr.social.view_holders.BadgeViewHolder;

/**
 * Created by PC-Comp on 1/31/2017.
 */

public class BadgeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private BadgeClickListener onClickListener;
    private Context context;
    private List<BadgeModel> badgeModels;

    public BadgeAdapter(Context context) {
        this.context = context;
        badgeModels = new LinkedList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_badge_item, parent, false);
        return new BadgeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((BadgeViewHolder) holder).initData(context, badgeModels.get(position), onClickListener);
    }

    @Override
    public int getItemCount() {
        return badgeModels.size();
    }

    public void setOnClickListener(BadgeClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setBadgeModels(List<BadgeModel> badgeModels) {
        this.badgeModels.clear();
        this.badgeModels.addAll(badgeModels);
        notifyDataSetChanged();
    }

    public void clear() {
        badgeModels.clear();
        notifyDataSetChanged();
    }
}
