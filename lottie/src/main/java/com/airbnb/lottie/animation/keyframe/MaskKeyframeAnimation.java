package com.airbnb.lottie.animation.keyframe;

import android.graphics.Matrix;
import android.graphics.Path;

import androidx.annotation.Nullable;
import com.airbnb.lottie.model.animatable.AnimatableIntegerValue;
import com.airbnb.lottie.model.content.Mask;
import com.airbnb.lottie.model.content.ShapeData;
import com.airbnb.lottie.model.layer.BaseLayer;
import com.airbnb.lottie.model.layer.Layer;

import java.util.ArrayList;
import java.util.List;

public class MaskKeyframeAnimation {
  private final List<BaseKeyframeAnimation<ShapeData, Path>> maskAnimations;
  private final List<BaseKeyframeAnimation<Integer, Integer>> opacityAnimations;
  private final List<Mask> masks;
  /** Reusable path for calculating masks. */
  private final Path combinedPath = new Path();
  private final Path addPath = new Path();
  private final Path subtractPath = new Path();
  @Nullable
  private BaseLayer matteLayer;
  @Nullable
  private Layer.MatteType matteType;

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

  public void setMatteLayer(@Nullable BaseLayer matteLayer, Layer.MatteType matteType) {
    this.matteLayer = matteLayer;
    this.matteType = matteType;
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

  public void applyToPath(Path contentPath, Matrix matrix, Matrix parentMatrix) {
    getMaskPath(contentPath, matrix, parentMatrix);

    contentPath.op(combinedPath, Path.Op.INTERSECT);
  }

  public Path getMaskPath(Path contentPath, Matrix matrix, Matrix parentMatrix) {
    combinedPath.reset();
    for (int i = 0; i < getMaskAnimations().size(); i++) {
      BaseKeyframeAnimation<ShapeData, Path> mask = getMaskAnimations().get(i);
      Path maskPath = mask.getValue();
      maskPath.transform(matrix);
      Mask.MaskMode maskMode = getMasks().get(i).getMaskMode();
      if (maskMode == Mask.MaskMode.MaskModeAdd || maskMode == Mask.MaskMode.MaskModeIntersect) {
        addPath.set(contentPath);
        addPath.op(maskPath, Path.Op.INTERSECT);
        combinedPath.op(maskPath, Path.Op.UNION);
      } else if (maskMode == Mask.MaskMode.MaskModeSubtract) {
        subtractPath.set(contentPath);
        subtractPath.op(maskPath, Path.Op.DIFFERENCE);
        if (combinedPath.isEmpty()) {
          combinedPath.addPath(contentPath);
        }
        combinedPath.op(subtractPath, Path.Op.INTERSECT);
      }
    }
    combinedPath.close();

    if (matteLayer != null && matteType != null) {
      Path mattePath = matteLayer.getPath();
      mattePath.transform(parentMatrix);
      mattePath.transform(matteLayer.getTransformMatrix());
      if (matteType == Layer.MatteType.Add) {
        addPath.set(contentPath);
        addPath.op(mattePath, Path.Op.INTERSECT);
        combinedPath.op(mattePath, Path.Op.UNION);
      } else if (matteType == Layer.MatteType.Invert){
        subtractPath.set(contentPath);
        subtractPath.op(mattePath, Path.Op.DIFFERENCE);
        if (combinedPath.isEmpty()) {
          combinedPath.addPath(contentPath);
        }
        combinedPath.op(subtractPath, Path.Op.INTERSECT);
      }
    }

    return combinedPath;
  }
}
