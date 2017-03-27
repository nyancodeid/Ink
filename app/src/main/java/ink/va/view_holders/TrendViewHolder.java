package ink.va.view_holders;

import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ink.va.R;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.va.fragments.TrendModel;
import ink.va.interfaces.RecyclerItemClickListener;
import ink.va.utils.SharedHelper;

/**
 * Created by PC-Comp on 3/27/2017.
 */

public class TrendViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.trendTitle)
    TextView trendTitle;
    @BindView(R.id.trendContent)
    TextView trendContent;
    @BindView(R.id.premiumText)
    TextView premiumText;
    @BindView(R.id.trendImage)
    ImageView trendImage;
    @BindView(R.id.premiumBadgeIcon)
    ImageView premiumBadgeIcon;
    @BindView(R.id.imageLoadingProgress)
    ProgressBar imageLoadingProgress;
    @BindView(R.id.trendSpacing)
    View trendSpacing;
    @BindView(R.id.removeTrendIV)
    ImageView removeTrendIV;
    private SharedHelper sharedHelper;
    private RecyclerItemClickListener onItemClickListener;
    private TrendModel trendModel;

    public TrendViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void initData(final TrendModel trendModel, int position, int maxSize, final Context context, @Nullable RecyclerItemClickListener onItemClickListener) {
        if (sharedHelper == null) {
            sharedHelper = new SharedHelper(context);
        }
        this.onItemClickListener = onItemClickListener;
        this.trendModel = trendModel;
        if (trendModel.isTop()) {
            premiumBadgeIcon.setVisibility(View.VISIBLE);
            premiumText.setVisibility(View.VISIBLE);
        } else {
            premiumBadgeIcon.setVisibility(View.INVISIBLE);
            premiumText.setVisibility(View.GONE);
        }

        removeTrendIV.setVisibility(trendModel.getCreatorId().equals(sharedHelper.getUserId()) ? View.VISIBLE : View.GONE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            trendTitle.setText(Html.fromHtml(trendModel.getTitle(), Html.FROM_HTML_MODE_LEGACY));
            trendContent.setText(Html.fromHtml(trendModel.getContent(), Html.FROM_HTML_MODE_LEGACY));
        } else {
            trendTitle.setText(Html.fromHtml(trendModel.getTitle()));
            trendContent.setText(Html.fromHtml(trendModel.getContent()));
        }


        if (trendModel.getImageUrl() != null && !trendModel.getImageUrl().isEmpty()) {
            imageLoadingProgress.setVisibility(View.VISIBLE);
            trendImage.setVisibility(View.VISIBLE);
            Ion.with(context).load(trendModel.getImageUrl()).withBitmap().intoImageView(trendImage).setCallback(new FutureCallback<ImageView>() {
                @Override
                public void onCompleted(Exception e, ImageView result) {
                    imageLoadingProgress.setVisibility(View.GONE);
                }
            });
        } else {
            imageLoadingProgress.setVisibility(View.GONE);
            trendImage.setVisibility(View.GONE);
        }

        trendSpacing.setVisibility(position >= maxSize ? View.VISIBLE : View.GONE);
    }

    @OnClick(R.id.removeTrendIV)
    public void removeTrendIVClicked() {
        if (onItemClickListener != null) {
            onItemClickListener.onAdditionalItemClicked(trendModel);
        }
    }

    @OnClick(R.id.trendViewMore)
    public void trendViewMoreClicked() {
        if (onItemClickListener != null) {
            onItemClickListener.onItemClicked(trendModel);
        }
    }
}
