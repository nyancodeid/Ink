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
        setArgbWhite();
    }

    public TransparentPanel(Context context) {
        super(context);
        innerPaint = new Paint();
        setArgbWhite();
    }

    public void setArgbDim() {
        innerPaint.setARGB(180, 75, 75, 75);
    }

    public void setArgbWhite() {
        innerPaint.setARGB(255, 255, 255, 255);
    }


    @Override
    protected void dispatchDraw(Canvas canvas) {

        RectF drawRect = new RectF();
        drawRect.set(0, 0, getMeasuredWidth(), getMeasuredHeight());

        canvas.drawRoundRect(drawRect, 5, 5, innerPaint);

        super.dispatchDraw(canvas);
    }
}