package com.airbnb.lottie.animation.keyframe;

import android.graphics.Color;
import android.graphics.Paint;

import androidx.annotation.Nullable;

import com.airbnb.lottie.model.layer.BaseLayer;
import com.airbnb.lottie.parser.DropShadowEffect;
import com.airbnb.lottie.value.LottieFrameInfo;
import com.airbnb.lottie.value.LottieValueCallback;


public class DropShadowKeyframeAnimation implements BaseKeyframeAnimation.AnimationListener {
  private static final double DEG_TO_RAD = Math.PI / 180.0;

  // We scale the parsed distance and softness values by a constant factor so that the Paint.setShadowLayer() call
  // gives results that more closely match After Effects
  private static final float AFTER_EFFECTS_DISTANCE_SCALE_FACTOR = 1.33f;
  private static final float AFTER_EFFECT_SOFTNESS_SCALE_FACTOR = 0.43f;

  private final BaseKeyframeAnimation.AnimationListener listener;
  private final BaseKeyframeAnimation<Integer, Integer> color;
  private final BaseKeyframeAnimation<Float, Float> opacity;
  private final BaseKeyframeAnimation<Float, Float> direction;
  private final BaseKeyframeAnimation<Float, Float> distance;
  private final BaseKeyframeAnimation<Float, Float> radius;

  public DropShadowKeyframeAnimation(BaseKeyframeAnimation.AnimationListener listener, BaseLayer layer, DropShadowEffect dropShadowEffect) {
    this.listener = listener;
    color = dropShadowEffect.getColor().createAnimation();
    color.addUpdateListener(this);
    layer.addAnimation(color);
    opacity = dropShadowEffect.getOpacity().createAnimation();
    opacity.addUpdateListener(this);
    layer.addAnimation(opacity);
    direction = dropShadowEffect.getDirection().createAnimation();
    direction.addUpdateListener(this);
    layer.addAnimation(direction);
    distance = dropShadowEffect.getDistance().createAnimation();
    distance.addUpdateListener(this);
    layer.addAnimation(distance);
    radius = dropShadowEffect.getRadius().createAnimation();
    radius.addUpdateListener(this);
    layer.addAnimation(radius);
  }

  @Override public void onValueChanged() {
    listener.onValueChanged();
  }

  /**
   * Applies a shadow to the provided Paint object, which will be applied to the Canvas behind whatever is drawn
   * (a shape, bitmap, path, etc.)
   * @param parentAlpha A value between 0 and 255 representing the combined alpha of all parents of this drop shadow effect.
   *                    E.g. The layer via transform, the fill/stroke via its opacity, etc.
   */
  public void applyTo(Paint paint, int parentAlpha) {
    double directionRad = ((double) direction.getValue()) * DEG_TO_RAD;
    float distance = this.distance.getValue() * AFTER_EFFECTS_DISTANCE_SCALE_FACTOR;
    float x = ((float) Math.sin(directionRad)) * distance;
    float y = ((float) Math.cos(directionRad + Math.PI)) * distance;

    int baseColor = color.getValue();
    int opacity = Math.round(this.opacity.getValue() * parentAlpha / 255f);
    int color = Color.argb(opacity, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor));

    // Paint.setShadowLayer() removes the shadow if radius is 0, so we use a small nonzero value in that case
    float radius = Math.max(this.radius.getValue() * AFTER_EFFECT_SOFTNESS_SCALE_FACTOR, Float.MIN_VALUE);
    paint.setShadowLayer(radius, x, y, color);
  }

  public void setColorCallback(@Nullable  LottieValueCallback<Integer> callback) {
   color.setValueCallback(callback);
  }

  public void setOpacityCallback(@Nullable final LottieValueCallback<Float> callback) {
    if (callback == null) {
      opacity.setValueCallback(null);
      return;
    }
    opacity.setValueCallback(new LottieValueCallback<Float>() {
      @Nullable
      @Override
      public Float getValue(LottieFrameInfo<Float> frameInfo) {
        Float value = callback.getValue(frameInfo);
        if (value == null) {
          return null;
        }
        // Convert [0,100] to [0,255] because other dynamic properties use [0,100].
        return value * 2.55f;
      }
    });
  }

  public void setDirectionCallback(@Nullable LottieValueCallback<Float> callback) {
    direction.setValueCallback(callback);
  }

  public void setDistanceCallback(@Nullable LottieValueCallback<Float> callback) {
    distance.setValueCallback(callback);
  }

  public void setRadiusCallback(@Nullable LottieValueCallback<Float> callback) {
    radius.setValueCallback(callback);
  }
}
