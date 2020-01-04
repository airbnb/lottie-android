package com.airbnb.lottie.animation.keyframe;

import androidx.annotation.Nullable;
import com.airbnb.lottie.value.Keyframe;
import com.airbnb.lottie.value.LottieFrameInfo;
import com.airbnb.lottie.value.LottieValueCallback;

import java.util.Collections;

public class ValueCallbackKeyframeAnimation<K, A> extends BaseKeyframeAnimation<K, A> {
  private final LottieFrameInfo<A> frameInfo = new LottieFrameInfo<>();

  private final A valueCallbackValue;

  public ValueCallbackKeyframeAnimation(LottieValueCallback<A> valueCallback) {
    this(valueCallback, null);
  }

  public ValueCallbackKeyframeAnimation(LottieValueCallback<A> valueCallback, @Nullable A valueCallbackValue) {
    super(Collections.<Keyframe<K>>emptyList());
    setValueCallback(valueCallback);
    this.valueCallbackValue = valueCallbackValue;
  }

  @Override public void setProgress(float progress) {
    this.progress = progress;
  }

  /**
   * If this doesn't return 1, then {@link #setProgress(float)} will always clamp the progress
   * to 0.
   */
  @Override float getEndProgress() {
    return 1f;
  }

  @Override public void notifyListeners() {
    if (this.valueCallback != null) {
      super.notifyListeners();
    }
  }

  @Override public A getValue() {
    //noinspection ConstantConditions
    return valueCallback.getValueInternal(0f, 0f, valueCallbackValue, valueCallbackValue, getProgress(), getProgress(), getProgress());
  }

  @Override A getValue(Keyframe<K> keyframe, float keyframeProgress) {
    return getValue();
  }
}
