package com.airbnb.lottie.animation.content;

import android.support.annotation.Nullable;

import com.airbnb.lottie.animation.keyframe.TransformKeyframeAnimation;

/**
 * Content that contains a transform.
 */
public interface TransformableContent {
  @Nullable TransformKeyframeAnimation getTransform();
}
