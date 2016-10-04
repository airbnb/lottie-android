package com.airbnb.lotte.layers;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.animation.LinearInterpolator;

public class RootLotteAnimatableLayer extends LotteAnimatableLayer {

    private final ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);

    public RootLotteAnimatableLayer(Drawable.Callback callback) {
        super(0, callback);
        animator.setRepeatCount(0);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setProgress(animation.getAnimatedFraction());
            }
        });
    }

    public void setCompDuration(long duration) {
        animator.setDuration(duration);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        super.draw(canvas);
        canvas.clipRect(getBounds());
    }

    public void loop(boolean loop) {
        animator.setRepeatCount(loop ? ValueAnimator.INFINITE : 0);
    }

    public void play() {
        animator.start();
    }

    public void pause() {
        animator.start();
    }

    public void addAnimatorUpdateListener(ValueAnimator.AnimatorUpdateListener updateListener) {
        animator.addUpdateListener(updateListener);
    }

    public void removeAnimatorUpdateListener(ValueAnimator.AnimatorUpdateListener updateListener) {
        animator.removeUpdateListener(updateListener);
    }

    public void addAnimatorListener(Animator.AnimatorListener listener) {
        animator.addListener(listener);
    }

    public void removeAnimatorListener(Animator.AnimatorListener listener) {
        animator.removeListener(listener);
    }

    @Override
    public int getIntrinsicWidth() {
        return getBounds().width();
    }

    @Override
    public int getIntrinsicHeight() {
        return getBounds().height();
    }
}
