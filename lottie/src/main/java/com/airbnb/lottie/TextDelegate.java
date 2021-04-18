package com.airbnb.lottie;

import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.VisibleForTesting;

import java.util.HashMap;
import java.util.Map;

/**
 * To replace static text in an animation at runtime, create an instance of this class and call {@link #setText(String, String)} to
 * replace the hard coded animation text (input) with the text of your choosing (output).
 * <p>
 * Alternatively, extend this class and override {@link #getText(String)} and if the text hasn't already been set
 * by {@link #setText(String, String)} then it will call {@link #getText(String)}.
 */
public class TextDelegate {

  private final Map<String, String> stringMap = new HashMap<>();
  @Nullable private final LottieAnimationView animationView;
  @Nullable private final LottieDrawable drawable;
  private boolean cacheText = true;

  /**
   * This normally needs to be able to invalidate the view/drawable but not for the test.
   */
  @VisibleForTesting TextDelegate() {
    animationView = null;
    drawable = null;
  }

  public TextDelegate(
      @SuppressWarnings("NullableProblems") LottieAnimationView animationView) {
    this.animationView = animationView;
    drawable = null;
  }

  @SuppressWarnings("unused")
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
   * Update the text that will be rendered for the given input text.
   */
  public void setText(String input, String output) {
    stringMap.put(input, output);
    invalidate();
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
   * Invalidates all cached strings.
   */
  public void invalidateAllText() {
    stringMap.clear();
    invalidate();
  }

  @RestrictTo(RestrictTo.Scope.LIBRARY)
  public final String getTextInternal(String input) {
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
