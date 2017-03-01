package ink.va.interfaces;

import ink.va.models.MafiaRoomsModel;

/**
 * Created by PC-Comp on 3/1/2017.
 */

public interface MafiaItemClickListener {
    void onJoinClicked(MafiaRoomsModel mafiaRoomsModel);

    void onDeleteClicked(MafiaRoomsModel mafiaRoomsModel);

    void onLeaveClicked(MafiaRoomsModel mafiaRoomsModel);

    void onItemClicked(MafiaRoomsModel mafiaRoomsModel);
}
