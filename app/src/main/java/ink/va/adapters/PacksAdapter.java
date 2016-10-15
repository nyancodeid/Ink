package ink.va.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ink.va.R;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerTextView;

import java.util.LinkedList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.va.models.PacksModel;
import ink.va.utils.Constants;

/**
 * Created by USER on 2016-10-15.
 */

public class PacksAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<PacksModel> packsModels;
    private PackClickListener packClickListener;
    private Context context;

    public PacksAdapter(@Nullable PackClickListener packClickListener, Context context) {
        packsModels = new LinkedList<>();
        this.packClickListener = packClickListener;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_pack_layout, parent, false);
        return new BaseViewHolder(view);
    }

    public void setData(List<PacksModel> packsModels) {
        packsModels.clear();
        packsModels.addAll(packsModels);
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((BaseViewHolder) holder).init(packsModels.get(position));
    }

    @Override
    public int getItemCount() {
        return packsModels.size();
    }

    public class BaseViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.pack_wrapper)
        ImageView packWrapper;
        @Bind(R.id.pack_image)
        ImageView packImage;
        @Bind(R.id.pack_coin_count)
        TextView packCoinCoins;
        @Bind(R.id.pack_loading_progress)
        ProgressBar packLoadingProgress;

        @Bind(R.id.pack_title_TV)
        ShimmerTextView packTitleTV;
        private Shimmer shimmer;


        public BaseViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void init(PacksModel packsModel) {
            packCoinCoins.setText(packsModel.packsPrice);
            Ion.with(context).load(Constants.MAIN_URL + Constants.PACK_BACKGROUNDS_FOLDER + packsModel.packBackground).withBitmap().asBitmap().setCallback(new FutureCallback<Bitmap>() {
                @Override
                public void onCompleted(Exception e, Bitmap result) {
                    if (e != null) {
                        packWrapper.setImageBitmap(result);
                    }
                }
            });
            Ion.with(context).load(Constants.MAIN_URL + Constants.PACK_BACKGROUNDS_FOLDER + packsModel.packImageBackground)
                    .withBitmap().intoImageView(packImage).setCallback(new FutureCallback<ImageView>() {
                @Override
                public void onCompleted(Exception e, ImageView result) {
                    packLoadingProgress.setVisibility(View.GONE);
                    if (e == null) {
                        packImage.setBackgroundResource(R.drawable.image_laoding_error);
                    }
                }
            });
            packTitleTV.setText(packsModel.packNameEn);
            shimmer = new Shimmer();
            shimmer.start(packTitleTV);

        }

        @OnClick(R.id.buy_button_wrapper)
        public void buyClicked() {
            if (packClickListener != null) {
                packClickListener.onBuyClicked();
            }
        }
    }

    public interface PackClickListener {
        void onBuyClicked();
    }
}
