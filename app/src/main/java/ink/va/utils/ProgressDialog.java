package ink.va.utils;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import com.ink.va.R;


/**
 * Created by PC-Comp on 9/1/2016.
 */
public class ProgressDialog {

    private static ProgressDialog progressDialog = new ProgressDialog();
    private android.app.ProgressDialog progressDialogBuilder;

    public ProgressDialog buildProgressDialog(Context context, boolean defaultProgress) {
        progressDialogBuilder = new android.app.ProgressDialog(context);
        if (!defaultProgress) {
            progressDialogBuilder.setProgressDrawable(ContextCompat.getDrawable(context, R.drawable.progress_dialog_circle));
            progressDialogBuilder.setProgressStyle(android.app.ProgressDialog.STYLE_SPINNER);
            progressDialogBuilder.setIndeterminate(true);
            progressDialogBuilder.setIndeterminateDrawable(ContextCompat.getDrawable(context, R.drawable.progress_dialog_circle));
        }

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

    public void setCancelable(boolean cancelable) {
        if (progressDialogBuilder != null) {
            progressDialogBuilder.setCancelable(cancelable);
        }
    }
}
