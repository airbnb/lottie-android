package com.airbnb.lottie.animation.keyframe;

import static com.airbnb.lottie.LottieProperty.TRANSFORM_ANCHOR_POINT;
import static com.airbnb.lottie.LottieProperty.TRANSFORM_END_OPACITY;
import static com.airbnb.lottie.LottieProperty.TRANSFORM_OPACITY;
import static com.airbnb.lottie.LottieProperty.TRANSFORM_POSITION;
import static com.airbnb.lottie.LottieProperty.TRANSFORM_POSITION_X;
import static com.airbnb.lottie.LottieProperty.TRANSFORM_POSITION_Y;
import static com.airbnb.lottie.LottieProperty.TRANSFORM_ROTATION;
import static com.airbnb.lottie.LottieProperty.TRANSFORM_SCALE;
import static com.airbnb.lottie.LottieProperty.TRANSFORM_SKEW;
import static com.airbnb.lottie.LottieProperty.TRANSFORM_SKEW_ANGLE;
import static com.airbnb.lottie.LottieProperty.TRANSFORM_START_OPACITY;

import android.graphics.Matrix;
import android.graphics.PointF;

import androidx.annotation.Nullable;

import com.airbnb.lottie.model.animatable.AnimatableTransform;
import com.airbnb.lottie.model.layer.BaseLayer;
import com.airbnb.lottie.value.Keyframe;
import com.airbnb.lottie.value.LottieValueCallback;
import com.airbnb.lottie.value.ScaleXY;

import java.util.Collections;

public class TransformKeyframeAnimation {
  private final Matrix matrix = new Matrix();
  private final Matrix skewMatrix1;
  private final Matrix skewMatrix2;
  private final Matrix skewMatrix3;
  private final float[] skewValues;

  @Nullable private BaseKeyframeAnimation<PointF, PointF> anchorPoint;
  @Nullable private BaseKeyframeAnimation<?, PointF> position;
  @Nullable private BaseKeyframeAnimation<ScaleXY, ScaleXY> scale;
  @Nullable private BaseKeyframeAnimation<Float, Float> rotation;
  @Nullable private BaseKeyframeAnimation<Integer, Integer> opacity;
  @Nullable private FloatKeyframeAnimation skew;
  @Nullable private FloatKeyframeAnimation skewAngle;

  // Used for repeaters
  @Nullable private BaseKeyframeAnimation<?, Float> startOpacity;
  @Nullable private BaseKeyframeAnimation<?, Float> endOpacity;

  private final boolean autoOrient;


  public TransformKeyframeAnimation(AnimatableTransform animatableTransform) {
    anchorPoint = animatableTransform.getAnchorPoint() == null ? null : animatableTransform.getAnchorPoint().createAnimation();
    position = animatableTransform.getPosition() == null ? null : animatableTransform.getPosition().createAnimation();
    scale = animatableTransform.getScale() == null ? null : animatableTransform.getScale().createAnimation();
    rotation = animatableTransform.getRotation() == null ? null : animatableTransform.getRotation().createAnimation();
    skew = animatableTransform.getSkew() == null ? null : (FloatKeyframeAnimation) animatableTransform.getSkew().createAnimation();
    autoOrient = animatableTransform.isAutoOrient();
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
    if (animatableTransform.getOpacity() != null) {
      opacity = animatableTransform.getOpacity().createAnimation();
    }
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
    layer.addAnimation(startOpacity);
    layer.addAnimation(endOpacity);

    layer.addAnimation(anchorPoint);
    layer.addAnimation(position);
    layer.addAnimation(scale);
    layer.addAnimation(rotation);
    layer.addAnimation(skew);
    layer.addAnimation(skewAngle);
  }

  public void addListener(final BaseKeyframeAnimation.AnimationListener listener) {
    if (opacity != null) {
      opacity.addUpdateListener(listener);
    }
    if (startOpacity != null) {
      startOpacity.addUpdateListener(listener);
    }
    if (endOpacity != null) {
      endOpacity.addUpdateListener(listener);
    }

    if (anchorPoint != null) {
      anchorPoint.addUpdateListener(listener);
    }
    if (position != null) {
      position.addUpdateListener(listener);
    }
    if (scale != null) {
      scale.addUpdateListener(listener);
    }
    if (rotation != null) {
      rotation.addUpdateListener(listener);
    }
    if (skew != null) {
      skew.addUpdateListener(listener);
    }
    if (skewAngle != null) {
      skewAngle.addUpdateListener(listener);
    }
  }

