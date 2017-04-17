package ink.va.view_holders;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ink.va.R;

import java.text.SimpleDateFormat;

import ink.va.adapters.WhoViewedAdapter;
import ink.va.models.WhoViewedModel;
import ink.va.utils.Constants;
import ink.va.utils.ImageLoader;
import ink.va.utils.Time;

/**
 * Created by USER on 2016-11-18.
 */

public class WhoViewedViewHolder extends RecyclerView.ViewHolder {
    private ImageView whoViewedImage;
    private TextView whoViewedName;
    private ImageView whoViewedMoreIcon;
    private TextView whoViewedTime;
    private CardView cardView;

    public WhoViewedViewHolder(View itemView) {
        super(itemView);
        whoViewedImage = (ImageView) itemView.findViewById(R.id.whoViewedImage);
        whoViewedName = (TextView) itemView.findViewById(R.id.whoViewedName);
        whoViewedMoreIcon = (ImageView) itemView.findViewById(R.id.whoViewedMoreIcon);
        whoViewedTime = (TextView) itemView.findViewById(R.id.whoViewedTime);
        cardView = (CardView) itemView.findViewById(R.id.whoViewedCardView);
    }

    public void init(Context context, final WhoViewedModel whoViewedModel, @Nullable final WhoViewedAdapter.OnItemClickListener onItemClickListener) {

        whoViewedName.setText(whoViewedModel.getFullName());
        whoViewedTime.setText(Time.getTimeInHumanFormat(context, whoViewedModel.getTimeViewed(),
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")));

        if (!whoViewedModel.getImageLink().isEmpty()) {

            final String url;
            if (whoViewedModel.isSocialAccount()) {
                url = whoViewedModel.getImageLink();
            } else {
                String encodedImage = Uri.encode(whoViewedModel.getImageLink());
                url = Constants.MAIN_URL + Constants.USER_IMAGES_FOLDER + encodedImage;
            }

            ImageLoader.loadImage(context, true, false, url, 0, R.drawable.user_image_placeholder, whoViewedImage, null);

        } else {
            ImageLoader.loadImage(context, true, true, null, R.drawable.no_image, R.drawable.user_image_placeholder, whoViewedImage, null);
        }

        if (whoViewedModel.isFriend()) {
            whoViewedMoreIcon.setVisibility(View.VISIBLE);
        } else {
            whoViewedMoreIcon.setVisibility(View.GONE);
        }
        whoViewedMoreIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onItemClickListener != null) {
                    onItemClickListener.onMoreItemClicked(whoViewedModel, whoViewedMoreIcon);
                }
            }
        });
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onItemClickListener != null) {
                    onItemClickListener.onCardItemClicked(whoViewedModel);
                }
            }
        });
    }
}
