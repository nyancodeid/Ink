package ink.va.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;
import android.view.View;

import ink.va.interfaces.ItemClickListener;

/**
 * Created by PC-Comp on 8/12/2016.
 */
public class DialogUtils {

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

    public static void showDialog(Context context, String title,
                                  String content, boolean cancelable,
                                  @Nullable final DialogListener dialogListener, boolean hasNegative,
                                  @Nullable String negativeButtonText) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setCancelable(cancelable);
        builder.setMessage(content);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                if (dialogListener != null) {
                    dialogListener.onPositiveClicked();
                }
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                dialogInterface.dismiss();
                if (dialogListener != null) {
                    dialogListener.onDialogDismissed();
                }
            }
        });
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                dialogInterface.dismiss();
                if (dialogListener != null) {
                    dialogListener.onDialogDismissed();
                }
            }
        });
        if (hasNegative) {
            builder.setNegativeButton(negativeButtonText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    if (dialogListener != null) {
                        dialogListener.onNegativeClicked();
                    }
                }
            });
            builder.show();
        }

    }

    public interface DialogListener {
        void onNegativeClicked();

        void onDialogDismissed();

        void onPositiveClicked();
    }
}
