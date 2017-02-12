package com.afollestad.polar.transitions;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.os.Build;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.transition.TransitionValues;
import android.transition.Visibility;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class CircularRevealTransition extends Visibility {

    private final static int REVEAL_ANIMATION_DURATION = 550;

    private final float startX;
    private final float startY;

    public CircularRevealTransition(float x, float y) {
        super();
        startX = x;
        startY = y;
    }

    @Override
    public Animator onAppear(ViewGroup sceneRoot, View view, TransitionValues startValues, TransitionValues endValues) {
        return createAnimator(view, true);
    }

    @Override
    public Animator onDisappear(ViewGroup sceneRoot, View view, TransitionValues startValues, TransitionValues endValues) {
        return createAnimator(view, false);
    }

    private Animator createAnimator(View view, boolean appear) {
        float dx = Math.max(view.getMeasuredWidth() - startX, startX);
        float dy = Math.max(view.getMeasuredHeight() - startY, startY);

        float radius = (float) Math.hypot(dx, dy);

        Animator anim = ViewAnimationUtils.createCircularReveal(view, (int) startX, (int) startY, appear ? 0 : radius, appear ? radius : 0);
        anim.setDuration(REVEAL_ANIMATION_DURATION);
        anim.setInterpolator(new FastOutSlowInInterpolator());

        return new NoPauseAnimator(anim);
    }
}
