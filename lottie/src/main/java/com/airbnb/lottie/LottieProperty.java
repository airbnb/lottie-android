package com.airbnb.lottie;

import android.graphics.PointF;

import com.airbnb.lottie.value.ScaleXY;
import com.airbnb.lottie.value.LottieValueCallback;

/**
 * Property values are the same type as the generic type of their corresponding
 * {@link LottieValueCallback}. With this, we can use generics to maintain type safety
 * of the callbacks.
 *
 * Supported properties:
 * Transform:
 *    {@link #TRANSFORM_ANCHOR_POINT}
 *    {@link #TRANSFORM_POSITION}
 *    {@link #TRANSFORM_OPACITY}
 *    {@link #TRANSFORM_SCALE}
 *    {@link #TRANSFORM_ROTATION}
 *
 * Fill:
 *    {@link #COLOR} (non-gradient)
 *    {@link #OPACITY}
 *
 * Stroke:
 *    {@link #COLOR} (non-gradient)
 *    {@link #STROKE_WIDTH}
 *    {@link #OPACITY}
 *
 * Ellipse:
 *    {@link #POSITION}
 *    {@link #ELLIPSE_SIZE}
 *
 * Polystar:
 *    {@link #POLYSTAR_POINTS}
 *    {@link #POLYSTAR_ROTATION}
 *    {@link #POSITION}
 *    {@link #POLYSTAR_INNER_RADIUS} (star)
 *    {@link #POLYSTAR_OUTER_RADIUS}
 *    {@link #POLYSTAR_INNER_ROUNDEDNESS} (star)
 *    {@link #POLYSTAR_OUTER_ROUNDEDNESS}
 *
 * Repeater:
 *    All transform properties
 *    {@link #REPEATER_COPIES}
 *    {@link #REPEATER_OFFSET}
 */
public interface LottieProperty {
  Integer COLOR = 1;
  Integer STROKE_COLOR = 2;
  /** Opacity value are 0-100 to match after effects **/
  Integer TRANSFORM_OPACITY = 3;
  Integer OPACITY = 4;

  PointF TRANSFORM_ANCHOR_POINT = new PointF();
  PointF TRANSFORM_POSITION = new PointF();
  PointF ELLIPSE_SIZE = new PointF();
  PointF POSITION = new PointF();

  ScaleXY TRANSFORM_SCALE = new ScaleXY();

  /** In degrees */
  Float TRANSFORM_ROTATION = 1f;
  Float STROKE_WIDTH = 2f;
  Float TEXT_TRACKING = 3f;
  Float REPEATER_COPIES = 4f;
  Float REPEATER_OFFSET = 5f;
  Float POLYSTAR_POINTS = 6f;
  /** In degrees */
  Float POLYSTAR_ROTATION = 7f;
  Float POLYSTAR_INNER_RADIUS = 8f;
  Float POLYSTAR_OUTER_RADIUS = 9f;
  /** [0,100] */
  Float POLYSTAR_INNER_ROUNDEDNESS = 10f;
  /** [0,100] */
  Float POLYSTAR_OUTER_ROUNDEDNESS = 11f;
}
