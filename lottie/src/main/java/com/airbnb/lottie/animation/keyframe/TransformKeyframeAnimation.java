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

import androidx.annotation.NonNull;
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

  @NonNull private BaseKeyframeAnimation<PointF, PointF> anchorPoint;
  @NonNull private BaseKeyframeAnimation<?, PointF> position;
  @NonNull private BaseKeyframeAnimation<ScaleXY, ScaleXY> scale;
  @NonNull private BaseKeyframeAnimation<Float, Float> rotation;
  @NonNull private BaseKeyframeAnimation<Integer, Integer> opacity;
  @Nullable private FloatKeyframeAnimation skew;
  @Nullable private FloatKeyframeAnimation skewAngle;

  // Used for repeaters
  @Nullable private BaseKeyframeAnimation<?, Float> startOpacity;
  @Nullable private BaseKeyframeAnimation<?, Float> endOpacity;

  public TransformKeyframeAnimation(AnimatableTransform animatableTransform) {
    anchorPoint = animatableTransform.getAnchorPoint() == null ? null : animatableTransform.getAnchorPoint().createAnimation();
    position = animatableTransform.getPosition() == null ? null : animatableTransform.getPosition().createAnimation();
    scale = animatableTransform.getScale() == null ? null : animatableTransform.getScale().createAnimation();
    rotation = animatableTransform.getRotation() == null ? null : animatableTransform.getRotation().createAnimation();
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
    if (position != null) {
      PointF position = this.position.getValue();
      if (position.x != 0 || position.y != 0) {
        matrix.preTranslate(position.x, position.y);
      }
    }

    if (rotation != null) {
      float rotation;
      if (this.rotation instanceof ValueCallbackKeyframeAnimation) {
        rotation = this.rotation.getValue();
      } else {
        rotation = ((FloatKeyframeAnimation) this.rotation).getFloatValue();
      }
      if (rotation != 0f) {
        matrix.preRotate(rotation);
      }
    }

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

    if (scale != null) {
      ScaleXY scaleTransform = this.scale.getValue();
      if (scaleTransform.getScaleX() != 1f || scaleTransform.getScaleY() != 1f) {
        matrix.preScale(scaleTransform.getScaleX(), scaleTransform.getScaleY());
      }
    }

    if (anchorPoint != null) {
      PointF anchorPoint = this.anchorPoint.getValue();
      if (anchorPoint.x != 0 || anchorPoint.y != 0) {
        matrix.preTranslate(-anchorPoint.x, -anchorPoint.y);
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
        anchorPoint = new ValueCallbackKeyframeAnimation(callback, new PointF());
      } else {
        anchorPoint.setValueCallback((LottieValueCallback<PointF>) callback);
      }
    } else if (property == TRANSFORM_POSITION) {
      if (position == null) {
        position = new ValueCallbackKeyframeAnimation(callback, new PointF());
      } else {
        position.setValueCallback((LottieValueCallback<PointF>) callback);
      }
    } else if (property == TRANSFORM_POSITION_X && position instanceof SplitDimensionPathKeyframeAnimation) {
      ((SplitDimensionPathKeyframeAnimation) position).setXValueCallback((LottieValueCallback<Float>) callback);
    } else if (property == TRANSFORM_POSITION_Y && position instanceof SplitDimensionPathKeyframeAnimation) {
      ((SplitDimensionPathKeyframeAnimation) position).setYValueCallback((LottieValueCallback<Float>) callback);
    } else if (property == TRANSFORM_SCALE) {
      if (scale == null) {
        scale = new ValueCallbackKeyframeAnimation(callback, new ScaleXY());
      } else {
        scale.setValueCallback((LottieValueCallback<ScaleXY>) callback);
      }
    } else if (property == TRANSFORM_ROTATION) {
      if (rotation == null) {
        rotation = new ValueCallbackKeyframeAnimation(callback, 0f);
      } else {
        rotation.setValueCallback((LottieValueCallback<Float>) callback);
      }
    } else if (property == TRANSFORM_OPACITY) {
      if (opacity == null) {
        opacity = new ValueCallbackKeyframeAnimation(callback, 100);
      } else {
        opacity.setValueCallback((LottieValueCallback<Integer>) callback);
      }
    } else if (property == TRANSFORM_START_OPACITY && startOpacity != null) {
      if (startOpacity == null) {
        startOpacity = new ValueCallbackKeyframeAnimation(callback, 100);
      } else {
        startOpacity.setValueCallback((LottieValueCallback<Float>) callback);
      }
    } else if (property == TRANSFORM_END_OPACITY && endOpacity != null) {
      if (endOpacity == null) {
        endOpacity = new ValueCallbackKeyframeAnimation(callback, 100);
      } else {
        endOpacity.setValueCallback((LottieValueCallback<Float>) callback);
      }
    } else if (property == TRANSFORM_SKEW && skew != null) {
      if (skew == null) {
        skew = new FloatKeyframeAnimation(Collections.singletonList(new Keyframe<Float>(0f)));
      }
      skew.setValueCallback((LottieValueCallback<Float>) callback);
    } else if (property == TRANSFORM_SKEW_ANGLE && skewAngle != null) {
      if (skewAngle == null) {
        skewAngle = new FloatKeyframeAnimation(Collections.singletonList(new Keyframe<Float>(0f)));
      }
      skewAngle.setValueCallback((LottieValueCallback<Float>) callback);
    } else {
      return false;
    }
    return true;
  }
}
