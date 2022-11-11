package com.airbnb.lottie.animation.keyframe;

import com.airbnb.lottie.model.DocumentData;
import com.airbnb.lottie.value.Keyframe;
import com.airbnb.lottie.value.LottieFrameInfo;
import com.airbnb.lottie.value.LottieValueCallback;

import java.util.List;

public class TextKeyframeAnimation extends KeyframeAnimation<DocumentData> {
  public TextKeyframeAnimation(List<Keyframe<DocumentData>> keyframes) {
    super(keyframes);
  }

  @Override DocumentData getValue(Keyframe<DocumentData> keyframe, float keyframeProgress) {
    if (valueCallback != null) {
      return valueCallback.getValueInternal(keyframe.startFrame, keyframe.endFrame == null ? Float.MAX_VALUE : keyframe.endFrame,
          keyframe.startValue, keyframe.endValue == null ? keyframe.startValue : keyframe.endValue, keyframeProgress,
          getInterpolatedCurrentKeyframeProgress(), getProgress());
    } else if (keyframeProgress != 1.0f || keyframe.endValue == null) {
      return keyframe.startValue;
    } else {
      return keyframe.endValue;
    }
  }

  public void setStringValueCallback(LottieValueCallback<String> valueCallback) {
    final LottieFrameInfo<String> stringFrameInfo = new LottieFrameInfo<>();
    final DocumentData documentData = new DocumentData();
    super.setValueCallback(new LottieValueCallback<DocumentData>() {
      @Override
      public DocumentData getValue(LottieFrameInfo<DocumentData> frameInfo) {
        stringFrameInfo.set(frameInfo.getStartFrame(), frameInfo.getEndFrame(), frameInfo.getStartValue().text,
            frameInfo.getEndValue().text, frameInfo.getLinearKeyframeProgress(), frameInfo.getInterpolatedKeyframeProgress(),
            frameInfo.getOverallProgress());
        String text = valueCallback.getValue(stringFrameInfo);
        DocumentData baseDocumentData = frameInfo.getInterpolatedKeyframeProgress() == 1f ? frameInfo.getEndValue() : frameInfo.getStartValue();
        documentData.set(text, baseDocumentData.fontName, baseDocumentData.size, baseDocumentData.justification, baseDocumentData.tracking,
            baseDocumentData.lineHeight, baseDocumentData.baselineShift, baseDocumentData.color, baseDocumentData.strokeColor,
            baseDocumentData.strokeWidth, baseDocumentData.strokeOverFill, baseDocumentData.boxPosition);
        return documentData;
      }
    });
  }
}
