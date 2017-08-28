package com.airbnb.lottie.value;

import java.util.Arrays;

/**
 * Defines which content to target.
 * End the key path with a * to include everything that matches the key path.
 * You can also use * in the middle of the key path to wildcard match anything at that depth.
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
 *     Match all of Gabriel: new KeyPath("Gabriel", "*");
 *     Match Gabriel left hand fill: new KeyPath("Gabriel", "Body", "Left Hand", "Fill");
 *     Match Gabriel and Brandon's left hand fill: new KeyPath("*", "Body", Left Hand", "Fill");
 */
public class KeyPath {

  private final String[] keyPath;
  private final boolean isWildcard;

  public KeyPath(String... keyPath) {
    if (keyPath.length == 0) {
      throw new IllegalArgumentException("You must specify a KeyPath.");
    }
    if (keyPath[keyPath.length - 1].equals("*")) {
      this.keyPath = Arrays.copyOfRange(keyPath, 0, keyPath.length - 1);
      isWildcard = true;
    } else {
      this.keyPath = keyPath;
      isWildcard = false;
    }
  }

  String[] getKeyPath() {
    return keyPath;
  }

  boolean isWildcard() {
    return isWildcard;
  }

  @Override public String toString() {
    final StringBuilder sb = new StringBuilder("KeyPath{");
    sb.append("keyPath=[").append(Arrays.toString(keyPath));
    sb.append("], isWildcard=").append(isWildcard);
    sb.append('}');
    return sb.toString();
  }
}
