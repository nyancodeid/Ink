package ink.va.view_holders;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ink.va.R;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import butterknife.Bind;
import butterknife.ButterKnife;
import ink.va.adapters.MyCollectionHorizontalAdapter;
import ink.va.models.MyCollectionModel;
import ink.va.utils.Constants;

/**
 * Created by PC-Comp on 12/22/2016.
 */

public class MyCollectionHorizontalViewHolder extends RecyclerView.ViewHolder {

    @Bind(R.id.collection_image)
    ImageView collectionImage;
    @Bind(R.id.collectionName)
    TextView collectionName;

    @Bind(R.id.collectionCardView)
    View collectionCardView;

    @Bind(R.id.collectionMoreIcon)
    View collectionMoreIcon;

    public MyCollectionHorizontalViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void initData(Context context, final MyCollectionModel myCollectionModel, final @Nullable MyCollectionHorizontalAdapter.OnCollectionClickListener onCollectionClickListener) {
        Ion.with(context).load(Constants.MAIN_URL + myCollectionModel.getStickerUrl()).intoImageView(collectionImage).setCallback(new FutureCallback<ImageView>() {
            @Override
            public void onCompleted(Exception e, ImageView result) {
                if (e != null) {
                    e.printStackTrace();
                }
            }
        });
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
