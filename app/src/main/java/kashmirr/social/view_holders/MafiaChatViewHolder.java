package kashmirr.social.view_holders;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kashmirr.social.R;

import org.apache.commons.lang3.StringEscapeUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import kashmirr.social.models.MafiaMessageModel;
import kashmirr.social.models.UserModel;
import kashmirr.social.utils.Constants;
import kashmirr.social.utils.ImageLoader;
import kashmirr.social.utils.SharedHelper;


/**
 * Created by PC-Comp on 3/13/2017.
 */

public class MafiaChatViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.messageUsername)
    TextView messageUsername;
    @BindView(R.id.mafiaMessageCardView)
    CardView mafiaMessageCardView;
    @BindView(R.id.mafiaMessageContainer)
    TextView mafiaMessageContainer;
    private SharedHelper sharedHelper;
    @BindView(R.id.messageWrapper)
    LinearLayout messageWrapper;
    @BindView(R.id.mafiaChatUserImage)
    ImageView mafiaChatUserImage;
    @BindView(R.id.mafiaMessageParent)
    View mafiaMessageParent;

    public MafiaChatViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void initData(MafiaMessageModel mafiaMessageModel, final Context context) {
        if (sharedHelper == null) {
            sharedHelper = new SharedHelper(context);
        }

        if (mafiaMessageModel.isMafiaMessage()) {
            mafiaMessageParent.setAlpha((float) 0.7);
        } else {
            mafiaMessageParent.setAlpha(1);
        }

        UserModel user = mafiaMessageModel.getUser();

        messageUsername.setText(user.getFirstName() + " " + user.getLastName());
        String finalMessage;
        if (mafiaMessageModel.getMessage().contains(Constants.TYPE_NEW_OWNER)) {
            String messageWithoutOwner = mafiaMessageModel.getMessage().replaceAll(Constants.TYPE_NEW_OWNER, "");
            finalMessage = context.getString(R.string.newOwnerText) + " " + messageWithoutOwner;
        } else {
            finalMessage = StringEscapeUtils.unescapeJava(mafiaMessageModel.getMessage());
        }

        mafiaMessageContainer.setText(finalMessage);

        if (mafiaMessageModel.isSystemMessage()) {
            messageUsername.setText(context.getString(R.string.system));
            mafiaChatUserImage.setImageResource(R.drawable.robot_vector);

            LinearLayout.LayoutParams messageUsernameParams = (LinearLayout.LayoutParams) messageUsername.getLayoutParams();
            LinearLayout.LayoutParams mafiaMessageCardViewParams = (LinearLayout.LayoutParams) mafiaMessageCardView.getLayoutParams();
            LinearLayout.LayoutParams mafiaChatUserImageParams = (LinearLayout.LayoutParams) mafiaChatUserImage.getLayoutParams();

            mafiaMessageCardViewParams.gravity = Gravity.CENTER;
            messageUsernameParams.gravity = Gravity.CENTER;
            mafiaChatUserImageParams.gravity = Gravity.CENTER;

            messageUsername.setLayoutParams(messageUsernameParams);
            mafiaMessageCardView.setLayoutParams(mafiaMessageCardViewParams);
            mafiaChatUserImage.setLayoutParams(mafiaChatUserImageParams);

            mafiaMessageCardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.red));

        } else {

            if (mafiaMessageModel.getSenderId().equals(sharedHelper.getUserId())) {
                LinearLayout.LayoutParams messageUsernameParams = (LinearLayout.LayoutParams) messageUsername.getLayoutParams();
                LinearLayout.LayoutParams mafiaChatUserImageParams = (LinearLayout.LayoutParams) mafiaChatUserImage.getLayoutParams();
                LinearLayout.LayoutParams mafiaMessageCardViewParams = (LinearLayout.LayoutParams) mafiaMessageCardView.getLayoutParams();

                messageUsernameParams.gravity = Gravity.RIGHT;
                mafiaChatUserImageParams.gravity = Gravity.RIGHT;
                mafiaMessageCardViewParams.gravity = Gravity.RIGHT;

                messageUsername.setLayoutParams(messageUsernameParams);
                mafiaChatUserImage.setLayoutParams(mafiaChatUserImageParams);
                mafiaMessageCardView.setLayoutParams(mafiaMessageCardViewParams);

                mafiaMessageCardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
            } else {
                LinearLayout.LayoutParams messageUsernameParams = (LinearLayout.LayoutParams) messageUsername.getLayoutParams();
                LinearLayout.LayoutParams mafiaChatUserImageParams = (LinearLayout.LayoutParams) mafiaChatUserImage.getLayoutParams();
                LinearLayout.LayoutParams mafiaMessageCardViewParams = (LinearLayout.LayoutParams) mafiaMessageCardView.getLayoutParams();

                messageUsernameParams.gravity = Gravity.LEFT;
                mafiaChatUserImageParams.gravity = Gravity.LEFT;
                mafiaMessageCardViewParams.gravity = Gravity.LEFT;

                messageUsername.setLayoutParams(messageUsernameParams);
                mafiaChatUserImage.setLayoutParams(mafiaChatUserImageParams);
                mafiaMessageCardView.setLayoutParams(mafiaMessageCardViewParams);

                mafiaMessageCardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.defaultGroupColor));
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
                        0, R.drawable.user_image_placeholder, mafiaChatUserImage, new ImageLoader.ImageLoadedCallback() {
                            @Override
                            public void onImageLoaded(Object result, Exception e) {
                                if (e != null) {
                                    ImageLoader.loadImage(context, true, true, null, R.drawable.no_image, R.drawable.user_image_placeholder, mafiaChatUserImage, null);
                                }
                            }
                        });
            } else {
                ImageLoader.loadImage(context, true, true, null, R.drawable.no_image, R.drawable.user_image_placeholder, mafiaChatUserImage, null);
            }
        }
    }
}
