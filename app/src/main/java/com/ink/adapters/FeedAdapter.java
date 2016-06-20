package com.ink.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.ink.R;
import com.ink.models.FeedModel;

import java.util.List;

/**
 * Created by USER on 2016-06-20.
 */
public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.ViewHolder> {

    private List<FeedModel> feedList;
    private Context mContext;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title, content;

        public ViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.feedTitle);
            content = (TextView) view.findViewById(R.id.feedContent);
        }
    }


    public FeedAdapter(List<FeedModel> feedList, Context context) {
        mContext = context;
        this.feedList = feedList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.feed_single_view, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        FeedModel feedModel = feedList.get(position);
        holder.title.setText(feedModel.getTitle());
        holder.content.setText(feedModel.getContent());
        animate(holder);
    }


    public void animate(RecyclerView.ViewHolder viewHolder) {
        final Animation animAnticipateOvershoot = AnimationUtils.loadAnimation(mContext, R.anim.bounce_interpolator);
        if (viewHolder.itemView.getTag() == null) {
            viewHolder.itemView.setAnimation(animAnticipateOvershoot);
            viewHolder.itemView.setTag("Animated");
        }
    }

    @Override
    public int getItemCount() {
        return feedList.size();
    }
}
