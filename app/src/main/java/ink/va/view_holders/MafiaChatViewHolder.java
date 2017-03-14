package ink.va.view_holders;

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

import com.ink.va.R;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.apache.commons.lang3.StringEscapeUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import ink.va.models.MafiaMessageModel;
import ink.va.models.UserModel;
import ink.va.utils.CircleTransform;
import ink.va.utils.Constants;
import ink.va.utils.SharedHelper;

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

    public MafiaChatViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void initData(MafiaMessageModel mafiaMessageModel, Context context) {
        if (sharedHelper == null) {
            sharedHelper = new SharedHelper(context);
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
            mafiaChatUserImage.setLayoutParams(mafiaMessageCardViewParams);

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

                Ion.with(context).load(url)
                        .withBitmap().placeholder(R.drawable.user_image_placeholder).transform(new CircleTransform()).
                        intoImageView(mafiaChatUserImage).setCallback(new FutureCallback<ImageView>() {
                    @Override
                    public void onCompleted(Exception e, ImageView result) {
                        if (e != null) {
                            mafiaChatUserImage.setImageResource(R.drawable.user_image_placeholder);
                        }
                    }
                });
            } else {
                Ion.with(context).load(Constants.ANDROID_DRAWABLE_DIR + "no_image")
                        .withBitmap().transform(new CircleTransform()).intoImageView(mafiaChatUserImage);
            }
        }
    }
}
