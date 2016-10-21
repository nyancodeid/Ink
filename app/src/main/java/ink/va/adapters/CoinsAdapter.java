package ink.va.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ink.va.R;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import ink.va.models.CoinsModel;
import ink.va.utils.Constants;

/**
 * Created by USER on 2016-10-22.
 */

public class CoinsAdapter extends RecyclerView.Adapter<CoinsAdapter.ViewHolder> {

    private CoinsModel[] coinsModels;
    private Context mContext;
    private ItemClick itemClickListener;

    public class ViewHolder extends RecyclerView.ViewHolder {

        private Button coisnDollarCoint;
        private TextView coinsGivenCount;
        private TextView coinsReducedCoint;
        private ImageView coinsIcon;
        private ProgressBar coinsLoading;

        public ViewHolder(View view) {
            super(view);
            coisnDollarCoint = (Button) view.findViewById(R.id.coins_dollar_count);
            coinsGivenCount = (TextView) view.findViewById(R.id.coins_given_count);
            coinsReducedCoint = (TextView) view.findViewById(R.id.coins_reduced_count);
            coinsIcon = (ImageView) view.findViewById(R.id.coins_icon);
            coinsLoading = (ProgressBar) view.findViewById(R.id.coinsLoading);
        }
    }


    public CoinsAdapter(CoinsModel[] coinsModels, Context context) {
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
        CoinsModel coinsModel = coinsModels[position];
        holder.coisnDollarCoint.setText(coinsModel.coinsPrice);
        holder.coinsGivenCount.setText(coinsModel.coinsCount);
        holder.coinsReducedCoint.setText(coinsModel.coinsReduced);
        holder.coinsLoading.setVisibility(View.VISIBLE);
        Ion.with(mContext).load(Constants.MAIN_URL + Constants.COIN_ICON_FOLDER + coinsModel.coinsIcon).asBitmap().setCallback(new FutureCallback<Bitmap>() {
            @Override
            public void onCompleted(Exception e, Bitmap result) {
                holder.coinsIcon.setImageBitmap(result);
                holder.coinsLoading.setVisibility(View.GONE);
            }
        });
        holder.coisnDollarCoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(coinsModels[position]);
                }
            }
        });

    }


    @Override
    public int getItemCount() {
        return coinsModels.length;
    }

    public interface ItemClick {
        void onItemClick(CoinsModel coinsModel);
    }

    public void setOnItemClickListener(ItemClick onItemClickListener) {
        this.itemClickListener = onItemClickListener;
    }
}
