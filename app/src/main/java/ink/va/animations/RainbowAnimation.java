package ink.va.animations;

import android.animation.FloatEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.text.SpannableString;
import android.text.format.DateUtils;
import android.util.Property;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by USER on 2016-08-29.
 */
public class RainbowAnimation {

    private static RainbowAnimation rainbowAnimation = new RainbowAnimation();

    private boolean stopCalled;
    private ArrayList<ObjectAnimator> objectAnimators = new ArrayList<>();

    public void startRainbowAnimation(Context context,
                                      final String textToShow,
                                      final TextView textViewToAttach) {
        stopCalled = false;
        AnimatedColorSpan span = new AnimatedColorSpan(context);
        final SpannableString spannableString = new SpannableString(textToShow);
        String substring = textToShow;
        int start = textToShow.indexOf(substring);
        int end = start + substring.length();
        spannableString.setSpan(span, start, end, 0);

        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(
                span, animatedColorSpanFloatProperty, 0, 100);
        objectAnimator.setEvaluator(new FloatEvaluator());

        objectAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (!stopCalled) {
                    textViewToAttach.setText(spannableString);
                }
            }
        });
        objectAnimator.setInterpolator(new LinearInterpolator());
        objectAnimator.setDuration(DateUtils.MINUTE_IN_MILLIS * 3);
        objectAnimator.setRepeatCount(ValueAnimator.INFINITE);
        objectAnimator.start();
        objectAnimators.add(objectAnimator);
    }

    private static final Property<AnimatedColorSpan, Float> animatedColorSpanFloatProperty
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
        if (!objectAnimators.isEmpty()) {
            for (int i = 0; i < objectAnimators.size(); i++) {
                ObjectAnimator eachAnimator = objectAnimators.get(i);
                eachAnimator.setRepeatCount(0);
                eachAnimator.end();
                eachAnimator.cancel();
                eachAnimator.removeAllListeners();
                eachAnimator.removeAllUpdateListeners();
            }
            objectAnimators.clear();
        }
    }
}
