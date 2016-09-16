package com.airbnb.lotte.animation;


import android.animation.ValueAnimator;
import android.view.animation.Animation;

public class LotteAnimation {
    private final ValueAnimator animator = new ValueAnimator();

    private float speed = 1f;
    private long duration;
    private long startDelay;
    private Animation.AnimationListener listener;
    private boolean loop;
    private boolean autoReverse;

    public void setDuration(long duration) {
        this.duration = duration;
        animator.setDuration(getScaledDuration(duration));
    }

    public void setStartDelay(long startDelay) {
        this.startDelay = startDelay;
        animator.setStartDelay(startDelay);
    }

    public void setListener(Animation.AnimationListener listener) {
        this.listener = listener;
    }

    public void loop() {
        loop(true);
    }

    public void loop(boolean loop) {
        animator.setRepeatCount(loop ? Integer.MAX_VALUE : 0);
        this.loop = loop;
    }

    public void setAutoReverse() {
        setAutoReverse(true);
    }

    public void setAutoReverse(boolean autoReverse) {
        this.autoReverse = autoReverse;
        animator.setRepeatMode(autoReverse ? ValueAnimator.REVERSE : ValueAnimator.RESTART);
    }

    public boolean isPlaying() {
        return animator.isRunning();
    }

    private long getScaledDuration(long duration) {
        return (long) (duration * speed);
    }
}
