package com.airbnb.lottie.animation.content;

import com.airbnb.lottie.model.content.ShapeData;

public interface ShapeModifierContent extends Content {
  ShapeData modifyShape(ShapeData shapeData);
}
