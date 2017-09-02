package kashmirr.social.view_holders;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kashmirr.social.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import kashmirr.social.adapters.MyCollectionHorizontalAdapter;
import kashmirr.social.models.MyCollectionModel;
import kashmirr.social.utils.Constants;
import kashmirr.social.utils.ImageLoader;

/**
 * Created by PC-Comp on 12/22/2016.
 */

public class MyCollectionHorizontalViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.collection_image)
    ImageView collectionImage;
    @BindView(R.id.collectionName)
    TextView collectionName;

    @BindView(R.id.collectionCardView)
    View collectionCardView;

    @BindView(R.id.collectionMoreIcon)
    View collectionMoreIcon;

    public MyCollectionHorizontalViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void initData(Context context, final MyCollectionModel myCollectionModel, final @Nullable MyCollectionHorizontalAdapter.OnCollectionClickListener onCollectionClickListener) {
        ImageLoader.loadImage(context, false, false, Constants.MAIN_URL + myCollectionModel.getStickerUrl(), 0, 0, collectionImage, null);

        collectionName.setText(myCollectionModel.getPackName());
        collectionCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onCollectionClickListener != null) {
                    onCollectionClickListener.onCollectionClicked(myCollectionModel);
                }
            }
        });
        collectionMoreIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onCollectionClickListener != null) {
                    onCollectionClickListener.onMoreClicked(view, myCollectionModel);
                }
            }
        });
    }
}
