package ink.va.interfaces;

import android.view.View;

/**
 * Created by PC-Comp on 8/4/2016.
 */
public interface RecyclerItemClickListener {
    void onItemClicked(int position, View view);

    void onItemLongClick(int position);

    void onAdditionItemClick(int position, View view);
}
