package com.airbnb.lotte.utils;

import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.support.annotation.FloatRange;

import java.util.List;


public class LottePathKeyframeAnimation extends LotteKeyframeAnimation<PointF> {
    private final PointF point = new PointF();
    private final float[] pos = new float[2];
    private final PathMeasure pathMeasure;

    public LottePathKeyframeAnimation(String objectProperty, long duration, List<Float> keyTimes, Path path) {
        super(objectProperty, duration, keyTimes);
        pathMeasure = new PathMeasure(path, false);
    }

    @Override
    public PointF getValueForProgress(@FloatRange(from = 0f, to = 1f) float progress) {
        if (progress < startDelay) {
            pathMeasure.getPosTan(0, pos, null);
            point.set(pos[0], pos[1]);
            return point;
        } else if (progress > startDelay + duration) {
            pathMeasure.getPosTan(pathMeasure.getLength(), pos, null);
            point.set(pos[0], pos[1]);
            return point;
        }

        int keyframeIndex = getKeyframeIndex();

        float startKeytime = keyTimes.get(keyframeIndex );
        float endKeytime = keyTimes.get(keyframeIndex);

        float percentageIntoFrame = (progress - startKeytime) / (endKeytime - startKeytime);
        if (interpolators != null) {
            percentageIntoFrame = interpolators.get(keyframeIndex - 1).getInterpolation(percentageIntoFrame);
        }

        pathMeasure.getPosTan(startKeytime + percentageIntoFrame * (endKeytime - startKeytime), pos, null);
        point.set(pos[0], pos[1]);
        return point;
    }
}
