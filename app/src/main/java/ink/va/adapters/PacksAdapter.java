package ink.va.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ink.va.R;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerTextView;

import java.util.LinkedList;
import java.util.List;

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
        this.packsModels.clear();
        this.packsModels.addAll(packsModels);
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
        ImageView packWrapper;
        ImageView packImage;
        TextView packCoinCount;
        ShimmerTextView packTitleTV;
        RelativeLayout buyButtonWrapper;

        private Shimmer shimmer;
        private String packId;
        private View packRootView;
        private PacksModel packsModel;


        public BaseViewHolder(View itemView) {
            super(itemView);
            packTitleTV = (ShimmerTextView) itemView.findViewById(R.id.pack_title_TV);
            packRootView = itemView.findViewById(R.id.packRootView);
            packCoinCount = (TextView) itemView.findViewById(R.id.pack_coin_count);
            packWrapper = (ImageView) itemView.findViewById(R.id.pack_wrapper);
            packImage = (ImageView) itemView.findViewById(R.id.pack_image);
            buyButtonWrapper = (RelativeLayout) itemView.findViewById(R.id.buy_button_wrapper);
            buyButtonWrapper.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (packClickListener != null) {
                        packClickListener.onBuyClicked(packsModel,Integer.valueOf(packCoinCount.getText().toString()),
                                packId, packRootView);
                    }
                }
            });
        }

        public void init(PacksModel packsModel) {
            this.packsModel = packsModel;
            packCoinCount.setText(packsModel.packsPrice);
            packId = String.valueOf(packsModel.packsId);
            packWrapper.setBackground(ContextCompat.getDrawable(context, R.drawable.big_image_place_holder));
            Ion.with(context).load(Constants.MAIN_URL + Constants.PACK_BACKGROUNDS_FOLDER + packsModel.packBackground).withBitmap().asBitmap().setCallback(new FutureCallback<Bitmap>() {
                @Override
                public void onCompleted(Exception e, Bitmap result) {
                    packWrapper.setBackground(null);
                    if (e == null) {
                        packWrapper.setImageBitmap(result);
                    }
                }
            });
            Ion.with(context).load(Constants.MAIN_URL + Constants.PACK_BACKGROUNDS_FOLDER + packsModel.packImageBackground)
                    .withBitmap().placeholder(R.drawable.no_background_image).intoImageView(packImage).setCallback(new FutureCallback<ImageView>() {
                @Override
                public void onCompleted(Exception e, ImageView result) {
                    packImage.clearAnimation();
                }
            });
            packTitleTV.setText(packsModel.packNameEn);
            shimmer = new Shimmer();
            shimmer.setDuration(4000);
            shimmer.start(packTitleTV);

        }
    }

    public interface PackClickListener {
        void onBuyClicked(PacksModel packsModel, int packPrice, String packId, View clickedView);
    }
}
