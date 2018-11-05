package com.airbnb.lottie.animation.keyframe;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;

import com.airbnb.lottie.L;
import com.airbnb.lottie.model.animatable.AnimatableIntegerValue;
import com.airbnb.lottie.model.content.Mask;
import com.airbnb.lottie.model.content.ShapeData;

import java.util.ArrayList;
import java.util.List;

public class MaskKeyframeAnimation {
  private final List<BaseKeyframeAnimation<ShapeData, Path>> maskAnimations;
  private final List<BaseKeyframeAnimation<Integer, Integer>> opacityAnimations;
  private final List<Mask> masks;
  /** Reusable path for calculating masks. */
  private final Path maskPath = new Path();
  private final Path contentPath = new Path();

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

  public void applyToPath(Path path, Matrix matrix) {
    applyMasks(path, matrix, Mask.MaskMode.MaskModeAdd);
    // Treat intersect masks like add masks. This is not correct but it's closer.
    applyMasks(path, matrix, Mask.MaskMode.MaskModeIntersect);
    applyMasks(path, matrix, Mask.MaskMode.MaskModeSubtract);
  }

  private void applyMasks(Path contentPath, Matrix matrix, Mask.MaskMode maskMode) {
    Path.Op op;
    switch (maskMode) {
      case MaskModeSubtract:
        op = Path.Op.DIFFERENCE;
        break;
      case MaskModeIntersect:
      case MaskModeAdd:
      default:
        // As a hack, we treat all non-subtract masks like add masks. This is not correct but it's
        // better than nothing.
        op = Path.Op.INTERSECT;
    }

    //noinspection ConstantConditions
    int size = getMasks().size();

    boolean hasMask = false;
    for (int i = 0; i < size; i++) {
      if (getMasks().get(i).getMaskMode() == maskMode) {
        hasMask = true;
        break;
      }
    }
    if (!hasMask) {
      return;
    }

    L.beginSection("Layer#drawMask");

    this.maskPath.reset();
    for (int i = 0; i < size; i++) {
      Mask mask = getMasks().get(i);
      if (mask.getMaskMode() != maskMode) {
        continue;
      }
      BaseKeyframeAnimation<?, Path> maskAnimation = getMaskAnimations().get(i);
      Path maskPath = maskAnimation.getValue();
      maskPath.transform(matrix);
      this.maskPath.addPath(maskPath);
      // TODO: opacity animation
    }
    contentPath.op(maskPath, op);
    L.endSection("Layer#drawMask");
  }
}
