package com.airbnb.lottie.animation;

import android.graphics.Path;
import android.view.animation.Interpolator;

import com.airbnb.lottie.model.LottieComposition;
import com.airbnb.lottie.model.ShapeData;
import com.airbnb.lottie.utils.MiscUtils;

import java.util.List;

public class ShapeKeyframeAnimation extends KeyframeAnimation<Path> {
    private final Path tempPath = new Path();
    private final ShapeData tempShapeData = new ShapeData();
    private final List<ShapeData> shapeData;

    public ShapeKeyframeAnimation(long duration, LottieComposition composition, List<Float> keyTimes, List<ShapeData> shapeData, List<Interpolator> interpolators) {
        super(duration, composition, keyTimes, interpolators);
        this.shapeData = shapeData;
    }

    @Override
    public Path getValue() {
        if (progress <= 0f) {
            MiscUtils.getPathFromData(shapeData.get(0), tempPath);
            return tempPath;
        } else if (progress >= 1f) {
            MiscUtils.getPathFromData(shapeData.get(shapeData.size() - 1), tempPath);
            return tempPath;
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

        ShapeData startShapeData = shapeData.get(keyframeIndex);
        ShapeData endShapeData = shapeData.get(keyframeIndex + 1);

        tempShapeData.interpolateBetween(startShapeData, endShapeData, percentageIntoFrame);
        MiscUtils.getPathFromData(tempShapeData, tempPath);
        return tempPath;
    }
}
