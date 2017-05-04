package ink.va.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.ink.va.R;
import com.mikhaellopez.hfrecyclerview.HFRecyclerView;

import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

import ink.va.interfaces.FeedItemClick;
import ink.va.models.FeedModel;
import ink.va.view_holders.FeedViewHolder;
import ink.va.view_holders.OpponentProfileHeaderView;
import lombok.Setter;

/**
 * Created by PC-Comp on 4/10/2017.
 */

public class OpponentProfileAdapter extends HFRecyclerView<FeedModel> {
    private Context context;
    private List<FeedModel> feedModels;
    @Setter
    private FeedItemClick onFeedItemClickListener;
    private JSONObject userJsonObject;
    @Setter
    private boolean hasServerError;
    @Setter
    private boolean showNoFeedsOrError;
    @Setter
    private boolean disableButton;
    @Setter
    private boolean enableButtons;
    @Setter
    private OpponentProfileHeaderView.HeaderViewClickListener headerViewClickListener;

    public OpponentProfileAdapter(List<FeedModel> data, boolean withHeader, boolean withFooter, Context context) {
        super(data, withHeader, withFooter);
        feedModels = new LinkedList<>();
        feedModels = data;
        this.context = context;
    }

    @Override
    protected RecyclerView.ViewHolder getItemView(LayoutInflater inflater, ViewGroup parent) {
        return new FeedViewHolder(inflater.inflate(R.layout.feed_single_view, parent, false));
    }

    @Override
    protected RecyclerView.ViewHolder getHeaderView(LayoutInflater inflater, ViewGroup parent) {
        return new OpponentProfileHeaderView(inflater.inflate(R.layout.opponent_profile_header_view, parent, false));
    }

    @Override
    protected RecyclerView.ViewHolder getFooterView(LayoutInflater inflater, ViewGroup parent) {
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof FeedViewHolder) {
            ((FeedViewHolder) holder).setHasServerError(hasServerError);
            ((FeedViewHolder) holder).setShowNoFeedOrError(showNoFeedsOrError);
            ((FeedViewHolder) holder).initData(context, feedModels.get(position - 1), position - 1, onFeedItemClickListener, getItemCount() - 1);
        } else if (holder instanceof OpponentProfileHeaderView) {
            if (userJsonObject != null) {
                ((OpponentProfileHeaderView) holder).initData(userJsonObject, context, disableButton, enableButtons, headerViewClickListener);
            }
        }
    }


    public void clear() {
        feedModels.clear();
        notifyDataSetChanged();
    }

    public void setUserJsonObject(JSONObject userJsonObject) {
        this.userJsonObject = userJsonObject;
        notifyDataSetChanged();
    }
}
