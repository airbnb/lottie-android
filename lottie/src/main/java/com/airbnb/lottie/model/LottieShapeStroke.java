package com.airbnb.lottie.model;

import com.airbnb.lottie.animation.LottieAnimatableColorValue;
import com.airbnb.lottie.animation.LottieAnimatableFloatValue;
import com.airbnb.lottie.animation.LottieAnimatableIntegerValue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LottieShapeStroke {

    public enum LineCapType {
        Butt,
        Round,
        Unknown
    }

    public enum LineJoinType {
        Miter,
        Round,
        Bevel
    }

    private LottieAnimatableFloatValue offset;
    private final List<LottieAnimatableFloatValue> lineDashPattern = new ArrayList<>();

    private LottieAnimatableColorValue color;
    private LottieAnimatableIntegerValue opacity;
    private LottieAnimatableFloatValue width;
    private LineCapType capType;
    private LineJoinType joinType;

    LottieShapeStroke(JSONObject json, int frameRate, long compDuration) {
        try {
            JSONObject colorJson = json.getJSONObject("c");
            color = new LottieAnimatableColorValue(colorJson, frameRate, compDuration);

            JSONObject widthJson = json.getJSONObject("w");
            width = new LottieAnimatableFloatValue(widthJson, frameRate, compDuration);

            JSONObject opacityJson = json.getJSONObject("o");
            opacity = new LottieAnimatableIntegerValue(opacityJson, frameRate, compDuration);
            opacity.remapValues(0, 100, 0, 255);

            capType = LineCapType.values()[json.getInt("lc") - 1];
            joinType = LineJoinType.values()[json.getInt("lj") - 1];

            if (json.has("d")) {
                JSONArray dashesJson = json.getJSONArray("d");
                for (int i = 0; i < dashesJson.length(); i++) {
                    JSONObject dashJson = dashesJson.getJSONObject(i);
                    String n = dashJson.getString("n");
                    if (n.equals("o")) {
                        JSONObject value = dashJson.getJSONObject("v");
                        offset = new LottieAnimatableFloatValue(value, frameRate, compDuration);
                    } else if (n.equals("d") || n.equals("g")) {
                        JSONObject value = dashJson.getJSONObject("v");
                        lineDashPattern.add(new LottieAnimatableFloatValue(value, frameRate, compDuration));
                    }
                }
                if (lineDashPattern.size() == 1) {
                    // If there is only 1 value then it is assumed to be equal parts on and off.
                    lineDashPattern.add(lineDashPattern.get(0));
                }
            }
        } catch (JSONException e) {
            throw new IllegalArgumentException("Unable to parse stroke " + json, e);
        }
    }

    public LottieAnimatableColorValue getColor() {
        return color;
    }

    public LottieAnimatableIntegerValue getOpacity() {
        return opacity;
    }

    public LottieAnimatableFloatValue getWidth() {
        return width;
    }

    public List<LottieAnimatableFloatValue> getLineDashPattern() {
        return lineDashPattern;
    }

    public LottieAnimatableFloatValue getDashOffset() {
        return offset;
    }

    public LineCapType getCapType() {
        return capType;
    }

    public LineJoinType getJoinType() {
        return joinType;
    }
}