  public void setProgress(float progress) {
    if (opacity != null) {
      opacity.setProgress(progress);
    }
    if (startOpacity != null) {
      startOpacity.setProgress(progress);
    }
    if (endOpacity != null) {
      endOpacity.setProgress(progress);
    }

    if (anchorPoint != null) {
      anchorPoint.setProgress(progress);
    }
    if (position != null) {
      position.setProgress(progress);
    }
    if (scale != null) {
      scale.setProgress(progress);
    }
    if (rotation != null) {
      rotation.setProgress(progress);
    }
    if (skew != null) {
      skew.setProgress(progress);
    }
    if (skewAngle != null) {
      skewAngle.setProgress(progress);
    }
  }

  @Nullable public BaseKeyframeAnimation<?, Integer> getOpacity() {
    return opacity;
  }

  @Nullable public BaseKeyframeAnimation<?, Float> getStartOpacity() {
    return startOpacity;
  }

  @Nullable public BaseKeyframeAnimation<?, Float> getEndOpacity() {
    return endOpacity;
  }

  public Matrix getMatrix() {
    matrix.reset();
    BaseKeyframeAnimation<?, PointF> position = this.position;
    if (position != null) {
      PointF positionValue = position.getValue();
      if (positionValue != null && (positionValue.x != 0 || positionValue.y != 0)) {
        matrix.preTranslate(positionValue.x, positionValue.y);
      }
    }

    // If autoOrient is true, the rotation should follow the derivative of the position rather
    // than the rotation property.
    if (autoOrient) {
      if (position != null) {
        float currentProgress = position.getProgress();
        PointF startPosition = position.getValue();
        // Store the start X and Y values because the pointF will be overwritten by the next getValue call.
        float startX = startPosition.x;
        float startY = startPosition.y;
        // 1) Find the next position value.
        // 2) Create a vector from the current position to the next position.
        // 3) Find the angle of that vector to the X axis (0 degrees).
        position.setProgress(currentProgress + 0.0001f);
        PointF nextPosition = position.getValue();
        position.setProgress(currentProgress);
        double rotationValue = Math.toDegrees(Math.atan2(nextPosition.y - startY, nextPosition.x - startX));
        matrix.preRotate((float) rotationValue);
      }
    } else {
      BaseKeyframeAnimation<Float, Float> rotation = this.rotation;
      if (rotation != null) {
        float rotationValue;
        if (rotation instanceof ValueCallbackKeyframeAnimation) {
          rotationValue = rotation.getValue();
        } else {
          rotationValue = ((FloatKeyframeAnimation) rotation).getFloatValue();
        }
        if (rotationValue != 0f) {
          matrix.preRotate(rotationValue);
        }
      }
    }

    FloatKeyframeAnimation skew = this.skew;
    if (skew != null) {
      float mCos = skewAngle == null ? 0f : (float) Math.cos(Math.toRadians(-skewAngle.getFloatValue() + 90));
      float mSin = skewAngle == null ? 1f : (float) Math.sin(Math.toRadians(-skewAngle.getFloatValue() + 90));
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

    BaseKeyframeAnimation<ScaleXY, ScaleXY> scale = this.scale;
    if (scale != null) {
      ScaleXY scaleTransform = scale.getValue();
      if (scaleTransform != null && (scaleTransform.getScaleX() != 1f || scaleTransform.getScaleY() != 1f)) {
        matrix.preScale(scaleTransform.getScaleX(), scaleTransform.getScaleY());
      }
    }

    BaseKeyframeAnimation<PointF, PointF> anchorPoint = this.anchorPoint;
    if (anchorPoint != null) {
      PointF anchorPointValue = anchorPoint.getValue();
      if (anchorPointValue != null && (anchorPointValue.x != 0 || anchorPointValue.y != 0)) {
        matrix.preTranslate(-anchorPointValue.x, -anchorPointValue.y);
      }
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
    PointF position = this.position == null ? null : this.position.getValue();
    ScaleXY scale = this.scale == null ? null : this.scale.getValue();

    matrix.reset();
    if (position != null) {
      matrix.preTranslate(position.x * amount, position.y * amount);
    }
    if (scale != null) {
      matrix.preScale(
          (float) Math.pow(scale.getScaleX(), amount),
          (float) Math.pow(scale.getScaleY(), amount));
    }
    if (this.rotation != null) {
      float rotation = this.rotation.getValue();
      PointF anchorPoint = this.anchorPoint == null ? null : this.anchorPoint.getValue();
      matrix.preRotate(rotation * amount, anchorPoint == null ? 0f : anchorPoint.x, anchorPoint == null ? 0f : anchorPoint.y);
    }

    return matrix;
  }

  /**
   * Returns whether the callback was applied.
   */
  @SuppressWarnings("unchecked")
  public <T> boolean applyValueCallback(T property, @Nullable LottieValueCallback<T> callback) {
    if (property == TRANSFORM_ANCHOR_POINT) {
      if (anchorPoint == null) {
        anchorPoint = new ValueCallbackKeyframeAnimation<>((LottieValueCallback<PointF>) callback, new PointF());
      } else {
        anchorPoint.setValueCallback((LottieValueCallback<PointF>) callback);
      }
    } else if (property == TRANSFORM_POSITION) {
      if (position == null) {
        position = new ValueCallbackKeyframeAnimation<>((LottieValueCallback<PointF>) callback, new PointF());
      } else {
        position.setValueCallback((LottieValueCallback<PointF>) callback);
      }
    } else if (property == TRANSFORM_POSITION_X && position instanceof SplitDimensionPathKeyframeAnimation) {
      ((SplitDimensionPathKeyframeAnimation) position).setXValueCallback((LottieValueCallback<Float>) callback);
    } else if (property == TRANSFORM_POSITION_Y && position instanceof SplitDimensionPathKeyframeAnimation) {
      ((SplitDimensionPathKeyframeAnimation) position).setYValueCallback((LottieValueCallback<Float>) callback);
    } else if (property == TRANSFORM_SCALE) {
      if (scale == null) {
        scale = new ValueCallbackKeyframeAnimation<>((LottieValueCallback<ScaleXY>) callback, new ScaleXY());
      } else {
        scale.setValueCallback((LottieValueCallback<ScaleXY>) callback);
      }
    } else if (property == TRANSFORM_ROTATION) {
      if (rotation == null) {
        rotation = new ValueCallbackKeyframeAnimation<>((LottieValueCallback<Float>) callback, 0f);
      } else {
        rotation.setValueCallback((LottieValueCallback<Float>) callback);
      }
    } else if (property == TRANSFORM_OPACITY) {
      if (opacity == null) {
        opacity = new ValueCallbackKeyframeAnimation<>((LottieValueCallback<Integer>) callback, 100);
      } else {
        opacity.setValueCallback((LottieValueCallback<Integer>) callback);
      }
    } else if (property == TRANSFORM_START_OPACITY) {
      if (startOpacity == null) {
        startOpacity = new ValueCallbackKeyframeAnimation<>((LottieValueCallback<Float>) callback, 100f);
      } else {
        startOpacity.setValueCallback((LottieValueCallback<Float>) callback);
      }
    } else if (property == TRANSFORM_END_OPACITY) {
      if (endOpacity == null) {
        endOpacity = new ValueCallbackKeyframeAnimation<>((LottieValueCallback<Float>) callback, 100f);
      } else {
        endOpacity.setValueCallback((LottieValueCallback<Float>) callback);
      }
    } else if (property == TRANSFORM_SKEW) {
      if (skew == null) {
        skew = new FloatKeyframeAnimation(Collections.singletonList(new Keyframe<>(0f)));
      }
      skew.setValueCallback((LottieValueCallback<Float>) callback);
    } else if (property == TRANSFORM_SKEW_ANGLE) {
      if (skewAngle == null) {
        skewAngle = new FloatKeyframeAnimation(Collections.singletonList(new Keyframe<>(0f)));
      }
      skewAngle.setValueCallback((LottieValueCallback<Float>) callback);
    } else {
      return false;
    }
    return true;
  }
}
