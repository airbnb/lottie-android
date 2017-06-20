package com.airbnb.lottie;

import java.util.List;

class TextKeyframeAnimation extends KeyframeAnimation<DocumentData> {
  TextKeyframeAnimation(List<? extends Keyframe<DocumentData>> keyframes) {
    super(keyframes);
  }

  @Override DocumentData getValue(Keyframe<DocumentData> keyframe, float keyframeProgress) {
    return keyframe.startValue;
  }
}
