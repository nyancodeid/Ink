package ink.va.view_holders;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ink.va.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.va.fragments.TrendModel;
import ink.va.interfaces.RecyclerItemClickListener;
import ink.va.utils.ImageLoader;
import ink.va.utils.SharedHelper;
import it.sephiroth.android.library.picasso.Picasso;
import it.sephiroth.android.library.picasso.Target;

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
    private Target target;

    public TrendViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void initData(final TrendModel trendModel, int position, int maxSize, final Context context, @Nullable RecyclerItemClickListener onItemClickListener) {
        if (sharedHelper == null) {
            sharedHelper = new SharedHelper(context);
        }
        target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
                Log.d("Fsaslfsaf", "onBitmapLoaded: ");
            }

            @Override
            public void onBitmapFailed(Drawable drawable) {
                Log.d("Fsaslfsaf", "on bitmap failed: ");
            }

            @Override
            public void onPrepareLoad(Drawable drawable) {
                Log.d("Fsaslfsaf", "on re load : ");
            }
        };

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
            ImageLoader.loadImage(context, false, false, trendModel.getImageUrl(), 0, 0, trendImage, new ImageLoader.ImageLoadedCallback() {
                @Override
                public void onImageLoaded(Object result, Exception e) {
                    imageLoadingProgress.setVisibility(View.GONE);
                }
            });
            Picasso.with(context).load(trendModel.getImageUrl()).placeholder(R.drawable.breaking_news_vector).into(target);
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
