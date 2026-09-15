package com.airbnb.lottie.animation.keyframe;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.view.animation.LinearInterpolator;

import com.airbnb.lottie.BaseTest;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.animatable.AnimatablePathValue;
import com.airbnb.lottie.model.animatable.AnimatablePointValue;
import com.airbnb.lottie.model.animatable.AnimatableTransform;
import com.airbnb.lottie.value.Keyframe;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertArrayEquals;

public class TransformKeyframeAnimationTest extends BaseTest {
  private static final float EPSILON = 0.001f;

  @Test
  public void skewStillAppliesWith3DRotation() {
    AnimatableTransform transform = new AnimatableTransform(null, null, null, null,
        null, null, null, value(45), value(0), value(60), null, null);
    assertMapsTo(transform.createAnimation().getMatrix(), new float[] {0, 2},
        new float[] {-2, 1});
  }

  @Test
  public void autoOrientComposesWithZAndXRotation() {
    AnimatablePointValue position = new AnimatablePointValue(Collections.singletonList(
        new Keyframe<PointF>(null, new PointF(0, 0), new PointF(0, 100),
            new LinearInterpolator(), 0f, 1f)));
    AnimatableTransform transform = new AnimatableTransform(null, position, null, value(243),
        null, null, null, null, null, value(60), null, value(30));
    transform.setAutoOrient(true);
    assertMapsTo(transform.createAnimation().getMatrix(), new float[] {2, 2},
        new float[] {-1.3660254f, 0.7320508f});
  }

  @Test
  public void autoOrientPreservesExplicitRotation() {
    // Like star.json: auto orientation and an explicit 243-degree rotation coexist.
    AnimatablePointValue position = new AnimatablePointValue(Collections.singletonList(
        new Keyframe<PointF>(null, new PointF(0, 0), new PointF(0, 100),
            new LinearInterpolator(), 0f, 1f)));
    AnimatableTransform transform = new AnimatableTransform(null, position, null, value(243),
        null, null, null, null, null, null, null, null);
    transform.setAutoOrient(true);
    // The downward path contributes 90 degrees: 243 + 90 = 333.
    assertMapsTo(transform.createAnimation().getMatrix(), new float[] {1, 0},
        new float[] {0.8910065f, -0.4539905f});
  }

  @Test
  public void threeDRotationDoesNotAlsoApply2DRotation() {
    AnimatableTransform transform = new AnimatableTransform(null, null, null, value(90),
        null, null, null, null, null, value(60), null, null);
    assertMapsTo(transform.createAnimation().getMatrix(), new float[] {1, 0, 0, 1},
        new float[] {1, 0, 0, 0.5f});
  }

  @Test
  public void crossingZeroDoesNotFallBackTo2DRotation() {
    AnimatableFloatValue rotationX = new AnimatableFloatValue(Collections.singletonList(
        new Keyframe<Float>(null, -60f, 60f, new LinearInterpolator(), 0f, 1f)));
    AnimatableTransform transform = new AnimatableTransform(null, null, null, value(90),
        null, null, null, null, null, rotationX, null, null);
    TransformKeyframeAnimation animation = transform.createAnimation();
    for (float progress : new float[] {0.49f, 0.5f, 0.51f, 0.5f}) {
      animation.setProgress(progress);
      assertMapsTo(animation.getMatrix(), new float[] {1, 0}, new float[] {1, 0});
      assertMapsTo(animation.getMatrixForRepeater(1), new float[] {1, 0}, new float[] {1, 0});
    }
  }

  @Test
  public void repeaterRotatesAroundAnchorAndRefreshesCacheForEachAmount() {
    AnimatablePathValue anchor = new AnimatablePathValue(Collections.singletonList(
        new Keyframe<>(new PointF(10, 20))));
    AnimatableTransform transform = new AnimatableTransform(anchor, null, null, null,
        null, null, null, null, null, value(30), value(30), null);
    TransformKeyframeAnimation animation = transform.createAnimation();
    for (float amount : new float[] {2, 0, 1, 2}) {
      float radians = (float) Math.toRadians(30 * amount);
      float cos = (float) Math.cos(radians);
      float sin = (float) Math.sin(radians);
      assertMapsTo(animation.getMatrixForRepeater(amount), new float[] {10, 20, 12, 20},
          new float[] {10, 20, 10 + 2 * cos, 20 + 2 * sin * sin});
    }
  }

  private static AnimatableFloatValue value(float value) {
    return new AnimatableFloatValue(Collections.singletonList(new Keyframe<>(value)));
  }

  private static void assertMapsTo(Matrix matrix, float[] points, float[] expected) {
    matrix.mapPoints(points);
    assertArrayEquals(expected, points, EPSILON);
  }
}
