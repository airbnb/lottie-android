package com.airbnb.lottie;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PointF;
import android.os.Build;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Animated a view based on a null layer. To use this, set a tag on your view with the key
 * {@link R.id#lottie_layer_name}
 * and the value as the null layer name from After Effects.
 * <p>
 * This supports position, scale, rotation, and anchor point (pivot)
 * <p>
 * Positions will all be relative to the initial point. This is for the ease of use of the animator.
 * Without subtracting the initial position, animators would have to work with the animation in the
 * top left corner of the composition.
 * <p>
 * Anchor points affect the pivot point of the animation and should be set between 0 and 1.
 * Those values will be multiplied by the laid out width and height of the view.
 * For example, setting the anchor to (1, 1) would set the pivot to the bottom right.
 */
public class LottieViewAnimator {
  public static LottieViewAnimator of(Context context, String fileName, View... views) {
    return new LottieViewAnimator(context, fileName, views);
  }

  @SuppressWarnings("FieldCanBeLocal")
  private final LottieComposition.OnCompositionLoadedListener loadedListener =
      new LottieComposition.OnCompositionLoadedListener() {
        @Override public void onCompositionLoaded(LottieComposition composition) {
          setComposition(composition);
        }
      };

  private final ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
  private final Map<String, View> viewsMap;

  private final List<BaseKeyframeAnimation<?, ?>> animatableValues = new ArrayList<>();

  private LottieComposition composition;
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
        for (BaseKeyframeAnimation<?, ?> av : animatableValues) {
          av.setProgress(animation.getAnimatedFraction());
        }
      }
    });

    LottieComposition.fromAssetFileName(context, fileName, loadedListener);
  }

  private void setComposition(LottieComposition composition) {
    this.composition = composition;
    animator.setDuration(composition.getDuration());

    for (final Layer layer : composition.getLayers()) {
      final View view = viewsMap.get(layer.getName());
      if (view == null) {
        continue;
      }

      if (layer.getPosition().hasAnimation()) {
        BaseKeyframeAnimation<?, PointF> position = layer.getPosition().createAnimation();
        position.addUpdateListener(new KeyframeAnimation.AnimationListener<PointF>() {
          @Override
          public void onValueChanged(PointF value) {
            PointF initialPoint = layer.getPosition().getInitialPoint();
            view.setTranslationX(value.x - initialPoint.x);
            view.setTranslationY(value.y - initialPoint.y);
          }
        });
        animatableValues.add(position);
      }

      if (layer.getScale().hasAnimation()) {
        KeyframeAnimation<ScaleXY> scale = layer.getScale().createAnimation();
        scale.addUpdateListener(new KeyframeAnimation.AnimationListener<ScaleXY>() {
          @Override
          public void onValueChanged(ScaleXY value) {
            view.setScaleX(value.getScaleX());
            view.setScaleY(value.getScaleY());
          }
        });
        animatableValues.add(scale);
      }
      ScaleXY initialScale = layer.getScale().getInitialValue();
      view.setScaleX(initialScale.getScaleX());
      view.setScaleY(initialScale.getScaleY());

      if (layer.getRotation().hasAnimation()) {
        KeyframeAnimation<Float> rotation = layer.getRotation().createAnimation();
        rotation.addUpdateListener(new KeyframeAnimation.AnimationListener<Float>() {
          @Override
          public void onValueChanged(Float value) {
            view.setRotation(value);
          }
        });
        animatableValues.add(rotation);
      }
      view.setRotation(layer.getRotation().getInitialValue());

      if (layer.getOpacity().hasAnimation()) {
        KeyframeAnimation<Integer> opacity = layer.getOpacity().createAnimation();
        opacity.addUpdateListener(new KeyframeAnimation.AnimationListener<Integer>() {
          @Override
          public void onValueChanged(Integer value) {
            view.setAlpha(value / 255f);
          }
        });
        animatableValues.add(opacity);
      }
      view.setAlpha(layer.getOpacity().getInitialValue() / 255f);

      if (layer.getAnchor().hasAnimation()) {
        BaseKeyframeAnimation<?, PointF> anchor = layer.getAnchor().createAnimation();
        anchor.addUpdateListener(new KeyframeAnimation.AnimationListener<PointF>() {
          @Override
          public void onValueChanged(PointF value) {
            setViewAnchor(view, value);
          }
        });
      }
      if (view.getWidth() > 0) {
        setViewAnchor(view, layer.getAnchor().getInitialPoint());

      } else {
        view.getViewTreeObserver()
            .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
              @Override
              public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                  view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                  //noinspection deprecation
                  view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                setViewAnchor(view, layer.getAnchor().getInitialPoint());
              }
            });
      }
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

  @SuppressWarnings("unused") public LottieViewAnimator cancel() {
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

  private void setViewAnchor(View view, PointF anchor) {
    view.setPivotX(anchor.x * view.getWidth() / (100f * composition.getScale()));
    view.setPivotY(anchor.y * view.getHeight() / (100f * composition.getScale()));
  }
}
