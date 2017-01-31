package ink.va.view_holders;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ink.va.R;
import com.koushikdutta.ion.Ion;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.va.interfaces.BadgeClickListener;
import ink.va.models.BadgeModel;
import ink.va.utils.Constants;

/**
 * Created by PC-Comp on 1/31/2017.
 */

public class BadgeViewHolder extends RecyclerView.ViewHolder {

    @Bind(R.id.badgeNameTV)
    TextView badgeNameTV;
    @Bind(R.id.badgeImageView)
    ImageView badgeImageView;
    @Bind(R.id.badgePrice)
    TextView badgePrice;
    private BadgeClickListener onClickListener;
    private BadgeModel badgeModel;

    public BadgeViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this,itemView);
    }


    public void initData(Context context, BadgeModel badgeModel, @Nullable BadgeClickListener onClickListener) {
        this.onClickListener = onClickListener;
        this.badgeModel = badgeModel;
        Ion.with(context).load(Constants.MAIN_URL + badgeModel.getBadgeName())
                .withBitmap().placeholder(R.drawable.badge_placeholder)
                .intoImageView(badgeImageView);
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
