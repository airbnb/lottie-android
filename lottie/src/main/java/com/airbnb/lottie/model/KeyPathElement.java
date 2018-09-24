package com.airbnb.lottie.model;

import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import com.airbnb.lottie.value.LottieValueCallback;

import java.util.List;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;

/**
 * Any item that can be a part of a {@link KeyPath} should implement this.
 */
@RestrictTo(LIBRARY)
public interface KeyPathElement {

  /**
   * Called recursively during keypath resolution.
   *
   * The overridden method should just call:
   *        MiscUtils.resolveKeyPath(keyPath, depth, accumulator, currentPartialKeyPath, this);
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

  /**
   * The overridden method should handle appropriate properties and set value callbacks on their
   * animations.
   */
  <T> void addValueCallback(T property, @Nullable LottieValueCallback<T> callback);
}