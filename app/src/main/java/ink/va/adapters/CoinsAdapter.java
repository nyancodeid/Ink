package ink.va.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ink.va.R;

import java.util.List;

import ink.va.models.CoinsModel;
import ink.va.utils.Constants;
import ink.va.utils.ImageLoader;

/**
 * Created by USER on 2016-10-22.
 */

public class CoinsAdapter extends RecyclerView.Adapter<CoinsAdapter.ViewHolder> {

    private List<CoinsModel> coinsModels;
    private Context mContext;
    private ItemClick itemClickListener;

    public class ViewHolder extends RecyclerView.ViewHolder {

        private Button coinsDollarCount;
        private TextView coinsGivenCount;
        private TextView coinsReducedCount;
        private ImageView coinsIcon;

        public ViewHolder(View view) {
            super(view);
            coinsDollarCount = (Button) view.findViewById(R.id.coins_dollar_count);
            coinsGivenCount = (TextView) view.findViewById(R.id.coins_given_count);
            coinsReducedCount = (TextView) view.findViewById(R.id.coins_reduced_count);
            coinsIcon = (ImageView) view.findViewById(R.id.coins_icon);
        }
    }


    public CoinsAdapter(List<CoinsModel> coinsModels, Context context) {
        mContext = context;
        this.coinsModels = coinsModels;
    }

    @Override
    public CoinsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.coins_single_item, parent, false);
        return new CoinsAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final CoinsAdapter.ViewHolder holder, final int position) {
        CoinsModel coinsModel = coinsModels.get(position);
        holder.coinsDollarCount.setText(coinsModel.coinsPrice);
        holder.coinsGivenCount.setText(coinsModel.coinsCount);
        holder.coinsReducedCount.setText(coinsModel.coinsReduced);

        ImageLoader.loadImage(mContext, false, false, Constants.MAIN_URL + Constants.COIN_ICON_FOLDER + coinsModel.coinsIcon,
                0, R.drawable.coins_icon, holder.coinsIcon, null);

        holder.coinsDollarCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(coinsModels.get(position));
                }
            }
        });

    }


    @Override
    public int getItemCount() {
        return coinsModels.size();
    }

    public interface ItemClick {
        void onItemClick(CoinsModel coinsModel);
    }

    public void setOnItemClickListener(ItemClick onItemClickListener) {
        this.itemClickListener = onItemClickListener;
    }
}
