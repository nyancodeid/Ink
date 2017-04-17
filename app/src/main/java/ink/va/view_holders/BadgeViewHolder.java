package ink.va.view_holders;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ink.va.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.va.interfaces.BadgeClickListener;
import ink.va.models.BadgeModel;
import ink.va.utils.Constants;
import ink.va.utils.ImageLoader;

/**
 * Created by PC-Comp on 1/31/2017.
 */

public class BadgeViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.badgeNameTV)
    TextView badgeNameTV;
    @BindView(R.id.badgeImageView)
    ImageView badgeImageView;
    @BindView(R.id.badgePrice)
    TextView badgePrice;
    private BadgeClickListener onClickListener;
    private BadgeModel badgeModel;

    public BadgeViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }


    public void initData(Context context, BadgeModel badgeModel, @Nullable BadgeClickListener onClickListener) {
        this.onClickListener = onClickListener;
        this.badgeModel = badgeModel;

        ImageLoader.loadImage(context, true,false, Constants.MAIN_URL + badgeModel.getBadgeName(), 0, R.drawable.badge_placeholder, badgeImageView, null);
        badgePrice.setText(context.getString(R.string.coinsText, badgeModel.getBadgePrice()));
        badgeNameTV.setText(badgeModel.getBadgeTitle());
    }

    @OnClick(R.id.buyBadge)
    public void buyBadge() {
        if (onClickListener != null) {
            onClickListener.onBuyClicked(badgeModel);
        }
    }

}
