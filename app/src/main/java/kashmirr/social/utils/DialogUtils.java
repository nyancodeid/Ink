package kashmirr.social.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.kashmirr.social.R;

import kashmirr.social.interfaces.ItemClickListener;

/**
 * Created by PC-Comp on 8/12/2016.
 */
public class DialogUtils {
    private static Dialog dialog;

    public static void showPopUp(final Context context, View viewToAttach, @Nullable final ItemClickListener<MenuItem> itemClickListener, String... itemsToAdd) {
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
        }
        builder.show();
    }

    public interface DialogListener {
        void onNegativeClicked();

        void onDialogDismissed();

        void onPositiveClicked();
    }

    public static void showCustomDialog(Context context, String requestContentText,
                                        @Nullable String positiveButtonText, String customDialogHeaderText,
                                        @Nullable final DialogListener dialogListener) {
        if (dialog != null) {
            if (!dialog.isShowing()) {
                dialog = new Dialog(context, R.style.FullscreenTheme);
                dialog.setContentView(R.layout.request_permission_view);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                TextView positiveButton = (TextView) dialog.findViewById(R.id.positiveButton);
                TextView customDialogHeaderContent = (TextView) dialog.findViewById(R.id.customDialogHeaderContent);
                customDialogHeaderContent.setText(customDialogHeaderText);
                if (positiveButtonText != null) {
                    positiveButton.setText(positiveButtonText);
                }
                View cancel = dialog.findViewById(R.id.cancelRequestPermission);
                TextView permissionRequestTV = (TextView) dialog.findViewById(R.id.permissionRequestTV);
                permissionRequestTV.setText(requestContentText);

                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (dialogListener != null) {
                            dialogListener.onNegativeClicked();
                        }
                        dialog.hide();
                        dialog = null;
                    }
                });
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (dialogListener != null) {
                            dialogListener.onPositiveClicked();
                        }
                        dialog.hide();
                        dialog = null;
                    }
                });
                dialog.show();
            }
        } else {
            dialog = new Dialog(context, R.style.FullscreenTheme);
            dialog.setContentView(R.layout.request_permission_view);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            TextView positiveButton = (TextView) dialog.findViewById(R.id.positiveButton);
            TextView customDialogHeaderContent = (TextView) dialog.findViewById(R.id.customDialogHeaderContent);
            customDialogHeaderContent.setText(customDialogHeaderText);
            if (positiveButtonText != null) {
                positiveButton.setText(positiveButtonText);
            }
            View cancel = dialog.findViewById(R.id.cancelRequestPermission);
            TextView permissionRequestTV = (TextView) dialog.findViewById(R.id.permissionRequestTV);
            permissionRequestTV.setText(requestContentText);

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (dialogListener != null) {
                        dialogListener.onNegativeClicked();
                    }
                    dialog.hide();
                    dialog = null;
                }
            });
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (dialogListener != null) {
                        dialogListener.onPositiveClicked();
                    }
                    dialog.hide();
                    dialog = null;
                }
            });
            dialog.show();
        }
    }
}
