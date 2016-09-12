package ink.utils;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import com.ink.R;

/**
 * Created by PC-Comp on 9/1/2016.
 */
public class ProgressDialog {

    private static ProgressDialog progressDialog = new ProgressDialog();
    private android.app.ProgressDialog progressDialogBuilder;

    public ProgressDialog buildProgressDialog(Context context, String title,
                                              String message, boolean cancelable) {
        System.gc();
        progressDialogBuilder = new android.app.ProgressDialog(context);
        progressDialogBuilder.setTitle(title);
        progressDialogBuilder.setMessage(message);
        progressDialogBuilder.setProgressDrawable(ContextCompat.getDrawable(context, R.drawable.progress_dialog_circle));
        progressDialogBuilder.setProgressStyle(android.app.ProgressDialog.STYLE_SPINNER);
        progressDialogBuilder.setCancelable(cancelable);
        progressDialogBuilder.setCanceledOnTouchOutside(cancelable);
        progressDialogBuilder.setIndeterminate(true);
        progressDialogBuilder.setIndeterminateDrawable(ContextCompat.getDrawable(context, R.drawable.progress_dialog_circle));
        return this;
    }

    public void show() {
        if (progressDialogBuilder != null) {
            progressDialogBuilder.show();
        }
    }

    public static ProgressDialog get() {
        return progressDialog;
    }

    public void hide() {
        if (progressDialogBuilder != null) {
            progressDialogBuilder.dismiss();
        }
    }

    public void setTitle(String title) {
        if (progressDialogBuilder != null) {
            progressDialogBuilder.setTitle(title);
        }
    }

    public void setMessage(String message) {
        if (progressDialogBuilder != null) {
            progressDialogBuilder.setMessage(message);
        }
    }
}
