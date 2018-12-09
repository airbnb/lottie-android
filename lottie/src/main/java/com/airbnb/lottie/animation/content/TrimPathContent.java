package com.airbnb.lottie.animation.content;

import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.model.content.ShapeTrimPath;
import com.airbnb.lottie.model.layer.BaseLayer;

import java.util.ArrayList;
import java.util.List;

public class TrimPathContent implements Content, BaseKeyframeAnimation.AnimationListener {

  private final String name;
  private final boolean hidden;
  private final List<BaseKeyframeAnimation.AnimationListener> listeners = new ArrayList<>();
  private final ShapeTrimPath.Type type;
  private final BaseKeyframeAnimation<?, Float> startAnimation;
  private final BaseKeyframeAnimation<?, Float> endAnimation;
  private final BaseKeyframeAnimation<?, Float> offsetAnimation;

  public TrimPathContent(BaseLayer layer, ShapeTrimPath trimPath) {
    name = trimPath.getName();
    hidden = trimPath.isHidden();
    type = trimPath.getType();
    startAnimation = trimPath.getStart().createAnimation();
    endAnimation = trimPath.getEnd().createAnimation();
    offsetAnimation = trimPath.getOffset().createAnimation();

    layer.addAnimation(startAnimation);
    layer.addAnimation(endAnimation);
    layer.addAnimation(offsetAnimation);

    startAnimation.addUpdateListener(this);
    endAnimation.addUpdateListener(this);
    offsetAnimation.addUpdateListener(this);
  }

  @Override public void onValueChanged() {
    for (int i = 0; i < listeners.size(); i++) {
      listeners.get(i).onValueChanged();
    }
  }

  @Override public void setContents(List<Content> contentsBefore, List<Content> contentsAfter) {
    // Do nothing.
  }

  @Override public String getName() {
    return name;
  }

  void addListener(BaseKeyframeAnimation.AnimationListener listener) {
    listeners.add(listener);
  }

  ShapeTrimPath.Type getType() {
    return type;
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

  public boolean isHidden() {
    return hidden;
  }
}
