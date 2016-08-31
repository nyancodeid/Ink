package ink.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;

import ink.callbacks.GeneralCallback;
import yuku.ambilwarna.AmbilWarnaDialog;

/**
 * Created by PC-Comp on 8/30/2016.
 */
public class ColorUtils {
    public static Drawable tintDrawable(Context context, int drawableResource, int desiredColor) {
        Drawable circleDrawable = ContextCompat.getDrawable(context, drawableResource);
        Drawable wrappedDrawable = DrawableCompat.wrap(circleDrawable);
        if (circleDrawable != null && wrappedDrawable != null) {
            circleDrawable.mutate();
            DrawableCompat.setTint(wrappedDrawable, desiredColor);
        }
        return wrappedDrawable;
    }

    public static String getSimpleHexColor(int color) {
        String hexWithoutAlpha = Integer.toHexString(color).toUpperCase().substring(2);
        return hexWithoutAlpha;
    }

    private void showColorPicker(Context context, final GeneralCallback<String> generalCallback) {
        AmbilWarnaDialog dialog = new AmbilWarnaDialog(context, Color.parseColor("#3F51B5"), new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                // color is the color selected by the user.
                String hexWithoutAlpha = Integer.toHexString(color).toUpperCase().substring(2);
                generalCallback.onSuccess("#" + hexWithoutAlpha);
            }

            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
                generalCallback.onFailure(null);
            }
        });
        dialog.show();

    }
}
