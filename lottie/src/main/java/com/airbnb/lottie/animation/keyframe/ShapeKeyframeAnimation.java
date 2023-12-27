package com.airbnb.lottie.animation.keyframe;

import android.graphics.Path;

import androidx.annotation.Nullable;

import com.airbnb.lottie.animation.content.ShapeModifierContent;
import com.airbnb.lottie.model.content.ShapeData;
import com.airbnb.lottie.utils.MiscUtils;
import com.airbnb.lottie.value.Keyframe;

import java.util.List;

public class ShapeKeyframeAnimation extends BaseKeyframeAnimation<ShapeData, Path> {
  private final ShapeData tempShapeData = new ShapeData();
  private final Path tempPath = new Path();

  private List<ShapeModifierContent> shapeModifiers;

  public ShapeKeyframeAnimation(List<Keyframe<ShapeData>> keyframes) {
    super(keyframes);
  }

  @Override public Path getValue(Keyframe<ShapeData> keyframe, float keyframeProgress) {
    ShapeData startShapeData = keyframe.startValue;
    ShapeData endShapeData = keyframe.endValue;

    tempShapeData.interpolateBetween(startShapeData, endShapeData == null ? startShapeData : endShapeData, keyframeProgress);
    ShapeData modifiedShapeData = tempShapeData;
    if (shapeModifiers != null) {
      for (int i = shapeModifiers.size() - 1; i >= 0; i--) {
        modifiedShapeData = shapeModifiers.get(i).modifyShape(modifiedShapeData);
      }
    }
    MiscUtils.getPathFromData(modifiedShapeData, tempPath);
    return tempPath;
  }

  public void setShapeModifiers(@Nullable List<ShapeModifierContent> shapeModifiers) {
    this.shapeModifiers = shapeModifiers;
  }
}
