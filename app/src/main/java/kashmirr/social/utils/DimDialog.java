package kashmirr.social.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;
import android.widget.TextView;

import com.kashmirr.social.R;


/**
 * Created by PC-Comp on 8/17/2016.
 */
public class DimDialog {
    private static Dialog dialog;

    public static void showDimDialog(Context context, String textToShow) {
        if (dialog == null) {
            dialog = new Dialog(context);
            dialog.setContentView(R.layout.request_friend_dialog);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(context, android.R.color.transparent)));

        }
        TextView textView = (TextView) dialog.findViewById(R.id.dimDialogText);
        textView.setText(textToShow);
        dialog.show();
    }

    public static void hideDialog() {
        if (dialog != null) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            dialog = null;
        }
    }

    public static boolean isDialogAlive() {
        return dialog != null && dialog.isShowing();
    }

    public static Dialog createVipLoadingDialog(Context context) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.vip_progress_dialog);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(context, android.R.color.transparent)));
        final ImageView imageView = (ImageView) dialog.findViewById(R.id.vip_place_holder_image);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                imageView.clearAnimation();
            }
        });
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                imageView.clearAnimation();
            }
        });
        return dialog;
    }
}
