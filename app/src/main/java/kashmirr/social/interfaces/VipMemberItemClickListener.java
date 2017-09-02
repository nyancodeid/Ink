package kashmirr.social.interfaces;

import android.support.annotation.Nullable;

import kashmirr.social.models.UserModel;

/**
 * Created by USER on 2017-01-09.
 */

public interface VipMemberItemClickListener {
    void onItemClicked(@Nullable UserModel userModel);

    void onSendCoinsClicked(@Nullable UserModel userModel);

    void onSendMessageClicked(@Nullable UserModel userModel);
}
