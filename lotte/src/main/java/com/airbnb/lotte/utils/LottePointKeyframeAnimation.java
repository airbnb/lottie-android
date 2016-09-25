package com.airbnb.lotte.utils;

import android.graphics.PointF;
import android.support.annotation.FloatRange;

import com.airbnb.lotte.animation.LotteAnimatableProperty.AnimatableProperty;

import java.util.List;

public class LottePointKeyframeAnimation extends LotteKeyframeAnimation<PointF> {
    private final PointF point = new PointF();
    private final List<PointF> points;

    public LottePointKeyframeAnimation(@AnimatableProperty int property, long duration, List<Float> keyTimes, List<PointF> points) {
        super(property, duration, keyTimes);
        this.points = points;
    }

    @Override
    public PointF getValueForProgress(@FloatRange(from = 0f, to = 1f) float progress) {
        if (progress <= 0f) {
            return points.get(0);
        } else if (progress > 1f) {
            return points.get(points.size() - 1);
        }

        int keyframeIndex = getKeyframeIndex();

        float startKeytime = keyTimes.get(keyframeIndex);
        float endKeytime = keyTimes.get(keyframeIndex + 1);

        float percentageIntoFrame = 0;
        if (!isDiscrete) {
            percentageIntoFrame = (progress - startKeytime) / (endKeytime - startKeytime);
            if (interpolators != null) {
                percentageIntoFrame = interpolators.get(keyframeIndex).getInterpolation(percentageIntoFrame);
            }
        }

        PointF startPoint = points.get(keyframeIndex);
        PointF endPoint = points.get(keyframeIndex + 1);

        point.set(startPoint.x + percentageIntoFrame * (endPoint.x - startPoint.x),
                startPoint.y + percentageIntoFrame * (endPoint.y - startPoint.y));
        return point;
    }
}
