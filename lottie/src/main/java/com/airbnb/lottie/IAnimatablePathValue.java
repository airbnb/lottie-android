package com.airbnb.lottie;

public interface IAnimatablePathValue extends AnimatableValue<CPointF, CPointF> {
  CPointF getInitialPoint();
}
