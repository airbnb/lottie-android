package com.airbnb.lottie.model;

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

  private KeyPath(List<String> keys, @NonNull KeyPathElement resolvedElement) {
    this.keys = keys;
    this.resolvedElement = resolvedElement;
  }

  public void addKey(String key) {
    keys.add(key);
  }

  public KeyPath resolve(KeyPathElement element) {
    return new KeyPath(new ArrayList<>(keys), element);
  }

  @Nullable KeyPathElement getResolvedElement() {
    return resolvedElement;
  }

  public boolean matches(String key, int depth) {
    if (isContainer(key)) {
      // This is an artificial layer we programatically create.
      return true;
    }
    if (depth >= keys.size()) {
      return false;
    }
    if (keys.get(depth).equals(key) || keys.get(depth).equals("**")) {
      return true;
    }
    return false;
  }

  public boolean incrementDepth(String key, int depth) {
    //noinspection SimplifiableIfStatement
    if (isContainer(key)) {
      return false;
    }
    return !(keys.get(depth).equals("**") &&
        depth < keys.size() - 2 &&
        !keys.get(depth + 1).equals(key));
  }

  public boolean isLastElement(int depth) {
    return depth == keys.size() - 1;
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
