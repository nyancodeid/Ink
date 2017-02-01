package ink.va.view_holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import butterknife.ButterKnife;

/**
 * Created by PC-Comp on 2/1/2017.
 */

public class GameViewHolder extends RecyclerView.ViewHolder {
    public GameViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
