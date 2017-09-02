package kashmirr.social.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.widget.Toast;

import com.kashmirr.social.R;


/**
 * Created by USER on 2017-02-22.
 */

public class ClipManager {

    public static void copy(Context context, String textToCopy) {
        if (textToCopy.isEmpty()) {
            Toast.makeText(context, context.getString(R.string.nothingToCopy), Toast.LENGTH_SHORT).show();
        } else {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(context.getString(R.string.messageText), textToCopy);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, context.getString(R.string.copied), Toast.LENGTH_SHORT).show();
        }
    }
}
