package ink.va.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ink.va.R;

import java.util.List;

import ink.va.interfaces.FeedItemClick;
import ink.va.models.FeedModel;
import ink.va.utils.Animations;
import ink.va.view_holders.FeedViewHolder;


/**
 * Created by USER on 2016-06-20.
 */
public class FeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<FeedModel> feedList;
    private Context mContext;
    private FeedItemClick mOnClickListener;
    private int lastPosition = -1;


    public FeedAdapter(List<FeedModel> feedList, Context context) {
        mContext = context;
        this.feedList = feedList;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.feed_single_view, parent, false);

        return new FeedViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        FeedModel feedModel = feedList.get(position);

        ((FeedViewHolder) holder).initData(mContext,
                feedModel, position, mOnClickListener, getItemCount());
        lastPosition = Animations.startRecyclerItemAnimation(mContext, ((FeedViewHolder) holder).getViewToAnimate(),
                position, lastPosition, R.anim.slide_up_with_fade_fast);
    }


    public void setOnFeedClickListener(FeedItemClick mOnClickListener) {
        this.mOnClickListener = mOnClickListener;
    }


    @Override
    public void onViewDetachedFromWindow(final RecyclerView.ViewHolder holder) {
        ((FeedViewHolder) holder).getViewToAnimate().clearAnimation();

    }


    @Override
    public int getItemCount() {
        return feedList.size();
    }
}
