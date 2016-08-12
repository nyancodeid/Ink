package ink.utils;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.view.View;

import ink.interfaces.ItemClickListener;

/**
 * Created by PC-Comp on 8/12/2016.
 */
public class PopupMenu {

    public static void showPopUp(final Context context, View viewToAttach, @Nullable final ItemClickListener<MenuItem> itemClickListener, String... itemsToAdd) {
        System.gc();
        android.support.v7.widget.PopupMenu popupMenu = new android.support.v7.widget.PopupMenu(context, viewToAttach);

        for (int i = 0; i < itemsToAdd.length; i++) {
            popupMenu.getMenu().add(0, i, 0, itemsToAdd[i]);
        }
        popupMenu.setOnMenuItemClickListener(new android.support.v7.widget.PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(item);
                    return true;
                }
                return false;
            }
        });
        popupMenu.show();
    }
}
