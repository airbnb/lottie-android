package com.airbnb.lottie.utils;

import android.content.res.Resources;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import androidx.annotation.Nullable;

import com.airbnb.lottie.L;
import com.airbnb.lottie.animation.content.TrimPathContent;

import java.io.Closeable;

public final class Utils {
  public static final int SECOND_IN_NANOS = 1000000000;

  private static final PathMeasure pathMeasure = new PathMeasure();
  private static final Path tempPath = new Path();
  private static final Path tempPath2 = new Path();
  private static final float[] points = new float[4];
  private static final float SQRT_2 = (float) Math.sqrt(2);
  private static float dpScale = -1;

  private Utils() {}

  public static Path createPath(PointF startPoint, PointF endPoint, PointF cp1, PointF cp2) {
    Path path = new Path();
    path.moveTo(startPoint.x, startPoint.y);

    if (cp1 != null  && cp2 != null && (cp1.length() != 0 || cp2.length() != 0)) {
      path.cubicTo(
          startPoint.x + cp1.x, startPoint.y + cp1.y,
          endPoint.x + cp2.x, endPoint.y + cp2.y,
          endPoint.x, endPoint.y);
    } else {
      path.lineTo(endPoint.x, endPoint.y);
    }
    return path;
  }

  public static void closeQuietly(Closeable closeable) {
    if (closeable != null) {
      try {
        closeable.close();
      } catch (RuntimeException rethrown) {
        throw rethrown;
      } catch (Exception ignored) {
      }
    }
  }

  public static float getScale(Matrix matrix) {
    points[0] = 0;
    points[1] = 0;
    // Use sqrt(2) so that the hypotenuse is of length 1.
    points[2] = SQRT_2;
    points[3] = SQRT_2;
    matrix.mapPoints(points);
    float dx = points[2] - points[0];
    float dy = points[3] - points[1];

    // TODO: figure out why the result needs to be divided by 2.
    return (float) Math.hypot(dx, dy) / 2f;
  }

  public static void applyTrimPathIfNeeded(Path path, @Nullable TrimPathContent trimPath) {
    if (trimPath == null) {
      return;
    }
    applyTrimPathIfNeeded(path, trimPath.getStart().getValue() / 100f,
        trimPath.getEnd().getValue() / 100f, trimPath.getOffset().getValue() / 360f);
  }

  public static void applyTrimPathIfNeeded(
      Path path, float startValue, float endValue, float offsetValue) {
    L.beginSection("applyTrimPathIfNeeded");
    pathMeasure.setPath(path, false);

    float length = pathMeasure.getLength();
    if (startValue == 1f && endValue == 0f) {
      L.endSection("applyTrimPathIfNeeded");
      return;
    }
    if (length < 1f || Math.abs(endValue - startValue - 1) < .01) {
      L.endSection("applyTrimPathIfNeeded");
      return;
    }
    float start = length * startValue;
    float end = length * endValue;
    float newStart = Math.min(start, end);
    float newEnd = Math.max(start, end);

    float offset = offsetValue * length;
    newStart += offset;
    newEnd += offset;

    // If the trim path has rotated around the path, we need to shift it back.
    if (newStart >= length && newEnd >= length) {
      newStart = MiscUtils.floorMod(newStart, length);
      newEnd = MiscUtils.floorMod(newEnd, length);
    }

    if (newStart < 0) {
      newStart = MiscUtils.floorMod(newStart, length);
    }
    if (newEnd < 0) {
      newEnd = MiscUtils.floorMod(newEnd, length);
    }

    // If the start and end are equals, return an empty path.
    if (newStart == newEnd) {
      path.reset();
      L.endSection("applyTrimPathIfNeeded");
      return;
    }

    if (newStart >= newEnd) {
      newStart -= length;
    }

    tempPath.reset();
    pathMeasure.getSegment(
        newStart,
        newEnd,
        tempPath,
        true);

    if (newEnd > length) {
      tempPath2.reset();
      pathMeasure.getSegment(
          0,
          newEnd % length,
          tempPath2,
          true);
      tempPath.addPath(tempPath2);
    } else if (newStart < 0) {
      tempPath2.reset();
      pathMeasure.getSegment(
          length + newStart,
          length,
          tempPath2,
          true);
      tempPath.addPath(tempPath2);
    }
    path.set(tempPath);
    L.endSection("applyTrimPathIfNeeded");
  }

  @SuppressWarnings("SameParameterValue")
  public static boolean isAtLeastVersion(int major, int minor, int patch, int minMajor, int minMinor, int
      minPatch) {
    if (major < minMajor) {
      return false;
    } else if (major > minMajor) {
      return true;
    }

    if (minor < minMinor) {
      return false;
    } else if (minor > minMinor) {
      return true;
    }

    return patch >= minPatch;
  }

  public static int hashFor(float a, float b, float c, float d) {
    int result = 17;
    if (a != 0) {
      result = (int) (31 * result * a);
    }
    if (b != 0) {
      result = (int) (31 * result * b);
    }
    if (c != 0) {
      result = (int) (31 * result * c);
    }
    if (d != 0) {
      result = (int) (31 * result * d);
    }
    return result;
  }

  public static float dpScale() {
    if (dpScale == -1) {
      dpScale = Resources.getSystem().getDisplayMetrics().density;
    }
    return dpScale;
  }
}
