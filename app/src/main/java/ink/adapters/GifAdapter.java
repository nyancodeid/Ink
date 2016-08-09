package ink.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ink.R;
import com.koushikdutta.ion.Ion;

import java.util.List;

import ink.interfaces.RecyclerItemClickListener;
import ink.models.GifModel;
import ink.utils.Constants;

/**
 * Created by PC-Comp on 8/9/2016.
 */
public class GifAdapter extends RecyclerView.Adapter<GifAdapter.ViewHolder> {
    private List<GifModel> gifAdapterList;
    private Context context;
    private RecyclerItemClickListener recyclerItemClickListener;


    public GifAdapter(List<GifModel> gifAdapterList, Context context) {
        this.gifAdapterList = gifAdapterList;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.gif_single_item_view, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        GifModel gifModel = gifAdapterList.get(position);
        if (gifModel.isAnimated()) {
            if (gifModel.hasSound()) {

            }
            Ion.with(context).load(Constants.MAIN_URL + Constants.ANIMATED_STICKERS_FOLDER + gifModel.getGifName()).intoImageView(holder.gifSingleView);
            holder.gifSingleView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (recyclerItemClickListener != null) {
                        recyclerItemClickListener.OnItemClicked(position);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return gifAdapterList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView gifSingleView;

        public ViewHolder(View itemView) {
            super(itemView);
            gifSingleView = (ImageView) itemView.findViewById(R.id.gifSingleView);
        }
    }


    public void setOnItemClickListener(RecyclerItemClickListener recyclerItemClickListener) {
        this.recyclerItemClickListener = recyclerItemClickListener;
    }
}
