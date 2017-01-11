package ink.va.interfaces;

import android.view.View;

import ink.va.models.VipGlobalChatModel;

/**
 * Created by PC-Comp on 1/11/2017.
 */

public interface VipGlobalChatClickListener {
    void onItemClicked(VipGlobalChatModel vipGlobalChatModel);

    void onMoreIconClicked(View clickedView, VipGlobalChatModel vipGlobalChatModel);
}
