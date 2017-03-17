package ink.va.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ink.va.R;

import java.util.LinkedList;
import java.util.List;

import ink.va.models.ParticipantModel;
import ink.va.view_holders.MafiaParticipantViewHolder;
import lombok.Setter;

/**
 * Created by PC-Comp on 3/13/2017.
 */

public class MafiaPlayersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<ParticipantModel> users;
    private Context context;
    @Setter
    private String ownerId;

    public MafiaPlayersAdapter() {
        users = new LinkedList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.mafia_participant_single_item, parent, false);
        return new MafiaParticipantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((MafiaParticipantViewHolder) holder).initData(context, users.get(position),ownerId);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void setUsers(List<ParticipantModel> users) {
        this.users.clear();
        this.users.addAll(users);
        notifyDataSetChanged();
    }
}
