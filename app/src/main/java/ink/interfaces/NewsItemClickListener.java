package ink.interfaces;

import android.view.View;

/**
 * Created by PC-Comp on 9/12/2016.
 */
public interface NewsItemClickListener {
    void onViewMoreClicked(View clickedView, int position);

    void onLoadMoreClicked(View clickedView);
}
