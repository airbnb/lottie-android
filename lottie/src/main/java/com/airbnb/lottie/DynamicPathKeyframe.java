package com.airbnb.lottie;

import android.graphics.Path;
import android.support.annotation.FloatRange;
import android.support.annotation.Nullable;
import android.view.animation.Interpolator;

import org.json.JSONArray;
import org.json.JSONObject;

class DynamicPathKeyframe extends Keyframe<CPointF> implements IPathKeyframe {
    @Nullable
    private Path path;
    private MagicPointFF magicPointF, magicPointF2, magicPointF3, magicPointF4;
    private LottieComposition composition;

    DynamicPathKeyframe(LottieComposition composition, @Nullable CPointF startValue,
                        @Nullable CPointF endValue, @Nullable Interpolator interpolator, float startFrame,
                        @Nullable Float endFrame) {
        super(composition, startValue, endValue, interpolator, startFrame, endFrame);

    }

    static class Factory {
        private Factory() {
        }

        static DynamicPathKeyframe newInstance(JSONObject json, LottieComposition composition,
                                               AnimatableValue<CPointF, ?> animatableValue, MagicPointFF magicPointF) {
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

            DynamicPathKeyframe pathKeyframe = new DynamicPathKeyframe(composition, keyframe.startValue,
                    keyframe.endValue, keyframe.interpolator, keyframe.startFrame, keyframe.endFrame);

            pathKeyframe.magicPointF = magicPointF;
            pathKeyframe.composition = composition;


            if (tiJson != null && toJson != null) {
                cp1 = JsonUtils.pointFromJsonArray(toJson, composition.getScale());
                cp2 = JsonUtils.pointFromJsonArray(tiJson, composition.getScale());
            }
            if (keyframe.endValue != null && !keyframe.startValue.equals(keyframe.endValue) && cp1 != null && cp2 != null) {
                magicPointF.init(keyframe.startValue, composition);
                pathKeyframe.magicPointF2 = magicPointF.copy(keyframe.endValue);
                pathKeyframe.magicPointF3 = magicPointF.copy(cp1);
                pathKeyframe.magicPointF4 = magicPointF.copy(cp2);
            }
            return pathKeyframe;
        }
    }

    @Override
    public void setProgress(@FloatRange(from = 0f, to = 1f) float progress) {
        if (magicPointF != null) magicPointF.setProgress(progress);
        if (magicPointF2 != null) magicPointF2.setProgress(progress);
        if (magicPointF3 != null) magicPointF3.setProgress(progress);
        if (magicPointF4 != null) magicPointF4.setProgress(progress);
    }

    /**
     * This will be null if the startValue and endValue are the same.
     */
    @Nullable
    public Path getPath() {
        if (magicPointF != null && magicPointF2 != null && magicPointF3 != null && magicPointF4 != null) {
            //found bug, magicPointF.point != startValue at sometime
            magicPointF.init(startValue, composition);
            return path = Utils.createPath(magicPointF, magicPointF2, magicPointF3, magicPointF4);
        }
        return null;
    }
}
