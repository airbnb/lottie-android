package com.airbnb.lottie;

import android.graphics.Path;
import android.support.annotation.FloatRange;
import android.support.annotation.Nullable;
import android.view.animation.Interpolator;

import org.json.JSONArray;
import org.json.JSONObject;

class PathKeyframe extends Keyframe<CPointF> implements IPathKeyframe {
  @Nullable private Path path;

  private PathKeyframe(LottieComposition composition, @Nullable CPointF startValue,
      @Nullable CPointF endValue, @Nullable Interpolator interpolator, float startFrame,
      @Nullable Float endFrame) {
    super(composition, startValue, endValue, interpolator, startFrame, endFrame);
  }

  static class Factory {
    private Factory() {
    }

    static PathKeyframe newInstance(JSONObject json, LottieComposition composition,
        AnimatableValue<CPointF, ?> animatableValue) {
      Keyframe<CPointF> keyframe = Keyframe.Factory.newInstance(json, composition,
          composition.getScale(), animatableValue);
      CPointF cp1 = null;
      CPointF cp2 = null;
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

  @Override
  public void setProgress(@FloatRange(from = 0f, to = 1f) float progress) {
    // do noting
  }

  /** This will be null if the startValue and endValue are the same. */
  public Path getPath() {
    return path;
  }
}
