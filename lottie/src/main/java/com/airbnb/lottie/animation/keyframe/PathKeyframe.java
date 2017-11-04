package com.airbnb.lottie.animation.keyframe;

import android.graphics.Path;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.view.animation.Interpolator;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.animation.Keyframe;
import com.airbnb.lottie.model.animatable.AnimatableValue;
import com.airbnb.lottie.utils.JsonUtils;
import com.airbnb.lottie.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

public class PathKeyframe extends Keyframe<PointF> {
  @Nullable private Path path;

  private PathKeyframe(LottieComposition composition, @Nullable PointF startValue,
      @Nullable PointF endValue, @Nullable Interpolator interpolator, int startFrame,
      @Nullable Integer endFrame) {
    super(composition, startValue, endValue, interpolator, startFrame, endFrame);
  }

  public static class Factory {
    private Factory() {
    }

    public static PathKeyframe newInstance(JSONObject json, LottieComposition composition,
        AnimatableValue.Factory<PointF> valueFactory) {
      Keyframe<PointF> keyframe = Keyframe.Factory.newInstance(json, composition,
          composition.getDpScale(), valueFactory);
      PointF cp1 = null;
      PointF cp2 = null;
      JSONArray tiJson = json.optJSONArray("ti");
      JSONArray toJson = json.optJSONArray("to");
      if (tiJson != null && toJson != null) {
        cp1 = JsonUtils.pointFromJsonArray(toJson, composition.getDpScale());
        cp2 = JsonUtils.pointFromJsonArray(tiJson, composition.getDpScale());
      }

      PathKeyframe pathKeyframe = new PathKeyframe(composition, keyframe.startValue,
          keyframe.endValue, keyframe.interpolator, keyframe.startFrame, keyframe.endFrame);

      // This must use equals(float, float) because PointF didn't have an equals(PathF) method
      // until KitKat...
      boolean equals = keyframe.endValue != null && keyframe.startValue != null &&
          keyframe.startValue.equals(keyframe.endValue.x, keyframe.endValue.y);
      //noinspection ConstantConditions
      if (pathKeyframe.endValue != null && !equals) {
        pathKeyframe.path = Utils.createPath(keyframe.startValue, keyframe.endValue, cp1, cp2);
      }
      return pathKeyframe;
    }
  }

  /** This will be null if the startValue and endValue are the same. */
  @Nullable Path getPath() {
    return path;
  }
}
