package com.airbnb.lotte.animation;

import android.graphics.Path;
import android.graphics.PointF;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.airbnb.lotte.model.LotteCubicCurveData;
import com.airbnb.lotte.model.LotteShapeData;
import com.airbnb.lotte.utils.JsonUtils;
import com.airbnb.lotte.utils.LotteKeyframeAnimation;
import com.airbnb.lotte.utils.LotteShapeKeyframeAnimation;
import com.airbnb.lotte.utils.MiscUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.airbnb.lotte.utils.MiscUtils.addPoints;

@SuppressWarnings({"EmptyCatchBlock", "unused", "FieldCanBeLocal", "WeakerAccess"})
public class LotteAnimatableShapeValue implements LotteAnimatableValue {
    private static final String TAG = LotteAnimatableShapeValue.class.getSimpleName();

    private final Path path = new Path();

    private LotteShapeData initialShape;
    private final List<LotteShapeData> shapeKeyframes = new ArrayList<>();
    private final List<Float> keyTimes = new ArrayList<>();
    private List<Interpolator> interpolators;
    private long delay;
    private long duration;
    private int startFrame;
    private long durationFrames;
    private int frameRate;

    public LotteAnimatableShapeValue(JSONObject shapeValues, int frameRate, boolean closed) {
        this.frameRate = frameRate;
        try {
            Object value = shapeValues.get("k");
            if (value instanceof JSONObject) {
                // Single value, no animation
                initialShape = bezierShapeFromValue(value, closed);
            } else if (value instanceof JSONArray) {
                Object firstObject = ((JSONArray) value).get(0);
                if (firstObject instanceof JSONObject && ((JSONObject) firstObject).has("t")) {
                    // Keyframes
                    buildAnimationForKeyFrames((JSONArray) value, closed);
                }
            }
        } catch (JSONException e) {
            throw new IllegalArgumentException("Unable to parse keyframes or initial value.");
        }
    }

    private void buildAnimationForKeyFrames(JSONArray keyframes, boolean closed) {
        try {
            startFrame = keyframes.getJSONObject(0).getInt("t");
            int endFrame = keyframes.getJSONObject(keyframes.length() - 1).getInt("t");

            if (endFrame <= startFrame) {
                throw new IllegalArgumentException("End frame must be after start frame " + endFrame + " vs " + startFrame);
            }

            durationFrames = endFrame - startFrame;

            duration = durationFrames / frameRate;
            delay = startFrame / frameRate;

            boolean addStartValue = true;
            boolean addTimePadding = false;
            LotteShapeData outShape = null;

            for (int i = 0; i < keyframes.length(); i++) {
                JSONObject keyframe = keyframes.getJSONObject(i);
                int frame = keyframe.getInt("t");
                float timePercentage = (frame - startFrame) / (float) durationFrames;

                if (outShape != null) {
                    shapeKeyframes.add(outShape);
                    interpolators.add(new LinearInterpolator());
                    outShape = null;
                }

                LotteShapeData startShape = keyframe.has("s") ? bezierShapeFromValue(keyframe.getJSONObject("s"), closed) : null;
                if (addStartValue) {
                    if (keyframe.has("s")) {
                        if (i == 0) {
                            initialShape = startShape;
                        }

                        shapeKeyframes.add(startShape);
                        if (!interpolators.isEmpty()) {
                            interpolators.add(new LinearInterpolator());
                        }
                    }
                    addStartValue = false;
                }

                if (addTimePadding) {
                    float holdPercentage = timePercentage - 0.00001f;
                    keyTimes.add(holdPercentage);
                    addTimePadding = false;
                }

                if (keyframe.has("e")) {
                    JSONObject endShape = keyframe.getJSONObject("e");

                    Interpolator interpolator;
                    if (keyframe.has("o") && keyframe.has("i")) {
                        PointF cp1 = JsonUtils.pointValueFromDict(keyframe.getJSONObject("o"));
                        PointF cp2 = JsonUtils.pointValueFromDict(keyframe.getJSONObject("i"));
                        interpolator = PathInterpolatorCompat.create(cp1.x, cp1.y, cp2.x, cp2.y);
                    } else {
                        interpolator = new LinearInterpolator();
                    }
                    interpolators.add(interpolator);

                    keyTimes.add(timePercentage);

                    if (keyframe.has("h") && keyframe.getBoolean("h")) {
                        outShape = startShape;
                        addStartValue = true;
                        addTimePadding = true;
                    }
                }
            }
        } catch (JSONException e) {
            throw new IllegalArgumentException("Unable to parse shape animation " + keyframes, e);
        }

    }

