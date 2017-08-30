package com.airbnb.lottie.animation.keyframe;

import android.graphics.Path;

import com.airbnb.lottie.model.animatable.AnimatableIntegerValue;
import com.airbnb.lottie.model.content.Mask;
import com.airbnb.lottie.model.content.ShapeData;

import java.util.ArrayList;
import java.util.List;

public class MaskKeyframeAnimation {
  private final List<BaseKeyframeAnimation<ShapeData, Path>> maskAnimations;
  private final List<BaseKeyframeAnimation<Integer, Integer>> opacityAnimations;
  private final List<Mask> masks;

  public MaskKeyframeAnimation(List<Mask> masks) {
    this.masks = masks;
    this.maskAnimations = new ArrayList<>(masks.size());
    this.opacityAnimations = new ArrayList<>(masks.size());
    for (int i = 0; i < masks.size(); i++) {
      this.maskAnimations.add(masks.get(i).getMaskPath().createAnimation());
      AnimatableIntegerValue opacity = masks.get(i).getOpacity();
      opacityAnimations.add(opacity.createAnimation());
    }
  }

  public List<Mask> getMasks() {
    return masks;
  }

  public List<BaseKeyframeAnimation<ShapeData, Path>> getMaskAnimations() {
    return maskAnimations;
  }

  public List<BaseKeyframeAnimation<Integer, Integer>> getOpacityAnimations() {
    return opacityAnimations;
  }
}
