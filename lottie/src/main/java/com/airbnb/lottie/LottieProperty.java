package com.airbnb.lottie;

import android.graphics.ColorFilter;
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
 *    {@link #COLOR_FILTER}
 *
 * Stroke:
 *    {@link #COLOR} (non-gradient)
 *    {@link #STROKE_WIDTH}
 *    {@link #OPACITY}
 *    {@link #COLOR_FILTER}
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
 *    {@link #TRANSFORM_ROTATION}
 *    {@link #TRANSFORM_START_OPACITY}
 *    {@link #TRANSFORM_END_OPACITY}
 *
 * Layers:
 *    All transform properties
 *    {@link #TIME_REMAP} (composition layers only)
 */
public interface LottieProperty {
  /** ColorInt **/
  Integer COLOR = 1;
  Integer STROKE_COLOR = 2;
  /** Opacity value are 0-100 to match after effects **/
  Integer TRANSFORM_OPACITY = 3;
  /** [0,100] */
  Integer OPACITY = 4;

  /** In Px */
  PointF TRANSFORM_ANCHOR_POINT = new PointF();
  /** In Px */
  PointF TRANSFORM_POSITION = new PointF();
  /** In Px */
  PointF ELLIPSE_SIZE = new PointF();
  /** In Px */
  PointF POSITION = new PointF();

  ScaleXY TRANSFORM_SCALE = new ScaleXY();

  /** In degrees */
  Float TRANSFORM_ROTATION = 1f;
  /** In Px */
  Float STROKE_WIDTH = 2f;
  Float TEXT_TRACKING = 3f;
  Float REPEATER_COPIES = 4f;
  Float REPEATER_OFFSET = 5f;
  Float POLYSTAR_POINTS = 6f;
  /** In degrees */
  Float POLYSTAR_ROTATION = 7f;
  /** In Px */
  Float POLYSTAR_INNER_RADIUS = 8f;
  /** In Px */
  Float POLYSTAR_OUTER_RADIUS = 9f;
  /** [0,100] */
  Float POLYSTAR_INNER_ROUNDEDNESS = 10f;
  /** [0,100] */
  Float POLYSTAR_OUTER_ROUNDEDNESS = 11f;
  /** [0,100] */
  Float TRANSFORM_START_OPACITY = 12f;
  /** [0,100] */
  Float TRANSFORM_END_OPACITY = 12f;
  /** The time value in seconds */
  Float TIME_REMAP = 13f;

  ColorFilter COLOR_FILTER = new ColorFilter();
}
