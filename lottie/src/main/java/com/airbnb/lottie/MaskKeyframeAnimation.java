package com.airbnb.lottie;

import android.graphics.Path;

import java.util.ArrayList;
import java.util.List;

class MaskKeyframeAnimation {
  private final List<BaseKeyframeAnimation<?, Path>> maskAnimations;
  private final List<KeyframeAnimation<Integer>> opacityAnimations;
  private final List<Mask> masks;

  MaskKeyframeAnimation(List<Mask> masks) {
    this.masks = masks;
    this.maskAnimations = new ArrayList<>(masks.size());
    this.opacityAnimations = new ArrayList<>(masks.size());
    for (int i = 0; i < masks.size(); i++) {
      this.maskAnimations.add(masks.get(i).getMaskPath().createAnimation());
      AnimatableIntegerValue opacity = masks.get(i).getOpacity();
      opacityAnimations.add(opacity.createAnimation());
    }
  }

  List<Mask> getMasks() {
    return masks;
  }

  List<BaseKeyframeAnimation<?, Path>> getMaskAnimations() {
    return maskAnimations;
  }

  List<KeyframeAnimation<Integer>> getOpacityAnimations() {
    return opacityAnimations;
  }
}
