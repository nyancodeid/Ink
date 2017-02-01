package ink.va.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.LinkedList;
import java.util.List;

import ink.va.interfaces.ItemClickListener;
import ink.va.models.GameModel;

/**
 * Created by PC-Comp on 2/1/2017.
 */

public class GamesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    List<GameModel> gameModelList;
    private ItemClickListener onItemClickListener;
    public GamesAdapter() {
        gameModelList = new LinkedList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

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
