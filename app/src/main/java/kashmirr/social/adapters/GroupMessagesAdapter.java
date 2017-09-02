package kashmirr.social.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kashmirr.social.R;

import java.util.List;

import kashmirr.social.interfaces.RecyclerItemClickListener;
import kashmirr.social.models.GroupMessagesModel;
import kashmirr.social.utils.Animations;
import kashmirr.social.utils.SharedHelper;
import kashmirr.social.view_holders.GroupMessagesViewHolder;

/**
 * Created by USER on 2016-07-10.
 */
public class GroupMessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<GroupMessagesModel> groupMessagesModels;
    private Context mContext;
    private RecyclerItemClickListener onClickListener;
    private SharedHelper sharedHelper;
    private int lastPosition = -1;


    public GroupMessagesAdapter(List<GroupMessagesModel> groupMessagesModels, Context context) {
        mContext = context;
        this.groupMessagesModels = groupMessagesModels;
        sharedHelper = new SharedHelper(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_group_message_item, parent, false);
        return new GroupMessagesViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        GroupMessagesModel groupMessagesModel = groupMessagesModels.get(position);
        ((GroupMessagesViewHolder) holder).initData(mContext, groupMessagesModel, onClickListener, position);
        lastPosition = Animations.startRecyclerItemAnimation(mContext, ((GroupMessagesViewHolder) holder).getViewToAnimate(),
                position, lastPosition, R.anim.slide_up_calm);
    }


    @Override
    public int getItemCount() {
        return groupMessagesModels.size();
    }

    public void setOnClickListener(RecyclerItemClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }


    @Override
    public void onViewDetachedFromWindow(final RecyclerView.ViewHolder holder) {
        ((GroupMessagesViewHolder) holder).getViewToAnimate().clearAnimation();

    }
}

