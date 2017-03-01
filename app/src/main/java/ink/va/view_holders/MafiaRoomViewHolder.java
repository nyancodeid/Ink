package ink.va.view_holders;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.ink.va.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.va.interfaces.MafiaItemClickListener;
import ink.va.models.MafiaRoomsModel;
import ink.va.utils.SharedHelper;

/**
 * Created by PC-Comp on 3/1/2017.
 */

public class MafiaRoomViewHolder extends RecyclerView.ViewHolder {
    private MafiaItemClickListener mafiaItemClickListener;
    @BindView(R.id.roomNameTV)
    TextView roomNameTV;
    @BindView(R.id.gameTypeTV)
    TextView gameTypeTV;
    @BindView(R.id.morningDurationTV)
    TextView morningDurationTV;
    @BindView(R.id.nightDurationTV)
    TextView nightDurationTV;
    @BindView(R.id.languageTV)
    TextView languageTV;
    private Context context;
    private SharedHelper sharedHelper;
    private MafiaRoomsModel mafiaRoomsModel;

    public MafiaRoomViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void initData(Context context, MafiaRoomsModel mafiaRoomsModel, @Nullable MafiaItemClickListener mafiaItemClickListener) {
        this.mafiaRoomsModel = mafiaRoomsModel;
        if (sharedHelper == null) {
            sharedHelper = new SharedHelper(context);
        }
        this.context = context;
        this.mafiaItemClickListener = mafiaItemClickListener;
    }

    @OnClick(R.id.mafiaRoomMoreIcon)
    public void onMoreIconClicked(View view) {
        PopupMenu popupMenu = new PopupMenu(context, view);

        if (sharedHelper.getUserId().equals(mafiaRoomsModel.getCreatorId())) {
            popupMenu.getMenu().add(0, 0, 0, context.getString(R.string.delete));
        }
        boolean isParticipant = false;
        for (String eachUserId : mafiaRoomsModel.getJoinedUserIds()) {
            if (sharedHelper.getUserId().equals(eachUserId)) {
                isParticipant = true;
                break;
            }
        }

        if (isParticipant) {
            popupMenu.getMenu().add(0, 1, 1, context.getString(R.string.leave));
        } else {
            popupMenu.getMenu().add(0, 1, 1, context.getString(R.string.join));
        }
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getTitle().toString().equals(context.getString(R.string.leave))) {
                    if (mafiaItemClickListener != null) {
                        mafiaItemClickListener.onLeaveClicked(mafiaRoomsModel);
                    }
                } else if (item.getTitle().toString().equals(context.getString(R.string.join))) {
                    if (mafiaItemClickListener != null) {
                        mafiaItemClickListener.onJoinClicked(mafiaRoomsModel);
                    }
                } else if (item.getTitle().toString().equals(context.getString(R.string.delete))) {
                    if (mafiaItemClickListener != null) {
                        mafiaItemClickListener.onDeleteClicked(mafiaRoomsModel);
                    }
                }
                return false;
            }
        });
        popupMenu.show();
    }
}
