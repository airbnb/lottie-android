package com.airbnb.lottie;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PointF;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.airbnb.lottie.animation.KeyframeAnimation;
import com.airbnb.lottie.model.Layer;
import com.airbnb.lottie.model.LottieComposition;
import com.airbnb.lottie.utils.ScaleXY;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LottieViewAnimator {

    public static LottieViewAnimator of(Context context, String fileName, View... views) {
        return new LottieViewAnimator(context, fileName, views);
    }

    private final LottieComposition.OnCompositionLoadedListener loadedListener = new LottieComposition.OnCompositionLoadedListener() {
        @Override
        public void onCompositionLoaded(LottieComposition composition) {
            setComposition(composition);
        }
    };

    private final ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
    private final Map<String, View> viewsMap;

    private final List<KeyframeAnimation<?>> animatableValues = new ArrayList<>();

    private boolean startWhenReady = false;

    private LottieViewAnimator(Context context, String fileName, View... views) {
        viewsMap = new HashMap<>(views.length);

        for (View view : views) {
            Object tag = view.getTag(R.id.lottie_layer_name);
            if (tag != null) {
                viewsMap.put((String) tag, view);
            }
        }

        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                for (KeyframeAnimation<?> av : animatableValues) {
                    av.setProgress(animation.getAnimatedFraction());
                }
            }
        });

        LottieComposition.fromFile(context, fileName, loadedListener);
    }

    private void setComposition(LottieComposition composition) {
        animator.setDuration(composition.getDuration());

        for (Layer layer : composition.getLayers()) {
            final View view = viewsMap.get(layer.getLayerName());
            if (view == null) {
                continue;
            }

            KeyframeAnimation<PointF> position = layer.getPosition().animationForKeyPath();
            position.addUpdateListener(new KeyframeAnimation.AnimationListener<PointF>() {
                @Override
                public void onValueChanged(PointF progress) {
                    view.setTranslationX(progress.x);
                    view.setTranslationY(progress.y);
                }
            });
            animatableValues.add(position);
            PointF initialPosition = layer.getPosition().getInitialPoint();
            view.setTranslationX(initialPosition.x);
            view.setTranslationY(initialPosition.y);

            KeyframeAnimation<ScaleXY> scale = layer.getScale().animationForKeyPath();
            scale.addUpdateListener(new KeyframeAnimation.AnimationListener<ScaleXY>() {
                @Override
                public void onValueChanged(ScaleXY scale) {
                    view.setScaleX(scale.getScaleX());
                    view.setScaleY(scale.getScaleY());
                }
            });
            animatableValues.add(scale);
            ScaleXY initialScale = layer.getScale().getInitialValue();
            view.setScaleX(initialScale.getScaleX());
            view.setScaleY(initialScale.getScaleY());

            KeyframeAnimation<Float> rotation = layer.getRotation().animationForKeyPath();
            rotation.addUpdateListener(new KeyframeAnimation.AnimationListener<Float>() {
                @Override
                public void onValueChanged(Float rotation) {
                    view.setRotation(rotation);
                }
            });
            animatableValues.add(rotation);
            view.setRotation(layer.getRotation().getInitialValue());
        }

        if (startWhenReady) {
            startWhenReady = false;
            start();
        }
    }

    public LottieViewAnimator start() {
        if (animatableValues.isEmpty()) {
            startWhenReady = true;
            return this;
        }

        animator.start();
        return this;
    }

    public LottieViewAnimator cancel() {
        animator.cancel();
        return this;
    }

    public LottieViewAnimator loop(boolean loop) {
        animator.setRepeatCount(loop ? ValueAnimator.INFINITE : 0);
        return this;
    }

    public LottieViewAnimator setProgress(float progress) {
        animator.setCurrentPlayTime((long) (progress * animator.getDuration()));
        return this;
    }
}
