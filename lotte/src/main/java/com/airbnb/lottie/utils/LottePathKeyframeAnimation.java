package com.airbnb.lottie.utils;

import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.view.animation.Interpolator;

import java.util.List;


public class LottePathKeyframeAnimation extends LotteKeyframeAnimation<PointF> {
    private final PointF point = new PointF();
    private final float[] pos = new float[2];
    private final SegmentedPath segmentedPath;
    private int pathMeasureKeyframeIndex = -1;
    @Nullable private PathMeasure pathMeasure;

    public LottePathKeyframeAnimation(long duration, long compDuration, List<Float> keyTimes, SegmentedPath segmentedPath, List<Interpolator> interpolators) {
        super(duration, compDuration, keyTimes, interpolators);
        this.segmentedPath = segmentedPath;
    }

    @Override
    public PointF getValue() {
        if (progress <= 0f) {
            if (pathMeasureKeyframeIndex != 0 || pathMeasure == null) {
                pathMeasureKeyframeIndex = 0;
                pathMeasure = new PathMeasure(segmentedPath.getSegment(0), false);
            }
            pathMeasure.getPosTan(0, pos, null);
            point.set(pos[0], pos[1]);
            return point;
        } else if (progress >= 1f) {
            if (pathMeasureKeyframeIndex != segmentedPath.getSegmentCount() - 1 || pathMeasure == null) {
                pathMeasureKeyframeIndex = segmentedPath.getSegmentCount() - 1;
                pathMeasure = new PathMeasure(segmentedPath.getSegment(segmentedPath.getSegmentCount() - 1), false);
            }
            pathMeasure.getPosTan(pathMeasure.getLength(), pos, null);
            point.set(pos[0], pos[1]);
            return point;
        }

        int keyframeIndex = getKeyframeIndex();
        if (pathMeasureKeyframeIndex != keyframeIndex) {
            pathMeasureKeyframeIndex = keyframeIndex;
            pathMeasure = new PathMeasure(segmentedPath.getSegment(keyframeIndex), false);
        }

        float startKeytime = keyTimes.get(keyframeIndex);
        float endKeytime = keyTimes.get(keyframeIndex + 1);

        float percentageIntoFrame = 0;
        if (!isDiscrete) {
            percentageIntoFrame = (progress - startKeytime) / (endKeytime - startKeytime);
            if (interpolators != null) {
                percentageIntoFrame = interpolators.get(keyframeIndex).getInterpolation(percentageIntoFrame);
            }
        }

        pathMeasure.getPosTan(percentageIntoFrame * pathMeasure.getLength(), pos, null);
        point.set(pos[0], pos[1]);
        return point;
    }
}
