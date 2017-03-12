package com.airbnb.lottie;

import java.util.ArrayList;
import java.util.List;

class TrimPathContent implements Content {

  private final List<BaseKeyframeAnimation.SimpleAnimationListener> listeners = new ArrayList<>();
  private final BaseKeyframeAnimation<?, Float> startAnimation;
  private final BaseKeyframeAnimation<?, Float> endAnimation;
  private final BaseKeyframeAnimation<?, Float> offsetAnimation;

  TrimPathContent(BaseLayer layer, ShapeTrimPath trimPath) {
    startAnimation = trimPath.getStart().createAnimation();
    endAnimation = trimPath.getEnd().createAnimation();
    offsetAnimation = trimPath.getOffset().createAnimation();

    layer.addAnimation(startAnimation);
    layer.addAnimation(endAnimation);
    layer.addAnimation(offsetAnimation);

    KeyframeAnimation.AnimationListener<Float> updateListener = new BaseKeyframeAnimation.AnimationListener<Float>() {
      @Override public void onValueChanged(Float value) {
        for (int i = 0; i < listeners.size(); i++) {
          listeners.get(i).onValueChanged();
        }
      }
    };
    startAnimation.addUpdateListener(updateListener);
    endAnimation.addUpdateListener(updateListener);
    offsetAnimation.addUpdateListener(updateListener);
  }

  @Override public void setContents(List<Content> contentsBefore, List<Content> contentsAfter) {
    // Do nothing.
  }

  void addListener(BaseKeyframeAnimation.SimpleAnimationListener listener) {
    listeners.add(listener);
  }

  public BaseKeyframeAnimation<?, Float> getStart() {
    return startAnimation;
  }

  public BaseKeyframeAnimation<?, Float> getEnd() {
    return endAnimation;
  }

  public BaseKeyframeAnimation<?, Float> getOffset() {
    return offsetAnimation;
  }
}
