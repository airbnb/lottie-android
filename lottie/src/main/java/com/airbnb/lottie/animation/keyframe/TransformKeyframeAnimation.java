package com.airbnb.lottie.animation.keyframe;

import android.graphics.Matrix;
import android.graphics.PointF;
import androidx.annotation.Nullable;

import com.airbnb.lottie.value.LottieValueCallback;
import com.airbnb.lottie.value.ScaleXY;
import com.airbnb.lottie.model.animatable.AnimatableTransform;
import com.airbnb.lottie.model.layer.BaseLayer;

import static com.airbnb.lottie.LottieProperty.TRANSFORM_ANCHOR_POINT;
import static com.airbnb.lottie.LottieProperty.TRANSFORM_END_OPACITY;
import static com.airbnb.lottie.LottieProperty.TRANSFORM_OPACITY;
import static com.airbnb.lottie.LottieProperty.TRANSFORM_POSITION;
import static com.airbnb.lottie.LottieProperty.TRANSFORM_ROTATION;
import static com.airbnb.lottie.LottieProperty.TRANSFORM_SCALE;
import static com.airbnb.lottie.LottieProperty.TRANSFORM_SKEW;
import static com.airbnb.lottie.LottieProperty.TRANSFORM_SKEW_ANGLE;
import static com.airbnb.lottie.LottieProperty.TRANSFORM_START_OPACITY;

public class TransformKeyframeAnimation {
  private final Matrix matrix = new Matrix();
  private final Matrix skewMatrix1;
  private final Matrix skewMatrix2;
  private final Matrix skewMatrix3;
  private final float[] skewValues;

  private boolean isIdentity;
  private final BaseKeyframeAnimation<PointF, PointF> anchorPoint;
  private final BaseKeyframeAnimation<?, PointF> position;
  private final BaseKeyframeAnimation<ScaleXY, ScaleXY> scale;
  private final BaseKeyframeAnimation<Float, Float> rotation;
  private final BaseKeyframeAnimation<Integer, Integer> opacity;
  @Nullable private final FloatKeyframeAnimation skew;
  @Nullable private final FloatKeyframeAnimation skewAngle;

  // Used for repeaters
  @Nullable private final BaseKeyframeAnimation<?, Float> startOpacity;
  @Nullable private final BaseKeyframeAnimation<?, Float> endOpacity;

  public TransformKeyframeAnimation(AnimatableTransform animatableTransform) {
    isIdentity = animatableTransform.isIdentity();
    if (isIdentity) {
      anchorPoint = null;
      position = null;
      scale = null;
      rotation = null;
      skew = null;
      skewAngle = null;
      skewMatrix1 = null;
      skewMatrix2 = null;
      skewMatrix3 = null;
      skewValues = null;
    } else  {
      anchorPoint = animatableTransform.getAnchorPoint().createAnimation();
      position = animatableTransform.getPosition().createAnimation();
      scale = animatableTransform.getScale().createAnimation();
      rotation = animatableTransform.getRotation().createAnimation();
      skew = animatableTransform.getSkew() == null ? null : (FloatKeyframeAnimation) animatableTransform.getSkew().createAnimation();
      if (skew != null) {
        skewMatrix1 = new Matrix();
        skewMatrix2 = new Matrix();
        skewMatrix3 = new Matrix();
        skewValues = new float[9];
      } else {
        skewMatrix1 = null;
        skewMatrix2 = null;
        skewMatrix3 = null;
        skewValues = null;
      }
      skewAngle = animatableTransform.getSkewAngle() == null ? null : (FloatKeyframeAnimation) animatableTransform.getSkewAngle().createAnimation();
    }
    opacity = animatableTransform.getOpacity().createAnimation();
    if (animatableTransform.getStartOpacity() != null) {
      startOpacity = animatableTransform.getStartOpacity().createAnimation();
    } else {
      startOpacity = null;
    }
    if (animatableTransform.getEndOpacity() != null) {
      endOpacity = animatableTransform.getEndOpacity().createAnimation();
    } else {
      endOpacity = null;
    }
  }

  public void addAnimationsToLayer(BaseLayer layer) {
    layer.addAnimation(opacity);
    if (startOpacity != null) {
      layer.addAnimation(startOpacity);
    }
    if (endOpacity != null) {
      layer.addAnimation(endOpacity);
    }

    if (isIdentity) {
      return;
    }
    layer.addAnimation(anchorPoint);
    layer.addAnimation(position);
    layer.addAnimation(scale);
    layer.addAnimation(rotation);
    if (skew != null) {
      layer.addAnimation(skew);
    }
    if (skewAngle != null) {
      layer.addAnimation(skewAngle);
    }
  }

  public void addListener(final BaseKeyframeAnimation.AnimationListener listener) {
    opacity.addUpdateListener(listener);
    if (startOpacity != null) {
      startOpacity.addUpdateListener(listener);
    }
    if (endOpacity != null) {
      endOpacity.addUpdateListener(listener);
    }

    if (isIdentity) {
      return;
    }
    anchorPoint.addUpdateListener(listener);
    position.addUpdateListener(listener);
    scale.addUpdateListener(listener);
    rotation.addUpdateListener(listener);
    if (skew != null) {
      skew.addUpdateListener(listener);
    }
    if (skewAngle != null) {
      skewAngle.addUpdateListener(listener);
    }
  }

