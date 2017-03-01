package ink.va.view_holders;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import butterknife.ButterKnife;
import ink.va.interfaces.MafiaItemClickListener;
import ink.va.models.MafiaRoomsModel;

/**
 * Created by PC-Comp on 3/1/2017.
 */

public class MafiaRoomViewHolder extends RecyclerView.ViewHolder {
    private MafiaItemClickListener mafiaItemClickListener;

    public MafiaRoomViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void initData(MafiaRoomsModel mafiaRoomsModel, @Nullable MafiaItemClickListener mafiaItemClickListener) {
        this.mafiaItemClickListener = mafiaItemClickListener;

    }
}
