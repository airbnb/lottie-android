package com.airbnb.lottie;

import android.graphics.Bitmap;
import android.graphics.ColorFilter;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Typeface;

import com.airbnb.lottie.value.LottieValueCallback;
import com.airbnb.lottie.value.ScaleXY;

/**
 * Property values are the same type as the generic type of their corresponding
 * {@link LottieValueCallback}. With this, we can use generics to maintain type safety
 * of the callbacks.
 * <p>
 * Supported properties:
 * Transform:
 * {@link #TRANSFORM_ANCHOR_POINT}
 * {@link #TRANSFORM_POSITION}
 * {@link #TRANSFORM_OPACITY}
 * {@link #TRANSFORM_SCALE}
 * {@link #TRANSFORM_ROTATION}
 * {@link #TRANSFORM_SKEW}
 * {@link #TRANSFORM_SKEW_ANGLE}
 * <p>
 * Fill:
 * {@link #COLOR} (non-gradient)
 * {@link #OPACITY}
 * {@link #COLOR_FILTER}
 * <p>
 * Stroke:
 * {@link #COLOR} (non-gradient)
 * {@link #STROKE_WIDTH}
 * {@link #OPACITY}
 * {@link #COLOR_FILTER}
 * <p>
 * Ellipse:
 * {@link #POSITION}
 * {@link #ELLIPSE_SIZE}
 * <p>
 * Polystar:
 * {@link #POLYSTAR_POINTS}
 * {@link #POLYSTAR_ROTATION}
 * {@link #POSITION}
 * {@link #POLYSTAR_INNER_RADIUS} (star)
 * {@link #POLYSTAR_OUTER_RADIUS}
 * {@link #POLYSTAR_INNER_ROUNDEDNESS} (star)
 * {@link #POLYSTAR_OUTER_ROUNDEDNESS}
 * <p>
 * Repeater:
 * All transform properties
 * {@link #REPEATER_COPIES}
 * {@link #REPEATER_OFFSET}
 * {@link #TRANSFORM_ROTATION}
 * {@link #TRANSFORM_START_OPACITY}
 * {@link #TRANSFORM_END_OPACITY}
 * <p>
 * Layers:
 * All transform properties
 * {@link #TIME_REMAP} (composition layers only)
 */