  public void  setProgress(float progress) {
    opacity.setProgress(progress);
    if (startOpacity != null) {
      startOpacity.setProgress(progress);
    }
    if (endOpacity != null) {
      endOpacity.setProgress(progress);
    }

    if (isIdentity) {
      return;
    }
    anchorPoint.setProgress(progress);
    position.setProgress(progress);
    scale.setProgress(progress);
    rotation.setProgress(progress);
    if (skew != null) {
      skew.setProgress(progress);
    }
    if (skewAngle != null) {
      skewAngle.setProgress(progress);
    }
  }

  public BaseKeyframeAnimation<?, Integer> getOpacity() {
    return opacity;
  }

  @Nullable public BaseKeyframeAnimation<?, Float> getStartOpacity() {
    return startOpacity;
  }

  @Nullable public BaseKeyframeAnimation<?, Float> getEndOpacity() {
    return endOpacity;
  }

  public Matrix getMatrix() {
    if (isIdentity) {
      return matrix;
    }
    matrix.reset();
    PointF position = this.position.getValue();
    if (position.x != 0 || position.y != 0) {
      matrix.preTranslate(position.x, position.y);
    }

    float rotation = ((FloatKeyframeAnimation) this.rotation).getFloatValue();
    if (rotation != 0f) {
      matrix.preRotate(rotation);
    }

    if (skew != null) {
      float mCos = skewAngle == null ? 0f : (float) Math.cos(Math.toRadians(-skewAngle.getFloatValue() + 90));
      float mSin = skewAngle == null ? 0f : (float) Math.sin(Math.toRadians(-skewAngle.getFloatValue() + 90));
      float aTan = (float) Math.tan(Math.toRadians(skew.getFloatValue()));
      clearSkewValues();
      skewValues[0] = mCos;
      skewValues[1] = mSin;
      skewValues[3] = -mSin;
      skewValues[4] = mCos;
      skewValues[8] = 1f;
      skewMatrix1.setValues(skewValues);
      clearSkewValues();
      skewValues[0] = 1f;
      skewValues[3] = aTan;
      skewValues[4] = 1f;
      skewValues[8] = 1f;
      skewMatrix2.setValues(skewValues);
      clearSkewValues();
      skewValues[0] = mCos;
      skewValues[1] = -mSin;
      skewValues[3] = mSin;
      skewValues[4] = mCos;
      skewValues[8] = 1;
      skewMatrix3.setValues(skewValues);
      skewMatrix2.preConcat(skewMatrix1);
      skewMatrix3.preConcat(skewMatrix2);

      matrix.preConcat(skewMatrix3);
    }

    ScaleXY scaleTransform = this.scale.getValue();
    if (scaleTransform.getScaleX() != 1f || scaleTransform.getScaleY() != 1f) {
      matrix.preScale(scaleTransform.getScaleX(), scaleTransform.getScaleY());
    }

    PointF anchorPoint = this.anchorPoint.getValue();
    if (anchorPoint.x != 0 || anchorPoint.y != 0) {
      matrix.preTranslate(-anchorPoint.x, -anchorPoint.y);
    }

    return matrix;
  }

  private void clearSkewValues() {
    for (int i = 0; i < 9; i++) {
      skewValues[i] = 0f;
    }
  }

  /**
   * TODO: see if we can use this for the main {@link #getMatrix()} method.
   */
  public Matrix getMatrixForRepeater(float amount) {
    if (isIdentity) {
      return matrix;
    }
    PointF position = this.position.getValue();
    PointF anchorPoint = this.anchorPoint.getValue();
    ScaleXY scale = this.scale.getValue();
    float rotation = this.rotation.getValue();

    matrix.reset();
    matrix.preTranslate(position.x * amount, position.y * amount);
    matrix.preScale(
        (float) Math.pow(scale.getScaleX(), amount),
         (float) Math.pow(scale.getScaleY(), amount));
    matrix.preRotate(rotation * amount, anchorPoint.x, anchorPoint.y);

    return matrix;
  }

  /**
   * Returns whether the callback was applied.
   */
  @SuppressWarnings("unchecked")
  public <T> boolean applyValueCallback(T property, @Nullable LottieValueCallback<T> callback) {
    isIdentity = false;
    if (property == TRANSFORM_ANCHOR_POINT) {
      anchorPoint.setValueCallback((LottieValueCallback<PointF>) callback);
    } else if (property == TRANSFORM_POSITION) {
      position.setValueCallback((LottieValueCallback<PointF>) callback);
    } else if (property == TRANSFORM_SCALE) {
      scale.setValueCallback((LottieValueCallback<ScaleXY>) callback);
    } else if (property == TRANSFORM_ROTATION) {
      rotation.setValueCallback((LottieValueCallback<Float>) callback);
    } else if (property == TRANSFORM_OPACITY) {
      opacity.setValueCallback((LottieValueCallback<Integer>) callback);
    } else if (property == TRANSFORM_START_OPACITY && startOpacity != null) {
      startOpacity.setValueCallback((LottieValueCallback<Float>) callback);
    } else if (property == TRANSFORM_END_OPACITY && endOpacity != null) {
      endOpacity.setValueCallback((LottieValueCallback<Float>) callback);
    } else if (property == TRANSFORM_SKEW && skew != null) {
      skew.setValueCallback((LottieValueCallback<Float>) callback);
    } else if (property == TRANSFORM_SKEW_ANGLE && skewAngle != null) {
      skewAngle.setValueCallback((LottieValueCallback<Float>) callback);
    } else {
      return false;
    }
    return true;
  }
}
