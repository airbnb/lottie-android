package com.airbnb.lottie.animation.keyframe;

import android.graphics.PointF;

import androidx.annotation.Nullable;

import com.airbnb.lottie.value.Keyframe;
import com.airbnb.lottie.value.LottieValueCallback;

import java.util.Collections;

public class SplitDimensionPathKeyframeAnimation extends BaseKeyframeAnimation<PointF, PointF> {
  private final PointF point = new PointF();
  private final PointF pointWithCallbackValues = new PointF();
  private final BaseKeyframeAnimation<Float, Float> xAnimation;
  private final BaseKeyframeAnimation<Float, Float> yAnimation;

  @Nullable protected LottieValueCallback<Float> xValueCallback;
  @Nullable protected LottieValueCallback<Float> yValueCallback;


  public SplitDimensionPathKeyframeAnimation(
      BaseKeyframeAnimation<Float, Float> xAnimation,
      BaseKeyframeAnimation<Float, Float> yAnimation) {
    super(Collections.<Keyframe<PointF>>emptyList());

    this.xAnimation = xAnimation;
    this.yAnimation = yAnimation;
    // We need to call an initial setProgress so point gets set with the initial value.
    setProgress(getProgress());
  }

  public void setXValueCallback(@Nullable LottieValueCallback<Float> xValueCallback) {
    if (this.xValueCallback != null) {
      this.xValueCallback.setAnimation(null);
    }
    this.xValueCallback = xValueCallback;
    if (xValueCallback != null) {
      xValueCallback.setAnimation(this);
    }
  }

  public void setYValueCallback(@Nullable LottieValueCallback<Float> yValueCallback) {
    if (this.yValueCallback != null) {
      this.yValueCallback.setAnimation(null);
    }
    this.yValueCallback = yValueCallback;
    if (yValueCallback != null) {
      yValueCallback.setAnimation(this);
    }
  }

  @Override public void setProgress(float progress) {
    xAnimation.setProgress(progress);
    yAnimation.setProgress(progress);
    point.set(xAnimation.getValue(), yAnimation.getValue());
    for (int i = 0; i < listeners.size(); i++) {
      listeners.get(i).onValueChanged();
    }
  }

  @Override public PointF getValue() {
    return getValue(null, 0);
  }

  @Override PointF getValue(Keyframe<PointF> keyframe, float keyframeProgress) {
    Float xCallbackValue = null;
    Float yCallbackValue = null;

    if (xValueCallback != null) {
      Keyframe<Float> xKeyframe = xAnimation.getCurrentKeyframe();
      if (xKeyframe != null) {
        float progress = xAnimation.getInterpolatedCurrentKeyframeProgress();
        Float endFrame = xKeyframe.endFrame;
        xCallbackValue =
            xValueCallback.getValueInternal(xKeyframe.startFrame, endFrame == null ? xKeyframe.startFrame : endFrame, xKeyframe.startValue,
                xKeyframe.endValue, keyframeProgress, keyframeProgress, progress);
      }
    }
    if (yValueCallback != null) {
      Keyframe<Float> yKeyframe = yAnimation.getCurrentKeyframe();
      if (yKeyframe != null) {
        float progress = yAnimation.getInterpolatedCurrentKeyframeProgress();
        Float endFrame = yKeyframe.endFrame;
        yCallbackValue =
            yValueCallback.getValueInternal(yKeyframe.startFrame, endFrame == null ? yKeyframe.startFrame : endFrame, yKeyframe.startValue,
                yKeyframe.endValue, keyframeProgress, keyframeProgress, progress);
      }
    }

    if (xCallbackValue == null) {
      pointWithCallbackValues.set(point.x, 0f);
    } else {
      pointWithCallbackValues.set(xCallbackValue, 0f);
    }

    if (yCallbackValue == null) {
      pointWithCallbackValues.set(pointWithCallbackValues.x, point.y);
    } else {
      pointWithCallbackValues.set(pointWithCallbackValues.x, yCallbackValue);
    }

    return pointWithCallbackValues;
  }
}
