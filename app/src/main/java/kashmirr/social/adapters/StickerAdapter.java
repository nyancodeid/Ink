package kashmirr.social.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kashmirr.social.R;

import java.util.List;

import kashmirr.social.interfaces.RecyclerItemClickListener;
import kashmirr.social.models.StickerModel;
import kashmirr.social.view_holders.StickerChooserViewHolder;


/**
 * Created by PC-Comp on 8/9/2016.
 */
public class StickerAdapter extends RecyclerView.Adapter<StickerChooserViewHolder> {
    private List<StickerModel> gifAdapterList;
    private Context context;
    private RecyclerItemClickListener recyclerItemClickListener;
    private boolean hideChooser;

    public StickerAdapter(List<StickerModel> gifAdapterList, Context context) {
        this.gifAdapterList = gifAdapterList;
        this.context = context;
    }

    @Override
    public StickerChooserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.gif_single_item_view, parent, false);
        return new StickerChooserViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final StickerChooserViewHolder holder, final int position) {
        StickerModel stickerModel = gifAdapterList.get(position);
        holder.init(context, stickerModel, recyclerItemClickListener, hideChooser);

    }

    @Override
    public int getItemCount() {
        return gifAdapterList.size();
    }


    public void setOnItemClickListener(RecyclerItemClickListener recyclerItemClickListener) {
        this.recyclerItemClickListener = recyclerItemClickListener;
    }

    public void setHideChooser(boolean hideChooser) {
        this.hideChooser = hideChooser;
    }

    public void clearItems() {
        if(gifAdapterList!=null){
            gifAdapterList.clear();
            notifyDataSetChanged();
        }

    }
}
