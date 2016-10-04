package com.airbnb.lotte.layers;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.animation.LinearInterpolator;

public class RootLotteAnimatableLayer extends LotteAnimatableLayer {

    private final ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);

    public RootLotteAnimatableLayer(long duration, Drawable.Callback callback) {
        super(duration, callback);
        animator.setDuration(duration);
        animator.setRepeatCount(0);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setProgress(animation.getAnimatedFraction());
            }
        });
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

    @Override
    public int getIntrinsicWidth() {
        return getBounds().width();
    }

    @Override
    public int getIntrinsicHeight() {
        return getBounds().height();
    }
}
