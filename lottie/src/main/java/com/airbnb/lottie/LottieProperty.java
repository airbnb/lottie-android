package com.airbnb.lottie;

import android.graphics.PointF;

import com.airbnb.lottie.value.ScaleXY;
import com.airbnb.lottie.value.LottieValueCallback;

/**
 * Property values are the same type as the generic type of their corresponding
 * {@link LottieValueCallback}. With this, we can use generics to maintain type safety
 * of the callbacks.
 */
public interface LottieProperty {
  Integer COLOR = 1;
  /** Opacity value are 0-100 to match after effects **/
  Integer TRANSFORM_OPACITY = 2;

  PointF TRANSFORM_ANCHOR_POINT = new PointF();
  PointF TRANSFORM_POSITION = new PointF();

  ScaleXY TRANSFORM_SCALE = new ScaleXY();

  Float TRANSFORM_ROTATION = 1f;
}
