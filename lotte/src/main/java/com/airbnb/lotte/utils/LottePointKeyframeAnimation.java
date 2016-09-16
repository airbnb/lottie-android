package com.airbnb.lotte.utils;

import android.graphics.PointF;
import android.support.annotation.FloatRange;

import java.util.List;

public class LottePointKeyframeAnimation extends LotteKeyframeAnimation<PointF> {
    private final PointF point = new PointF();
    private final List<PointF> points;

    public LottePointKeyframeAnimation(String objectProperty, long duration, List<Float> keyTimes, List<PointF> points) {
        super(objectProperty, duration, keyTimes);
        this.points = points;
    }

    @Override
    public PointF getValueForProgress(@FloatRange(from = 0f, to = 1f) float progress) {
        if (progress < startDelay) {
            return points.get(0);
        } else if (progress > startDelay + duration) {
            return points.get(points.size() - 1);
        }

        int keyframeIndex = getKeyframeIndex();

        float startKeytime = keyTimes.get(keyframeIndex);
        float endKeytime = keyTimes.get(keyframeIndex + 1);

        float percentageIntoFrame = (progress - startKeytime) / (endKeytime - startKeytime);
        if (interpolators != null) {
            percentageIntoFrame = interpolators.get(keyframeIndex - 1).getInterpolation(percentageIntoFrame);
        }

        PointF startPoint = points.get(keyframeIndex - 1);
        PointF endPoint = points.get(keyframeIndex);

        point.set(startPoint.x + percentageIntoFrame * (endPoint.x - startPoint.x),
                startPoint.y + percentageIntoFrame * (endPoint.y - startPoint.y));
        return point;
    }
}
