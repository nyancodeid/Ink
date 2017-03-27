package ink.va.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ink.va.R;

import java.util.ArrayList;

import ink.va.fragments.TrendModel;
import ink.va.view_holders.TrendViewHolder;

/**
 * Created by PC-Comp on 9/12/2016.
 */
public class TrendAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<TrendModel> trendModelArrayList;
    private Context context;

    public TrendAdapter(Context context, ArrayList<TrendModel> trendModelArrayList) {
        this.context = context;
        this.trendModelArrayList = trendModelArrayList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View trendSingleView = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_trend_view, parent, false);
        return new TrendViewHolder(trendSingleView);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final TrendModel trendModel = trendModelArrayList.get(position);
        ((TrendViewHolder) holder).initData(trendModel, position, getItemCount() - 1, context);
    }

    @Override
    public int getItemCount() {
        return trendModelArrayList.size();
    }

}
