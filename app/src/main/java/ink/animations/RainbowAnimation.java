package ink.animations;

import android.animation.FloatEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.text.SpannableString;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.Property;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

/**
 * Created by USER on 2016-08-29.
 */
public class RainbowAnimation {
    private static final long DURATION = DateUtils.MINUTE_IN_MILLIS * 3;

    private static RainbowAnimation rainbowAnimation = new RainbowAnimation();

    private ObjectAnimator objectAnimator;
    private TextView textViewToAttach;
    private String textToShow;
    private boolean stopCalled;
    private SpannableString spannableString;

    public void startRainbowAnimation(Context context,
                                      String textToShow,
                                      final TextView textViewToAttach) {
        stopCalled = false;
        this.textToShow = textToShow;
        this.textViewToAttach = textViewToAttach;

        AnimatedColorSpan span = new AnimatedColorSpan(context);
        final SpannableString spannableString = new SpannableString(textToShow);
        this.spannableString = spannableString;
        String substring = textToShow.toLowerCase();
        int start = textToShow.toLowerCase().indexOf(substring);
        int end = start + substring.length();
        spannableString.setSpan(span, start, end, 0);

        objectAnimator = ObjectAnimator.ofFloat(
                span, ANIMATED_COLOR_SPAN_FLOAT_PROPERTY, 0, 100);
        objectAnimator.setEvaluator(new FloatEvaluator());
        objectAnimator.addUpdateListener(updateListener);
        objectAnimator.setInterpolator(new LinearInterpolator());
        objectAnimator.setDuration(DURATION);
        objectAnimator.setRepeatCount(ValueAnimator.INFINITE);
        objectAnimator.start();
    }

    private static final Property<AnimatedColorSpan, Float> ANIMATED_COLOR_SPAN_FLOAT_PROPERTY
            = new Property<AnimatedColorSpan, Float>(Float.class, "ANIMATED_COLOR_SPAN_FLOAT_PROPERTY") {
        @Override
        public void set(AnimatedColorSpan span, Float value) {
            span.setTranslateXPercentage(value);
        }

        @Override
        public Float get(AnimatedColorSpan span) {
            return span.getTranslateXPercentage();
        }
    };

    public static RainbowAnimation get() {
        return rainbowAnimation;
    }

    public void stopRainbowAnimation() {
        stopCalled = true;
        if (objectAnimator != null) {
            objectAnimator.removeUpdateListener(updateListener);
            objectAnimator.cancel();
            objectAnimator.removeAllListeners();
            objectAnimator.removeAllUpdateListeners();
            textViewToAttach.setText(textToShow);
        }
        System.gc();
    }


    private ValueAnimator.AnimatorUpdateListener updateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            System.gc();
            Log.d("Fafasfasfas", "onAnimationUpdate: outsie");
            if (!stopCalled) {
                Log.d("Fafasfasfas", "onAnimationUpdate: inside true");
                textViewToAttach.setText(spannableString);
            } else {
                stopCalled = true;
                if (objectAnimator != null) {
                    objectAnimator.setDuration(10);
                    objectAnimator.setRepeatCount(0);
                    objectAnimator.removeUpdateListener(this);
                    objectAnimator = null;
                    updateListener = null;
                }
            }
        }
    };
}
