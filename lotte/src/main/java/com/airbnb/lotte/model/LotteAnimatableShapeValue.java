package com.airbnb.lotte.model;

import android.graphics.Path;
import android.graphics.PointF;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import static com.airbnb.lotte.utils.MiscUtils.addPoints;

@SuppressWarnings({"EmptyCatchBlock", "unused", "FieldCanBeLocal", "WeakerAccess"})
public class LotteAnimatableShapeValue implements LotteAnimatableValue {
    private static final String TAG = LotteAnimatableShapeValue.class.getSimpleName();

    private Path initialShape;
    private List<Integer> shapeKeyframes;
    private List<Integer> keyTimes;
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

    private void buildAnimationForKeyFrames(JSONArray value, boolean closed) {
        // TODO
    }

    private Path bezierShapeFromValue(Object value, boolean closed) {
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

        Path shape = new Path();

        PointF vertex = vertexAtIndex(0, pointsArray);
        shape.moveTo(vertex.x, vertex.y);

        for (int i = 1; i < pointsArray.length(); i++) {
            vertex = vertexAtIndex(i, pointsArray);
            PointF previousVertex = vertexAtIndex(i - 1, pointsArray);
            PointF cp1 = vertexAtIndex(i - 1, outTangents);
            PointF cp2 = vertexAtIndex(i, inTangents);

            PointF shapeCp1 = addPoints(previousVertex, cp1);
            PointF shapeCp2 = addPoints(vertex, cp2);
            shape.cubicTo(shapeCp1.x, shapeCp1.y, shapeCp2.x, shapeCp2.y, vertex.x, vertex.y);
        }

        if (closed) {
            vertex = vertexAtIndex(0, pointsArray);
            PointF previousVertex = vertexAtIndex(pointsArray.length() - 1, pointsArray);
            PointF cp1 = vertexAtIndex(pointsArray.length() - 1, outTangents);
            PointF cp2 = vertexAtIndex(0, inTangents);

            PointF shapeCp1 = addPoints(previousVertex, cp1);
            PointF shapeCp2 = addPoints(vertex, cp2);
            shape.cubicTo(shapeCp1.x, shapeCp1.y, shapeCp2.x, shapeCp2.y, vertex.x, vertex.y);
        }

        return shape;
    }

    private PointF vertexAtIndex(int idx, JSONArray points) {
        if (idx >= points.length()) {
            throw new IllegalArgumentException("Invalid index " + idx + ". There are only " + points.length() + " points.");
        }

        try {
            JSONArray pointArray = points.getJSONArray(idx);
            return new PointF((Float) pointArray.get(0), (Float) pointArray.get(1));
        } catch (JSONException e) {
            throw new IllegalArgumentException("Unable to get point.", e);
        }
    }

    public Path getInitialShape() {
        return initialShape;
    }

    @Override
    public Object animationForKeyPath(String keyPath) {
        return null;
    }

    @Override
    public boolean hasAnimation() {
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LotteAnimatableShapeValue{");
        sb.append("initialShape=").append(initialShape);
        sb.append('}');
        return sb.toString();
    }
}
