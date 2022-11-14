package com.airbnb.lottie;

/**
 * Register this listener via {@link LottieCompositionFactory#registerLottieTaskIdleListener(LottieTaskIdleListener)}.
 *
 * Can be used to create an espresso idle resource. Refer to {@link LottieCompositionFactory#registerLottieTaskIdleListener(LottieTaskIdleListener)}
 * for more information.
 */
public interface LottieTaskIdleListener {
  void onIdleChanged(boolean idle);
}