public interface LottieProperty {
  /**
   * ColorInt
   **/
  Integer COLOR = 1;
  Integer STROKE_COLOR = 2;
  /**
   * Opacity value are 0-100 to match after effects
   **/
  Integer TRANSFORM_OPACITY = 3;
  /**
   * [0,100]
   */
  Integer OPACITY = 4;
  Integer DROP_SHADOW_COLOR = 5;
  /**
   * In Px
   */
  PointF TRANSFORM_ANCHOR_POINT = new PointF();
  /**
   * In Px
   */
  PointF TRANSFORM_POSITION = new PointF();
  /**
   * When split dimensions is enabled. In Px
   */
  Float TRANSFORM_POSITION_X = 15f;
  /**
   * When split dimensions is enabled. In Px
   */
  Float TRANSFORM_POSITION_Y = 16f;
  /**
   * In Px
   */
  Float BLUR_RADIUS = 17f;
  /**
   * In Px
   */
  PointF ELLIPSE_SIZE = new PointF();
  /**
   * In Px
   */
  PointF RECTANGLE_SIZE = new PointF();
  /**
   * In degrees
   */
  Float CORNER_RADIUS = 0f;
  /**
   * In Px
   */
  PointF POSITION = new PointF();
  ScaleXY TRANSFORM_SCALE = new ScaleXY();
  /**
   * In degrees
   */
  Float TRANSFORM_ROTATION = 1f;
  /**
   * In degrees - 3D X-axis rotation
   */
  Float TRANSFORM_ROTATION_X = 1.1f;
  /**
   * In degrees - 3D Y-axis rotation
   */
  Float TRANSFORM_ROTATION_Y = 1.2f;
  /**
   * In degrees - 3D Z-axis rotation
   */
  Float TRANSFORM_ROTATION_Z = 1.3f;
  /**
   * 0-85
   */
  Float TRANSFORM_SKEW = 0f;
  /**
   * In degrees
   */
  Float TRANSFORM_SKEW_ANGLE = 0f;
  /**
   * In Px
   */
  Float STROKE_WIDTH = 2f;
  Float TEXT_TRACKING = 3f;
  Float REPEATER_COPIES = 4f;
  Float REPEATER_OFFSET = 5f;
  Float POLYSTAR_POINTS = 6f;
  /**
   * In degrees
   */
  Float POLYSTAR_ROTATION = 7f;
  /**
   * In Px
   */
  Float POLYSTAR_INNER_RADIUS = 8f;
  /**
   * In Px
   */
  Float POLYSTAR_OUTER_RADIUS = 9f;
  /**
   * [0,100]
   */
  Float POLYSTAR_INNER_ROUNDEDNESS = 10f;
  /**
   * [0,100]
   */
  Float POLYSTAR_OUTER_ROUNDEDNESS = 11f;
  /**
   * [0,100]
   */
  Float TRANSFORM_START_OPACITY = 12f;
  /**
   * [0,100]
   */
  Float TRANSFORM_END_OPACITY = 12.1f;
  /**
   * The time value in seconds
   */
  Float TIME_REMAP = 13f;
  /**
   * In Dp
   */
  Float TEXT_SIZE = 14f;
  /**
   * [0,100]
   * Lottie Android resolved drop shadows on drawing content such as fills and strokes.
   * If a drop shadow is applied to a layer, the dynamic properties must be set on all
   * of its child elements that draw. The easiest way to do this is to append "**" to your
   * Keypath after the layer name.
   */
  Float DROP_SHADOW_OPACITY = 15f;
  /**
   * Degrees from 12 o'clock.
   * Lottie Android resolved drop shadows on drawing content such as fills and strokes.
   * If a drop shadow is applied to a layer, the dynamic properties must be set on all
   * of its child elements that draw. The easiest way to do this is to append "**" to your
   * Keypath after the layer name.
   */
  Float DROP_SHADOW_DIRECTION = 16f;
  /**
   * In Px
   * Lottie Android resolved drop shadows on drawing content such as fills and strokes.
   * If a drop shadow is applied to a layer, the dynamic properties must be set on all
   * of its child elements that draw. The easiest way to do this is to append "**" to your
   * Keypath after the layer name.
   */
  Float DROP_SHADOW_DISTANCE = 17f;
  /**
   * In Px
   * Lottie Android resolved drop shadows on drawing content such as fills and strokes.
   * If a drop shadow is applied to a layer, the dynamic properties must be set on all
   * of its child elements that draw. The easiest way to do this is to append "**" to your
   * Keypath after the layer name.
   */
  Float DROP_SHADOW_RADIUS = 18f;
  /**
   * Set the color filter for an entire drawable content. Can be applied to fills, strokes, images, and solids.
   */
  ColorFilter COLOR_FILTER = new ColorFilter();
  /**
   * Array of ARGB colors that map to position stops in the original gradient.
   * For example, a gradient from red to blue could be remapped with [0xFF00FF00, 0xFFFF00FF] (green to purple).
   */
  Integer[] GRADIENT_COLOR = new Integer[0];
  /**
   * Set on text layers.
   */
  Typeface TYPEFACE = Typeface.DEFAULT;
  /**
   * Set on image layers.
   */
  Bitmap IMAGE = Bitmap.createBitmap(1, 1, Bitmap.Config.ALPHA_8);
  /**
   * Replace the text for a text layer.
   */
  CharSequence TEXT = "dynamic_text";

  /**
   * Replace a path. This can only be used on path contents. For other shapes such as rectangles and polystars,
   * use LottieProperties corresponding to their specific properties.
   * <p>
   * If you need to do any operations on the path such as morphing, use the Jetpack androidx.graphics.path library.
   * <p>
   * In After Effects, any of those other shapes can be converted to a bezier path by right clicking it and
   * selecting "Convert To Bezier Path".
   */
  Path PATH = new Path();
}
