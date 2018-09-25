package com.airbnb.lottie.animation.keyframe;

import android.graphics.Path;
import android.graphics.PointF;
import androidx.annotation.Nullable;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.value.Keyframe;
import com.airbnb.lottie.utils.Utils;

public class PathKeyframe extends Keyframe<PointF> {
  @Nullable private Path path;

  public PathKeyframe(LottieComposition composition, Keyframe<PointF> keyframe) {
    super(composition, keyframe.startValue, keyframe.endValue, keyframe.interpolator,
        keyframe.startFrame, keyframe.endFrame);

    // This must use equals(float, float) because PointF didn't have an equals(PathF) method
    // until KitKat...
    boolean equals = endValue != null && startValue != null &&
        startValue.equals(endValue.x, endValue.y);
    //noinspection ConstantConditions
    if (endValue != null && !equals) {
      path = Utils.createPath(startValue, endValue, keyframe.pathCp1, keyframe.pathCp2);
    }
  }

  /** This will be null if the startValue and endValue are the same. */
  @Nullable Path getPath() {
    return path;
  }
}
