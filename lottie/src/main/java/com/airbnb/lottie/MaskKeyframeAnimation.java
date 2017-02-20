package com.airbnb.lottie;

import android.graphics.Path;

import java.util.ArrayList;
import java.util.List;

class MaskKeyframeAnimation {
  private final Path path = new Path();
  private final List<BaseKeyframeAnimation<?, Path>> masks;

  MaskKeyframeAnimation(List<Mask> masks) {
    this.masks = new ArrayList<>(masks.size());
    for (int i = 0; i < masks.size(); i++) {
      this.masks.add(masks.get(i).getMaskPath().createAnimation());
    }
  }

  List<BaseKeyframeAnimation<?, Path>> getMasks() {
    return masks;
  }

  Path getMaskUnionPath() {
    path.reset();
    for (BaseKeyframeAnimation<?, Path> mask : masks) {
      this.path.addPath(mask.getValue());
    }
    return path;
  }
}
