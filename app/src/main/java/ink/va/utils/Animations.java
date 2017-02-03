package ink.va.utils;

import android.animation.Animator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.AnimRes;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by USER on 2016-07-06.
 */
public class Animations {
    private static final long CIRCULAR_REVEAL_ANIMATION_DURATION = 300;

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

    public static void circularOut(View viewToCircularOut, @Nullable Animator.AnimatorListener animatorListener) {
        int cx = viewToCircularOut.getMeasuredWidth() / 2;
        int cy = viewToCircularOut.getMeasuredHeight() / 2;

        int initialRadius = viewToCircularOut.getWidth() / 2;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            Animator animation = ViewAnimationUtils.createCircularReveal(viewToCircularOut, cx, cy, initialRadius, 0);
            animation.addListener(animatorListener);
            animation.start();
        } else {
            if (animatorListener != null) {
                animatorListener.onAnimationStart(null);
                animatorListener.onAnimationEnd(null);
            }
        }
    }

    public static void circularInFromTouch(ViewGroup viewGroup,
                                           int x, int y,
                                           @Nullable Animator.AnimatorListener animatorListener) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            float finalRadius = (float) Math.hypot(viewGroup.getWidth(), viewGroup.getHeight());

            Animator animator;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                animator = ViewAnimationUtils.createCircularReveal(viewGroup, x, y, 0, finalRadius);
                animator.setDuration(CIRCULAR_REVEAL_ANIMATION_DURATION);
                animator.setInterpolator(new AccelerateDecelerateInterpolator());
                if (animatorListener != null) {
                    animator.addListener(animatorListener);
                }
                animator.start();
            }
        } else {
            if (animatorListener != null) {
                animatorListener.onAnimationStart(null);
                animatorListener.onAnimationEnd(null);
            }
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


    public static int startRecyclerItemAnimation(Context context,
                                                 View viewToAnimate,
                                                 int position, int lastPosition,
                                                 @AnimRes int animationResource) {
        if (position > lastPosition) {
            Animation animation = android.view.animation.AnimationUtils.loadAnimation(context, animationResource);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
        return lastPosition;
    }

    public static void flip(final View viewToFlip, final Drawable resourceToApply) {
        viewToFlip.setRotationY(0f);
        viewToFlip.animate().rotationY(90f).setListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                viewToFlip.setBackground(resourceToApply);
                viewToFlip.setRotationY(270f);
                viewToFlip.animate().rotationY(360f).setListener(null);

            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }
        });
    }
}
