package com.airbnb.lottie.animation.keyframe;

import android.graphics.Matrix;
import android.graphics.Path;

import android.util.Log;
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
  private final Path masksPath = new Path();

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

  public void applyToPath(Path contentPath, Matrix matrix) {
    masksPath.reset();
    for (int i = 0; i < getMaskAnimations().size(); i++) {
      BaseKeyframeAnimation<ShapeData, Path> mask = getMaskAnimations().get(i);
      Path maskPath = mask.getValue();
      maskPath.transform(matrix);
      Mask.MaskMode maskMode = getMasks().get(i).getMaskMode();
      if (maskMode == Mask.MaskMode.MaskModeAdd) {
        Path addMaskPaint = new Path(contentPath);
        addMaskPaint.op(maskPath, Path.Op.INTERSECT);
        masksPath.op(maskPath, Path.Op.UNION);
      } else if (maskMode == Mask.MaskMode.MaskModeSubtract) {
        Path subtractMaskPath = new Path(contentPath);
        subtractMaskPath.op(maskPath, Path.Op.DIFFERENCE);
        if (masksPath.isEmpty()) {
          masksPath.addPath(contentPath);
        }
        masksPath.op(subtractMaskPath, Path.Op.INTERSECT);
      }
    }

    contentPath.op(masksPath, Path.Op.INTERSECT);
  }
}
