package ink.va.utils;

import android.animation.Animator;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by USER on 2016-07-06.
 */
public class Animations {

    public static void animateCircular(View view) {

        int cx = view.getMeasuredWidth() / 2;
        int cy = view.getMeasuredHeight() / 2;

        int finalRadius = Math.max(view.getWidth(), view.getHeight()) / 2;

        Animator anim = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, finalRadius);
        }

        if (anim != null) {
            anim.start();
        }
    }


    public static void expand(final View viewToExpand) {
        viewToExpand.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int targetHeight = viewToExpand.getMeasuredHeight();

        viewToExpand.getLayoutParams().height = 1;
        viewToExpand.setVisibility(View.VISIBLE);
        Animation animation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                viewToExpand.getLayoutParams().height = interpolatedTime == 1
                        ? ViewGroup.LayoutParams.WRAP_CONTENT
                        : (int) (targetHeight * interpolatedTime);
                viewToExpand.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        animation.setDuration(200);
        viewToExpand.startAnimation(animation);
    }

    public static void collapse(final View viewToCollapse) {
        final int initialHeight = viewToCollapse.getMeasuredHeight();

        Animation animation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    viewToCollapse.setVisibility(View.GONE);
                } else {
                    viewToCollapse.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    viewToCollapse.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        animation.setDuration(200);
        viewToCollapse.startAnimation(animation);
    }
}
