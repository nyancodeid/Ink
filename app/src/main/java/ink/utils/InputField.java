package ink.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.ink.R;

/**
 * Created by PC-Comp on 8/15/2016.
 */
public class InputField {

    public static void createInputFieldView(final Context context,
                                            @Nullable final ClickHandler clickHandler,
                                            @Nullable String text, @Nullable String hint,
                                            @Nullable String additionalText) {


        View newCommentView = ((Activity) context).getLayoutInflater().inflate(R.layout.new_comment_body, null);
        final EditText newCommentBody = (EditText) newCommentView.findViewById(R.id.newCommentBody);
        newCommentBody.getBackground().mutate().setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
        final TextView additionTextField = (TextView) newCommentView.findViewById(R.id.additionTextField);
        if (additionalText != null) {
            additionTextField.setText(additionalText);
        }
        if (hint != null) {
            newCommentBody.setHint(hint);
        }
        if (text != null) {
            newCommentBody.setText(text);
            newCommentBody.setSelection(text.length());
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(newCommentView);
        builder.setCancelable(false);
        builder.setPositiveButton(context.getString(R.string.saveText), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //just override for dialog not to close automatically
            }
        });
        builder.setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //just override for dialog not to close automatically
            }
        });

        final AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String finalBody = newCommentBody.getText().toString().trim();
                if (finalBody.isEmpty()) {
                    newCommentBody.setError(context.getString(R.string.fieldEmptyError));
                } else {
                    if (clickHandler != null) {
                        clickHandler.onPositiveClicked(finalBody, dialog);
                    }
                }
            }
        });
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (clickHandler != null) {
                    clickHandler.onNegativeClicked(null, dialog);
                }
            }
        });
    }

    public interface ClickHandler {
        void onPositiveClicked(Object... result);

        void onNegativeClicked(Object... result);
    }
}
