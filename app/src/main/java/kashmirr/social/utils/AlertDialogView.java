package kashmirr.social.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;

/**
 * Created by USER on 2016-08-07.
 */
public class AlertDialogView {

    public static void buildAlertDialog(Context context,
                                        String title,
                                        String message,
                                        @Nullable boolean isCancelable) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(isCancelable);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }
}
