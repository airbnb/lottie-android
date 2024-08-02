package com.airbnb.lottie.animation.keyframe;

import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;

import androidx.annotation.Nullable;

import com.airbnb.lottie.model.layer.BaseLayer;
import com.airbnb.lottie.parser.DropShadowEffect;
import com.airbnb.lottie.value.LottieFrameInfo;
import com.airbnb.lottie.value.LottieValueCallback;


public class DropShadowKeyframeAnimation implements BaseKeyframeAnimation.AnimationListener {
  private static final float DEG_TO_RAD = (float) (Math.PI / 180.0);

  // We scale the parsed distance and softness values by a constant factor so that the Paint.setShadowLayer() call
  // gives results that more closely match After Effects
  private static final float AFTER_EFFECTS_DISTANCE_SCALE_FACTOR = 1.33f;
  private static final float AFTER_EFFECT_SOFTNESS_SCALE_FACTOR = 0.43f;

  private final BaseKeyframeAnimation.AnimationListener listener;
  private final BaseKeyframeAnimation<Integer, Integer> color;
  private final FloatKeyframeAnimation opacity;
  private final FloatKeyframeAnimation direction;
  private final FloatKeyframeAnimation distance;
  private final FloatKeyframeAnimation radius;

  // Cached paint values.
  private float paintRadius = Float.NaN;
  private float paintX = Float.NaN;
  private float paintY = Float.NaN;
  // 0 is a valid color but it is transparent so it will not draw anything anyway.
  private int paintColor = 0;

  private final float[] distanceSrc = new float[2];
  private final float[] distanceDst = new float[2];

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
  public void applyTo(Paint paint, Matrix parentMatrix, int parentAlpha) {
    float directionRad = this.direction.getFloatValue() * DEG_TO_RAD;
    float distance = this.distance.getValue(); // * AFTER_EFFECTS_DISTANCE_SCALE_FACTOR;
    float rawX = ((float) Math.sin(directionRad)) * distance;
    float rawY = ((float) Math.cos(directionRad + Math.PI)) * distance;

    float[] matrixValues = new float[9];
    parentMatrix.getValues(matrixValues);
    float scaleX = matrixValues[Matrix.MSCALE_X];
    float scaleY = matrixValues[Matrix.MSCALE_Y];
    float x = rawX * scaleX;
    float y = rawY * scaleY;

    int baseColor = color.getValue();
    int opacity = Math.round(this.opacity.getValue() * parentAlpha / 255f);
    int color = Color.argb(opacity, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor));

    // Paint.setShadowLayer() removes the shadow if radius is 0, so we use a small nonzero value in that case
    float radius = Math.max(this.radius.getValue() * AFTER_EFFECT_SOFTNESS_SCALE_FACTOR, Float.MIN_VALUE);

    if (paintRadius == radius && paintX == x && paintY == y && paintColor == color) {
      return;
    }
    paintRadius = radius;
    paintX = x;
    paintY = y;
    paintColor = color;
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
