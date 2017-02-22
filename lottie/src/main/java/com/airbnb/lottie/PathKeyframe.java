package com.airbnb.lottie;

import android.graphics.Path;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.view.animation.Interpolator;

import org.json.JSONArray;
import org.json.JSONObject;

class PathKeyframe extends Keyframe<PointF> {
  @Nullable private Path path;

  PathKeyframe(LottieComposition composition, @Nullable PointF startValue,
      @Nullable PointF endValue, @Nullable Interpolator interpolator, float startFrame,
      @Nullable Float endFrame) {
    super(composition, startValue, endValue, interpolator, startFrame, endFrame);
  }

  static class Factory {
    static PathKeyframe newInstance(JSONObject json, LottieComposition composition,
        AnimatableValue<PointF, ?> animatableValue) {
      Keyframe<PointF> keyframe = Keyframe.Factory.newInstance(json, composition,
          composition.getScale(), animatableValue);
      PointF cp1 = null;
      PointF cp2 = null;
      JSONArray tiJson = json.optJSONArray("ti");
      JSONArray toJson = json.optJSONArray("to");
      if (tiJson != null && toJson != null) {
        cp1 = JsonUtils.pointFromJsonArray(toJson, composition.getScale());
        cp2 = JsonUtils.pointFromJsonArray(tiJson, composition.getScale());
      }

      PathKeyframe pathKeyframe = new PathKeyframe(composition, keyframe.startValue,
          keyframe.endValue, keyframe.interpolator, keyframe.startFrame, keyframe.endFrame);

      if (keyframe.endValue != null && !keyframe.startValue.equals(keyframe.endValue)) {
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
