package ink.va.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by PC-Comp on 3/3/2017.
 */

public class TransparentPanel extends RelativeLayout {
    private Paint innerPaint;

    public TransparentPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        innerPaint = new Paint();
        setDay();
    }

    public TransparentPanel(Context context) {
        super(context);
        innerPaint = new Paint();
        setDay();
    }

    public void setNight() {
        innerPaint.setARGB(180, 75, 75, 75);
        invalidate();
    }

    public void setDay() {
        innerPaint.setARGB(255, 255, 255, 255);
        invalidate();
    }


    @Override
    protected void dispatchDraw(Canvas canvas) {

        RectF drawRect = new RectF();
        drawRect.set(0, 0, getMeasuredWidth(), getMeasuredHeight());

        canvas.drawRoundRect(drawRect, 5, 5, innerPaint);

        super.dispatchDraw(canvas);
    }
}