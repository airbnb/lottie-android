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

  private final BaseKeyframeAnimation.AnimationListener listener;
  private final BaseKeyframeAnimation<Integer, Integer> color;
  private final BaseKeyframeAnimation<Float, Float> opacity;
  private final BaseKeyframeAnimation<Float, Float> direction;
  private final BaseKeyframeAnimation<Float, Float> distance;
  private final BaseKeyframeAnimation<Float, Float> radius;

  private boolean isDirty = true;

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
    isDirty = true;
    listener.onValueChanged();
  }

  public void applyTo(Paint paint) {
    if (!isDirty) {
      return;
    }
    isDirty = false;

    double directionRad = ((double) direction.getValue()) * DEG_TO_RAD;
    float distance = this.distance.getValue();
    float x = ((float) Math.sin(directionRad)) * distance;
    float y = ((float) Math.cos(directionRad + Math.PI)) * distance;
    int baseColor = color.getValue();
    int opacity = Math.round(this.opacity.getValue());
    int color = Color.argb(opacity, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor));
    float radius = this.radius.getValue();
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
