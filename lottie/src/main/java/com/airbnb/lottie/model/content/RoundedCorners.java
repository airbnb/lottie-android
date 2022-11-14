package com.airbnb.lottie.model.content;

import androidx.annotation.Nullable;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.content.Content;
import com.airbnb.lottie.animation.content.RoundedCornersContent;
import com.airbnb.lottie.model.animatable.AnimatableValue;
import com.airbnb.lottie.model.layer.BaseLayer;

public class RoundedCorners implements ContentModel {
  private final String name;
  private final AnimatableValue<Float, Float> cornerRadius;

  public RoundedCorners(String name, AnimatableValue<Float, Float> cornerRadius) {
    this.name = name;
    this.cornerRadius = cornerRadius;
  }

  public String getName() {
    return name;
  }

  public AnimatableValue<Float, Float> getCornerRadius() {
    return cornerRadius;
  }

  @Nullable @Override public Content toContent(LottieDrawable drawable, LottieComposition composition, BaseLayer layer) {
    return new RoundedCornersContent(drawable, layer, this);
  }
}
