package ink.va.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ink.va.R;

import java.util.LinkedList;
import java.util.List;

import ink.va.models.MafiaMessageModel;
import ink.va.view_holders.MafiaChatViewHolder;

/**
 * Created by PC-Comp on 3/13/2017.
 */

public class MafiaChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<MafiaMessageModel> mafiaMessageModels;
    private Context context;

    public MafiaChatAdapter() {
        mafiaMessageModels = new LinkedList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mafia_chat_single_item, parent, false);
        return new MafiaChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((MafiaChatViewHolder) holder).initData(mafiaMessageModels.get(position), context);
    }

    @Override
    public int getItemCount() {
        return mafiaMessageModels.size();
    }

    public void setMafiaMessageModels(List<MafiaMessageModel> mafiaMessageModels, boolean isMafia) {
        if (!isMafia) {
            this.mafiaMessageModels.clear();
            for (int i = 0; i < mafiaMessageModels.size(); i++) {
                MafiaMessageModel mafiaMessageModel = mafiaMessageModels.get(i);
                if (mafiaMessageModel.isMafiaMessage()) {
                    continue;
                }
                this.mafiaMessageModels.add(mafiaMessageModel);
            }
            notifyDataSetChanged();
        } else {
            for (MafiaMessageModel mafiaMessageModel : mafiaMessageModels) {
                if (!mafiaMessageModel.isShowMessage()) {
                    continue;
                }
                this.mafiaMessageModels.clear();
                this.mafiaMessageModels.add(mafiaMessageModel);
            }
            notifyDataSetChanged();
        }
    }

    public void insertMessage(MafiaMessageModel mafiaMessageModel, boolean isMafia) {
        if (mafiaMessageModel.isMafiaMessage()) {
            if (isMafia) {
                mafiaMessageModels.add(mafiaMessageModel);
                int index = mafiaMessageModels.indexOf(mafiaMessageModel);
                notifyItemInserted(index);
            }
        } else {
            mafiaMessageModels.add(mafiaMessageModel);
            int index = mafiaMessageModels.indexOf(mafiaMessageModel);
            notifyItemInserted(index);
        }

    }

    public void clear() {
        mafiaMessageModels.clear();
        notifyDataSetChanged();
    }
}
