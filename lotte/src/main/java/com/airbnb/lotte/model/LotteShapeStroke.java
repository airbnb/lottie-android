package com.airbnb.lotte.model;

import com.airbnb.lotte.animation.LotteAnimatableColorValue;
import com.airbnb.lotte.animation.LotteAnimatableNumberValue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LotteShapeStroke {

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

    private LotteAnimatableNumberValue offset;
    private final List<LotteAnimatableNumberValue> lineDashPattern = new ArrayList<>();

    private LotteAnimatableColorValue color;
    private LotteAnimatableNumberValue opacity;
    private LotteAnimatableNumberValue width;
    private LineCapType capType;
    private LineJoinType joinType;

    public LotteShapeStroke(JSONObject json, int frameRate, long compDuration) {
        try {
            JSONObject colorJson = json.getJSONObject("c");
            color = new LotteAnimatableColorValue(colorJson, frameRate, compDuration);

            JSONObject widthJson = json.getJSONObject("w");
            width = new LotteAnimatableNumberValue(widthJson, frameRate, compDuration);

            JSONObject opacityJson = json.getJSONObject("o");
            opacity = new LotteAnimatableNumberValue(opacityJson, frameRate, compDuration);
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
                        offset = new LotteAnimatableNumberValue(value, frameRate, compDuration);
                    } else if (n.equals("d") || n.equals("g")) {
                        JSONObject value = dashJson.getJSONObject("v");
                        LotteAnimatableNumberValue initialValue = new LotteAnimatableNumberValue(value, frameRate, compDuration);
                        lineDashPattern.add(initialValue);
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

    public LotteAnimatableColorValue getColor() {
        return color;
    }

    public LotteAnimatableNumberValue getOpacity() {
        return opacity;
    }

    public LotteAnimatableNumberValue getWidth() {
        return width;
    }

    public List<LotteAnimatableNumberValue> getLineDashPattern() {
        return lineDashPattern;
    }

    public LotteAnimatableNumberValue getDashOffset() {
        return offset;
    }

    public LineCapType getCapType() {
        return capType;
    }

    public LineJoinType getJoinType() {
        return joinType;
    }
}
