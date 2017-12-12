package com.airbnb.lottie.model;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Defines which content to target.
 * The keypath can contain wildcards ('*') with match exactly 1 item.
 * or globstars ('**') which match 1 or more items.
 *
 * For example, if your content were arranged like this:
 * Gabriel (Shape Layer)
 *     Body (Shape Group)
 *         Left Hand (Shape)
 *             Fill (Fill)
 *             Transform (Transform)
 *         ...
 * Brandon (Shape Layer)
 *     Body (Shape Group)
 *         Left Hand (Shape)
 *             Fill (Fill)
 *             Transform (Transform)
 *         ...
 *
 *
 * You could:
 *     Match Gabriel left hand fill: new KeyPath(COLOR, "Gabriel", "Body", "Left Hand", "Fill");
 *     Match Gabriel and Brandon's left hand fill:
 *        new KeyPath(COLOR, "*", "Body", Left Hand", "Fill");
 *     Match all colors:
 *        new KeyPath(COLOR, "**");
 */
public class KeyPath {

  private final List<String> keys;
  @Nullable private KeyPathElement resolvedElement;

  public KeyPath(String... keys) {
    this.keys = new ArrayList<>(Arrays.asList(keys));
  }

  private KeyPath(KeyPath keyPath) {
    keys = new ArrayList<>(keyPath.keys);
    resolvedElement = keyPath.resolvedElement;
  }

  private KeyPath(List<String> keys, @NonNull KeyPathElement resolvedElement) {
    this.keys = keys;
    this.resolvedElement = resolvedElement;
  }

  /**
   * Returns a new KeyPath with the key added.
   */
  @CheckResult
  public KeyPath addKey(String key) {
    KeyPath newKeyPath = new KeyPath(this);
    newKeyPath.keys.add(key);
    return newKeyPath;
  }

  public KeyPath resolve(KeyPathElement element) {
    return new KeyPath(new ArrayList<>(keys), element);
  }

  @Nullable KeyPathElement getResolvedElement() {
    return resolvedElement;
  }

  /**
   * Returns whether they key matches at the specified depth.
   */
  @SuppressWarnings("RedundantIfStatement")
  public boolean matches(String key, int depth) {
    if (isContainer(key)) {
      // This is an artificial layer we programatically create.
      return true;
    }
    if (depth >= keys.size()) {
      return false;
    }
    if (keys.get(depth).equals(key) ||
        keys.get(depth).equals("**") ||
        keys.get(depth).equals("*")) {
      return true;
    }
    return false;
  }

  public int incrementDepthBy(String key, int depth) {
    if (isContainer(key)) {
      // If it's a container then we added programatically and it isn't a part of the keypath.
      return 0;
    }
    if (!keys.get(depth).equals("**")) {
      // If it's not a globstar then it is part of the keypath.
      return 1;
    }
    if (depth == keys.size() - 1) {
      // The last key is a globstar.
      return 0;
    }
    if (keys.get(depth + 1).equals(key)) {
      // We are a globstar and the next key is our current key so consume both.
      return 2;
    }
    return 0;
  }

  public boolean fullyResolvesTo(String key, int depth) {
    boolean isLastDepth = depth == keys.size() - 1;
    String keyAtDepth = keys.get(depth);
    boolean isGlobstar = keyAtDepth.equals("**");

    if (!isGlobstar) {
      return isLastDepth && (keyAtDepth.equals(key) || keyAtDepth.equals("*"));
    }

    boolean isGlobstarButNextKeyMatches = !isLastDepth && keys.get(depth + 1).equals(key);
    if (isGlobstarButNextKeyMatches) {
      return depth == keys.size() - 2 ||
          (depth == keys.size() - 3 && keys.get(depth + 2).equals("**"));
    }

    if (isLastDepth) {
      return true;
    }
    if (depth + 1 < keys.size() - 1) {
      // We are a globstar but there is more than 1 key after the globstar we we can't fully match.
      return false;
    }
    // Return whether the next key (which we now know is the last one) is the same as the current
    // key.
    return keys.get(depth + 1).equals(key);
  }

  @SuppressWarnings("SimplifiableIfStatement") public boolean propagateToChildren(String key, int depth) {
    if (key.equals("__container")) {
      return true;
    }
    return depth < keys.size() - 1 || keys.get(depth).equals("**");
  }

  /**
   * We artificially create some container groups (like a root ContentGroup for the entire animation
   * and for the contents of a ShapeLayer).
   */
  private boolean isContainer(String key) {
    return key.equals("__container");
  }

  @Override public String toString() {
    final StringBuilder sb = new StringBuilder("KeyPath{");
    sb.append("keys=[").append(keys).append("]");
    sb.append('}');
    return sb.toString();
  }
}
