package com.airbnb.lottie.animation.keyframe;

import android.graphics.Color;
import android.graphics.Matrix;
import androidx.annotation.Nullable;
import com.airbnb.lottie.model.layer.BaseLayer;
import com.airbnb.lottie.parser.DropShadowEffect;
import com.airbnb.lottie.utils.DropShadow;
import com.airbnb.lottie.value.LottieFrameInfo;
import com.airbnb.lottie.value.LottieValueCallback;


public class DropShadowKeyframeAnimation implements BaseKeyframeAnimation.AnimationListener {

  private static final float DEG_TO_RAD = (float) (Math.PI / 180.0);

  // We scale the "Softness" value by a constant factor so that the Paint.setShadowLayer() call
  // gives results that more closely match After Effects
  private static final float AFTER_EFFECT_SOFTNESS_SCALE_FACTOR = 0.33f;

  private final BaseLayer layer;
  private final BaseKeyframeAnimation.AnimationListener listener;
  private final BaseKeyframeAnimation<Integer, Integer> color;
  private final FloatKeyframeAnimation opacity;
  private final FloatKeyframeAnimation direction;
  private final FloatKeyframeAnimation distance;
  private final FloatKeyframeAnimation radius;

  @Nullable private Matrix layerInvMatrix;

  public DropShadowKeyframeAnimation(BaseKeyframeAnimation.AnimationListener listener, BaseLayer layer, DropShadowEffect dropShadowEffect) {
    this.listener = listener;
    this.layer = layer;
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

  public DropShadow evaluate(Matrix parentMatrix, int parentAlpha) {
    float directionRad = this.direction.getFloatValue() * DEG_TO_RAD;
    float distance = this.distance.getValue();
    float rawX = ((float) Math.sin(directionRad)) * distance;
    float rawY = ((float) Math.cos(directionRad + Math.PI)) * distance;
    float rawRadius = radius.getValue();

    int baseColor = color.getValue();
    int opacity = Math.round(this.opacity.getValue() * parentAlpha / 255f);
    int color = Color.argb(opacity, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor));

    DropShadow shadow = new DropShadow(rawRadius * AFTER_EFFECT_SOFTNESS_SCALE_FACTOR, rawX, rawY, color);
    shadow.transformBy(parentMatrix);

    // Since the shadow parameters are relative to the layer on which the shadow resides, correct for this
    // by undoing the layer's own transform. For example, if the layer is scaled, the screen-space blur
    // radius should stay constant.
    if (layerInvMatrix == null) layerInvMatrix = new Matrix();
    layer.transform.getMatrix().invert(layerInvMatrix);
    shadow.transformBy(layerInvMatrix);

    return shadow;
  }

  public void setColorCallback(@Nullable LottieValueCallback<Integer> callback) {
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
