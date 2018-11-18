package com.airbnb.lottie.animation.keyframe;

import android.graphics.Matrix;
import android.graphics.Path;

import android.graphics.RectF;
import android.util.Log;
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
  /**
   * Reusable path for calculating masks.
   */
  private final Path combinedPath = new Path();
  private final Path addPath = new Path();
  private final Path subtractPath = new Path();
  @Nullable
  public BaseLayer matteLayer;
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

  public void applyToPath(Path contentPath, Matrix maskMatrix, Matrix matteMatrix, Matrix parentMatrix) {
    getMaskPath(contentPath, maskMatrix, matteMatrix, parentMatrix, true);
  }

  public Path getMaskPath(Path contentPath, Matrix maskMatrix, Matrix matteMatrix, Matrix parentMatrix, boolean applyToPath) {
    combinedPath.reset();
    for (int i = 0; i < getMaskAnimations().size(); i++) {
      BaseKeyframeAnimation<ShapeData, Path> mask = getMaskAnimations().get(i);
      Path maskPath = mask.getValue();
      maskPath.transform(maskMatrix);
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
      // TODO this won't work if there are masks and mattes
//      combinedPath.set(contentPath);

      List<Path> mattePaths = matteLayer.getPaths();
      combinedPath.setFillType(Path.FillType.WINDING);
//      mattePath.transform(parentMatrix);
      if (matteType == Layer.MatteType.Add) {
        RectF bounds = new RectF();
//        mattePath.computeBounds(bounds, false);
//        Log.d("Gabe", "Matte: " + bounds);

        for (int i = 0; i < mattePaths.size(); i++) {
          Path mattePath = new Path(mattePaths.get(i));
//          mattePath.transform(parentMatrix);


//          combinedPath.addPath(mattePath);


          addPath.set(contentPath);
//          addPath.transform(parentMatrix);
          mattePath.transform(matteMatrix);
          addPath.op(mattePath, Path.Op.INTERSECT);
          if (combinedPath.isEmpty()) {
            combinedPath.addPath(addPath);
          } else {
            combinedPath.op(addPath, Path.Op.UNION);
          }
//          combinedPath.addPath(addPath);

        }

//        addPath.set(contentPath);
//        long now = System.currentTimeMillis();
//        addPath.op(mattePath, Path.Op.UNION);
//        long end = System.currentTimeMillis() - now;
//        Log.d("Gabe", "getPath\t" + end);
//        combinedPath.addPath(addPath);

//        combinedPath.computeBounds(bounds, false);
//        Log.d("Gabe", "Combined: " + bounds);

      } else if (matteType == Layer.MatteType.Invert) {
//        subtractPath.set(contentPath);
//        subtractPath.op(mattePath, Path.Op.DIFFERENCE);
//        if (combinedPath.isEmpty()) {
//          combinedPath.addPath(contentPath);
//        }
//        combinedPath.op(subtractPath, Path.Op.INTERSECT);
      }

//      combinedPath.addPath(mattePath);
    }

    if (applyToPath) {
      contentPath.set(combinedPath);
    }

    return combinedPath;
  }
}
