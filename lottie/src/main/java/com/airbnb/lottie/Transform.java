package com.airbnb.lottie;

import android.graphics.Rect;

interface Transform {
  Rect getBounds();
  AnimatablePathValue getPosition();
  AnimatablePathValue getAnchor();
  AnimatableScaleValue getScale();
  AnimatableFloatValue getRotation();
  AnimatableIntegerValue getOpacity();
}