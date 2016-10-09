package com.airbnb.lotte.model;

import android.util.Log;

import com.airbnb.lotte.L;
import com.airbnb.lotte.animation.LotteAnimatableNumberValue;
import com.airbnb.lotte.animation.LotteAnimatablePathValue;
import com.airbnb.lotte.animation.LotteAnimatablePointValue;

import org.json.JSONException;
import org.json.JSONObject;

public class LotteShapeRectangle {
    private static final String TAG = LotteShapeRectangle.class.getSimpleName();

    private LotteAnimatablePathValue position;
    private LotteAnimatablePointValue size;
    private LotteAnimatableNumberValue cornerRadius;

    public LotteShapeRectangle(JSONObject json, int frameRate, long compDuration) {
        try {
            JSONObject positionJson = json.getJSONObject("p");
            position = new LotteAnimatablePathValue(positionJson, frameRate, compDuration);
        } catch (JSONException e) {
            throw new IllegalArgumentException("Unable to parse rectangle position.", e);
        }

        try {
            JSONObject cornerRadiusJson = json.getJSONObject("r");
            cornerRadius = new LotteAnimatableNumberValue(cornerRadiusJson, frameRate, compDuration);
        } catch (JSONException e) {
            throw new IllegalArgumentException("Unable to parse rectangle corner radius.", e);
        }

        try {
            JSONObject sizeJson = json.getJSONObject("s");
            size = new LotteAnimatablePointValue(sizeJson, frameRate, compDuration);
        } catch (JSONException e) {
            throw new IllegalArgumentException("Unable to parse rectangle size.", e);
        }

        if (L.DBG) Log.d(TAG, "Parsed new rectangle " + toString());
    }

    public LotteAnimatableNumberValue getCornerRadius() {
        return cornerRadius;
    }

    public LotteAnimatablePointValue getSize() {
        return size;
    }

    public LotteAnimatablePathValue getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return "LotteShapeRectangle{" + "cornerRadius=" + cornerRadius.getInitialValue() +
                ", position=" + position +
                ", size=" + size +
                '}';
    }
}
