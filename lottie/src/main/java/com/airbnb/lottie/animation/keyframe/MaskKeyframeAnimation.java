package com.airbnb.lottie.animation.keyframe;

import android.graphics.Matrix;
import android.graphics.Path;

import android.graphics.RectF;
import androidx.annotation.Nullable;
import com.airbnb.lottie.animation.canvas.RecordedCanvas;
import com.airbnb.lottie.model.animatable.AnimatableIntegerValue;
import com.airbnb.lottie.model.content.Mask;
import com.airbnb.lottie.model.content.ShapeData;
import com.airbnb.lottie.model.layer.BaseLayer;
import com.airbnb.lottie.model.layer.Layer;
import com.airbnb.lottie.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class MaskKeyframeAnimation {
  private final List<BaseKeyframeAnimation<ShapeData, Path>> maskAnimations;
  private final List<BaseKeyframeAnimation<Integer, Integer>> opacityAnimations;
  private final List<Mask> masks;
  /**
   * Reusable path for calculating masks.
   */
  private final Path masksPath = new Path();
  private final Path mattesPath = new Path();
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
    if (getMaskAnimations().isEmpty() && matteLayer == null) {
      return contentPath;
    }

    masksPath.reset();
    for (int i = 0; i < getMaskAnimations().size(); i++) {
      BaseKeyframeAnimation<ShapeData, Path> mask = getMaskAnimations().get(i);
      Path maskPath = mask.getValue();
      maskPath.transform(maskMatrix);
      Mask.MaskMode maskMode = getMasks().get(i).getMaskMode();
      if (maskMode == Mask.MaskMode.MaskModeAdd || maskMode == Mask.MaskMode.MaskModeIntersect) {
        addPath.set(contentPath);
        addPath.op(maskPath, Path.Op.INTERSECT);
        masksPath.op(addPath, Path.Op.UNION);
      } else if (maskMode == Mask.MaskMode.MaskModeSubtract) {
        subtractPath.set(contentPath);
        subtractPath.op(maskPath, Path.Op.DIFFERENCE);
        if (masksPath.isEmpty()) {
          masksPath.addPath(contentPath);
        }
        masksPath.op(subtractPath, Path.Op.INTERSECT);
      }
    }
    masksPath.close();

    mattesPath.reset();
//    mattesPath.setFillType(Path.FillType.WINDING);
    if (matteLayer != null && matteType != null) {
      RectF contentBounds = Utils.getBounds(contentPath);
      RecordedCanvas canvas = new RecordedCanvas((int) contentBounds.right, (int) contentBounds.bottom);


      matteLayer.draw(canvas, matteMatrix, 255, null, new Matrix(), new Matrix());
      List<Path> mattePaths = canvas.getPaths();
      if (matteType == Layer.MatteType.Add) {
        for (int i = 0; i < mattePaths.size(); i++) {
          Path mattePath = new Path(mattePaths.get(i));
          addPath.set(contentPath);
          addPath.op(mattePath, Path.Op.INTERSECT);
          mattesPath.op(addPath, Path.Op.UNION);
        }
      } else if (matteType == Layer.MatteType.Invert) {
        for (int i = 0; i < mattePaths.size(); i++) {
          Path mattePath = new Path(mattePaths.get(i));
          subtractPath.set(contentPath);
          mattePath.transform(matteMatrix);
          subtractPath.op(mattePath, Path.Op.DIFFERENCE);
        }
        if (mattesPath.isEmpty()) {
          mattesPath.addPath(subtractPath);
        } else {
          mattesPath.op(subtractPath, Path.Op.INTERSECT);
        }


//        subtractPath.set(contentPath);
//        subtractPath.op(mattePath, Path.Op.DIFFERENCE);
//        if (combinedPath.isEmpty()) {
//          combinedPath.addPath(contentPath);
//        }
//        combinedPath.op(subtractPath, Path.Op.INTERSECT);
      }

    }

    if (!masksPath.isEmpty() && !mattesPath.isEmpty()) {
      combinedPath.op(masksPath, mattesPath, Path.Op.INTERSECT);
    } else if (!masksPath.isEmpty()) {
      combinedPath.set(masksPath);
    } else if (mattesPath != null) {
      combinedPath.set(mattesPath);
    }
    if (applyToPath) {
      contentPath.set(combinedPath);
    }
    return combinedPath;
  }
}
