package com.airbnb.lottie;

import java.util.List;

class TextKeyframeAnimation extends KeyframeAnimation<DocumentData> {
  // private DocumentData tempTextFrame = new DocumentData();

  TextKeyframeAnimation(List<? extends Keyframe<DocumentData>> keyframes) {
    super(keyframes);
  }

  @Override void setProgress(float progress) {
    super.setProgress(progress);
  }

  @Override DocumentData getValue(Keyframe<DocumentData> keyframe, float keyframeProgress) {
    return keyframe.startValue;
    // DocumentData startValue = keyframe.startValue;
    // DocumentData endValue = keyframe.endValue;
    // if (startValue == null) {
    //   tempTextFrame.text = "";
    //   return tempTextFrame;
    // }
    // if (endValue == null) {
    //   tempTextFrame.set(startValue);
    //   return tempTextFrame;
    // }
    // tempTextFrame.text = startValue.text;
    // tempTextFrame.fontFamily = startValue.fontFamily;
    // tempTextFrame.size = lerp(startValue.size, endValue.size, keyframeProgress);
    // tempTextFrame.justification = startValue.justification;
    // tempTextFrame.tracking = lerp(startValue.tracking, endValue.tracking, keyframeProgress);
    // tempTextFrame.lineHeight = lerp(startValue.lineHeight, endValue.lineHeight, keyframeProgress);
    // tempTextFrame.color =
    //     GammaEvaluator.evaluate(keyframeProgress, startValue.color, endValue.color);
    // return tempTextFrame;
  }
}
