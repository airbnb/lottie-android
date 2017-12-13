package com.airbnb.lottie.model;

import android.support.annotation.Nullable;

import com.airbnb.lottie.LottieProperty;
import com.airbnb.lottie.LottieValueCallback;

import java.util.List;

/**
 * Any item that can be a part of a {@link KeyPath} should implement this.
 */
public interface KeyPathElement {

  /**
   * Called recursively during keypath resolution.
   *
   * @param keyPath The full keypath being resolved.
   * @param depth The current depth that this element should be checked at in the keypath.
   * @param accumulator A list of fully resolved keypaths. If this element fully matches the
   *                    keypath then it should add itself to this list.
   * @param currentPartialKeyPath A keypath that contains all parent element of this one.
   *                              This element should create a copy of this and append itself
   *                              with KeyPath#addKey when it adds itself to the accumulator
   *                              or propagates resolution to its children.
   */
  void resolveKeyPath(
      KeyPath keyPath, int depth, List<KeyPath> accumulator, KeyPath currentPartialKeyPath);

  <T> void applyValueCallback(
      @LottieProperty int property, @Nullable LottieValueCallback<T> callback);
}