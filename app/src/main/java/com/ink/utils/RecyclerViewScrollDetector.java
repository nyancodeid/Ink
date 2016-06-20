package com.ink.utils;

import android.support.v7.widget.RecyclerView;
import android.util.Log;

/**
 * Created by USER on 2016-06-20.
 */
public abstract class RecyclerViewScrollDetector extends RecyclerView.OnScrollListener {
    private int mScrollThreshold = 1;

    public abstract void onScrollUp();

    public abstract void onScrollDown();

    public abstract void onDragging();

    public abstract void onIdle();

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        boolean isSignificantDelta = Math.abs(dy) > mScrollThreshold;
        if (isSignificantDelta) {
            if (dy > 0) {
                onScrollUp();
            } else {
                onScrollDown();
            }
        }
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            onIdle();
        } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
            onDragging();
        }
        super.onScrollStateChanged(recyclerView, newState);
    }

    public void setScrollThreshold(int scrollThreshold) {
        mScrollThreshold = scrollThreshold;
        Log.i("Abscroll", "RView thresh " + scrollThreshold);
    }
}