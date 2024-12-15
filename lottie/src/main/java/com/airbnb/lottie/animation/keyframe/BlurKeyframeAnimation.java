package com.airbnb.lottie.animation.keyframe;

import android.graphics.Color;
import android.graphics.Matrix;
import androidx.annotation.Nullable;
import com.airbnb.lottie.model.content.BlurEffect;
import com.airbnb.lottie.model.layer.BaseLayer;
import com.airbnb.lottie.value.LottieValueCallback;


public class BlurKeyframeAnimation implements BaseKeyframeAnimation.AnimationListener {
  private final BaseLayer layer;
  private final BaseKeyframeAnimation.AnimationListener listener;
  private final FloatKeyframeAnimation blurriness;

  public BlurKeyframeAnimation(BaseKeyframeAnimation.AnimationListener listener, BaseLayer layer, BlurEffect blurEffect) {
    this.listener = listener;
    this.layer = layer;
    blurriness = blurEffect.getBlurriness().createAnimation();
    blurriness.addUpdateListener(this);
    layer.addAnimation(blurriness);
  }

  @Override public void onValueChanged() {
    listener.onValueChanged();
  }

  /**
   * Evaluates the blur at this layer, returning a BlurMaskFilter-compatible blur
   * parameter (by convention). Other implementations adjust based on this.
   */
  public float evaluate(Matrix parentMatrix) {
    float rawBlurriness = blurriness.getValue();

    float parentScale = parentMatrix.mapRadius(1.0f);
    float layerScale = layer.transform.getMatrix().mapRadius(1.0f);

    // Similarly to shadows, the blur radius is provided relative to the layer post-,
    // not pre-transform. For this reason, we should undo the layer's transformation
    // here --- if the layer is scaled, the screen-space blur should stay constant.
    float factor = parentScale * (1 / layerScale);

    // Apply scaling factor to go from the to standard deviation to blur radius.
    return rawBlurriness * 0.5f * factor;
  }

  public void setBlurrinessCallback(@Nullable LottieValueCallback<Float> callback) {
    blurriness.setValueCallback(callback);
  }
}
