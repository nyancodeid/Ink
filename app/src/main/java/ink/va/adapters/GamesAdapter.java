package ink.va.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ink.va.R;

import java.util.LinkedList;
import java.util.List;

import ink.va.interfaces.ItemClickListener;
import ink.va.models.GameModel;
import ink.va.view_holders.GameViewHolder;

/**
 * Created by PC-Comp on 2/1/2017.
 */

public class GamesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    List<GameModel> gameModelList;
    private ItemClickListener onItemClickListener;
    private Context context;

    public GamesAdapter() {
        gameModelList = new LinkedList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.games_single_view, parent, false);
        return new GameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((GameViewHolder) holder).initData(gameModelList.get(position), context, onItemClickListener);
    }

    @Override
    public int getItemCount() {
        return gameModelList.size();
    }

    public void setGameModelList(List<GameModel> gameModelList) {
        this.gameModelList.clear();
        this.gameModelList.addAll(gameModelList);
        notifyDataSetChanged();
    }

    public void clear() {
        gameModelList.clear();
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(ItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
