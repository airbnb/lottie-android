package com.airbnb.lottie;

import android.graphics.Rect;

interface Transform {
  Rect getBounds();
  IAnimatablePathValue getPosition();
  IAnimatablePathValue getAnchor();
  AnimatableScaleValue getScale();
  AnimatableFloatValue getRotation();
  AnimatableIntegerValue getOpacity();
}