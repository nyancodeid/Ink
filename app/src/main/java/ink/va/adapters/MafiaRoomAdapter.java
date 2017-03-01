package ink.va.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ink.va.R;

import java.util.LinkedList;
import java.util.List;

import ink.va.interfaces.MafiaItemClickListener;
import ink.va.models.MafiaRoomsModel;
import ink.va.view_holders.MafiaRoomViewHolder;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by PC-Comp on 3/1/2017.
 */

public class MafiaRoomAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    @Getter
    List<MafiaRoomsModel> mafiaRoomsModels;
    @Setter
    MafiaItemClickListener onMafiaItemClickListener;
    private Context context;

    public MafiaRoomAdapter() {
        mafiaRoomsModels = new LinkedList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View mafiaSingleView = LayoutInflater.from(parent.getContext()).inflate(R.layout.mafia_room_single_item, parent, false);
        return new MafiaRoomViewHolder(mafiaSingleView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((MafiaRoomViewHolder) holder).initData(context, mafiaRoomsModels.get(position), onMafiaItemClickListener,
                mafiaRoomsModels.size() - 1, position);
    }

    @Override
    public int getItemCount() {
        return mafiaRoomsModels.size();
    }


    public void clear() {
        mafiaRoomsModels.clear();
        notifyDataSetChanged();
    }

    public void setMafiaRoomsModels(List<MafiaRoomsModel> mafiaRoomsModels) {
        this.mafiaRoomsModels.clear();
        this.mafiaRoomsModels.addAll(mafiaRoomsModels);
        notifyDataSetChanged();
    }
}
