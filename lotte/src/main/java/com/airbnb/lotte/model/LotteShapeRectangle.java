package com.airbnb.lotte.model;

import org.json.JSONException;
import org.json.JSONObject;

public class LotteShapeRectangle {

    private LotteAnimatablePointValue position;
    private LotteAnimatablePointValue size;
    private LotteAnimatableNumberValue cornerRadius;

    public LotteShapeRectangle(JSONObject json, int frameRate) {
        try {
            JSONObject positionJson = json.getJSONObject("p");
            position = new LotteAnimatablePointValue(positionJson, frameRate);
            position.setUsePathAnimation(false);
        } catch (JSONException e) {
            throw new IllegalArgumentException("Unable to parse rectangle position.", e);
        }

        try {
            JSONObject cornerRadiusJson = json.getJSONObject("r");
            cornerRadius = new LotteAnimatableNumberValue(cornerRadiusJson, frameRate);
        } catch (JSONException e) {
            throw new IllegalArgumentException("Unable to parse rectangle corner radius.", e);
        }

        try {
            JSONObject sizeJson = json.getJSONObject("s");
            size = new LotteAnimatablePointValue(sizeJson, frameRate);
        } catch (JSONException e) {
            throw new IllegalArgumentException("Unable to parse rectangle size.", e);
        }
    }

    public LotteAnimatableNumberValue getCornerRadius() {
        return cornerRadius;
    }

    public LotteAnimatablePointValue getSize() {
        return size;
    }

    public LotteAnimatablePointValue getPosition() {
        return position;
    }
}
