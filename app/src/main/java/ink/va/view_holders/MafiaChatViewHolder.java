package ink.va.view_holders;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ink.va.R;

import org.apache.commons.lang3.StringEscapeUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import ink.va.models.MafiaMessageModel;
import ink.va.models.UserModel;
import ink.va.utils.SharedHelper;

/**
 * Created by PC-Comp on 3/13/2017.
 */

public class MafiaChatViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.messageUsername)
    TextView messageUsername;
    @BindView(R.id.mafiaMessageWrapper)
    CardView mafiaMessageWrapper;
    @BindView(R.id.mafiaMessageContainer)
    TextView mafiaMessageContainer;
    private SharedHelper sharedHelper;

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
        mafiaMessageContainer.setText(StringEscapeUtils.unescapeJava(mafiaMessageModel.getMessage()));

        if (mafiaMessageModel.getSenderId().equals(sharedHelper.getUserId())) {
            LinearLayout.LayoutParams messageUsernameParams = (LinearLayout.LayoutParams) messageUsername.getLayoutParams();
            LinearLayout.LayoutParams mafiaMessageContainerParams = (LinearLayout.LayoutParams) mafiaMessageContainer.getLayoutParams();
            messageUsernameParams.gravity = Gravity.RIGHT;
            mafiaMessageContainerParams.gravity = Gravity.RIGHT;
            messageUsername.setLayoutParams(messageUsernameParams);
            mafiaMessageContainer.setLayoutParams(mafiaMessageContainerParams);
            mafiaMessageWrapper.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
        } else {
            LinearLayout.LayoutParams messageUsernameParams = (LinearLayout.LayoutParams) messageUsername.getLayoutParams();
            LinearLayout.LayoutParams mafiaMessageContainerParams = (LinearLayout.LayoutParams) mafiaMessageContainer.getLayoutParams();
            messageUsernameParams.gravity = Gravity.LEFT;
            mafiaMessageContainerParams.gravity = Gravity.LEFT;
            messageUsername.setLayoutParams(messageUsernameParams);
            mafiaMessageContainer.setLayoutParams(mafiaMessageContainerParams);
            mafiaMessageWrapper.setCardBackgroundColor(ContextCompat.getColor(context, R.color.defaultGroupColor));
        }
    }
}