    private LotteShapeData bezierShapeFromValue(Object value, boolean closed) {
        JSONObject pointsData = null;
        if (value instanceof JSONArray) {
            try {
                Object firstObject = ((JSONArray) value).get(0);
                if (firstObject instanceof JSONObject && ((JSONObject) firstObject).has("v")) {
                    pointsData = (JSONObject) firstObject;
                }
            } catch (JSONException e) {
                throw new IllegalStateException("Unable to get shape. " + value);
            }
        } else if (value instanceof JSONObject && ((JSONObject) value).has("v")) {
            pointsData = (JSONObject) value;
        }

        if (pointsData == null) {
            return null;
        }

        JSONArray pointsArray = null;
        JSONArray inTangents = null;
        JSONArray outTangents = null;
        try {
            pointsArray = pointsData.getJSONArray("v");
            inTangents = pointsData.getJSONArray("i");
            outTangents = pointsData.getJSONArray("o");
        } catch (JSONException e) { }

        if (pointsArray == null || inTangents == null || outTangents == null ||
                pointsArray.length() != inTangents.length() || pointsArray.length() != outTangents.length()) {
            throw new IllegalStateException("Unable to process points array or tangents. " + pointsData);
        }

        LotteShapeData shape = new LotteShapeData();

        PointF vertex = vertexAtIndex(0, pointsArray);
        shape.setInitialPoint(vertex);

        for (int i = 1; i < pointsArray.length(); i++) {
            vertex = vertexAtIndex(i, pointsArray);
            PointF previousVertex = vertexAtIndex(i - 1, pointsArray);
            PointF cp1 = vertexAtIndex(i - 1, outTangents);
            PointF cp2 = vertexAtIndex(i, inTangents);

            PointF shapeCp1 = addPoints(previousVertex, cp1);
            PointF shapeCp2 = addPoints(vertex, cp2);
            shape.addCurve(new LotteCubicCurveData(shapeCp1, shapeCp2, vertex));
        }

        if (closed) {
            vertex = vertexAtIndex(0, pointsArray);
            PointF previousVertex = vertexAtIndex(pointsArray.length() - 1, pointsArray);
            PointF cp1 = vertexAtIndex(pointsArray.length() - 1, outTangents);
            PointF cp2 = vertexAtIndex(0, inTangents);

            PointF shapeCp1 = addPoints(previousVertex, cp1);
            PointF shapeCp2 = addPoints(vertex, cp2);
            shape.addCurve(new LotteCubicCurveData(shapeCp1, shapeCp2, vertex));
        }

        return shape;
    }

    private PointF vertexAtIndex(int idx, JSONArray points) {
        if (idx >= points.length()) {
            throw new IllegalArgumentException("Invalid index " + idx + ". There are only " + points.length() + " points.");
        }

        try {
            JSONArray pointArray = points.getJSONArray(idx);
            Object x = pointArray.get(0);
            Object y = pointArray.get(1);
                return new PointF(
                        x instanceof Double ? new Float((Double) x) : (int) x,
                        y instanceof Double ? new Float((Double) y) : (int) y);
        } catch (JSONException e) {
            throw new IllegalArgumentException("Unable to get point.", e);
        }
    }

    public Path getInitialShape() {
        MiscUtils.getPathFromData(initialShape, path);
        return path;
    }

    @Override
    public LotteKeyframeAnimation animationForKeyPath(@LotteAnimatableProperty.AnimatableProperty int property) {
        LotteShapeKeyframeAnimation animation = new LotteShapeKeyframeAnimation(property, duration, keyTimes, shapeKeyframes);
        animation.setInterpolators(interpolators);

        return animation;
    }

    @Override
    public boolean hasAnimation() {
        return !shapeKeyframes.isEmpty();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LotteAnimatableShapeValue{");
        sb.append("initialShape=").append(initialShape);
        sb.append('}');
        return sb.toString();
    }
}
