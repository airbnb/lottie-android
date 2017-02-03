package com.airbnb.lottie;

import android.graphics.Rect;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

class ShapeTransform implements Transform {
  private static final String TAG = ShapeTransform.class.getSimpleName();

  private Rect compBounds;
  private AnimatablePathValue position;
  private AnimatablePathValue anchor;
  private AnimatableScaleValue scale;
  private AnimatableFloatValue rotation;
  private AnimatableIntegerValue opacity;

  ShapeTransform(LottieComposition composition) {
    this.compBounds = composition.getBounds();
    this.position = new AnimatablePathValue(composition);
    this.anchor = new AnimatablePathValue(composition);
    this.scale = new AnimatableScaleValue(composition);
    this.rotation = new AnimatableFloatValue(composition, 0f);
    this.opacity = new AnimatableIntegerValue(composition, 255);
  }

  ShapeTransform(JSONObject json, int frameRate, LottieComposition composition) {
    this.compBounds = composition.getBounds();

    JSONObject jsonPosition;
    try {
      jsonPosition = json.getJSONObject("p");
    } catch (JSONException e) {
      throw new IllegalStateException("Transform has no position.");
    }
    position = new AnimatablePathValue(jsonPosition, frameRate, composition);

    JSONObject jsonAnchor;
    try {
      jsonAnchor = json.getJSONObject("a");
    } catch (JSONException e) {
      throw new IllegalStateException("Transform has no anchor.");
    }
    anchor = new AnimatablePathValue(jsonAnchor, frameRate, composition);

    JSONObject jsonScale;
    try {
      jsonScale = json.getJSONObject("s");
    } catch (JSONException e) {
      throw new IllegalStateException("Transform has no scale.");
    }
    scale = new AnimatableScaleValue(jsonScale, frameRate, composition, false);

    JSONObject jsonRotation;
    try {
      jsonRotation = json.getJSONObject("r");
    } catch (JSONException e) {
      throw new IllegalStateException("Transform has no rotation.");
    }
    rotation = new AnimatableFloatValue(jsonRotation, frameRate, composition, false);

    JSONObject jsonOpacity;
    try {
      jsonOpacity = json.getJSONObject("o");
    } catch (JSONException e) {
      throw new IllegalStateException("Transform has no opacity.");
    }
    opacity = new AnimatableIntegerValue(jsonOpacity, frameRate, composition, false, true);

    if (L.DBG) Log.d(TAG, "Parsed new shape transform " + toString());
  }

  @Override public Rect getBounds() {
    return compBounds;
  }

  @Override public AnimatablePathValue getPosition() {
    return position;
  }

  @Override public AnimatablePathValue getAnchor() {
    return anchor;
  }

  @Override public AnimatableScaleValue getScale() {
    return scale;
  }

  @Override public AnimatableFloatValue getRotation() {
    return rotation;
  }

  @Override public AnimatableIntegerValue getOpacity() {
    return opacity;
  }

  @Override public String toString() {
    return "ShapeTransform{" + "anchor=" + anchor.toString() +
        ", compBounds=" + compBounds +
        ", position=" + position.toString() +
        ", scale=" + scale.toString() +
        ", rotation=" + rotation.getInitialValue() +
        ", opacity=" + opacity.getInitialValue() +
        '}';
  }
}
