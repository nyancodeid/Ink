package ink.animations;

import android.animation.Animator;
import android.animation.FloatEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.format.DateUtils;
import android.util.Property;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

/**
 * Created by USER on 2016-08-29.
 */
public class RainbowAnimation {
    public static final long DURATION = DateUtils.MINUTE_IN_MILLIS * 3;

    public static ObjectAnimator startRainbowAnimation(Context context,
                                                       String textToShow,
                                                       final TextView textViewToAttach,
                                                       @Nullable Animator.AnimatorListener animatorListener,
                                                       @Nullable int duration) {
        AnimatedColorSpan span = new AnimatedColorSpan(context);

        final SpannableString spannableString = new SpannableString(textToShow);
        String substring = textToShow.toLowerCase();
        int start = textToShow.toLowerCase().indexOf(substring);
        int end = start + substring.length();
        spannableString.setSpan(span, start, end, 0);

        final ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(
                span, ANIMATED_COLOR_SPAN_FLOAT_PROPERTY, 0, 100);
        objectAnimator.setEvaluator(new FloatEvaluator());
        objectAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                textViewToAttach.setText(spannableString);
            }
        });
        objectAnimator.setInterpolator(new LinearInterpolator());
        if (duration != 0) {
            objectAnimator.setDuration(duration);
        } else {
            objectAnimator.setDuration(DURATION);
        }
        objectAnimator.setRepeatCount(ValueAnimator.INFINITE);
        objectAnimator.start();
        if (animatorListener != null) {
            objectAnimator.addListener(animatorListener);
        }
        return objectAnimator;
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
}
