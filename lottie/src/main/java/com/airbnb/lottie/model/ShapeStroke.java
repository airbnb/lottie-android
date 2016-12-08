package com.airbnb.lottie.model;

import android.support.annotation.RestrictTo;

import com.airbnb.lottie.animatable.AnimatableColorValue;
import com.airbnb.lottie.animatable.AnimatableFloatValue;
import com.airbnb.lottie.animatable.AnimatableIntegerValue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@RestrictTo(RestrictTo.Scope.GROUP_ID)
public class ShapeStroke {

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

    private AnimatableFloatValue offset;
    private final List<AnimatableFloatValue> lineDashPattern = new ArrayList<>();

    private AnimatableColorValue color;
    private AnimatableIntegerValue opacity;
    private AnimatableFloatValue width;
    private LineCapType capType;
    private LineJoinType joinType;

    ShapeStroke(JSONObject json, int frameRate, LottieComposition composition) {
        try {
            JSONObject colorJson = json.getJSONObject("c");
            color = new AnimatableColorValue(colorJson, frameRate, composition);

            JSONObject widthJson = json.getJSONObject("w");
            width = new AnimatableFloatValue(widthJson, frameRate, composition);

            JSONObject opacityJson = json.getJSONObject("o");
            opacity = new AnimatableIntegerValue(opacityJson, frameRate, composition, false, true);

            capType = LineCapType.values()[json.getInt("lc") - 1];
            joinType = LineJoinType.values()[json.getInt("lj") - 1];

            if (json.has("d")) {
                JSONArray dashesJson = json.getJSONArray("d");
                for (int i = 0; i < dashesJson.length(); i++) {
                    JSONObject dashJson = dashesJson.getJSONObject(i);
                    String n = dashJson.getString("n");
                    if (n.equals("o")) {
                        JSONObject value = dashJson.getJSONObject("v");
                        offset = new AnimatableFloatValue(value, frameRate, composition);
                    } else if (n.equals("d") || n.equals("g")) {
                        JSONObject value = dashJson.getJSONObject("v");
                        lineDashPattern.add(new AnimatableFloatValue(value, frameRate, composition));
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

    public AnimatableColorValue getColor() {
        return color;
    }

    public AnimatableIntegerValue getOpacity() {
        return opacity;
    }

    public AnimatableFloatValue getWidth() {
        return width;
    }

    public List<AnimatableFloatValue> getLineDashPattern() {
        return lineDashPattern;
    }

    public AnimatableFloatValue getDashOffset() {
        return offset;
    }

    public LineCapType getCapType() {
        return capType;
    }

    public LineJoinType getJoinType() {
        return joinType;
    }
}
