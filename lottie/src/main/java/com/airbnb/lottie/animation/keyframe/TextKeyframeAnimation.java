package com.airbnb.lottie.animation.keyframe;

import com.airbnb.lottie.animation.Keyframe;
import com.airbnb.lottie.model.DocumentData;

import java.util.List;

public class TextKeyframeAnimation extends KeyframeAnimation<DocumentData> {
  public TextKeyframeAnimation(List<? extends Keyframe<DocumentData>> keyframes) {
    super(keyframes);
  }

  @Override DocumentData getValue(Keyframe<DocumentData> keyframe, float keyframeProgress) {
    return keyframe.startValue;
  }
}
