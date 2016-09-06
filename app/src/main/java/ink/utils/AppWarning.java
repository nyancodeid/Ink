package ink.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.ink.R;

import ink.callbacks.GeneralCallback;

/**
 * Created by PC-Comp on 9/6/2016.
 */
public class AppWarning {
    private static final AppWarning appWarning = new AppWarning();
    private WindowManager windowManager;
    private View warningView;

    public void showWarning(Context context, String textContent, String buttonTitle, @Nullable final GeneralCallback onButtonClickListener) {
        windowManager = (WindowManager) context.getSystemService(context.WINDOW_SERVICE);
        warningView = ((Activity) context).getLayoutInflater().inflate(R.layout.app_warning_view, null);
        Button warningButton = (Button) warningView.findViewById(R.id.warningButton);
        TextView warningText = (TextView) warningView.findViewById(R.id.warningText);
        warningText.setText(textContent);
        warningButton.setText(buttonTitle);
        warningButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onButtonClickListener != null) {
                    onButtonClickListener.onSuccess(null);
                }
            }
        });
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.CENTER;
        windowManager.addView(warningView, params);
    }

    public void hideWarning() {
        if (windowManager != null && warningView != null) {
            windowManager.removeView(warningView);
        }
    }

    public static AppWarning get() {
        return appWarning;
    }
}
