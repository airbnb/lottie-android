package com.airbnb.lottie.animation;

import android.graphics.PointF;
import android.view.animation.Interpolator;

import java.util.List;

public class PointKeyframeAnimation extends KeyframeAnimation<PointF> {
    private final PointF point = new PointF();
    private final List<PointF> points;

    public PointKeyframeAnimation(long duration, long compDuration, List<Float> keyTimes, List<PointF> points, List<Interpolator> interpolators) {
        super(duration, compDuration, keyTimes, interpolators);
        this.points = points;
    }

    @Override
    public PointF getValue() {
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
