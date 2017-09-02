package kashmirr.social.view_holders;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kashmirr.social.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import kashmirr.social.interfaces.RecyclerItemClickListener;
import kashmirr.social.models.ParticipantModel;
import kashmirr.social.models.UserModel;
import kashmirr.social.utils.Constants;
import kashmirr.social.utils.ImageLoader;

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
    @BindView(R.id.victimIcon)
    ImageView victimIcon;
    @BindView(R.id.votingCountTV)
    TextView votingCountTV;

    private RecyclerItemClickListener onItemClickListener;
    private ParticipantModel participantModel;

    public MafiaParticipantViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    @OnClick(R.id.playersParentItem)
    public void playersParentItemClicked() {
        if (onItemClickListener != null) {
            onItemClickListener.onItemClicked(participantModel);
        }
    }

    public void initData(final Context context, ParticipantModel participantModel, String ownerId, RecyclerItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
        this.participantModel = participantModel;
        if (participantModel.isVictim()) {
            victimIcon.setVisibility(View.VISIBLE);
        } else {
            victimIcon.setVisibility(View.GONE);
        }

        if (participantModel.getVotingCont() == 0) {
            votingCountTV.setVisibility(View.GONE);
        } else {
            votingCountTV.setText(context.getString(R.string.votes, participantModel.getVotingCont()));
            votingCountTV.setVisibility(View.VISIBLE);
        }

        UserModel user = participantModel.getUser();
        participantName.setText(user.getFirstName() + " " + user.getLastName());

        if (participantModel.isEliminated()) {
            participantName.setText(context.getString(R.string.eliminatedText));
        }

        if (ownerId.equals(user.getUserId())) {
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

            ImageLoader.loadImage(context, true, false, url,
                    0, R.drawable.user_image_placeholder, participantImage, new ImageLoader.ImageLoadedCallback() {
                        @Override
                        public void onImageLoaded(Object result, Exception e) {
                            if (e != null) {
                                ImageLoader.loadImage(context, true, true, null, R.drawable.no_image, R.drawable.user_image_placeholder, participantImage, null);
                            }
                        }
                    });
        } else {
            ImageLoader.loadImage(context, true, true, null, R.drawable.no_image, R.drawable.user_image_placeholder, participantImage, null);
        }

    }
}
