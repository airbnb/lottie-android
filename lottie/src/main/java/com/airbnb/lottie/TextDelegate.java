package com.airbnb.lottie;

import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

public abstract class TextDelegate {

  private final Map<String, String> stringMap = new HashMap<>();
  @Nullable private final LottieAnimationView animationView;
  @Nullable private final LottieDrawable drawable;
  private boolean cacheText = true;

  protected TextDelegate(
      @SuppressWarnings("NullableProblems") LottieAnimationView animationView) {
    this.animationView = animationView;
    drawable = null;
  }

  public TextDelegate(@SuppressWarnings("NullableProblems") LottieDrawable drawable) {
    this.drawable = drawable;
    animationView = null;
  }

  /**
   * Override this to replace the animation text with something dynamic. This can be used for
   * translations or custom data.
   */
  public String getText(String input) {
    return input;
  }

  /**
   * Sets whether or not {@link TextDelegate} will cache (memoize) the results of getText.
   * If this isn't necessary then set it to false.
   */
  public void setCacheText(boolean cacheText) {
    this.cacheText = cacheText;
  }

  /**
   * Invalidates a cached string with the given input.
   */
  public void invalidateText(String input) {
    stringMap.remove(input);
    invalidate();
  }

  /**
   * Invalidates all cached strings
   */
  public void invalidateAllText() {
    stringMap.clear();
    invalidate();
  }

  final String getTextInternal(String input) {
    if (cacheText && stringMap.containsKey(input)) {
      return stringMap.get(input);
    }
    String text = getText(input);
    if (cacheText) {
      stringMap.put(input, text);
    }
    return text;
  }

  private void invalidate() {
    if (animationView != null) {
      animationView.invalidate();
    }
    if (drawable != null) {
      drawable.invalidateSelf();
    }
  }
}
