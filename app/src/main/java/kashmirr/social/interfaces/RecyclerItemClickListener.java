package kashmirr.social.interfaces;

import android.view.View;

/**
 * Created by PC-Comp on 8/4/2016.
 */
public interface RecyclerItemClickListener {
    void onItemClicked(int position, View view);

    void onItemLongClick(Object object);

    void onAdditionalItemClick(int position, View view);

    void onAdditionalItemClicked(Object object);

    void onItemClicked(Object object);
}
