package ink.va.view_holders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.google.cloud.translate.Translation;
import com.ink.va.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.va.interfaces.RecyclerItemClickListener;
import ink.va.models.UserNotificationModel;
import ink.va.utils.Constants;
import ink.va.utils.LanguageUtils;
import ink.va.utils.TranslationUtils;
import lombok.Setter;

/**
 * Created by USER on 2017-03-23.
 */

public class NotificationViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.notificationTitleTV)
    TextView notificationTitleTV;
    @BindView(R.id.notificationMessageTV)
    TextView notificationMessageTV;

    private UserNotificationModel userNotificationModel;

    @Setter
    private RecyclerItemClickListener onItemClickListener;

    public NotificationViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }


    public void initData(final UserNotificationModel userNotificationModel, Context context) {
        this.userNotificationModel = userNotificationModel;
        TranslationUtils.Translate(userNotificationModel.getNotificationTitle(),
                Constants.APP_SOURCE_LANGUAGE, LanguageUtils.getLocalLanguage(context), new TranslationUtils.TranslationCallback() {
                    @Override
                    public void onTranslationDone(Translation result) {
                        notificationTitleTV.setText(result.getTranslatedText());
                    }

                    @Override
                    public void onTranslationFailed(Exception e) {
                        notificationTitleTV.setText(userNotificationModel.getNotificationTitle());
                    }
                });

        TranslationUtils.Translate(userNotificationModel.getNotificationText(),
                Constants.APP_SOURCE_LANGUAGE, LanguageUtils.getLocalLanguage(context), new TranslationUtils.TranslationCallback() {
                    @Override
                    public void onTranslationDone(Translation result) {
                        notificationMessageTV.setText(result.getTranslatedText());
                    }

                    @Override
                    public void onTranslationFailed(Exception e) {
                        notificationMessageTV.setText(userNotificationModel.getNotificationText());
                    }
                });

    }

    @OnClick(R.id.removeNotificationIV)
    public void removeNotificationIVClicked() {
        if (onItemClickListener != null) {
            onItemClickListener.onAdditionalItemClicked(userNotificationModel);
        }
    }

    @OnClick(R.id.notificationParentLayout)
    public void notificationParentLayoutClicked() {
        if (onItemClickListener != null) {
            onItemClickListener.onItemClicked(userNotificationModel);
        }
    }
}
