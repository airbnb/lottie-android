package com.airbnb.lotte.utils;

import android.graphics.Path;
import android.support.annotation.FloatRange;

import com.airbnb.lotte.model.LotteShapeData;

import java.util.List;

public class LotteShapeKeyframeAnimation extends LotteKeyframeAnimation<Path> {
    private final Path tempPath = new Path();
    private final LotteShapeData tempShapeData = new LotteShapeData();
    private final List<LotteShapeData> shapeData;

    public LotteShapeKeyframeAnimation(String objectProperty, long duration, List<Float> keyTimes, List<LotteShapeData> shapeData) {
        super(objectProperty, duration, keyTimes);
        this.shapeData = shapeData;
    }

    @Override
    public Path getValueForProgress(@FloatRange(from = 0f, to = 1f) float progress) {
        if (progress <= startDelay) {
            MiscUtils.getPathFromData(shapeData.get(0), tempPath);
            return tempPath;
        } else if (progress >= startDelay + duration) {
            MiscUtils.getPathFromData(shapeData.get(shapeData.size() - 1), tempPath);
            return tempPath;
        }

        int keyframeIndex = getKeyframeIndex();

        float startKeytime = keyTimes.get(keyframeIndex);
        float endKeytime = keyTimes.get(keyframeIndex + 1);

        float percentageIntoFrame = (progress - startKeytime) / (endKeytime - startKeytime);
        if (interpolators != null) {
            percentageIntoFrame = interpolators.get(keyframeIndex - 1).getInterpolation(percentageIntoFrame);
        }

        LotteShapeData startShapeData = shapeData.get(keyframeIndex - 1);
        LotteShapeData endShapeData = shapeData.get(keyframeIndex);

        tempShapeData.interpolateBetween(startShapeData, endShapeData, percentageIntoFrame);
        MiscUtils.getPathFromData(tempShapeData, tempPath);
        return tempPath;
    }
}
