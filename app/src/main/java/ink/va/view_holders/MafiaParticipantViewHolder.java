package ink.va.view_holders;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ink.va.R;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import butterknife.BindView;
import butterknife.ButterKnife;
import ink.va.models.UserModel;
import ink.va.utils.CircleTransform;
import ink.va.utils.Constants;
import ink.va.utils.User;

/**
 * Created by PC-Comp on 3/13/2017.
 */

public class MafiaParticipantViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.participantImage)
    ImageView participantImage;
    @BindView(R.id.participantName)
    TextView participantName;
    @BindView(R.id.roomOwnerTV)
    TextView roomOwnerTV;

    public MafiaParticipantViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void initData(Context context, UserModel user, String ownerId) {
        participantName.setText(user.getFirstName() + " " + user.getLastName());

        if (ownerId.equals(User.get().getUserId())) {
            roomOwnerTV.setVisibility(View.VISIBLE);
        } else {
            roomOwnerTV.setVisibility(View.GONE);
        }

        if (!user.getImageUrl().isEmpty()) {

            final String url;
            if (user.isSocialAccount()) {
                url = user.getImageUrl();
            } else {
                String encodedImage = Uri.encode(user.getImageUrl());
                url = Constants.MAIN_URL + Constants.USER_IMAGES_FOLDER + encodedImage;
            }

            Ion.with(context).load(url)
                    .withBitmap().placeholder(R.drawable.user_image_placeholder).transform(new CircleTransform()).
                    intoImageView(participantImage).setCallback(new FutureCallback<ImageView>() {
                @Override
                public void onCompleted(Exception e, ImageView result) {
                    if (e != null) {
                        participantImage.setImageResource(R.drawable.no_image);
                    }
                }
            });
        } else {
            Ion.with(context).load(Constants.ANDROID_DRAWABLE_DIR + "no_image")
                    .withBitmap().transform(new CircleTransform()).intoImageView(participantImage);
        }

    }
}
